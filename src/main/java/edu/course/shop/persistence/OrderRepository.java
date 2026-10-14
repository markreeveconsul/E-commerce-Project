package edu.course.shop.persistence;
import edu.course.shop.model.*;
import java.sql.*;
import java.util.*;

public final class OrderRepository {
    public Order save(Connection c, Order order) throws SQLException {
        long id;
        try (PreparedStatement p = c.prepareStatement("INSERT INTO orders(customer_id,created_at,address,payment_method,payment_status,total_cents) VALUES(?,?,?,?,?,?)")) {
            p.setLong(1, order.customerId()); p.setString(2, order.createdAt()); p.setString(3, order.address());
            p.setString(4, order.paymentMethod()); p.setString(5, order.paymentStatus()); p.setLong(6, Money.cents(order.total())); p.executeUpdate();
        }
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT last_insert_rowid()")) { r.next(); id = r.getLong(1); }
        try (PreparedStatement p = c.prepareStatement("INSERT INTO order_items(order_id,product_id,product_name,unit_price_cents,quantity) VALUES(?,?,?,?,?)")) {
            for (OrderItem item : order.items()) {
                p.setLong(1, id); p.setLong(2, item.productId()); p.setString(3, item.productName());
                p.setLong(4, Money.cents(item.unitPrice())); p.setInt(5, item.quantity()); p.addBatch();
            }
            p.executeBatch();
        }
        return new Order(id, order.customerId(), order.createdAt(), order.address(), order.paymentMethod(), order.paymentStatus(), order.total(), order.items());
    }
    public List<Order> forCustomer(Connection c, long customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM orders WHERE customer_id=? ORDER BY id DESC")) {
            p.setLong(1, customerId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) orders.add(new Order(r.getLong("id"), customerId, r.getString("created_at"), r.getString("address"), r.getString("payment_method"), r.getString("payment_status"), Money.amount(r.getLong("total_cents")), items(c, r.getLong("id"))));
            }
        }
        return orders;
    }
    private List<OrderItem> items(Connection c, long id) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM order_items WHERE order_id=? ORDER BY product_name")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) items.add(new OrderItem(r.getLong("product_id"), r.getString("product_name"), Money.amount(r.getLong("unit_price_cents")), r.getInt("quantity")));
            }
        }
        return items;
    }
}