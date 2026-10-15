package edu.course.shop.model;
import edu.course.shop.exception.OutOfStockException;
import edu.course.shop.exception.ValidationException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Cart {
    private final long customerId;
    private final Map<Long, CartItem> items = new LinkedHashMap<>();
    public Cart(long customerId) { this.customerId = customerId; }
    public long getCustomerId() { return customerId; }
    public List<CartItem> getItems() { return List.copyOf(items.values()); }
    public void add(Product product, int quantity) {
        if (quantity <= 0) throw new ValidationException("Quantity must be positive.");
        int old = items.containsKey(product.getId()) ? items.get(product.getId()).getQuantity() : 0;
        if ((long) old + quantity > 100000) throw new ValidationException("Quantity is too large.");
        setQuantity(product, old + quantity);
    }
    public void setQuantity(Product product, int quantity) {
        if (!product.isActive()||product.getCategory().isRetired()) throw new ValidationException("This product is no longer available.");
        CartItem item = new CartItem(product, quantity);
        if (quantity > product.getStock()) throw new OutOfStockException(product.getName());
        items.put(product.getId(), item);
    }
    public void remove(long productId) { items.remove(productId); }
    public void clear() { items.clear(); }
    public BigDecimal getTotal() {
        return items.values().stream().map(CartItem::getSubtotal).reduce(new BigDecimal("0.00"), BigDecimal::add);
    }
}