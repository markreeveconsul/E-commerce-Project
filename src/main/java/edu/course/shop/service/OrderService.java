package edu.course.shop.service;
import edu.course.shop.model.*;
import edu.course.shop.persistence.*;
import edu.course.shop.exception.*;
import java.sql.*;
import java.util.List;

public final class OrderService {
    private final DatabaseManager database;
    private final OrderRepository orders;
    public OrderService(DatabaseManager database, OrderRepository orders) { this.database = database; this.orders = orders; }
    public List<Order> history(User customer) {
        if (!(customer instanceof Customer)) throw new ValidationException("Customer access is required.");
        try (Connection c = database.open()) { return orders.forCustomer(c, customer.getId()); }
        catch (SQLException e) { throw new StorageException(e); }
    }
}