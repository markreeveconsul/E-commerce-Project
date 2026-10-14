package edu.course.shop.model;
public final class Customer extends User {
    public Customer(long id, String username) { super(id, username); }
    @Override public boolean canManageProducts() { return false; }
    @Override public boolean canShop() { return true; }
    @Override public String getRole() { return "Customer"; }
}