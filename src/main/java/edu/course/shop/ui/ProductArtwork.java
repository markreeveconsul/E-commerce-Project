package edu.course.shop.ui;
import edu.course.shop.model.Product;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.io.ByteArrayInputStream;

/** Uploaded photos take priority; known demo items otherwise use clearly identified illustrations. */
public final class ProductArtwork extends StackPane {
    public ProductArtwork(Product product, double height) {
        this(product.getPhoto(), product.getName(), height);
        getStyleClass().add(product.getCategory().name().toLowerCase(java.util.Locale.ROOT));
        if (product.getPhoto() == null && CollectionArtwork.supports(product.getName())) {
            getChildren().setAll(new CollectionArtwork(product.getName()));
            setAccessibleText("Illustration of " + product.getName() + "; actual product may vary");
        }
    }
    public ProductArtwork(byte[] photo, String name, double height) {
        setMinHeight(height); setPrefHeight(height); setMaxHeight(height);
        setMinWidth(0); setPrefWidth(height * 1.35); getStyleClass().add("product-art");
        if (photo != null && photo.length > 0) {
            Image image = new Image(new ByteArrayInputStream(photo), 800, 800, true, true);
            if (!image.isError()) {
                ImageView view = new ImageView(image); view.getStyleClass().add("product-photo"); view.setPreserveRatio(true); view.setSmooth(true);
                view.setFitHeight(height - 16); view.fitWidthProperty().bind(widthProperty().subtract(16));
                getChildren().add(view); setAccessibleText("Photo of " + name); return;
            }
        }
        var placeholder = Ui.label(height<100?"NO PHOTO":"PHOTO COMING SOON", "photo-placeholder");
        getChildren().add(placeholder); setAccessibleText("No photo for " + name);
    }
}
