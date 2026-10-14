package edu.course.shop.model;
import edu.course.shop.exception.ValidationException;

/** Validated delivery information, independent of JavaFX controls. */
public record DeliveryAddress(String recipient, String street, String city, String postalCode) {
    public DeliveryAddress {
        recipient=recipient==null?"":recipient.trim();street=street==null?"":street.trim();
        city=city==null?"":city.trim();postalCode=postalCode==null?"":postalCode.trim();
        if(recipient.isEmpty()||recipient.length()>80)throw new ValidationException("Enter a recipient name of 1–80 characters.");
        if(street.length()<5||street.length()>250)throw new ValidationException("Enter a street address between 5 and 250 characters.");
        if(city.isEmpty()||city.length()>60)throw new ValidationException("Enter a city of 1–60 characters.");
        if(!postalCode.matches("[A-Za-z0-9 -]{2,12}"))throw new ValidationException("Enter a valid postal code of 2–12 characters.");
        if(recipient.length()+street.length()+city.length()+postalCode.length()+3>250)
            throw new ValidationException("Keep the complete delivery details within 250 characters.");
    }
    public String formatted() { return recipient+"\n"+street+"\n"+city+" "+postalCode; }
}
