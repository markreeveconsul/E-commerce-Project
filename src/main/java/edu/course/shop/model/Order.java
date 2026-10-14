package edu.course.shop.model;
import java.math.BigDecimal;
import java.util.List;
public record Order(long id, long customerId, String createdAt, String address,
                    String paymentMethod, String paymentStatus, BigDecimal total, List<OrderItem> items) {
    public Order { items = List.copyOf(items); }
}