package edu.course.shop.payment;
import java.math.BigDecimal;
public final class CashOnDeliveryPayment implements PaymentMethod {
    @Override public String getName() { return "Cash on delivery"; }
    @Override public PaymentResult pay(BigDecimal amount) { return new PaymentResult(true, "PENDING"); }
    @Override public String toString() { return getName(); }
}