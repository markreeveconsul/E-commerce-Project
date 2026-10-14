package edu.course.shop.persistence;
import edu.course.shop.model.*;
import edu.course.shop.exception.*;
import java.sql.*;
import java.util.*;

public final class ProductRepository {
    public List<Product> list(Connection c, boolean includeInactive) throws SQLException {
        List<Product> products = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM products" + (includeInactive ? "" : " WHERE active = 1") + " ORDER BY name"); ResultSet r = p.executeQuery()) {
            while (r.next()) products.add(read(r));
        }
        return products;
    }
    public Product find(Connection c, long id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM products WHERE id = ?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new ValidationException("Product no longer exists.");
                return read(r);
            }
        }
    }
    public void save(Connection c, Product product) throws SQLException {
        String sql = product.getId() == 0
            ? "INSERT INTO products(name,category,price_cents,stock,active,description,photo) VALUES(?,?,?,?,?,?,?)"
            : "UPDATE products SET name=?,category=?,price_cents=?,stock=?,active=?,description=?,photo=? WHERE id=?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, product.getName()); p.setString(2, product.getCategory().name()); p.setLong(3, Money.cents(product.getPrice()));
            p.setInt(4, product.getStock()); p.setBoolean(5, product.isActive());
            p.setString(6, product.getDescription()); p.setBytes(7, product.getPhoto());
            if (product.getId() != 0) p.setLong(8, product.getId());
            if (p.executeUpdate() != 1) throw new ValidationException("Product no longer exists.");
        }
    }
    public void deductStock(Connection c, Product product, int quantity) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE products SET stock=stock-? WHERE id=? AND active=1 AND stock>=?")) {
            p.setInt(1, quantity); p.setLong(2, product.getId()); p.setInt(3, quantity);
            if (p.executeUpdate() != 1) throw new OutOfStockException(product.getName());
        }
    }
    private Product read(ResultSet r) throws SQLException {
        return new Product(r.getLong("id"), r.getString("name"), Category.valueOf(r.getString("category")), Money.amount(r.getLong("price_cents")), r.getInt("stock"), r.getBoolean("active"), r.getString("description"), r.getBytes("photo"));
    }
}