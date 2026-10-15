package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.payment.*;
import edu.course.shop.service.CheckoutService;
import edu.course.shop.service.AccountService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public final class CheckoutView extends VBox {
    public CheckoutView(User user, Cart cart, CheckoutService checkout, CheckoutDraft draft, Consumer<Order> placed, Runnable back) {
        this(user,cart,checkout,null,draft,placed,back);
    }
    public CheckoutView(User user,Cart cart,CheckoutService checkout,AccountService accounts,CheckoutDraft draft,Consumer<Order> placed,Runnable back) {
        getStyleClass().add("page"); setSpacing(18);
        TextArea address=new TextArea(draft.getAddress()); address.setWrapText(true);
        address.setPromptText("Street, building, unit or apartment"); address.setPrefRowCount(2); address.setId("address");
        Label addressError=Ui.feedback("address-error"), paymentError=Ui.feedback("payment-error"), feedback=Ui.feedback("checkout-feedback");
        address.textProperty().addListener((o,a,b)-> { draft.setAddress(b); addressError.setText(""); Ui.flag(address,false); });
        TextField recipient=new TextField(draft.getRecipient()),city=new TextField(draft.getCity()),postal=new TextField(draft.getPostalCode());
        recipient.setId("recipient"); city.setId("city"); postal.setId("postal-code");
        recipient.setPromptText("Full name");
        Label recipientError=Ui.feedback("recipient-error"),cityError=Ui.feedback("city-error"),postalError=Ui.feedback("postal-error");
        recipient.textProperty().addListener((o,a,b)->{draft.setRecipient(b);recipientError.setText("");Ui.flag(recipient,false);});
        city.textProperty().addListener((o,a,b)->{draft.setCity(b);cityError.setText("");Ui.flag(city,false);});
        postal.textProperty().addListener((o,a,b)->{draft.setPostalCode(b);postalError.setText("");Ui.flag(postal,false);});
        ComboBox<PaymentMethod> payment=new ComboBox<>();
        payment.getItems().addAll(new CashOnDeliveryPayment(),new SimulatedWalletPayment(true),new SimulatedWalletPayment(false));
        payment.getSelectionModel().select(draft.getPaymentIndex()); payment.setMaxWidth(Double.MAX_VALUE); payment.setId("payment");
        payment.getSelectionModel().selectedIndexProperty().addListener((o,a,b)->draft.setPaymentIndex(b.intValue()));
        Label explanation=Ui.label(payment.getValue() instanceof CashOnDeliveryPayment ? "Pay when your order arrives. Payment stays pending." : "A simulated wallet payment. No real money is collected.", "muted");
        payment.setOnAction(e->{explanation.setText(payment.getValue() instanceof CashOnDeliveryPayment
            ? "Pay when your order arrives. Payment stays pending." : "A simulated wallet payment. No real money is collected.");paymentError.setText("");Ui.flag(payment,false);});
        Button place=Ui.primary("Place order · "+Money.format(cart.getTotal()),()-> {
            String delivery=address.getText(); PaymentMethod method=payment.getValue();
            addressError.setText(""); paymentError.setText(""); feedback.setText("");
            if(delivery.trim().length()<5 || delivery.trim().length()>250) {
                addressError.setText("Enter a delivery address between 5 and 250 characters."); Ui.flag(address,true); address.requestFocus(); return;
            }
            recipientError.setText("");cityError.setText("");postalError.setText("");
            boolean valid=true;
            if(recipient.getText().isBlank()||recipient.getText().trim().length()>80){recipientError.setText("Enter a recipient name of 1–80 characters.");Ui.flag(recipient,true);valid=false;}
            if(city.getText().isBlank()||city.getText().trim().length()>60){cityError.setText("Enter a city of 1–60 characters.");Ui.flag(city,true);valid=false;}
            if(!postal.getText().trim().matches("[A-Za-z0-9 -]{2,12}")){postalError.setText("Enter a postal code of 2–12 letters, numbers, spaces or hyphens.");Ui.flag(postal,true);valid=false;}
            if(!valid){
                if(!recipientError.getText().isEmpty())recipient.requestFocus();
                else if(!cityError.getText().isEmpty())city.requestFocus();else postal.requestFocus();return;
            }
            String completeAddress=recipient.getText().trim()+"\n"+delivery.trim()+"\n"+city.getText().trim()+" "+postal.getText().trim();
            if(completeAddress.length()>250){addressError.setText("Keep the complete delivery details within 250 characters.");Ui.flag(address,true);address.requestFocus();return;}
            if(method==null) { paymentError.setText("Choose a payment method."); Ui.flag(payment,true); payment.requestFocus();return; }
            DeliveryAddress destination=new DeliveryAddress(recipient.getText(),delivery,city.getText(),postal.getText());
            Ui.async(this,"Placing your order…",()->checkout.checkout(user,cart,destination.formatted(),method),order-> {
                draft.clear(); placed.accept(order);
            },error->Ui.failure(feedback,error));
        });
        place.setId("place-order"); place.setMaxWidth(Double.MAX_VALUE); place.getStyleClass().addAll("cta","purchase-action");

        // Two separate cards make the two jobs (where, how to pay) obvious at a glance.
        VBox cityField=Ui.field("City",city,cityError),postalField=Ui.field("Postal code",postal,postalError);
        HBox.setHgrow(cityField,Priority.ALWAYS);HBox.setHgrow(postalField,Priority.ALWAYS);cityField.setMinWidth(0);postalField.setMinWidth(0);
        VBox deliveryCard=new VBox(14,Ui.label("Delivery details","section-title"),Ui.field("Recipient name",recipient,recipientError),
            Ui.field("Street address",address,addressError),new HBox(14,cityField,postalField));
        deliveryCard.getStyleClass().add("card");
        if(accounts!=null) {
            ComboBox<AccountService.SavedAddress> addresses=new ComboBox<>();addresses.setId("checkout-saved-address");addresses.setPromptText("Use a saved address");addresses.setMaxWidth(Double.MAX_VALUE);addresses.setAccessibleText("Use a saved delivery address");addresses.setVisible(false);addresses.managedProperty().bind(addresses.visibleProperty());
            addresses.setOnAction(e->{if(addresses.getValue()!=null){DeliveryAddress value=addresses.getValue().address();recipient.setText(value.recipient());address.setText(value.street());city.setText(value.city());postal.setText(value.postalCode());}});
            deliveryCard.getChildren().add(1,addresses);
            javafx.application.Platform.runLater(()->Ui.async(this,"Loading saved addresses…",()->accounts.addresses(user),rows->{addresses.getItems().setAll(rows);addresses.setVisible(!rows.isEmpty());},error->Ui.failure(feedback,error)));
        }
        javafx.scene.control.ToggleGroup paymentGroup=new javafx.scene.control.ToggleGroup();FlowPane paymentChoices=new FlowPane(12,12);
        for(int i=0;i<2;i++) {int index=i;ToggleButton choice=new ToggleButton(i==0?"Cash on delivery":"Simulated wallet");choice.setId(i==0?"payment-cash":"payment-wallet");choice.getStyleClass().add("payment-choice");choice.setToggleGroup(paymentGroup);choice.setSelected(i==0?payment.getSelectionModel().getSelectedIndex()==0:payment.getSelectionModel().getSelectedIndex()>0);choice.setOnAction(e->{payment.getSelectionModel().select(index);choice.setSelected(true);});RadioButton indicator=new RadioButton();indicator.setMouseTransparent(true);indicator.setFocusTraversable(false);indicator.selectedProperty().bind(choice.selectedProperty());choice.setGraphic(indicator);paymentChoices.getChildren().add(choice);}
        payment.getSelectionModel().selectedIndexProperty().addListener((o,a,b)->{for(int i=0;i<2;i++)((ToggleButton)paymentChoices.getChildren().get(i)).setSelected(i==0?b.intValue()==0:b.intValue()>0);});
        TitledPane demoOptions=new TitledPane("Demo test options",Ui.field("Simulated payment outcome",payment,paymentError));demoOptions.setId("demo-payment-options");demoOptions.setExpanded(false);demoOptions.setAnimated(false);
        VBox paymentCard=new VBox(14,Ui.label("Payment","section-title"),paymentChoices,explanation,demoOptions,
            Ui.label("Course demo · Payments are simulated.","notice"));
        paymentCard.getStyleClass().add("card");
        VBox form=new VBox(16,deliveryCard,paymentCard,feedback); form.setMinWidth(0); HBox.setHgrow(form,Priority.ALWAYS);
        // Scroll both columns together so the summary and payment remain reachable in short windows.
        VBox columns=Ui.purchaseColumns(form,new OrderSummary(cart,place));columns.setId("checkout-columns");

        Button backLink=Ui.link("← Back to cart",back);backLink.setMaxWidth(Region.USE_PREF_SIZE);
        getChildren().addAll(Ui.label("Checkout","page-title"),steps(),Ui.scroll(columns));
    }

    /** Progress indicator: finished step, current step (filled pill), upcoming step. */
    private static HBox steps() {
        String[] names = {"Cart", "Delivery & payment", "Confirmation"};
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < names.length; i++) {
            Label step = Ui.label(i == 0 ? "✓  " + names[i] : (i + 1) + "  " + names[i], i == 0 ? "step-done" : i == 1 ? "step-now" : "step-next");
            step.setWrapText(false); row.getChildren().add(step);
            if (i < names.length - 1) {
                Region line = new Region(); line.getStyleClass().add("step-line");
                line.setMinHeight(2); line.setPrefHeight(2); line.setMaxHeight(2); line.setPrefWidth(36);
                row.getChildren().add(line);
            }
        }
        return row;
    }
}
