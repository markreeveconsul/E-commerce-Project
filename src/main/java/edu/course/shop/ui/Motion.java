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
}
