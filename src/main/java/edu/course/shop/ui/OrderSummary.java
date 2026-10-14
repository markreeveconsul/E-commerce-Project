package edu.course.shop.ui;
import edu.course.shop.model.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Readable item amounts and totals; the containing page controls scrolling. */
public final class OrderSummary extends VBox {
    public OrderSummary(Cart cart,Node action) {
        setSpacing(10);getStyleClass().add("summary");setPrefWidth(330);setMinWidth(280);setMaxHeight(USE_PREF_SIZE);
        getChildren().add(Ui.label("Order summary","section-title"));
        VBox items=new VBox(10);
        for(CartItem item:cart.getItems()) {
            HBox row=Ui.row(item.getQuantity()+" × "+item.getProduct().getName(),Money.format(item.getSubtotal()),"summary-item","summary-value");
            ((Label)row.getChildren().getFirst()).setMinWidth(0);items.getChildren().add(row);
        }
        ScrollPane list=Ui.scroll(items);list.setPrefViewportHeight(Math.min(130,Math.max(28,cart.getItems().size()*35)));list.setMaxHeight(130);list.setMinHeight(28);VBox.setVgrow(list,Priority.NEVER);
        // The total is the biggest number in the card; subtotal and shipping stay quiet.
        getChildren().addAll(list,new Separator(),row("Subtotal",Money.format(cart.getTotal()),false),row("Shipping",Money.format(java.math.BigDecimal.ZERO),false),new Separator(),
            row("Total",Money.format(cart.getTotal()),true),action,Ui.label("Free shipping · USD · Simulated payments","muted"));
    }
    private HBox row(String name,String value,boolean strong){
        Region space=new Region();HBox.setHgrow(space,Priority.ALWAYS);
        Label left=Ui.label(name,strong?"summary-value":"summary-item"),right=Ui.label(value,strong?"total":"summary-value");
        left.setWrapText(false);right.setWrapText(false);
        HBox row=new HBox(left,space,right);row.setAlignment(Pos.CENTER_LEFT);return row;
    }
}