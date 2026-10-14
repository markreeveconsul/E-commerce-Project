package edu.course.shop.ui;
import edu.course.shop.service.ProductPhotos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.nio.file.Path;

/** A photo draft. SQLite is updated only when the surrounding product form is saved. */
public final class ProductPhotoEditor extends VBox {
    private byte[] photo;
    private final java.util.List<byte[]> additional=new java.util.ArrayList<>();
    private final FlowPane gallery=new FlowPane(8,8);
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
            setPhotos(null,java.util.List.of());Ui.success(feedback,"Photo removed from draft. Save product to apply.");
        });remove.setId("remove-photo");
        Button angle=Ui.button("Add gallery photo",()->{
            FileChooser chooser=new FileChooser();chooser.setTitle("Choose an additional product angle");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG or JPEG photos","*.png","*.jpg","*.jpeg","*.PNG","*.JPG","*.JPEG"));
            java.io.File file=chooser.showOpenDialog(getScene().getWindow());if(file!=null)importAdditionalPhoto(file.toPath());
        });angle.setId("upload-gallery-photo");gallery.setId("gallery-draft");
        getChildren().addAll(Ui.label("Photos · PNG/JPEG · Up to 5 MB and 4096 × 4096 px each","muted"),
            preview,new VBox(6,upload,angle,remove),gallery,feedback);setPhoto(null);
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
    public void setPhotos(byte[] main,java.util.List<byte[]> extras) {
        additional.clear();extras.forEach(bytes->additional.add(bytes.clone()));setPhoto(main);renderGallery();
    }
    public void importAdditionalPhoto(Path file) {
        if(photo==null){Ui.failure(feedback,new edu.course.shop.exception.ValidationException("Upload a main photo first."));return;}
        if(additional.size()>=4){Ui.failure(feedback,new edu.course.shop.exception.ValidationException("Add at most four additional photos."));return;}
        Ui.async(this,"Checking gallery photo…",()->ProductPhotos.read(file),bytes->{
            additional.add(bytes);renderGallery();Ui.success(feedback,"Gallery photo ready. Save product to apply it.");
        },error->Ui.failure(feedback,error));
    }
    private void renderGallery() {
        gallery.getChildren().clear();
        for(int i=0;i<additional.size();i++) {
            int index=i;ProductArtwork art=new ProductArtwork(additional.get(i),"Gallery angle "+(i+1),60);art.setPrefWidth(90);
            Button remove=Ui.link("Remove angle "+(i+1),()->{additional.remove(index);renderGallery();});
            remove.setAccessibleText("Remove gallery photo "+(i+1)+" from draft");gallery.getChildren().add(new VBox(4,art,remove));
        }
    }
    public java.util.List<byte[]> getAdditionalPhotos() {return additional.stream().map(byte[]::clone).toList();}
    public byte[] getPhoto() { return photo==null?null:photo.clone(); }
}
