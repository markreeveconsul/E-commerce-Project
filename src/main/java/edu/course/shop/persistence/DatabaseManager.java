package edu.course.shop.persistence;
import edu.course.shop.exception.StorageException;
import edu.course.shop.service.Passwords;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public final class DatabaseManager {
    private final String url;
    public DatabaseManager(Path file) {
        try {
            Path absolute = file.toAbsolutePath();
            Files.createDirectories(absolute.getParent());
            url = "jdbc:sqlite:" + absolute;
        } catch (java.io.IOException e) { throw new StorageException(e); }
    }
    public Connection open() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 3000");
        } catch (SQLException e) { connection.close(); throw e; }
        return connection;
    }
    public void initialize() {
        try (Connection c = open(); Statement s = c.createStatement()) {
            c.setAutoCommit(false);
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, username TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, role TEXT NOT NULL CHECK(role IN ('CUSTOMER','ADMIN')))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY, name TEXT NOT NULL, category TEXT NOT NULL, price_cents INTEGER NOT NULL CHECK(price_cents > 0), stock INTEGER NOT NULL CHECK(stock >= 0), active INTEGER NOT NULL CHECK(active IN (0,1)))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY, customer_id INTEGER NOT NULL REFERENCES users(id), created_at TEXT NOT NULL, address TEXT NOT NULL, payment_method TEXT NOT NULL, payment_status TEXT NOT NULL, total_cents INTEGER NOT NULL CHECK(total_cents > 0))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS order_items (order_id INTEGER NOT NULL REFERENCES orders(id), product_id INTEGER NOT NULL REFERENCES products(id), product_name TEXT NOT NULL, unit_price_cents INTEGER NOT NULL CHECK(unit_price_cents > 0), quantity INTEGER NOT NULL CHECK(quantity > 0), PRIMARY KEY(order_id, product_id))");
            // Additive migration: preserve existing products, customers and receipts.
            java.util.Set<String> columns = new java.util.HashSet<>();
            try (ResultSet r = s.executeQuery("PRAGMA table_info(products)")) { while (r.next()) columns.add(r.getString("name")); }
            if (!columns.contains("description")) s.executeUpdate("ALTER TABLE products ADD COLUMN description TEXT NOT NULL DEFAULT ''");
            if (!columns.contains("photo")) s.executeUpdate("ALTER TABLE products ADD COLUMN photo BLOB");
            int userCount;
            try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM users")) { r.next(); userCount = r.getInt(1); }
            if (userCount == 0) {
                seedUser(c, "customer", "customer123", "CUSTOMER");
                seedUser(c, "admin", "admin123", "ADMIN");
            }
            int productCount;
            try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM products")) { r.next(); productCount = r.getInt(1); }
            if (productCount == 0) {
                s.executeUpdate("INSERT INTO products(name,category,price_cents,stock,active) VALUES ('Wireless headphones','ELECTRONICS',5990,12,1),('Mechanical keyboard','ELECTRONICS',7990,8,1),('Everyday cotton tee','CLOTHING',1890,30,1),('Canvas tote','CLOTHING',1290,20,1),('Ceramic mug','HOME',1490,15,1),('Desk lamp','HOME',3290,6,1)");
            }
            c.commit();
        } catch (SQLException e) { throw new StorageException(e); }
    }
    private void seedUser(Connection c, String username, String password, String role) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("INSERT INTO users(username,password_hash,role) VALUES(?,?,?)")) {
            p.setString(1, username); p.setString(2, Passwords.hash(password)); p.setString(3, role); p.executeUpdate();
        }
    }
}