package edu.course.shop.ui;
import edu.course.shop.model.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Fixed totals and action stay visible while a long item list scrolls. */
public final class OrderSummary extends VBox {
    public OrderSummary(Cart cart,Node action) {
        setSpacing(10);getStyleClass().add("summary");setPrefWidth(300);setMinWidth(260);setMaxHeight(USE_PREF_SIZE);
        getChildren().add(Ui.label("Order summary","section-title"));
        VBox items=new VBox(10);
        for(CartItem item:cart.getItems())items.getChildren().add(Ui.label(item.getQuantity()+" × "+item.getProduct().getName(),"summary-item"));
        ScrollPane list=Ui.scroll(items);list.setPrefViewportHeight(Math.min(130,Math.max(28,cart.getItems().size()*35)));list.setMaxHeight(130);list.setMinHeight(28);VBox.setVgrow(list,Priority.NEVER);
        // The total is the biggest number in the card; subtotal and shipping stay quiet.
        getChildren().addAll(list,new Separator(),row("Subtotal",Money.format(cart.getTotal()),false),row("Shipping","Free",false),new Separator(),
            row("Total",Money.format(cart.getTotal()),true),action,Ui.label("Demo payment · USD","muted"));
    }
    private HBox row(String name,String value,boolean strong){
        Region space=new Region();HBox.setHgrow(space,Priority.ALWAYS);
        Label left=Ui.label(name,strong?"summary-value":"summary-item"),right=Ui.label(value,strong?"price":"summary-value");
        left.setWrapText(false);right.setWrapText(false);
        HBox row=new HBox(left,space,right);row.setAlignment(Pos.CENTER_LEFT);return row;
    }
}