package edu.course.shop.ui;

import edu.course.shop.model.Product;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

/** Only real uploaded angles are offered; illustrations remain explicitly labeled. */
public final class ProductGallery extends VBox {
    private final Product product;
    private final List<byte[]> photos=new ArrayList<>();
    private final List<Button> thumbnails=new ArrayList<>();
    private final Button main;
    private final Label caption=Ui.label("","muted");
    private int selected;
    public ProductGallery(Product product) {
        this.product=product;setId("product-gallery");setSpacing(14);setMinWidth(0);
        if(product.getPhoto()!=null)photos.add(product.getPhoto());photos.addAll(product.getAdditionalPhotos());
        main=Ui.button("",this::enlarge);main.setId("zoom-photo");main.getStyleClass().add("photo-button");
        main.setAccessibleText("Enlarge photo of "+product.getName());main.setMaxWidth(Double.MAX_VALUE);
        FlowPane strip=new FlowPane(10,10);strip.setId("gallery-thumbnails");
        for(int i=0;i<photos.size();i++) {
            int index=i;
            ProductArtwork preview=new ProductArtwork(photos.get(i),product.getName(),64);preview.setPrefWidth(76);
            Button thumb=Ui.button("",()->select(index));thumb.setGraphic(preview);
            thumb.setId("gallery-photo-"+i);thumb.setAccessibleText("Show photo "+(i+1)+" of "+product.getName());
            thumb.getStyleClass().add("gallery-thumbnail");thumbnails.add(thumb);strip.getChildren().add(thumb);
        }
        strip.setVisible(photos.size()>1);strip.managedProperty().bind(strip.visibleProperty());
        getChildren().addAll(main,strip,caption);select(0);
    }
    private void select(int index) {
        selected=index;
        ProductArtwork art=photos.isEmpty()?new ProductArtwork(product,520):new ProductArtwork(photos.get(index),product.getName(),520);
        art.setId("gallery-main");art.getStyleClass().add(product.getCategory().name().toLowerCase(java.util.Locale.ROOT));
        var height=javafx.beans.binding.Bindings.min(560,javafx.beans.binding.Bindings.max(320,widthProperty().multiply(.90)));
        art.minHeightProperty().bind(height);art.prefHeightProperty().bind(height);art.maxHeightProperty().bind(height);
        // Full photograph stays visible; the catalog crop is not applied to the detail gallery.
        if(art.lookup(".product-photo") instanceof javafx.scene.image.ImageView image)image.fitHeightProperty().bind(height.subtract(32));
        art.prefWidthProperty().bind(main.widthProperty().subtract(4));main.setGraphic(art);
        for(int i=0;i<thumbnails.size();i++) {
            thumbnails.get(i).getStyleClass().remove("gallery-selected");
            if(i==index)thumbnails.get(i).getStyleClass().add("gallery-selected");
        }
        caption.setText(photos.isEmpty()?(CollectionArtwork.supports(product.getName())?"Product illustration · Actual product may vary.":"Photo not available yet."):
            "Photo "+(index+1)+" of "+photos.size()+" · Select image to enlarge");
    }
    private void enlarge() {
        Dialog<Void> dialog=new Dialog<>();dialog.setTitle(product.getName());dialog.initOwner(getScene().getWindow());
        ProductArtwork image=photos.isEmpty()?new ProductArtwork(product,520):new ProductArtwork(photos.get(selected),product.getName(),520);
        image.setPrefWidth(Math.min(700,getScene().getWidth()-100));
        dialog.getDialogPane().setContent(image);dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);dialog.showAndWait();
    }
}
