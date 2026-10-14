package edu.course.shop.payment;
import java.math.BigDecimal;
/** Local simulations only: no external payment provider is contacted. */
public interface PaymentMethod {
    String getName();
    PaymentResult pay(BigDecimal amount);
}