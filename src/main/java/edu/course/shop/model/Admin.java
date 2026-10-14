package edu.course.shop.model;
public final class Admin extends User {
    public Admin(long id, String username) { super(id, username); }
    @Override public boolean canManageProducts() { return true; }
    @Override public boolean canShop() { return false; }
    @Override public String getRole() { return "Administrator"; }
}