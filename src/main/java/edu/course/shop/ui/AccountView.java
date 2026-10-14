package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.service.AccountService;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Profile and address-book drafts stay in the form until Save. */
public final class AccountView extends VBox {
    private long editing;
    public AccountView(User user,AccountService accounts,Runnable savedItems,Runnable orders) {
        getStyleClass().add("page");setSpacing(20);setId("account-page");
        Label feedback=Ui.feedback("account-feedback");TextField name=new TextField();name.setId("display-name");
        TextField recipient=new TextField(),street=new TextField(),city=new TextField(),postal=new TextField();
        recipient.setId("saved-recipient");street.setId("saved-street");city.setId("saved-city");postal.setId("saved-postal");
        CheckBox preferred=new CheckBox("Use as my default address");preferred.setId("default-address");
        VBox list=new VBox(12);list.setId("address-list");
        Runnable reset=()->{editing=0;recipient.clear();street.clear();city.clear();postal.clear();preferred.setSelected(false);};
        Runnable refresh=()->Ui.async(this,"Loading your account…",()->accounts.addresses(user),rows->{
            list.getChildren().clear();if(rows.isEmpty())list.getChildren().add(Ui.label("No addresses yet. Add one below.","muted"));
            for(var row:rows) {
                Button edit=Ui.link("Edit",()->{editing=row.id();recipient.setText(row.address().recipient());street.setText(row.address().street());city.setText(row.address().city());postal.setText(row.address().postalCode());preferred.setSelected(row.preferred());recipient.requestFocus();});
                Button remove=Ui.link("Remove",()->Ui.async(this,"Removing address…",()->{accounts.removeAddress(user,row.id());return true;},done->{reset.run();reload();Ui.success(feedback,"Address removed.");},error->Ui.failure(feedback,error)));
                VBox card=new VBox(8,Ui.label(row.address().formatted(),"summary-item"),Ui.label(row.preferred()?"Default address":"Saved address","eyebrow"),new HBox(16,edit,remove));card.getStyleClass().add("address-card");list.getChildren().add(card);
            }
        },error->Ui.failure(feedback,error));
        // Reload through the existing refresh action after a mutation.
        Button reload=new Button();reload.setOnAction(e->refresh.run());getProperties().put("market.reload",reload);
        Button saveName=Ui.primary("Save profile",()->{String value=name.getText();Ui.async(this,"Saving profile…",()->{accounts.saveName(user,value);return true;},done->Ui.success(feedback,"Profile saved."),error->Ui.failure(feedback,error));});saveName.setId("save-profile");saveName.getStyleClass().add("purchase-action");
        VBox profile=new VBox(16,Ui.label("Profile","section-title"),Ui.label("Signed in as "+user.getUsername(),"muted"),Ui.field("Display name",name,Ui.feedback("profile-error")),saveName);profile.getStyleClass().add("card");profile.setMinWidth(0);
        Button saveAddress=Ui.primary("Save address",()->{
            try {DeliveryAddress value=new DeliveryAddress(recipient.getText(),street.getText(),city.getText(),postal.getText());long id=editing;boolean isDefault=preferred.isSelected();
                Ui.async(this,"Saving address…",()->{accounts.saveAddress(user,id,value,isDefault);return true;},done->{reset.run();Ui.success(feedback,"Address saved.");refresh.run();},error->Ui.failure(feedback,error));
            }catch(RuntimeException e){Ui.failure(feedback,e);recipient.requestFocus();}
        });saveAddress.setId("save-address");saveAddress.getStyleClass().add("purchase-action");
        VBox addressBook=new VBox(16,Ui.label("Saved addresses","section-title"),list,
            Ui.field("Recipient",recipient,Ui.feedback("saved-recipient-error")),Ui.field("Street address",street,Ui.feedback("saved-street-error")),
            Ui.field("City",city,Ui.feedback("saved-city-error")),Ui.field("Postal code",postal,Ui.feedback("saved-postal-error")),preferred,new HBox(12,saveAddress,Ui.button("New address",reset)));
        addressBook.getStyleClass().add("card");addressBook.setMinWidth(0);
        VBox content=Ui.responsiveColumns(profile,addressBook,850,24);
        getChildren().addAll(Ui.label("Your space","page-title"),new HBox(12,Ui.button("Saved items",savedItems),Ui.button("Your orders",orders)),feedback,Ui.scroll(content));
        Platform.runLater(()->Ui.async(this,"Loading profile…",()->accounts.displayName(user),value->{name.setText(value);refresh.run();},error->Ui.failure(feedback,error)));
    }
    private void reload() { ((Button)getProperties().get("market.reload")).fire(); }
}
