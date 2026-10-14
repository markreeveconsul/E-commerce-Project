package edu.course.shop.model;
/** A browsing session, never a persisted account. Checkout requires a Customer. */
public final class Guest extends User {
    public Guest() { super(0, "Guest"); }
    @Override public boolean canManageProducts() { return false; }
    @Override public boolean canShop() { return true; }
    @Override public String getRole() { return "GUEST"; }
}
