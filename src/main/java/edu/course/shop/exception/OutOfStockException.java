package edu.course.shop.exception;
public final class OutOfStockException extends ValidationException {
    public OutOfStockException(String name) { super("Not enough stock for " + name + ". Update your cart and try again."); }
}