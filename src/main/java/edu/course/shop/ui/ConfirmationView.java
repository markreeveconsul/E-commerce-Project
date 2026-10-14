package edu.course.shop.ui;
import edu.course.shop.model.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public final class ConfirmationView extends VBox {
    public ConfirmationView(Order order,Runnable orders,Runnable browse) {
        setId("confirmation"); getStyleClass().add("page"); setAlignment(Pos.TOP_CENTER); setSpacing(20);
        boolean paid=order.paymentStatus().equals("PAID");

        // Success mark + title: the one clear message of this screen.
        Label check=Ui.label("✓","success-mark"); check.setWrapText(false);
        HBox status=new HBox(14,check,Ui.label("Order confirmed","section-title")); status.setAlignment(Pos.CENTER_LEFT);

        // Order number and payment state on one line; the badge is green when paid, neutral when due.
        Label payment=Ui.label(paid?"Payment received (demo)":"Payment due on delivery",paid?"stock":"badge-neutral");
        payment.setWrapText(false); payment.setMaxWidth(Region.USE_PREF_SIZE);
        HBox meta=new HBox(12,Ui.label("Order #"+order.id(),"field-label"),payment); meta.setAlignment(Pos.CENTER_LEFT);

        VBox card=new VBox(18,status,Ui.label("GOOD CHOICE.\nTHANK YOU!","hero-title"),meta,
            Ui.label("Deliver to: "+order.address(),"summary-item"));

        // Receipt: item on the left, amount pinned to the right.
        VBox receipt=new VBox(10);
        for(OrderItem item:order.items()) receipt.getChildren().add(Ui.row(item.quantity()+" × "+item.productName(),Money.format(item.subtotal()),"summary-item","summary-value"));
        card.getChildren().addAll(new Separator(),receipt,new Separator(),
            Ui.row(paid?"Total paid":"Total due",Money.format(order.total()),"field-label","total"),
            new FlowPane(12,12,Ui.primary("View my orders",orders),Ui.button("Continue shopping",browse)));
        card.getStyleClass().addAll("card","confirmation-card"); card.setMaxWidth(680);
        getChildren().add(Ui.scroll(card));
    }
}