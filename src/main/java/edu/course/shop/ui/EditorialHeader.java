package edu.course.shop.ui;

import edu.course.shop.model.Product;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import java.util.List;

/** Collection introduction. Uploaded photos are reused; no external images or product rules. */
public final class EditorialHeader extends HBox {
    private final HBox showcase=new HBox(12);

    public EditorialHeader() {
        super(30);
        getStyleClass().add("editorial-header");setAlignment(Pos.CENTER_LEFT);
        VBox copy=new VBox(10,Ui.label("OBJECTS FOR YOUR EVERYDAY.","editorial-eyebrow"),
            Ui.label("LESS NOISE.\nGOOD FINDS.","editorial-title"),
            Ui.label("For the way you listen, dress, and live.","muted"));
        copy.setMinWidth(0);HBox.setHgrow(copy,Priority.ALWAYS);
        showcase.setPrefWidth(400);showcase.setMinWidth(280);showcase.setAlignment(Pos.CENTER);
        getChildren().addAll(copy,showcase);
    }

    public void setProducts(List<Product> products) {
        showcase.getChildren().clear();
        List<Product> photographed=products.stream().filter(p->p.getPhoto()!=null).limit(2).toList();
        if(photographed.isEmpty()) {
            // An intentional typographic composition also works before the admin uploads photos.
            addPanel(new VBox(8,Ui.label("01 / ELECTRONICS","editorial-eyebrow"),
                Ui.label("LISTEN.\nFOCUS.\nREPEAT.","editorial-panel-title")),"editorial-panel-dark");
            addPanel(new VBox(8,Ui.label("02 / EVERYDAY","editorial-eyebrow"),
                Ui.label("WEAR.\nCARRY.\nLIVE.","editorial-panel-title")),"editorial-panel-light");
        } else {
            for(Product product:photographed) {
                ProductArtwork photo=new ProductArtwork(product,170);
                photo.setPrefWidth(180);
                VBox panel=new VBox(8,photo,Ui.label(product.getName(),"editorial-caption"));
                panel.setMinWidth(0);HBox.setHgrow(panel,Priority.ALWAYS);showcase.getChildren().add(panel);
            }
        }
    }

    private void addPanel(VBox panel,String style) {
        panel.setAlignment(Pos.CENTER_LEFT);panel.setMinWidth(0);panel.setPrefWidth(195);
        panel.getStyleClass().add(style);HBox.setHgrow(panel,Priority.ALWAYS);showcase.getChildren().add(panel);
    }
}
