package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.service.OrderService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class OrdersView extends VBox {
    public OrdersView(User user,OrderService orders,Runnable browse) {
        getStyleClass().add("page"); setSpacing(16);
        TableView<Order> table=Ui.table(); table.setId("orders-table"); table.setMinHeight(180); table.setPrefHeight(240);
        DateTimeFormatter date=DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm").withZone(ZoneId.systemDefault());
        table.getColumns().add(Ui.column("Order",o->"#"+o.id()));
        table.getColumns().add(Ui.column("Placed",o->date.format(Instant.parse(o.createdAt()))));
        table.getColumns().add(Ui.column("Payment",Order::paymentMethod));

        // Payment status shows as a small badge: green when paid, neutral when due. Cell values are unchanged.
        TableColumn<Order,String> status=Ui.column("Payment status",o->o.paymentStatus().equals("PAID")?"Paid (demo)":"Due on delivery");
        status.setCellFactory(col->new TableCell<Order,String>() {
            @Override protected void updateItem(String text,boolean empty) {
                super.updateItem(text,empty);
                if(empty||text==null){setText(null);setGraphic(null);return;}
                Label badge=new Label(text); badge.getStyleClass().add(text.startsWith("Paid")?"stock":"badge-neutral");
                setText(null); setGraphic(badge);
            }
        });
        table.getColumns().add(status);
        TableColumn<Order,String> total=Ui.column("Total",o->Money.format(o.total()));
        total.setStyle("-fx-alignment: CENTER-RIGHT;"); // amounts read best right-aligned
        table.getColumns().add(total);

        // Receipt card for the selected order: items on the left, amounts pinned to the right.
        VBox receipt=new VBox(12); receipt.getStyleClass().add("card");
        table.getSelectionModel().selectedItemProperty().addListener((obs,old,order)-> {
            receipt.getChildren().clear(); if(order==null)return;
            receipt.getChildren().addAll(Ui.label("Receipt · Order #"+order.id(),"section-title"),Ui.label("Deliver to: "+order.address(),"muted"),new Separator());
            for(OrderItem item:order.items())receipt.getChildren().add(Ui.row(item.quantity()+" × "+item.productName()+" @ "+Money.format(item.unitPrice()),Money.format(item.subtotal()),"summary-item","summary-value"));
            receipt.getChildren().addAll(new Separator(),Ui.row("Total",Money.format(order.total()),"field-label","total"));
        });
        Label feedback=Ui.feedback("orders-feedback"); VBox body=new VBox(18,table,receipt);
        Runnable refresh=()->Ui.async(this,"Loading your orders…",()->orders.history(user),rows-> {
            table.setItems(FXCollections.observableArrayList(rows));
            if(rows.isEmpty())body.getChildren().setAll(Ui.empty("Your story starts here","Your completed purchases will appear here.","Browse products",browse));
            else { body.getChildren().setAll(table,receipt); table.getSelectionModel().selectFirst(); }
        },error->Ui.failure(feedback,error));
        Button refreshButton=Ui.link("Refresh orders",refresh); refreshButton.setMaxWidth(Region.USE_PREF_SIZE);
        getChildren().addAll(Ui.label("Your orders","page-title"),Ui.label("Good finds, kept on record. Select an order to see its receipt.","muted"),
            refreshButton,feedback,Ui.scroll(body));
        Platform.runLater(refresh);
    }
}