package edu.course.shop.model;
import java.math.BigDecimal;
/** Purchase-time snapshot. Later product edits cannot change this receipt. */
public record OrderItem(long productId, String productName, BigDecimal unitPrice, int quantity) {
    public BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}