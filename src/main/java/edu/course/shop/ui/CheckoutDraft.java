package edu.course.shop.ui;
/** Temporary form state, owned by the signed-in session; never persisted. */
public final class CheckoutDraft {
    private String address="";
    private int paymentIndex;
    private String recipient="", city="", postalCode="";
    public String getRecipient() { return recipient; }
    public void setRecipient(String value) { recipient=value; }
    public String getCity() { return city; }
    public void setCity(String value) { city=value; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String value) { postalCode=value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address=value; }
    public int getPaymentIndex() { return paymentIndex; }
    public void setPaymentIndex(int value) { paymentIndex=value; }
    public void clear() { address=""; recipient=""; city=""; postalCode=""; paymentIndex=0; }
}