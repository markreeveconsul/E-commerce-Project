package edu.course.shop.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.*;
import java.util.Locale;

/** Original, offline illustrations for the six demo products, never presented as photos. */
public final class CollectionArtwork extends Region {
    private final Canvas canvas = new Canvas();
    private final String subject;

    public CollectionArtwork(String subject) {
        this.subject = subject.toLowerCase(Locale.ROOT);
        getStyleClass().add("collection-artwork");
        getChildren().add(canvas);
        setMinWidth(0); setMinHeight(0);
        setMouseTransparent(true);
        setAccessibleText("Illustration of " + subject);
    }

    public static boolean supports(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "wireless headphones", "mechanical keyboard", "everyday cotton tee", "canvas tote", "ceramic mug", "desk lamp" -> true;
            default -> false;
        };
    }

    @Override protected void layoutChildren() {
        double width = getWidth(), height = getHeight();
        canvas.setWidth(width); canvas.setHeight(height);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, width, height);
        g.save();
        double scale = Math.min(width / 420, height / 340);
        g.translate((width - 420 * scale) / 2, (height - 340 * scale) / 2);
        g.scale(scale, scale);
        // Soft concentric shadows give the objects depth without external assets.
        for (int i = 12; i > 0; i--) {
            g.setFill(Color.rgb(15, 15, 15, .008));
            g.fillOval(100 - i * 2, 288 - i / 2.0, 220 + i * 4, 20 + i);
        }
        switch (subject) {
            case "wireless headphones" -> headphones(g);
            case "mechanical keyboard" -> keyboard(g);
            case "everyday cotton tee" -> tee(g);
            case "canvas tote" -> tote(g);
            case "ceramic mug" -> mug(g);
            case "desk lamp" -> lamp(g);
            default -> { }
        }
        g.restore();
    }

    private static LinearGradient gradient(String light, String dark) {
        return new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(light)), new Stop(1, Color.web(dark)));
    }

    private void headphones(GraphicsContext g) {
        g.save(); g.translate(210, 160); g.rotate(-18); g.translate(-210, -160);
        g.setLineWidth(34); g.setStroke(Color.web("#161719"));
        g.strokeArc(110, 42, 200, 230, 0, 180, javafx.scene.shape.ArcType.OPEN);
        g.setLineWidth(19); g.setStroke(gradient("#56585B", "#222326"));
        g.strokeArc(110, 42, 200, 230, 0, 180, javafx.scene.shape.ArcType.OPEN);
        g.setLineWidth(3); g.setStroke(Color.web("#787A7D"));
        g.strokeArc(110, 42, 200, 230, 28, 125, javafx.scene.shape.ArcType.OPEN);
        g.setFill(gradient("#A7A9AB", "#535557"));
        g.fillRoundRect(100, 149, 16, 65, 8, 8); g.fillRoundRect(304, 149, 16, 65, 8, 8);
        g.setFill(Color.web("#121315"));
        g.fillRoundRect(89, 184, 53, 103, 44, 44); g.fillRoundRect(278, 184, 53, 103, 44, 44);
        g.setFill(gradient("#55565A", "#202125"));
        g.fillRoundRect(82, 179, 42, 100, 32, 32); g.fillRoundRect(296, 179, 42, 100, 32, 32);
        g.setStroke(Color.web("#737477")); g.setLineWidth(1.5);
        g.strokeRoundRect(87, 184, 31, 89, 25, 25); g.strokeRoundRect(301, 184, 31, 89, 25, 25);
        g.setFill(Color.web("#E84A2F")); g.fillRoundRect(313, 243, 6, 16, 3, 3);
        g.restore();
    }

    private void keyboard(GraphicsContext g) {
        g.save(); g.translate(210, 175); g.rotate(-12); g.translate(-210, -175);
        g.setFill(Color.web("#24262A")); g.fillRoundRect(48, 114, 324, 149, 14, 14);
        g.setFill(gradient("#4F5156", "#27292D")); g.fillRoundRect(48, 102, 324, 147, 14, 14);
        g.setFill(Color.web("#191B1E")); g.fillRoundRect(57, 111, 306, 126, 7, 7);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 12; col++) {
                if (row == 4 && col > 2 && col < 8) continue;
                double x = 64 + col * 24, y = 117 + row * 23;
                g.setFill(row == 0 && col == 0 ? Color.web("#E84A2F") : gradient("#DAD8D1", "#AAA8A2"));
                g.fillRoundRect(x, y, 20, 19, 3, 3);
                g.setFill(Color.web("#73736F")); g.fillRect(x + 5, y + 5, 4, 1);
            }
        }
        g.setFill(gradient("#DAD8D1", "#AAA8A2")); g.fillRoundRect(136, 209, 140, 19, 3, 3);
        g.restore();
    }

    private void tee(GraphicsContext g) {
        g.setFill(gradient("#EEEAE1", "#CBC5B8"));
        g.fillPolygon(new double[]{164,124,124,91,145,185,274,275,329,296,256},
            new double[]{48,57,114,132,145,289,289,145,132,114,57}, 11);
        g.setStroke(Color.web("#B8B1A4")); g.setLineWidth(1.2);
        g.strokeLine(145,145,148,273); g.strokeLine(274,145,271,273); g.strokeLine(150,281,267,281);
        g.setFill(Color.web("#C3BCAD")); g.fillOval(181,41,59,32);
        g.setFill(Color.web("#EFEAE0")); g.fillOval(184,37,53,28);
        g.setStroke(Color.web("#B8B1A4")); g.strokeArc(182,40,57,32,180,180,javafx.scene.shape.ArcType.OPEN);
        g.setStroke(Color.web("#D6D0C4"));g.strokeLine(172,105,161,240);g.strokeLine(251,120,260,230);
        g.setFill(Color.web("#A9A294")); g.fillRect(223,116,18,2);
    }

    private void tote(GraphicsContext g) {
        g.setStroke(Color.web("#A99A7F")); g.setLineWidth(12);
        g.strokeRoundRect(163,43,94,142,66,66);
        g.setStroke(Color.web("#E6D7BC")); g.setLineWidth(7);
        g.strokeRoundRect(163,43,94,142,66,66);
        g.setFill(gradient("#E5D5B6", "#B7A585"));
        g.fillPolygon(new double[]{120,299,282,137}, new double[]{116,116,287,287},4);
        g.setStroke(Color.web("#A89B80"));g.setLineWidth(1.2);
        g.strokeLine(132,127,145,276);g.strokeLine(285,127,273,276);g.strokeLine(145,276,273,276);
        g.setStroke(Color.web("#EEE1C9"));g.strokeLine(139,131,280,131);
        g.setFill(Color.web("#2D302B")); g.fillRoundRect(186,188,48,29,2,2);
        g.setFill(Color.web("#E7DDC9")); g.fillRect(196,199,28,2);g.fillRect(196,205,19,2);
    }

    private void mug(GraphicsContext g) {
        g.setStroke(Color.web("#777C68")); g.setLineWidth(22); g.strokeOval(267,129,75,98);
        g.setStroke(Color.web("#BCC1A8")); g.setLineWidth(12); g.strokeOval(270,124,69,95);
        g.setFill(gradient("#C6CCB5", "#7D846D")); g.fillRoundRect(111,98,178,175,35,35);
        g.setFill(Color.web("#C7CEB7")); g.fillOval(111,81,178,46);
        g.setFill(Color.web("#646B56")); g.fillOval(123,90,154,27);
        g.setFill(Color.web("#AAB197")); g.fillOval(129,98,142,19);
        g.setStroke(Color.web("#E0E5D4"));g.setLineWidth(3);g.strokeLine(126,141,126,229);
        g.setStroke(Color.web("#727A62"));g.setLineWidth(2);g.strokeArc(112,229,176,44,180,180,javafx.scene.shape.ArcType.OPEN);
    }

    private void lamp(GraphicsContext g) {
        g.setFill(Color.web("#2E3435"));g.fillOval(117,264,195,34);
        g.setFill(gradient("#616B6A", "#262E2D"));g.fillOval(117,255,195,31);
        g.setStroke(Color.web("#313B39"));g.setLineWidth(12);g.strokeLine(213,263,239,155);g.strokeLine(239,155,196,85);
        g.setStroke(Color.web("#75817C"));g.setLineWidth(4);g.strokeLine(210,254,233,158);
        g.setFill(Color.web("#333E3A"));g.fillOval(226,144,23,23);
        g.save();g.translate(179,91);g.rotate(-24);
        g.setFill(gradient("#788878", "#35473C"));
        g.fillPolygon(new double[]{-38,38,69,-69},new double[]{-33,-33,53,53},4);
        g.setFill(Color.web("#DEDCC7"));g.fillOval(-69,40,138,26);
        g.setFill(Color.web("#F7EAC1"));g.fillOval(-48,45,96,14);g.restore();
    }
}
