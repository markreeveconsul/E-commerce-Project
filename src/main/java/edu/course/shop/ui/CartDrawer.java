package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.service.CartService;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** A compact cart editor; services validate every quantity change. */
public final class CartDrawer extends VBox {
    private final User user;private final Cart cart;private final CartService carts;
    private final Runnable changed,close,review,checkout;
    private final VBox lines=new VBox(),bottom=new VBox(12);
    private final Label feedback=Ui.feedback("drawer-feedback"),count=Ui.label("","section-title");
    public CartDrawer(User user,Cart cart,CartService carts,Runnable changed,Runnable close,Runnable review,Runnable checkout) {
        this.user=user;this.cart=cart;this.carts=carts;this.changed=changed;this.close=close;this.review=review;this.checkout=checkout;
        setId("cart-drawer");getStyleClass().add("cart-drawer");setSpacing(16);setPrefWidth(410);setMaxWidth(410);
        Button dismiss=Ui.button("×",close);dismiss.setId("close-cart-drawer");dismiss.setAccessibleText("Close cart");
        dismiss.getStyleClass().add("icon-button");
        Region gap=new Region();HBox.setHgrow(gap,Priority.ALWAYS);
        HBox header=new HBox(12,count,gap,dismiss);header.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(header,feedback,Ui.scroll(lines),bottom);render();
    }
    private void render() {
        int total=cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();count.setText("Your cart ("+total+")");
        lines.getChildren().clear();bottom.getChildren().clear();
        if(cart.getItems().isEmpty()){
            lines.getChildren().add(Ui.empty("Room for good things","Your next favorite is waiting in the shop.","Keep browsing",close));
            Button reviewButton=Ui.button("Review cart",review);reviewButton.setId("drawer-review");bottom.getChildren().add(reviewButton);return;
        }
        for(CartItem item:cart.getItems()){
            Product p=item.getProduct();
            Label name=Ui.label(p.getName(),"product-name");name.setMaxWidth(Double.MAX_VALUE);name.setMaxHeight(44);
            Label price=Ui.label(Money.format(item.getSubtotal()),"price");
            VBox info=new VBox(4,name);
            if(item.getQuantity()>1)info.getChildren().add(Ui.label(Money.format(p.getPrice())+" each","muted"));
            info.getChildren().add(price);HBox.setHgrow(info,Priority.ALWAYS);

            // Quantity stepper: one pill holding − qty +, instead of three loose buttons.
            Button minus=Ui.button("−",()->update(p,item.getQuantity()-1));minus.setDisable(item.getQuantity()<=1);minus.setId("drawer-minus-"+p.getId());
            Button plus=Ui.button("+",()->update(p,item.getQuantity()+1));plus.setId("drawer-plus-"+p.getId());
            minus.getStyleClass().add("stepper-button");plus.getStyleClass().add("stepper-button");
            minus.setAccessibleText("Decrease "+p.getName());plus.setAccessibleText("Increase "+p.getName());
            Label quantity=Ui.label(Integer.toString(item.getQuantity()),"field-label");
            quantity.setMinWidth(30);quantity.setAlignment(Pos.CENTER);quantity.setWrapText(false);
            HBox stepper=new HBox(2,minus,quantity,plus);stepper.getStyleClass().add("stepper");stepper.setAlignment(Pos.CENTER);

            Button remove=Ui.button("Remove",()->{cart.remove(p.getId());changed.run();render();});remove.getStyleClass().add("text-button");remove.setId("drawer-remove-"+p.getId());
            Region controlGap=new Region();HBox.setHgrow(controlGap,Priority.ALWAYS);
            HBox controls=new HBox(8,stepper,controlGap,remove);controls.setAlignment(Pos.CENTER_LEFT);

            ProductArtwork art=new ProductArtwork(p,72);
            HBox top=new HBox(12,art,info);top.setAlignment(Pos.TOP_LEFT);
            VBox line=new VBox(12,top,controls);line.getStyleClass().add("drawer-line");lines.getChildren().add(line);
        }
        // Subtotal first, then one dominant checkout button, a quieter review button, and a text link to leave.
        VBox totalBox=new VBox(6,Ui.label("Subtotal","field-label"),Ui.label(Money.format(cart.getTotal()),"total"),
            Ui.label("USD · Free shipping · No taxes in this demo","muted"));totalBox.getStyleClass().add("drawer-total");
        Button pay=Ui.primary(user instanceof Guest?"Sign in to checkout":"Checkout",checkout);pay.setId("drawer-checkout");
        pay.setMaxWidth(Double.MAX_VALUE);pay.getStyleClass().add("cta");
        Button reviewButton=Ui.button("Review cart",review);reviewButton.setId("drawer-review");reviewButton.setMaxWidth(Double.MAX_VALUE);
        Button keepBrowsing=Ui.link("Keep browsing",close);keepBrowsing.setMaxWidth(Double.MAX_VALUE);
        bottom.getChildren().addAll(totalBox,pay,reviewButton,keepBrowsing);
    }
    private void update(Product product,int quantity) {
        Ui.async(this,"Updating cart…",()->{carts.update(user,cart,product.getId(),quantity);return true;},
            done->{changed.run();render();Ui.success(feedback,"Quantity updated.");},error->Ui.failure(feedback,error));
    }
}