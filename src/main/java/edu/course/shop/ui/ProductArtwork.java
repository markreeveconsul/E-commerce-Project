package edu.course.shop.ui;
import edu.course.shop.model.Product;
import edu.course.shop.model.PhotoFrame;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.io.ByteArrayInputStream;

/** Uploaded photos take priority; known demo items otherwise use clearly identified illustrations. */
public final class ProductArtwork extends StackPane {
    public ProductArtwork(Product product, double height) {
        this(product,height,false);
    }
    public ProductArtwork(Product product,double height,boolean square) {
        this(product.getPhoto(), product.getName(), height,square,product.getPhotoFrame());
        getStyleClass().add(product.getCategory().name().toLowerCase(java.util.Locale.ROOT));
        if (product.getPhoto() == null && CollectionArtwork.supports(product.getName())) {
            getChildren().setAll(new CollectionArtwork(product.getName()));
            setAccessibleText("Illustration of " + product.getName() + "; actual product may vary");
        }
    }
    public ProductArtwork(byte[] photo, String name, double height) { this(photo,name,height,false,PhotoFrame.AUTO); }
    public ProductArtwork(byte[] photo, String name, double height,boolean square,PhotoFrame frame) {
        setMinHeight(height); setPrefHeight(height); setMaxHeight(height);
        setMinWidth(0); setPrefWidth(height * 1.35); getStyleClass().add("product-art");
        if(square) {
            minHeightProperty().bind(widthProperty());prefHeightProperty().bind(widthProperty());maxHeightProperty().bind(widthProperty());
            getStyleClass().add("square-product-art");
        }
        if (photo != null && photo.length > 0) {
            Image image = new Image(new ByteArrayInputStream(photo), 800, 800, true, true);
            if (!image.isError()) {
                ImageView view = new ImageView(image); view.getStyleClass().add("product-photo"); view.setPreserveRatio(true); view.setSmooth(true);
                if(square) {
                    javafx.geometry.Rectangle2D subject=alphaBounds(image);
                    boolean cutout=subject!=null;
                    boolean contain=frame.mode()==PhotoFrame.Mode.CONTAIN||(frame.mode()==PhotoFrame.Mode.AUTO&&cutout);
                    javafx.geometry.Rectangle2D source=cutout?subject:new javafx.geometry.Rectangle2D(0,0,image.getWidth(),image.getHeight());
                    javafx.beans.InvalidationListener size=o->{
                        double w=Math.max(1,getWidth()),h=Math.max(1,getHeight());
                        if(contain) {
                            view.setViewport(source);view.setFitWidth(w*.88);view.setFitHeight(h*.88);
                        } else {
                            double scale=Math.max(w/source.getWidth(),h/source.getHeight());
                            double vw=w/scale,vh=h/scale;
                            view.setViewport(new javafx.geometry.Rectangle2D(source.getMinX()+(source.getWidth()-vw)*frame.x(),source.getMinY()+(source.getHeight()-vh)*frame.y(),vw,vh));
                            view.setFitWidth(w);view.setFitHeight(h);
                        }
                    };
                    widthProperty().addListener(size);heightProperty().addListener(size);size.invalidated(null);
                } else {
                    view.setFitHeight(height - 16);view.fitWidthProperty().bind(widthProperty().subtract(16));
                }
                getChildren().add(view); setAccessibleText("Photo of " + name); return;
            }
        }
        var placeholder = Ui.label(height<100?"NO PHOTO":"PHOTO COMING SOON", "photo-placeholder");
        getChildren().add(placeholder); setAccessibleText("No photo for " + name);
    }
    /** Trim only transparent margins; opaque backgrounds remain photographic content. */
    private static javafx.geometry.Rectangle2D alphaBounds(Image image) {
        var pixels=image.getPixelReader();int w=(int)image.getWidth(),h=(int)image.getHeight();
        int left=w,top=h,right=-1,bottom=-1;boolean transparent=false;
        for(int y=0;y<h;y++)for(int x=0;x<w;x++) {
            int alpha=pixels.getArgb(x,y)>>>24;
            if(alpha<255)transparent=true;
            if(alpha>8) {left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x);bottom=Math.max(bottom,y);}
        }
        return transparent&&right>=left?new javafx.geometry.Rectangle2D(left,top,right-left+1,bottom-top+1):null;
    }
}
