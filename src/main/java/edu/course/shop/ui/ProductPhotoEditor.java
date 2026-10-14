package edu.course.shop.ui;
import edu.course.shop.service.ProductPhotos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.nio.file.Path;

/** A photo draft. SQLite is updated only when the surrounding product form is saved. */
public final class ProductPhotoEditor extends VBox {
    private byte[] photo;
    private final VBox preview=new VBox();
    private final Label feedback=Ui.feedback("photo-feedback");
    public ProductPhotoEditor() {
        setId("photo-editor");setSpacing(10);preview.setId("photo-preview");
        Button upload=Ui.button("Upload photo",()->{
            FileChooser chooser=new FileChooser();chooser.setTitle("Choose a product photo");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG or JPEG photos","*.png","*.jpg","*.jpeg","*.PNG","*.JPG","*.JPEG"));
            java.io.File file=chooser.showOpenDialog(getScene().getWindow());
            if(file!=null)importPhoto(file.toPath());
        });upload.setId("upload-photo");
        Button remove=Ui.button("Remove photo",()->{
            setPhoto(null);Ui.success(feedback,"Photo removed from draft. Save product to apply.");
        });remove.setId("remove-photo");
        getChildren().addAll(Ui.label("Product photo · PNG/JPEG · Up to 5 MB and 4096 × 4096 px","muted"),
            preview,new VBox(6,upload,remove),feedback);setPhoto(null);
    }
    public void importPhoto(Path file) {
        Ui.async(this,"Checking photo…",()->ProductPhotos.read(file),bytes->{
            setPhoto(bytes);Ui.success(feedback,"Photo ready. Save product to apply it.");
        },error->Ui.failure(feedback,error));
    }
    public void setPhoto(byte[] bytes) {
        photo=bytes==null?null:bytes.clone();feedback.setText("");
        preview.getChildren().setAll(new ProductArtwork(photo,"Selected product",120));
    }
    public byte[] getPhoto() { return photo==null?null:photo.clone(); }
}
