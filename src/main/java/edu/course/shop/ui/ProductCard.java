package edu.course.shop.ui;
import edu.course.shop.model.Money;
import edu.course.shop.model.Product;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/** Photo and details open the same product; quick add delegates to CartService. */
public final class ProductCard extends VBox {
    private final Button quick;
    private final javafx.animation.PauseTransition confirmation=new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
    public void confirmAdded() {
        confirmation.stop();quick.setText("Added ✓");quick.getStyleClass().add("added-confirmation");confirmation.playFromStart();
    }
    public ProductCard(Product product,Runnable add,Runnable details) {
        setId("product-"+product.getId());getStyleClass().add("product-card");setSpacing(0);setMinWidth(0);setMaxWidth(Double.MAX_VALUE);

        // Photo: same box for every product, with rounded corners that match the card.
        ProductArtwork photo=new ProductArtwork(product,230,true);
        StackPane frame=new StackPane(photo);frame.setMinWidth(0);
        Rectangle clip=new Rectangle();clip.setArcWidth(4);clip.setArcHeight(4);
        frame.layoutBoundsProperty().addListener((o,a,b)->{clip.setWidth(b.getWidth());clip.setHeight(b.getHeight());});
        frame.setClip(clip);
        Button visual=Ui.button("",details);visual.setGraphic(frame);visual.setMaxWidth(Double.MAX_VALUE);
        frame.prefWidthProperty().bind(visual.widthProperty());visual.getStyleClass().add("photo-button");
        Motion.zoom(visual,photo);
        visual.setAccessibleText("View "+product.getName());

        // Name: always two lines tall so every card lines up; longer names end in an ellipsis.
        Label name=Ui.label(product.getName(),"product-name");
        name.setMinHeight(42);name.setPrefHeight(42);name.setMaxHeight(42);name.setMaxWidth(Double.MAX_VALUE);
        name.setTextOverrun(OverrunStyle.ELLIPSIS);

        // Price is the biggest thing in the body; stock is a small badge beside it (text, not color alone).
        boolean soldOut=product.getStock()==0,low=product.getStock()<=5;
        Label availability=Ui.label(soldOut?"Sold out":low?"Only "+product.getStock()+" left":"In stock",soldOut?"stock-out":"stock");
        availability.setWrapText(false);availability.setMaxWidth(Region.USE_PREF_SIZE);
        HBox priceRow=new HBox(10,Ui.label(Money.format(product.getPrice()),"price"),availability);priceRow.setAlignment(Pos.CENTER_LEFT);

        // Actions: "+ Add" is the one solid button; "View product" is a quiet text link.
        Button view=Ui.link("View product",details);view.setId("view-"+product.getId());
        view.setAccessibleText("View details for "+product.getName());
        Region space=new Region();HBox.setHgrow(space,Priority.ALWAYS);
        quick=Ui.button(soldOut?"Sold out":"+ Add",add);quick.setId("add-"+product.getId());
        confirmation.setOnFinished(e->{quick.setText("+ Add");quick.getStyleClass().remove("added-confirmation");});
        sceneProperty().addListener((o,a,b)->{if(b==null){confirmation.stop();quick.setText(soldOut?"Sold out":"+ Add");quick.getStyleClass().remove("added-confirmation");}});
        quick.getStyleClass().add("quick-add");quick.setDisable(soldOut);quick.setAccessibleText("Add "+product.getName()+" to cart");
        HBox actions=new HBox(8,view,space,quick);actions.setAlignment(Pos.CENTER_LEFT);

        VBox body=new VBox(8,Ui.label(Ui.category(product.getCategory()),"eyebrow"),name,priceRow,actions);
        body.getStyleClass().add("product-body");getChildren().addAll(visual,body);
    }
}
