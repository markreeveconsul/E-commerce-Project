package edu.course.shop.ui;
import edu.course.shop.service.ProductPhotos;
import edu.course.shop.model.PhotoFrame;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.nio.file.Path;

/** A photo draft. SQLite is updated only when the surrounding product form is saved. */
public final class ProductPhotoEditor extends VBox {
    private byte[] photo;
    private final ComboBox<PhotoFrame.Mode> mode=new ComboBox<>();
    private final Slider focalX=new Slider(0,1,.5),focalY=new Slider(0,1,.5);
    private final Label dimensions=Ui.label("No photo selected","muted");
    private boolean updating;
    public PhotoFrame getFraming(){return new PhotoFrame(mode.getValue(),focalX.getValue(),focalY.getValue());}
    public void setFraming(PhotoFrame framing){updating=true;mode.setValue(framing.mode());focalX.setValue(framing.x());focalY.setValue(framing.y());updating=false;renderPreview();}
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
        mode.getItems().setAll(PhotoFrame.Mode.values());mode.setValue(PhotoFrame.Mode.AUTO);mode.setId("photo-frame-mode");mode.setAccessibleText("Catalog photo fitting: automatic, contain or cover");focalX.setId("photo-focal-x");focalY.setId("photo-focal-y");focalX.setAccessibleText("Horizontal photo focal point");focalY.setAccessibleText("Vertical photo focal point");
        mode.valueProperty().addListener((o,a,b)->{if(!updating)renderPreview();});focalX.valueProperty().addListener((o,a,b)->{if(!updating)renderPreview();});focalY.valueProperty().addListener((o,a,b)->{if(!updating)renderPreview();});
        TitledPane framing=new TitledPane("Catalog framing",new VBox(8,Ui.label("Fit mode","field-label"),mode,Ui.label("Focal point: horizontal / vertical","muted"),focalX,focalY));framing.setExpanded(false);framing.setAnimated(false);framing.setId("photo-framing");
        Button compare=Ui.button("Preview photo frames",this::compare);compare.setId("preview-photo-frames");
        getChildren().addAll(Ui.label("Photos · PNG/JPEG · Up to 5 MB and 4096 × 4096 px each","muted"),
            preview,dimensions,new VBox(6,upload,angle,remove,compare),framing,gallery,feedback);setPhoto(null);
    }
    public void importPhoto(Path file) {
        Ui.async(this,"Checking photo…",()->ProductPhotos.read(file),bytes->{
            setPhoto(bytes);setFraming(PhotoFrame.AUTO);Ui.success(feedback,"Photo ready. Save product to apply it.");
        },error->Ui.failure(feedback,error));
    }
    public void setPhoto(byte[] bytes) {
        photo=bytes==null?null:bytes.clone();feedback.setText("");
        renderPreview();
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
            Button main=Ui.link("Make main",()->{byte[] old=photo;setPhoto(additional.get(index));additional.set(index,old);setFraming(PhotoFrame.AUTO);renderGallery();});main.setId("make-main-"+i);
            Button left=Ui.button("←",()->{java.util.Collections.swap(additional,index,index-1);renderGallery();}),right=Ui.button("→",()->{java.util.Collections.swap(additional,index,index+1);renderGallery();});left.setId("angle-earlier-"+i);right.setId("angle-later-"+i);left.setDisable(index==0);right.setDisable(index==additional.size()-1);left.setAccessibleText("Move angle "+(i+1)+" earlier");right.setAccessibleText("Move angle "+(i+1)+" later");
            VBox tile=new VBox(4,art,main,new HBox(4,left,right),remove);tile.getProperties().put("market.angleIndex",index);
            tile.setOnDragDetected(e->{var drag=tile.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);var content=new javafx.scene.input.ClipboardContent();content.putString(Integer.toString(index));drag.setContent(content);e.consume();});
            tile.setOnDragOver(e->{if(gallery.getChildren().contains(e.getGestureSource())&&e.getGestureSource()!=tile)e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);e.consume();});
            tile.setOnDragDropped(e->{if(e.getGestureSource() instanceof VBox source&&gallery.getChildren().contains(source)){int from=(Integer)source.getProperties().get("market.angleIndex");byte[] moved=additional.remove(from);additional.add(index,moved);renderGallery();e.setDropCompleted(true);}e.consume();});
            remove.setAccessibleText("Remove gallery photo "+(i+1)+" from draft");gallery.getChildren().add(tile);
        }
    }
    public java.util.List<byte[]> getAdditionalPhotos() {return additional.stream().map(byte[]::clone).toList();}
    public byte[] getPhoto() { return photo==null?null:photo.clone(); }
    private void renderPreview() {
        if(mode.getValue()==null)return;
        preview.getChildren().setAll(new ProductArtwork(photo,"Selected product",120));
        if(photo==null){dimensions.setText("No photo selected");return;}
        javafx.scene.image.Image image=new javafx.scene.image.Image(new java.io.ByteArrayInputStream(photo));
        dimensions.setText((int)image.getWidth()+" × "+(int)image.getHeight()+" px · "+Math.max(1,photo.length/1024)+" KB"+
            (Math.min(image.getWidth(),image.getHeight())<600?"\nSmall source: may look soft when enlarged.":"\nOriginal retained; framing only changes the catalog."));
    }
    private void compare() {
        Dialog<Void> dialog=new Dialog<>();dialog.setTitle("Catalog & gallery photo preview");dialog.initOwner(getScene().getWindow());dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ProductArtwork square=new ProductArtwork(photo,"Catalog preview",260,true,getFraming());square.setPrefWidth(260);square.setMaxWidth(260);
        ProductArtwork full=new ProductArtwork(photo,"Full gallery photo",300);full.setPrefWidth(350);full.setMaxWidth(350);
        dialog.getDialogPane().setContent(new HBox(24,new VBox(12,Ui.label("Catalog frame","section-title"),square),new VBox(12,Ui.label("Full gallery image","section-title"),full)));dialog.showAndWait();
    }
}
