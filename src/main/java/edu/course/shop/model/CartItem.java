package edu.course.shop.model;
import edu.course.shop.exception.ValidationException;
import java.math.BigDecimal;
public final class CartItem {
    private final Product product;
    private final int quantity;
    public CartItem(Product product, int quantity) {
        if (product == null || quantity <= 0 || quantity > 100000)
            throw new ValidationException("Quantity must be between 1 and 100,000.");
        this.product = product; this.quantity = quantity;
    }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return product.getPrice().multiply(BigDecimal.valueOf(quantity)); }
}