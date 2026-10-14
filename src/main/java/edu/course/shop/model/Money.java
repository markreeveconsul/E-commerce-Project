package edu.course.shop.model;
import edu.course.shop.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** One currency throughout the demo: USD, with two decimal places. */
public final class Money {
    private Money() { }
    public static BigDecimal price(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.compareTo(new BigDecimal("1000000")) > 0)
            throw new ValidationException("Price must be greater than zero and at most 1,000,000.00.");
        try { return value.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException e) { throw new ValidationException("Price can have at most two decimal places."); }
    }
    public static BigDecimal parse(String text) {
        try { return price(new BigDecimal(text.trim())); }
        catch (NumberFormatException e) { throw new ValidationException("Enter a valid numeric price."); }
    }
    public static long cents(BigDecimal amount) { return amount.movePointRight(2).longValueExact(); }
    public static BigDecimal amount(long cents) { return BigDecimal.valueOf(cents, 2); }
    public static String format(BigDecimal amount) { return "$" + amount.setScale(2).toPlainString(); }
}