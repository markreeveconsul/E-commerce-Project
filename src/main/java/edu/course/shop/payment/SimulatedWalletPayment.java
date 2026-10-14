package edu.course.shop.payment;
import java.math.BigDecimal;
public final class SimulatedWalletPayment implements PaymentMethod {
    private final boolean approved;
    public SimulatedWalletPayment(boolean approved) { this.approved = approved; }
    @Override public String getName() { return "Demo wallet"; }
    @Override public PaymentResult pay(BigDecimal amount) {
        return new PaymentResult(approved, approved ? "PAID" : "DECLINED");
    }
    @Override public String toString() { return getName() + (approved ? " (approve)" : " (decline test)"); }
}