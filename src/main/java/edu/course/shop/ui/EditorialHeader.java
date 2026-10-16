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
import javafx.scene.shape.Ellipse;
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
        if(account)getStyleClass().add("account-campaign");else getStyleClass().add("storefront-campaign");
        glow.setId("hero-glow");glow.getStyleClass().add("hero-glow");glow.setMouseTransparent(true);glow.setOpacity(.65);
        glow.setMaxSize(640,640);glow.setPrefSize(640,640);
        StackPane.setAlignment(glow,Pos.CENTER_RIGHT);StackPane.setMargin(glow,new Insets(0,-25,0,0));
        Pane rings=new Rings();rings.setMouseTransparent(true);

        Label eyebrow=Ui.label(account?"FORGE / THE POWER EDIT":"FORGE / THE SOUND EDIT","hero-eyebrow");
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

        Image image=new Image(java.util.Objects.requireNonNull(getClass().getResource(account?"/images/login-pc-cutout.png":"/images/headset-campaign.png")).toExternalForm());
        if(image.isError())throw new IllegalStateException("Campaign image could not be loaded",image.getException());
        ImageView headset=new ImageView(image);headset.setId(account?"login-pc":"hero-headset");headset.setPreserveRatio(true);headset.setSmooth(true);
        // Normal compositing keeps the PC interior dark instead of blending in the orange backdrop.
        if(!account) {
            headset.fitHeightProperty().bind(heightProperty().add(26));
            // Alpha mask fades only the cable's final pixels; the source artwork stays intact.
            javafx.scene.shape.Rectangle cableMask=new javafx.scene.shape.Rectangle();
            cableMask.setId("hero-cable-fade");
            cableMask.widthProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
                ()->headset.getLayoutBounds().getWidth(),headset.layoutBoundsProperty()));
            cableMask.heightProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
                ()->headset.getLayoutBounds().getHeight()*.96,headset.layoutBoundsProperty()));
            cableMask.setFill(new javafx.scene.paint.LinearGradient(0,0,0,1,true,javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0,Color.BLACK),new javafx.scene.paint.Stop(.91,Color.BLACK),
                new javafx.scene.paint.Stop(.95,Color.color(0,0,0,.55)),new javafx.scene.paint.Stop(1,Color.TRANSPARENT)));
            headset.setClip(cableMask);
            floating.setEffect(new javafx.scene.effect.DropShadow(12,0,6,Color.web("#000000",.18)));
        }
        floating.setId("hero-float");floating.getChildren().add(headset);
        StackPane.setAlignment(headset,Pos.TOP_CENTER);StackPane.setMargin(headset,Insets.EMPTY);
        parallax.setId("hero-parallax");parallax.getChildren().add(floating);
        productReveal.getChildren().add(parallax);productReveal.setMouseTransparent(true);
        floating.setMinSize(0,0);parallax.setMinSize(0,0);productReveal.setMinSize(0,0);
        VBox caption=new VBox(4,Ui.label("01 / FIND YOUR FOCUS","campaign-kicker"),Ui.label("Shop electronics  ↗","campaign-link"));
        caption.getStyleClass().add("campaign-caption");caption.setMaxHeight(USE_PREF_SIZE);caption.setMouseTransparent(true);
        StackPane visual=new StackPane(productReveal,caption);visual.setId("hero-visual");
        visual.setMinSize(0,0);
        if(!account) {
            // A broad contact shadow anchors the overlapping microphone and earcups to the white section.
            javafx.scene.shape.Ellipse contact=new javafx.scene.shape.Ellipse();contact.setId("hero-contact-shadow");
            contact.setManaged(false);contact.setMouseTransparent(true);contact.setFill(Color.web("#000000",.38));
            contact.centerXProperty().bind(visual.widthProperty().multiply(.5).add(headset.translateXProperty()));
            contact.centerYProperty().bind(visual.heightProperty().add(12));
            contact.radiusXProperty().bind(visual.widthProperty().multiply(.21));contact.setRadiusY(8);
            contact.setEffect(new javafx.scene.effect.GaussianBlur(16));visual.getChildren().add(0,contact);
        }
        StackPane.setAlignment(caption,Pos.BOTTOM_RIGHT);
        Button discover=Ui.button("",electronics);discover.setId("hero-electronics");discover.setGraphic(visual);
        discover.setAccessibleText("Shop electronics; headset campaign artwork");discover.getStyleClass().add("hero-art-button");
        discover.setMinWidth(310);discover.setPrefWidth(500);discover.setMaxWidth(550);discover.setMaxHeight(Double.MAX_VALUE);
        if(!account)headset.translateXProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
            ()->getWidth()*.77-(getWidth()-discover.getWidth()/2),widthProperty(),discover.widthProperty()));
        visual.prefWidthProperty().bind(discover.widthProperty());visual.prefHeightProperty().bind(account?heightProperty():heightProperty().subtract(0));
        if(account) {
            visual.getChildren().remove(productReveal);
            Label edition=Ui.label("01 / BUILT FOR YOUR NEXT CHAPTER","campaign-kicker");
            edition.getStyleClass().add("account-edition");
            Ellipse pcShadow=new Ellipse();pcShadow.setId("login-pc-shadow");pcShadow.setManaged(false);pcShadow.setMouseTransparent(true);
            pcShadow.setFill(Color.web("#000000",.62));pcShadow.setEffect(new javafx.scene.effect.GaussianBlur(18));
            pcShadow.centerXProperty().bind(widthProperty().multiply(.60));pcShadow.centerYProperty().bind(heightProperty().multiply(.94));
            pcShadow.radiusXProperty().bind(widthProperty().multiply(.30));pcShadow.radiusYProperty().bind(heightProperty().multiply(.02));
            StackPane artwork=new StackPane(productReveal);artwork.setMinSize(0,0);
            VBox.setVgrow(artwork,Priority.ALWAYS);
            // The PNG cutout exposes the campaign only outside the PC silhouette.
            // Keep normal compositing so the dark chassis does not become see-through.
            // Compact windows scale the photo down slightly so the login copy stays clear.
            headset.fitWidthProperty().bind(javafx.beans.binding.Bindings.createDoubleBinding(
                ()->Math.max(1,artwork.getWidth()*(getWidth()<550?.84:.96)),artwork.widthProperty(),widthProperty()));
            StackPane.setAlignment(headset,Pos.TOP_CENTER);Ui.round(artwork,0);
            VBox content=new VBox(0,copy,artwork,edition);content.setMinSize(0,0);
            getChildren().addAll(glow,rings,pcShadow,new CircuitAccents(),content);
        } else {
            HBox content=new HBox(0,copy,discover);content.setAlignment(Pos.CENTER_LEFT);
            StackPane backdrop=new StackPane(glow,rings);Ui.round(backdrop,0);
            Runnable arrange=()->{boolean compact=getWidth()<1200;headset.setTranslateY(compact?0:2);StackPane.setMargin(content,new Insets(compact?64:0,0,0,0));visual.prefHeightProperty().unbind();visual.prefHeightProperty().bind(heightProperty().subtract(compact?64:0));headset.fitHeightProperty().unbind();headset.fitHeightProperty().bind(heightProperty().add(compact?-20:55));};
            widthProperty().addListener((o,a,b)->arrange.run());arrange.run();
            getChildren().addAll(backdrop,content);
        }
        // Only the backdrop clips. The campaign photo deliberately extends onto the catalog.
        if(account)Ui.round(this,0);
        widthProperty().addListener((o,a,b)->{
            getStyleClass().remove("hero-compact");if(b.doubleValue()<(account?550:1200))getStyleClass().add("hero-compact");
            discover.setPrefWidth(b.doubleValue()<1050?340:500);
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
                Circle ring=new Circle();ring.setFill(null);ring.setStroke(Color.web("#E65436",.18));ring.setStrokeWidth(i==2?1.2:.7);
                rings[i]=ring;getChildren().add(ring);
            }
            for(int i=0;i<streaks.length;i++) {
                Line streak=new Line();streak.setStroke(Color.web("#F08B63",.16));streaks[i]=streak;getChildren().add(streak);
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
