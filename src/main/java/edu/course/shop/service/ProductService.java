package edu.course.shop.service;
import edu.course.shop.model.*;
import edu.course.shop.persistence.*;
import edu.course.shop.exception.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public final class ProductService {
    private final DatabaseManager database;
    private final ProductRepository products;
    public ProductService(DatabaseManager database, ProductRepository products) { this.database = database; this.products = products; }
    public List<Product> search(User user, String query, Category category) {
        if (user == null) throw new ValidationException("Please sign in.");
        String term = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        try (Connection c = database.open()) {
            return products.list(c, user.canManageProducts()).stream()
                .filter(p -> p.getName().toLowerCase(Locale.ROOT).contains(term))
                .filter(p -> category == null || p.getCategory() == category).toList();
        } catch (SQLException e) { throw new StorageException(e); }
    }
    public List<Product> search(User user,String query,Category category,boolean inStockOnly,BigDecimal maximumPrice) {
        if(maximumPrice!=null)Money.price(maximumPrice);
        return search(user,query,category).stream()
            .filter(p->!inStockOnly||p.getStock()>0)
            .filter(p->maximumPrice==null||p.getPrice().compareTo(maximumPrice)<=0).toList();
    }
    public Product get(long id) {
        try (Connection c = database.open()) { return products.find(c, id); }
        catch (SQLException e) { throw new StorageException(e); }
    }
    public void save(User actor, long id, String name, Category category, BigDecimal price, int stock, boolean active) {
        if (actor == null || !actor.canManageProducts()) throw new ValidationException("Administrator access is required.");
        Product existing = id == 0 ? null : get(id);
        save(actor, id, name, category, price, stock, active, existing == null ? "" : existing.getDescription(), existing == null ? null : existing.getPhoto());
    }
    public void save(User actor, long id, String name, Category category, BigDecimal price, int stock, boolean active, String description, byte[] photo) {
        if (actor == null || !actor.canManageProducts()) throw new ValidationException("Administrator access is required.");
        byte[] snapshot = photo == null ? null : photo.clone();
        ProductPhotos.validate(snapshot);
        Product product = new Product(id, name, category, price, stock, active, description, snapshot);
        try (Connection c = database.open()) { products.save(c, product); }
        catch (SQLException e) { throw new StorageException(e); }
    }
}