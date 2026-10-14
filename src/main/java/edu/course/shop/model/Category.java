package edu.course.shop.model;

import java.util.List;

/** Stored enum names stay stable so existing inventory and order references remain readable. */
public enum Category {
    PC_SETS("PC Sets"), PC_PARTS("PC Parts"), LAPTOPS("Laptops"),
    PHONES_TABLETS("Phones & Tablets"), PERIPHERALS("Peripherals & Accessories"), ELECTRONICS("Other Electronics"),
    CLOTHING("Clothing (retired)"), HOME("Home (retired)");

    private final String label;
    Category(String label) { this.label=label; }
    public String label() { return label; }
    public boolean isRetired() { return this==CLOTHING||this==HOME; }
    public static List<Category> shopping() { return List.of(PC_SETS,PC_PARTS,LAPTOPS,PHONES_TABLETS,PERIPHERALS,ELECTRONICS); }
    public int shoppingIndex() { return shopping().indexOf(this)+1; }
}
