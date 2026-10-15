package edu.course.shop.ui;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.util.Duration;

/** Session-wide motion preference and small, reversible hover effects. FX thread only. */
public final class Motion {
    private static final BooleanProperty reduced = new SimpleBooleanProperty(Boolean.getBoolean("shop.reduceMotion"));
    private Motion() { }
    public static BooleanProperty reducedProperty() { return reduced; }
    public static boolean isReduced() { return reduced.get(); }

    public static void lift(Node target) {
        TranslateTransition animation=new TranslateTransition(Duration.millis(160),target);
        animation.setInterpolator(Interpolator.EASE_OUT);
        InvalidationListener update=o->{
            animation.stop();
            if(target.getScene()==null||target.isDisabled()||isReduced()) { target.setTranslateY(0);return; }
            animation.setToY(target.isHover()?-2:0);animation.play();
        };
        target.hoverProperty().addListener(update);target.disabledProperty().addListener(update);target.sceneProperty().addListener(update);
        // The global preference must not retain discarded pages or their controls.
        reduced.addListener(new WeakInvalidationListener(update));
        target.getProperties().put("market.buttonMotion",update);
    }

    public static void zoom(Node trigger,Node image) {
        ScaleTransition animation=new ScaleTransition(Duration.millis(220),image);
        animation.setInterpolator(Interpolator.EASE_OUT);
        InvalidationListener update=o->{
            animation.stop();
            if(trigger.getScene()==null||trigger.isDisabled()||isReduced()) {
                image.setScaleX(1);image.setScaleY(1);return;
            }
            double scale=trigger.isHover()?1.035:1;
            animation.setToX(scale);animation.setToY(scale);animation.play();
        };
        trigger.hoverProperty().addListener(update);trigger.disabledProperty().addListener(update);trigger.sceneProperty().addListener(update);
        reduced.addListener(new WeakInvalidationListener(update));
        trigger.getProperties().put("market.imageMotion",update);
    }
    /** Short entrance only; callers exclude campaign nodes. */
    public static void reveal(Node target) {
        javafx.animation.FadeTransition fade=new javafx.animation.FadeTransition(Duration.millis(180),target);
        if(isReduced()) {target.setOpacity(1);return;}
        target.setOpacity(.25);fade.setToValue(1);fade.play();
        InvalidationListener cancel=o->{if(target.getScene()==null||isReduced()){fade.stop();target.setOpacity(1);}};
        target.sceneProperty().addListener(cancel);reduced.addListener(new WeakInvalidationListener(cancel));
        target.getProperties().put("market.reveal",cancel);
    }

    /** Short top-down entrance for transient panels; reduced-motion users see the final state immediately. */
    public static void dropIn(Node target) {
        if(isReduced()) { target.setOpacity(1);target.setTranslateY(0);return; }
        javafx.animation.FadeTransition fade=new javafx.animation.FadeTransition(Duration.millis(190),target);
        fade.setFromValue(0);fade.setToValue(1);fade.setInterpolator(Interpolator.EASE_OUT);
        TranslateTransition slide=new TranslateTransition(Duration.millis(210),target);
        slide.setFromY(-22);slide.setToY(0);slide.setInterpolator(Interpolator.EASE_OUT);
        javafx.animation.ParallelTransition entrance=new javafx.animation.ParallelTransition(fade,slide);
        Runnable reset=()->{entrance.stop();target.setOpacity(1);target.setTranslateY(0);};
        InvalidationListener cancel=o->{if(target.getScene()==null||isReduced())reset.run();};
        target.sceneProperty().addListener(cancel);reduced.addListener(new WeakInvalidationListener(cancel));
        entrance.setOnFinished(e->{target.sceneProperty().removeListener(cancel);target.setOpacity(1);target.setTranslateY(0);});
        target.setOpacity(0);target.setTranslateY(-22);entrance.play();
    }
}
