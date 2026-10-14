package edu.course.shop.model;
/** Abstract parent: each role supplies its own permission behavior. */
public abstract class User {
    private final long id;
    private final String username;
    protected User(long id, String username) { this.id = id; this.username = username; }
    public long getId() { return id; }
    public String getUsername() { return username; }
    public abstract boolean canManageProducts();
    public abstract boolean canShop();
    public abstract String getRole();
}