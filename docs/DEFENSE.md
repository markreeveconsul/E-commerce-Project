# Defense preparation

1. **Where is encapsulation?** Product fields are private and final; its constructor rejects invalid values. Cart changes go through methods and getItems returns an unmodifiable copy.
2. **Why no Product setters?** Immutable snapshots prevent partially updated state. ProductService constructs a validated replacement before saving it.
3. **Show inheritance.** Customer and Admin extend User and inherit its identity fields/getters.
4. **Is inheritance meaningful here?** Yes: permission methods differ. Services use those methods to authorize operations.
5. **Show runtime polymorphism.** CheckoutService calls payment.pay(total) through PaymentMethod. COD returns PENDING; an approved wallet returns PAID.
6. **Why an interface?** Checkout depends on a behavior contract. A new payment simulation can implement it without adding a type-based branch in checkout.
7. **Why an abstract User?** Every concrete user must specify role and permission behavior; an incomplete generic user should not be instantiated.
8. **Why not put checkout in the button handler?** Business rules would become tied to the GUI and hard to test. The button collects values and calls a service.
9. **How do you prevent overselling?** The database UPDATE includes stock>=quantity, and checkout checks the affected row count.
10. **What if a later item lacks stock?** The transaction rolls back earlier deductions. A test explicitly checks this.
11. **What if the database fails after stock deduction?** The same transaction is rolled back; the cart clears only after commit. An injected failing SQLite trigger tests this.
12. **Why BigDecimal?** It represents decimal money exactly with explicit scale rules. SQLite stores whole cents, avoiding binary floating-point rounding.
13. **Why copy product details into OrderItem?** A receipt must preserve what was purchased even after the catalog changes.
14. **Can you add a new payment method live?** Implement PaymentMethod, return a PaymentResult, add an instance to CheckoutView's choices, and test it. Real payment integration would require a different failure/recovery design.
15. **Can you modify a validation rule live?** For example, change Product's maximum name length, update its message and test, then run build.ps1 Test. Explain why GUI-only validation would be insufficient.

Rehearse tracing this exact path: CheckoutView button -> CheckoutService.checkout -> ProductRepository.deductStock -> PaymentMethod.pay -> OrderRepository.save -> commit -> Cart.clear.

For a discount question: there is no discount feature currently. Explain where a pricing policy would belong (the service/domain layer), how cart and checkout totals must agree, and how an order would preserve the final charged amounts. Do not pretend an unimplemented method exists.
