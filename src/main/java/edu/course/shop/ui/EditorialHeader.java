package edu.course.shop.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;

/** Original editorial campaign, independent of product availability and catalog data. */
public final class EditorialHeader extends HBox {
    public EditorialHeader(Runnable shop, Runnable electronics) {
        setId("editorial-hero"); getStyleClass().add("editorial-header");
        VBox copy=new VBox(16);
        copy.getStyleClass().add("hero-copy");
        copy.setAlignment(Pos.CENTER_LEFT); copy.setMinWidth(0);
        HBox.setHgrow(copy,Priority.ALWAYS);
        Button browse=Ui.button("Explore the collection  →",shop);
        browse.setId("hero-shop");browse.getStyleClass().add("hero-solid");
        copy.getChildren().addAll(Ui.label("THE EVERYDAY COLLECTION / MARKET","hero-eyebrow"),
            Ui.label("LESS NOISE.\nMORE YOU.","hero-headline"),
            Ui.label("Find your rhythm. Make room for the things you love.","hero-sub"),browse);

        CollectionArtwork art=new CollectionArtwork("Wireless headphones");
        art.setRotate(10);art.setScaleX(1.08);art.setScaleY(1.08);
        StackPane.setMargin(art,new javafx.geometry.Insets(0,0,50,0));
        LabelPanel caption=new LabelPanel();
        StackPane visual=new StackPane(art,caption);
        visual.getStyleClass().add("hero-visual");
        visual.setPrefWidth(450);visual.setMinWidth(300);visual.setMaxWidth(500);
        StackPane.setAlignment(caption,Pos.BOTTOM_LEFT);
        Button discover=Ui.button("",electronics);discover.setGraphic(visual);
        discover.setId("hero-electronics");discover.setAccessibleText("Explore electronics");
        discover.getStyleClass().add("hero-art-button");
        discover.setMinWidth(300);discover.setPrefWidth(450);discover.setMaxWidth(500);
        discover.setMaxHeight(Double.MAX_VALUE);
        visual.prefWidthProperty().bind(discover.widthProperty());
        visual.prefHeightProperty().bind(heightProperty());
        getChildren().addAll(copy,discover);
        // Scale the typography at the compact desktop size without hiding the campaign.
        widthProperty().addListener((o,a,b)->{
            getStyleClass().remove("hero-compact");
            if(b.doubleValue()<1050)getStyleClass().add("hero-compact");
        });
        Ui.round(visual,0);
    }
    private static final class LabelPanel extends VBox {
        private LabelPanel() {
            super(4,Ui.label("01 / FIND YOUR FOCUS","campaign-kicker"),
                Ui.label("Explore electronics  ↗","campaign-link"));
            setMaxHeight(USE_PREF_SIZE);
            getStyleClass().add("campaign-caption");setMouseTransparent(true);
        }
    }
}
