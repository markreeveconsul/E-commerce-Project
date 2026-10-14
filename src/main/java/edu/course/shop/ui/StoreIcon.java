package edu.course.shop.ui;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/** Small original outline icons, sharing one stroke and size. */
public final class StoreIcon {
    private StoreIcon() { }
    public static Group of(String name) {
        String path=switch(name) {
            case "bag" -> "M5 7 H19 L20 21 H4 Z M9 8 V6 A3 3 0 0 1 15 6 V8";
            case "account" -> "M16 7 A4 4 0 1 1 8 7 A4 4 0 1 1 16 7 M4 21 V19 A8 8 0 0 1 20 19 V21";
            case "heart" -> "M12 21 L3 12 C-2 6 5 -1 12 6 C19 -1 26 6 21 12 Z";
            case "search" -> "M16 10 A6 6 0 1 1 4 10 A6 6 0 1 1 16 10 M15 15 L21 21";
            default -> throw new IllegalArgumentException("Unknown icon: "+name);
        };
        SVGPath icon=new SVGPath();icon.setContent(path);icon.setFill(null);icon.setStroke(Color.web("#18181B"));
        icon.setStrokeWidth(1.7);icon.getStyleClass().add("store-icon");
        Group group=new Group(icon);group.setMouseTransparent(true);return group;
    }
}
