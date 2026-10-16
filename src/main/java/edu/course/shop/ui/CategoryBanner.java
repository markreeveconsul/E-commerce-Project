package edu.course.shop.ui;

import edu.course.shop.model.Category;
import java.util.EnumMap;
import java.util.Locale;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.shape.Ellipse;
import javafx.stage.Window;
import javafx.util.Duration;

/** Shared category campaigns; the Shop all hero remains a separate component. */
public final class CategoryBanner extends Pane {
    private record Campaign(String headline,String support,String image) {}
    private record Artwork(Image image,Rectangle2D viewport) {}
    private static final EnumMap<Category,Artwork> ART=new EnumMap<>(Category.class);
    private final Canvas canvas=new Canvas();
    private final Label eyebrow=Ui.label("","category-banner-eyebrow");
    private final Label headline=Ui.label("","category-banner-headline");
    private final Label support=Ui.label("","category-banner-support");
    private final Label pending=Ui.label("","category-banner-pending");
    private final VBox copy=new VBox(8,eyebrow,headline,support);
    private final ImageView product=new ImageView();
    private final StackPane floating=new StackPane(product);
    private final Ellipse contactShadow=new Ellipse();
    private final TranslateTransition drift=new TranslateTransition(Duration.seconds(3.2),floating);
    private final InvalidationListener lifecycle=o->syncMotion();
    private final ChangeListener<Window> windowListener=(o,a,b)->watchWindow(b);
    private Window watchedWindow;
    private Category category;
    private Campaign campaign;

    public CategoryBanner(){
        setId("category-banner");setMinWidth(0);setPrefWidth(0);
        canvas.setMouseTransparent(true);copy.setMouseTransparent(true);pending.setMouseTransparent(true);
        floating.setMouseTransparent(true);floating.setManaged(false);
        contactShadow.setMouseTransparent(true);contactShadow.setManaged(false);
        product.setPreserveRatio(true);product.setSmooth(true);product.setMouseTransparent(true);
        product.setEffect(new DropShadow(26,0,16,Color.web("#000000",.62)));
        contactShadow.setFill(Color.web("#000000",.42));
        contactShadow.setEffect(new javafx.scene.effect.GaussianBlur(16));
        getChildren().addAll(canvas,contactShadow,floating,copy,pending);
        widthProperty().addListener((o,a,b)->{double height=b.doubleValue()<1200?242:260;setMinHeight(height);setPrefHeight(height);setMaxHeight(height);});
        setMinHeight(260);setPrefHeight(260);setMaxHeight(260);
        visibleProperty().addListener(lifecycle);
        Motion.reducedProperty().addListener(new WeakInvalidationListener(lifecycle));
        sceneProperty().addListener((o,a,b)->{
            if(a!=null)a.windowProperty().removeListener(windowListener);
            if(b!=null)b.windowProperty().addListener(windowListener);
            watchWindow(b==null?null:b.getWindow());
        });
        drift.setFromY(0);drift.setToY(-7);drift.setAutoReverse(true);drift.setCycleCount(Animation.INDEFINITE);
        drift.setInterpolator(Interpolator.EASE_BOTH);
    }

    public void showCategory(Category value){
        category=value;
        campaign=switch(value){
            case PC_SETS -> new Campaign("YOUR\nNEXT SETUP.","Built for work. Ready for play.","category-pc.png");
            case PC_PARTS -> new Campaign("BUILD YOUR\nADVANTAGE.","The parts behind your next upgrade.","category-gpu.png");
            case LAPTOPS -> new Campaign("POWER\nTHAT MOVES.","Performance, wherever you go.","category-laptop.png");
            case PHONES_TABLETS -> new Campaign("STAY\nCONNECTED.","Everyday essentials. Extraordinary possibilities.","category-phones.png");
            case PERIPHERALS -> new Campaign("MAKE IT\nYOURS.","The finishing touches for your setup.","category-keyboard.png");
            case ELECTRONICS -> new Campaign("DISCOVER\nWHAT'S NEXT.","More possibilities for your everyday.","category-controller.png");
            default -> throw new IllegalArgumentException("Unsupported category");
        };
        eyebrow.setText(value.label().toUpperCase(Locale.ROOT));headline.setText(campaign.headline());support.setText(campaign.support());
        pending.setText(value==Category.PC_SETS?"PC set model\nto be selected":"Electronics model\nto be selected");
        pending.setVisible(campaign.image()==null);
        if(campaign.image()!=null){
            Artwork art=ART.computeIfAbsent(value,c->load(campaign.image()));
            product.setImage(art.image());product.setViewport(art.viewport());
            floating.setVisible(true);contactShadow.setVisible(true);
        }else{
            product.setImage(null);product.setViewport(null);
            floating.setVisible(false);contactShadow.setVisible(false);
        }
        setAccessibleText(value.label()+". "+campaign.headline().replace('\n',' ')+". "+campaign.support());
        syncMotion();requestLayout();
    }

    /** Crop transparent padding so the visible product, rather than its file canvas, fills the artwork area. */
    private static Artwork load(String file){
        Image image=new Image(java.util.Objects.requireNonNull(CategoryBanner.class.getResource("/images/"+file)).toExternalForm());
        if(image.isError())throw new IllegalStateException("Category artwork could not load",image.getException());
        var pixels=image.getPixelReader();
        int minX=(int)image.getWidth(),minY=(int)image.getHeight(),maxX=-1,maxY=-1;
        for(int y=0;y<(int)image.getHeight();y++)for(int x=0;x<(int)image.getWidth();x++){
            if((pixels.getArgb(x,y)>>>24)<24)continue;
            minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);
        }
        if(maxX<minX)return new Artwork(image,null);
        return new Artwork(image,new Rectangle2D(minX,minY,maxX-minX+1,maxY-minY+1));
    }

    private void watchWindow(Window next){
        if(watchedWindow!=null)watchedWindow.showingProperty().removeListener(lifecycle);
        watchedWindow=next;
        if(watchedWindow!=null)watchedWindow.showingProperty().addListener(lifecycle);
        syncMotion();
    }

    private void syncMotion(){
        boolean active=category!=null&&campaign!=null&&campaign.image()!=null&&isVisible()
            &&watchedWindow!=null&&watchedWindow.isShowing()&&!Motion.isReduced();
        if(active){if(drift.getStatus()!=Animation.Status.RUNNING)drift.playFromStart();}
        else{drift.stop();floating.setTranslateY(0);}
    }

    @Override protected void layoutChildren(){
        double w=getWidth(),h=getHeight(),nav=w<1200?52:0,body=h-nav;
        canvas.setWidth(w);canvas.setHeight(h);var g=canvas.getGraphicsContext2D();
        g.setFill(Color.web("#111214"));g.fillRect(0,0,w,h);
        if(campaign==null)return;
        double cx=w*.78,cy=nav+body*.5;
        g.setFill(new RadialGradient(0,0,cx,cy,body*1.2,false,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#8A3817",.72)),new Stop(.5,Color.web("#492516",.45)),new Stop(1,Color.TRANSPARENT)));
        g.fillRect(w*.43,nav,w*.57,body);
        g.setStroke(Color.web("#E96020",.55));g.setLineWidth(.8);
        for(double r:new double[]{body*.62,body*.83,body*1.08})g.strokeOval(cx-r,cy-r,r*2,r*2);
        g.setStroke(Color.web("#E96020",.30));
        for(int i=0;i<5;i++){double x=w*.44+i*body*.31;g.strokeLine(x,h,x+body*.85,nav);}
        if(nav>0){g.setFill(Color.web("#111214"));g.fillRect(0,0,w,nav);}
        double left=w*.045,copyWidth=w*.45;
        headline.setStyle("-fx-font-size: "+(w<1200?32:52)+"px;");
        copy.resize(copyWidth,copy.prefHeight(copyWidth));copy.relocate(left,nav+(body-copy.getHeight())/2);
        if(campaign.image()!=null){
            double artWidth=w*.47,artHeight=body+36,overlap=18;
            product.setFitWidth(artWidth);product.setFitHeight(artHeight);
            // Center the PC and its contact shadow on the background rings.
            double artCenter=category==Category.PC_SETS?cx:w*.755;
            floating.resizeRelocate(artCenter-artWidth/2,nav+(body-artHeight)/2+overlap,artWidth,artHeight);
            contactShadow.setCenterX(artCenter);contactShadow.setCenterY(h+2);
            contactShadow.setRadiusX(Math.min(artWidth*.26,150));contactShadow.setRadiusY(8);
            pending.setVisible(false);
        }else{
            floating.setVisible(false);contactShadow.setVisible(false);
            double artWidth=w*.43,artHeight=body*.74,x=w*.52+(w*.47-artWidth)/2,y=nav+(body-artHeight)/2;
            g.setStroke(Color.web("#919191",.6));g.setLineDashes(6,5);g.strokeRoundRect(x,y,artWidth,artHeight,10,10);g.setLineDashes();
            pending.setVisible(true);pending.resizeRelocate(x+12,y,artWidth-24,artHeight);
        }
    }
}
