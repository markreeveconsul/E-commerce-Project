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
    private final javafx.beans.property.BooleanProperty compact=new javafx.beans.property.SimpleBooleanProperty();
    private final javafx.beans.property.DoubleProperty windowHeight=new javafx.beans.property.SimpleDoubleProperty(860);
    public void setCompact(boolean value){compact.set(value);}
    public ProductGallery(Product product) {
        this.product=product;
        sceneProperty().addListener((o,a,b)->{windowHeight.unbind();if(b!=null)windowHeight.bind(b.heightProperty());});
        setId("product-gallery");setSpacing(14);setMinWidth(0);
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
        var height=javafx.beans.binding.Bindings.createDoubleBinding(()->compact.get()?Math.min(360,Math.max(240,windowHeight.get()*.42)):Math.min(560,Math.max(320,getWidth()*.9)),widthProperty(),compact,windowHeight);
        art.minHeightProperty().bind(height);art.prefHeightProperty().bind(height);art.maxHeightProperty().bind(height);
        // Full photograph stays visible; the catalog crop is not applied to the detail gallery.
        if(art.lookup(".product-photo") instanceof javafx.scene.image.ImageView image)image.fitHeightProperty().bind(height.subtract(32));
        art.prefWidthProperty().bind(main.widthProperty().subtract(4));main.setGraphic(art);Motion.reveal(art);
        for(int i=0;i<thumbnails.size();i++) {
            thumbnails.get(i).getStyleClass().remove("gallery-selected");
            if(i==index)thumbnails.get(i).getStyleClass().add("gallery-selected");
        }
        caption.setText(photos.isEmpty()?(CollectionArtwork.supports(product.getName())?"Product illustration · Actual product may vary.":"Photo not available yet."):
            "Photo "+(index+1)+" of "+photos.size()+" · Select image to enlarge");
    }
    private void enlarge() {
        Dialog<Void> dialog=new Dialog<>();dialog.setTitle(product.getName());dialog.initOwner(getScene().getWindow());dialog.setResizable(true);dialog.setOnHidden(e->main.requestFocus());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        StackPane surface=new StackPane();surface.setMinSize(0,0);
        ScrollPane viewport=new ScrollPane(surface);viewport.setPannable(true);viewport.setId("image-zoom-viewport");viewport.setPrefViewportWidth(Math.min(850,getScene().getWidth()-120));viewport.setPrefViewportHeight(Math.min(560,getScene().getHeight()-210));
        javafx.beans.property.DoubleProperty scale=new javafx.beans.property.SimpleDoubleProperty(1);
        Label caption=Ui.label("","muted");caption.setId("image-zoom-caption");
        int[] current={selected};double baseWidth=Math.min(800,getScene().getWidth()-150),baseHeight=Math.min(520,getScene().getHeight()-240);
        javafx.scene.image.ImageView image=new javafx.scene.image.ImageView();image.setPreserveRatio(true);image.setSmooth(true);
        image.fitWidthProperty().bind(scale.multiply(baseWidth));image.fitHeightProperty().bind(scale.multiply(baseHeight));
        Runnable render=()->{
            scale.set(1);viewport.setHvalue(.5);viewport.setVvalue(.5);
            if(photos.isEmpty())surface.getChildren().setAll(new ProductArtwork(product,baseHeight));
            else {image.setImage(new javafx.scene.image.Image(new java.io.ByteArrayInputStream(photos.get(current[0])),3000,3000,true,true));image.setAccessibleText("Photo "+(current[0]+1)+" of "+product.getName());surface.getChildren().setAll(image);}
            caption.setText(photos.isEmpty()?"Illustration / photo unavailable":"Photo "+(current[0]+1)+" of "+photos.size()+" · Drag to pan when zoomed");
        };
        Button previous=Ui.button("←",()->{current[0]=Math.floorMod(current[0]-1,photos.size());render.run();}),next=Ui.button("→",()->{current[0]=(current[0]+1)%photos.size();render.run();});
        previous.setId("image-previous");next.setId("image-next");previous.setAccessibleText("Previous photo");next.setAccessibleText("Next photo");previous.setDisable(photos.size()<2);next.setDisable(photos.size()<2);
        Button minus=Ui.button("−",()->scale.set(Math.max(1,scale.get()-.5))),plus=Ui.button("+",()->scale.set(Math.min(4,scale.get()+.5)));
        minus.setId("image-zoom-out");plus.setId("image-zoom-in");minus.setAccessibleText("Zoom out");plus.setAccessibleText("Zoom in");minus.disableProperty().bind(scale.lessThanOrEqualTo(1));plus.disableProperty().bind(scale.greaterThanOrEqualTo(4));
        if(photos.isEmpty()){minus.disableProperty().unbind();plus.disableProperty().unbind();minus.setDisable(true);plus.setDisable(true);}
        Button reset=Ui.button("Reset zoom",()->scale.set(1));Label level=Ui.label("","field-label");level.textProperty().bind(scale.multiply(100).asString("%.0f%%"));
        HBox tools=new HBox(12,previous,minus,level,plus,reset,next);tools.setAlignment(javafx.geometry.Pos.CENTER);render.run();
        dialog.getDialogPane().setContent(new VBox(16,viewport,tools,caption));
        dialog.getDialogPane().addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,e->{if(e.getCode()==javafx.scene.input.KeyCode.LEFT){previous.fire();e.consume();}else if(e.getCode()==javafx.scene.input.KeyCode.RIGHT){next.fire();e.consume();}});dialog.showAndWait();
    }
}
