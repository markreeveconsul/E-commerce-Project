package edu.course.shop.ui;

import javafx.animation.*;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/** Layered campaign. Timelines live only while this banner is visible in an active window. */
public final class EditorialHeader extends StackPane {
    private final StackPane floating=new StackPane(),parallax=new StackPane(),productReveal=new StackPane();
    private final Region glow=new Region();
    private final TranslateTransition drift=new TranslateTransition(Duration.seconds(3.2),floating);
    private final FadeTransition pulse=new FadeTransition(Duration.seconds(3.8),glow);
    private final TranslateTransition pointer=new TranslateTransition(Duration.millis(240),parallax);
    private final ParallelTransition entrance=new ParallelTransition();
    private final InvalidationListener lifecycle=o->syncMotion();
    private final ChangeListener<Window> windowListener=(o,a,b)->watchWindow(b);
    private final Node[] copyNodes;
    private Window watchedWindow;
    private boolean inViewport=true,entered;

    public EditorialHeader(Runnable shop,Runnable electronics) { this(shop,electronics,electronics); }

    public EditorialHeader(Runnable shop,Runnable electronics,Runnable headphones) { this(shop,electronics,headphones,false); }

    /** Account campaign uses the same motion lifecycle, with a vertical PC composition. */
    public static EditorialHeader forLogin() { return new EditorialHeader(null,null,null,true); }

    private EditorialHeader(Runnable shop,Runnable electronics,Runnable headphones,boolean account) {
        setId(account?"login-campaign":"editorial-hero");getStyleClass().add("editorial-header");
        if(account)getStyleClass().add("account-campaign");
        glow.setId("hero-glow");glow.getStyleClass().add("hero-glow");glow.setMouseTransparent(true);glow.setOpacity(.65);
        glow.setMaxSize(640,640);glow.setPrefSize(640,640);
        StackPane.setAlignment(glow,Pos.CENTER_RIGHT);StackPane.setMargin(glow,new Insets(0,-25,0,0));
        Pane rings=new Rings();rings.setMouseTransparent(true);

        Label eyebrow=Ui.label(account?"MARKET / THE POWER EDIT":"MARKET / THE SOUND EDIT","hero-eyebrow");
        VBox headline=new VBox(-5,Ui.label(account?"POWER UP.":"TUNE IN.","hero-headline"),Ui.label("STAND OUT.","hero-headline"));
        headline.setId("hero-headline");
        Label support=Ui.label(account?"Sign in. Find your next favorite.":"Find your rhythm. Make it yours.","hero-sub");
        Button browse=Ui.button("Explore headphones  →",headphones);browse.setId("hero-headphones");browse.getStyleClass().add("hero-cta");
        Button all=Ui.button("Browse collection  ↗",shop);all.setId("hero-shop");all.getStyleClass().add("hero-link");
        FlowPane actions=new FlowPane(16,8,browse,all);actions.setId("hero-actions");
        VBox copy=new VBox(18,eyebrow,headline,support,actions);copy.getStyleClass().add("hero-copy");
        copy.setMinWidth(0);copy.setAlignment(Pos.CENTER_LEFT);HBox.setHgrow(copy,Priority.ALWAYS);
        if(account)copy.getChildren().remove(actions);
        copyNodes=account?new Node[]{eyebrow,headline,support}:new Node[]{eyebrow,headline,support,actions};

        Image image=new Image(java.util.Objects.requireNonNull(getClass().getResource(account?"/images/login-pc-campaign.png":"/images/headset-campaign.png")).toExternalForm());
        if(image.isError())throw new IllegalStateException("Campaign image could not be loaded",image.getException());
        ImageView headset=new ImageView(image);headset.setId(account?"login-pc":"hero-headset");headset.setPreserveRatio(true);headset.setSmooth(true);
        if(!account)headset.fitHeightProperty().bind(heightProperty().multiply(1.10));
        floating.setId("hero-float");floating.getChildren().add(headset);
        StackPane.setAlignment(headset,Pos.TOP_CENTER);StackPane.setMargin(headset,new Insets(account?0:16,0,0,0));
        parallax.setId("hero-parallax");parallax.getChildren().add(floating);
        productReveal.getChildren().add(parallax);productReveal.setMouseTransparent(true);
        floating.setMinSize(0,0);parallax.setMinSize(0,0);productReveal.setMinSize(0,0);
        VBox caption=new VBox(4,Ui.label("01 / FIND YOUR FOCUS","campaign-kicker"),Ui.label("Explore electronics  ↗","campaign-link"));
        caption.getStyleClass().add("campaign-caption");caption.setMaxHeight(USE_PREF_SIZE);caption.setMouseTransparent(true);
        StackPane visual=new StackPane(productReveal,caption);visual.setId("hero-visual");
        visual.setMinSize(0,0);
        StackPane.setAlignment(caption,Pos.BOTTOM_RIGHT);
        Button discover=Ui.button("",electronics);discover.setId("hero-electronics");discover.setGraphic(visual);
        discover.setAccessibleText("Explore electronics; headset campaign artwork");discover.getStyleClass().add("hero-art-button");
        discover.setMinWidth(310);discover.setPrefWidth(500);discover.setMaxWidth(550);discover.setMaxHeight(Double.MAX_VALUE);
        visual.prefWidthProperty().bind(discover.widthProperty());visual.prefHeightProperty().bind(heightProperty());
        if(account) {
            visual.getChildren().remove(productReveal);
            Label edition=Ui.label("01 / BUILT FOR YOUR NEXT CHAPTER","campaign-kicker");
            edition.getStyleClass().add("account-edition");
            StackPane artwork=new StackPane(productReveal);artwork.setMinSize(0,0);
            VBox.setVgrow(artwork,Priority.ALWAYS);
            headset.fitHeightProperty().bind(javafx.beans.binding.Bindings.max(1,artwork.heightProperty().subtract(24)));
            headset.fitWidthProperty().bind(javafx.beans.binding.Bindings.max(1,artwork.widthProperty().subtract(56)));
            StackPane.setAlignment(headset,Pos.CENTER);
            VBox content=new VBox(0,copy,artwork,edition);content.setMinSize(0,0);
            getChildren().addAll(glow,rings,new CircuitAccents(),content);
        } else {
            HBox content=new HBox(0,copy,discover);content.setAlignment(Pos.CENTER_LEFT);
            getChildren().addAll(glow,rings,content);
        }
        Ui.round(this,0);
        widthProperty().addListener((o,a,b)->{
            getStyleClass().remove("hero-compact");if(b.doubleValue()<(account?550:1050))getStyleClass().add("hero-compact");
            discover.setPrefWidth(b.doubleValue()<1050?410:500);
        });

        drift.setFromY(0);drift.setToY(-9);drift.setAutoReverse(true);drift.setCycleCount(Animation.INDEFINITE);
        drift.setInterpolator(Interpolator.EASE_BOTH);
        pulse.setFromValue(.55);pulse.setToValue(.88);pulse.setAutoReverse(true);pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);pointer.setInterpolator(Interpolator.EASE_OUT);
        for(int i=0;i<copyNodes.length;i++) {
            FadeTransition fade=new FadeTransition(Duration.millis(460),copyNodes[i]);fade.setFromValue(0);fade.setToValue(1);
            TranslateTransition slide=new TranslateTransition(Duration.millis(460),copyNodes[i]);slide.setFromY(14);slide.setToY(0);slide.setInterpolator(Interpolator.EASE_OUT);
            ParallelTransition reveal=new ParallelTransition(fade,slide);reveal.setDelay(Duration.millis(i*90));entrance.getChildren().add(reveal);
        }
        FadeTransition productFade=new FadeTransition(Duration.millis(650),productReveal);productFade.setFromValue(0);productFade.setToValue(1);
        TranslateTransition productSlide=new TranslateTransition(Duration.millis(650),productReveal);productSlide.setFromX(26);productSlide.setToX(0);productSlide.setInterpolator(Interpolator.EASE_OUT);
        entrance.getChildren().addAll(productFade,productSlide);
        addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED,e->{
            if(!canAnimate())return;
            var local=sceneToLocal(e.getSceneX(),e.getSceneY());
            pointer.stop();pointer.setToX((local.getX()/Math.max(1,getWidth())-.5)*14);
            pointer.setToY((local.getY()/Math.max(1,getHeight())-.5)*8);pointer.play();
        });
        hoverProperty().addListener((o,a,b)->{if(!b)centerPointer();});
        visibleProperty().addListener(lifecycle);disabledProperty().addListener(lifecycle);heightProperty().addListener(lifecycle);
        Motion.reducedProperty().addListener(new WeakInvalidationListener(lifecycle));
        sceneProperty().addListener((o,a,b)->{
            if(a!=null)a.windowProperty().removeListener(windowListener);
            if(b!=null)b.windowProperty().addListener(windowListener);
            watchWindow(b==null?null:b.getWindow());
        });
    }

    /** CatalogView supplies viewport intersection; visibility alone does not detect scrolling. */
    public void setInViewport(boolean value) { if(inViewport!=value) {inViewport=value;syncMotion();} }

    private void watchWindow(Window next) {
        if(watchedWindow!=null) {
            watchedWindow.showingProperty().removeListener(lifecycle);
            if(watchedWindow instanceof Stage stage)stage.iconifiedProperty().removeListener(lifecycle);
        }
        watchedWindow=next;
        if(next!=null) {
            next.showingProperty().addListener(lifecycle);
            if(next instanceof Stage stage)stage.iconifiedProperty().addListener(lifecycle);
        }
        syncMotion();
    }

    private boolean canAnimate() {
        return !Motion.isReduced()&&getScene()!=null&&watchedWindow!=null&&watchedWindow.isShowing()
            &&(!(watchedWindow instanceof Stage stage)||!stage.isIconified())&&isVisible()&&!isDisabled()&&inViewport&&getHeight()>0;
    }

    private void syncMotion() {
        if(canAnimate()) {
            if(!entered) {entered=true;entrance.playFromStart();}
            else if(entrance.getStatus()==Animation.Status.PAUSED)entrance.play();
            drift.play();pulse.play();
        } else {
            drift.pause();pulse.pause();pointer.stop();parallax.setTranslateX(0);parallax.setTranslateY(0);
            if(entrance.getStatus()==Animation.Status.RUNNING)entrance.pause();
            if(Motion.isReduced()||getScene()==null) {
                entrance.stop();drift.stop();pulse.stop();floating.setTranslateY(0);glow.setOpacity(.65);finishEntrance();
                if(Motion.isReduced()&&getScene()!=null)entered=true;
            }
        }
    }
    private void finishEntrance() {
        for(Node node:copyNodes) {node.setOpacity(1);node.setTranslateY(0);}
        productReveal.setOpacity(1);productReveal.setTranslateX(0);
    }
    private void centerPointer() {
        pointer.stop();
        if(canAnimate()) {pointer.setToX(0);pointer.setToY(0);pointer.play();}
        else {parallax.setTranslateX(0);parallax.setTranslateY(0);}
    }

    /** Fine grid and framing marks leave the artwork and copy as the focal points. */
    private static final class CircuitAccents extends Pane {
        CircuitAccents() { setMouseTransparent(true); }
        @Override protected void layoutChildren() {
            getChildren().clear();
            double w=getWidth(),h=getHeight();
            for(double x=28;x<w;x+=48)line(x,h*.36,x,h-48,Color.web("#F08B63",.05));
            for(double y=h*.36;y<h-48;y+=48)line(28,y,w-28,y,Color.web("#F08B63",.05));
            double top=h*.39,bottom=h-58;
            for(double x:new double[]{28,w-28}) {
                double d=x==28?1:-1;
                line(x,top,x+d*24,top,Color.web("#F08B63",.65));
                line(x,top,x,top+24,Color.web("#F08B63",.65));
                line(x,bottom,x+d*24,bottom,Color.web("#F08B63",.4));
                line(x,bottom,x,bottom-24,Color.web("#F08B63",.4));
            }
        }
        private void line(double x1,double y1,double x2,double y2,Color color) {
            Line line=new Line(x1,y1,x2,y2);line.setStroke(color);getChildren().add(line);
        }
    }

    /** Restrained sound-wave rings and diagonal accents, drawn at the current banner size. */
    private static final class Rings extends Pane {
        private final Circle[] rings=new Circle[4];
        private final Line[] streaks=new Line[3];
        private Rings() {
            for(int i=0;i<rings.length;i++) {
                Circle ring=new Circle();ring.setFill(null);ring.setStroke(Color.web("#E65436",.10));ring.setStrokeWidth(i==2?1.2:.7);
                rings[i]=ring;getChildren().add(ring);
            }
            for(int i=0;i<streaks.length;i++) {
                Line streak=new Line();streak.setStroke(Color.web("#F08B63",.09));streaks[i]=streak;getChildren().add(streak);
            }
        }
        @Override protected void layoutChildren() {
            for(int i=0;i<rings.length;i++) {
                rings[i].setCenterX(getWidth()*.77);rings[i].setCenterY(getHeight()*.5);rings[i].setRadius(getHeight()*(.35+i*.17));
            }
            for(int i=0;i<streaks.length;i++) {
                streaks[i].setStartX(getWidth()*.55+i*35);streaks[i].setStartY(getHeight());
                streaks[i].setEndX(getWidth()*.75+i*35);streaks[i].setEndY(0);
            }
        }
    }
}
