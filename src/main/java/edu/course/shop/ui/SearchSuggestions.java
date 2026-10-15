package edu.course.shop.ui;

import edu.course.shop.model.Money;
import edu.course.shop.model.Product;
import edu.course.shop.model.User;
import edu.course.shop.service.ProductService;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Centered, keyboard-friendly product suggestions for the storefront search. */
public final class SearchSuggestions {
    private final TextField headerSearch;
    private final User user;
    private final ProductService products;
    private final Consumer<Product> details;
    private final Runnable results;
    private final PauseTransition debounce=new PauseTransition(Duration.millis(180));
    private Task<List<Product>> pending;
    private long generation;
    private Runnable closeOverlay;
    private Runnable cleanup=()->{};
    private int activeIndex=-1;

    public SearchSuggestions(TextField search,User user,ProductService products,Consumer<Product> details,Runnable results) {
        this.headerSearch=search;this.user=user;this.products=products;this.details=details;this.results=results;
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> headerKeys=e->{
            if(e.getCode()==javafx.scene.input.KeyCode.ESCAPE)dismiss();
        };
        search.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,headerKeys);
        javafx.beans.value.ChangeListener<String> headerListener=(o,a,b)->{
            if(closeOverlay!=null) {
                Node panel=search.getScene()==null?null:search.getScene().lookup("#search-overlay-panel");
                TextField input=panel==null?null:(TextField)panel.lookup("#search-overlay-input");
                if(input!=null&&!input.getText().equals(b))input.setText(b);
            }
            else if(b.trim().length()>=2&&search.isFocused())open(b);
        };
        search.textProperty().addListener(headerListener);
        javafx.beans.value.ChangeListener<Scene> lifecycle=(o,a,b)->{if(b==null)close();};
        search.sceneProperty().addListener(lifecycle);
        cleanup=()->{search.removeEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,headerKeys);search.textProperty().removeListener(headerListener);search.sceneProperty().removeListener(lifecycle);};
    }

    public void dismiss() {
        generation++;debounce.stop();if(pending!=null)pending.cancel();pending=null;
        Runnable close=closeOverlay;if(close!=null)close.run();
    }

    public void close(){dismiss();cleanup.run();}

    private void open(String query) {
        if(headerSearch.getScene()==null||closeOverlay!=null)return;
        Scene scene=headerSearch.getScene();
        TextField input=new TextField(query);input.setPromptText("Search products...");input.setId("search-overlay-input");input.setAccessibleText("Search products");input.getStyleClass().add("search-overlay-input");
        Button clear=Ui.button("×",()->{input.clear();input.requestFocus();});clear.setId("search-overlay-clear");clear.setAccessibleText("Clear search");clear.getStyleClass().add("search-overlay-clear");
        HBox pill=new HBox(14,StoreIcon.of("search"),input,clear);pill.setAlignment(Pos.CENTER_LEFT);pill.setId("search-overlay-pill");pill.getStyleClass().add("search-overlay-pill");HBox.setHgrow(input,Priority.ALWAYS);

        Label heading=Ui.label("SUGGESTED PRODUCTS","search-overlay-heading");
        VBox rows=new VBox();rows.setId("search-overlay-results");
        Label hint=Ui.label("Keyboard: ↑ ↓, Enter, Esc","search-overlay-hint");
        Button all=Ui.link("View all results  ↗",()->showAll());all.setId("search-overlay-all");all.getStyleClass().add("search-overlay-all");
        Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);
        HBox footer=new HBox(12,hint,spacer,all);footer.setAlignment(Pos.CENTER_LEFT);footer.getStyleClass().add("search-overlay-footer");
        ScrollPane resultScroll=new ScrollPane(rows);resultScroll.setFitToWidth(true);resultScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);resultScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);resultScroll.getStyleClass().add("search-overlay-scroll");
        resultScroll.setFitToHeight(false);resultScroll.setPrefViewportHeight(76);
        VBox card=new VBox(10,heading,resultScroll,footer);card.setId("search-overlay-card");card.getStyleClass().add("search-overlay-card");card.setMaxHeight(Region.USE_PREF_SIZE);
        VBox panel=new VBox(12,pill,card);panel.setId("search-overlay-panel");panel.getStyleClass().add("search-overlay-panel");panel.setMinWidth(0);panel.setMaxWidth(920);panel.setMaxHeight(Region.USE_PREF_SIZE);panel.prefWidthProperty().bind(scene.widthProperty().multiply(.84));
        Runnable[] handle={null};
        handle[0]=ShopOverlay.show(headerSearch,panel,false,false,()->{if(closeOverlay==handle[0])closeOverlay=null;generation++;debounce.stop();if(pending!=null)pending.cancel();pending=null;});
        closeOverlay=handle[0];
        Motion.dropIn(panel);
        input.textProperty().addListener((o,a,b)->{
            if(!headerSearch.getText().equals(b))headerSearch.setText(b);
            query(b,rows,resultScroll);
        });
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> keys=e->{
            switch(e.getCode()){
                case UP->{moveActive(-1,rows);e.consume();}
                case DOWN->{moveActive(1,rows);e.consume();}
                case ENTER->{if(activeIndex>=0&&activeIndex<rows.getChildren().size()&&rows.getChildren().get(activeIndex) instanceof Button selected)selected.fire();else showAll();e.consume();}
                default->{ }
            }
        };
        input.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,keys);
        input.sceneProperty().addListener((o,a,b)->{if(b==null)input.removeEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,keys);});
        input.requestFocus();query(input.getText(),rows,resultScroll);
    }

    private void query(String value,VBox rows,ScrollPane resultScroll) {
        long request=++generation;debounce.stop();if(pending!=null)pending.cancel();activeIndex=-1;rows.getChildren().clear();
        String term=value.trim();
        if(term.length()<2){rows.getChildren().add(Ui.label("Type at least 2 characters to find a product.","search-overlay-empty"));resultScroll.setPrefViewportHeight(76);return;}
        rows.getChildren().add(Ui.label("Searching the collection…","search-overlay-empty"));resultScroll.setPrefViewportHeight(76);
        debounce.setOnFinished(e->{
            Task<List<Product>> task=new Task<>(){@Override protected List<Product> call(){return products.search(user,term,null);}};pending=task;
            task.setOnSucceeded(done->{if(request!=generation||closeOverlay==null)return;render(rows,resultScroll,task.getValue());});
            task.setOnFailed(done->{if(request==generation&&closeOverlay!=null){rows.getChildren().setAll(Ui.label("Search is unavailable. Close this panel and try again.","search-overlay-empty"));resultScroll.setPrefViewportHeight(76);}});
            Thread worker=new Thread(task,"market-product-suggestions");worker.setDaemon(true);worker.start();
        });debounce.playFromStart();
    }

    private void render(VBox rows,ScrollPane resultScroll,List<Product> matches) {
        rows.getChildren().clear();activeIndex=-1;
        List<Product> limited=matches.stream().limit(5).toList();
        if(limited.isEmpty()){rows.getChildren().add(Ui.label("No matching products. Try another search.","search-overlay-empty"));resultScroll.setPrefViewportHeight(76);return;}
        resultScroll.setPrefViewportHeight(limited.size()*74.0);
        for(Product p:limited){
            ProductArtwork photo=new ProductArtwork(p,54,true);photo.setPrefWidth(64);photo.setMinWidth(64);photo.setMaxWidth(64);photo.getStyleClass().add("search-overlay-photo");
            Label name=Ui.label(p.getName(),"search-overlay-name");name.setWrapText(false);name.setMaxWidth(Double.MAX_VALUE);
            String stock=p.getStock()==0?"Out of stock":"In stock";
            Label meta=Ui.label(Ui.category(p.getCategory())+" / "+stock,"search-overlay-meta");
            VBox copy=new VBox(4,name,meta);copy.setMinWidth(0);HBox.setHgrow(copy,Priority.ALWAYS);
            Label price=Ui.label(Money.format(p.getPrice()),"search-overlay-price");
            HBox content=new HBox(14,photo,copy,price);content.setAlignment(Pos.CENTER_LEFT);content.setMinHeight(60);content.setPrefHeight(60);content.setMaxHeight(60);content.getStyleClass().add("search-overlay-row-content");
            Button row=Ui.button("",()->{dismiss();details.accept(p);});row.setGraphic(content);row.setAccessibleText(p.getName()+", "+Money.format(p.getPrice())+", "+stock);row.setMaxWidth(Double.MAX_VALUE);row.setMinHeight(74);row.setPrefHeight(74);row.setMaxHeight(74);row.setId("search-result-"+p.getId());row.getStyleClass().add("search-overlay-result");rows.getChildren().add(row);
        }
    }

    private void moveActive(int delta,VBox rows) {
        List<Node> items=rows.getChildren().stream().filter(Button.class::isInstance).toList();if(items.isEmpty())return;
        if(activeIndex>=0&&activeIndex<items.size())items.get(activeIndex).getStyleClass().remove("search-overlay-active");
        activeIndex=activeIndex<0?(delta>0?0:items.size()-1):Math.floorMod(activeIndex+delta,items.size());
        items.get(activeIndex).getStyleClass().add("search-overlay-active");
    }

    private void showAll() {dismiss();results.run();}
}
