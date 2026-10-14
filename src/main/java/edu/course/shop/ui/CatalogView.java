package edu.course.shop.ui;

import edu.course.shop.model.Cart;
import edu.course.shop.model.Category;
import edu.course.shop.model.Product;
import edu.course.shop.model.User;
import edu.course.shop.service.CartService;
import edu.course.shop.service.ProductService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public final class CatalogView extends VBox {
    private boolean clearing, restoring;
    private final javafx.beans.property.BooleanProperty rangeApplied=new javafx.beans.property.SimpleBooleanProperty();
    private final Button toggle=Ui.button("Filters +",()->{});
    private final FlowPane filters=new FlowPane(20,18);
    private final TilePane grid=new TilePane(20,24);
    private final TextField search;
    private final TextField minimum=new TextField(),maximum=new TextField();
    private SavedItems saved;
    public void enableSavedItems(SavedItems saved){this.saved=saved;}
    private final CatalogState state;
    private final Consumer<Product> details;
    private final ComboBox<String> category=new ComboBox<>(),sort=new ComboBox<>(),budget=new ComboBox<>();
    private final CheckBox available=new CheckBox("In stock only");
    // Sentence case reads calmer and more modern than ALL CAPS; the hero stays uppercase as the one brand moment.
    private final Label feedback=Ui.feedback("catalog-feedback"),result=Ui.label("","muted"),title=Ui.label("The everyday edit","collection-title");
    private final FlowPane chips=new FlowPane(8,8);
    private final VBox results=new VBox(16);
    private final ScrollPane scroll;
    private final EditorialHeader hero;
    private final User user;private final Cart cart;private final ProductService products;private final CartService carts;
    private final Runnable changed;
    private final Consumer<String> toast; // optional: shows a floating "added to cart" message (null = old inline message)
    private final Runnable navSync;       // optional: lets the header highlight the active category (null = nothing)

    /** Original signature kept, so any existing caller or test still compiles. */
    public CatalogView(User user,Cart cart,ProductService products,CartService carts,Runnable changed,Runnable openCart,
                       Consumer<Product> details,TextField search,CatalogState state) {
        this(user,cart,products,carts,changed,openCart,details,search,state,null,null);
    }

    public CatalogView(User user,Cart cart,ProductService products,CartService carts,Runnable changed,Runnable openCart,
                       Consumer<Product> details,TextField search,CatalogState state,Consumer<String> toast,Runnable navSync) {
        this.user=user;this.cart=cart;this.products=products;this.carts=carts;this.changed=changed;
        this.details=details;this.search=search;this.state=state;this.toast=toast;this.navSync=navSync;
        getStyleClass().add("page");setSpacing(16);
        category.getItems().add("All categories");for(Category c:Category.values())category.getItems().add(Ui.category(c));
        category.getSelectionModel().select(state.getCategoryIndex());category.setId("category");category.setAccessibleText("Filter by category");
        sort.getItems().addAll("Name: A–Z","Price: low to high","Price: high to low","Name: Z–A");sort.getSelectionModel().select(state.getSortIndex());
        sort.setId("sort");sort.setAccessibleText("Sort products");
        budget.getItems().addAll("Any price","Up to $25","Up to $50","Up to $100");budget.getSelectionModel().select(state.getBudgetIndex());
        budget.setId("budget");budget.setAccessibleText("Maximum price");available.setId("in-stock");available.setSelected(state.isInStockOnly());
        category.setOnAction(e->filterChanged());sort.setOnAction(e->filterChanged());budget.setOnAction(e->{if(clearing)return;minimum.clear();maximum.clear();applyFilters();});available.setOnAction(e->filterChanged());
        grid.setId("product-grid");
        grid.widthProperty().addListener((o,a,b)->{
            int columns=Math.max(1,Math.min(4,(int)((b.doubleValue()+20)/270)));
            grid.setPrefTileWidth(Math.max(180,Math.floor((b.doubleValue()-20*(columns-1))/columns)-2));
        });

        VBox collectionTitle=new VBox(6,Ui.label("CURATED FOR YOUR EVERYDAY","eyebrow"),title,result);
        collectionTitle.setMinWidth(0);
        Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);
        search.setPromptText("Search products by name — headphones, tees, home…");
        search.setMinWidth(0);search.setMaxWidth(Double.MAX_VALUE);HBox.setHgrow(search,Priority.ALWAYS);
        if(!search.getStyleClass().contains("catalog-search-input"))search.getStyleClass().add("catalog-search-input");
        Button clearSearch=Ui.button("×",()->{search.clear();state.setScrollPosition(0);refresh();search.requestFocus();});
        clearSearch.setId("clear-search");clearSearch.setAccessibleText("Clear search");
        clearSearch.getStyleClass().add("search-clear");
        clearSearch.visibleProperty().bind(search.textProperty().isNotEmpty());clearSearch.managedProperty().bind(clearSearch.visibleProperty());
        HBox searchInput=new HBox(12,StoreIcon.of("search"),search,clearSearch);searchInput.setAlignment(Pos.CENTER_LEFT);
        searchInput.getStyleClass().add("catalog-search-surface");HBox.setHgrow(searchInput,Priority.ALWAYS);
        Button find=Ui.primary("Search",()->{state.setScrollPosition(0);refresh();});find.setId("catalog-search");
        find.getStyleClass().add("purchase-action");
        search.setOnAction(e->find.fire());
        SearchSuggestions suggestions=new SearchSuggestions(search,user,products,details,()->{state.setScrollPosition(0);refresh();});
        find.setOnAction(e->{suggestions.dismiss();state.setScrollPosition(0);refresh();});
        HBox searchRow=new HBox(10,searchInput,find);searchRow.setAlignment(Pos.CENTER_LEFT);searchRow.setId("catalog-search-row");
        toggle.setId("toggle-filters");
        minimum.setText(state.getMinimumPrice());maximum.setText(state.getMaximumPrice());minimum.setPromptText("0.00");maximum.setPromptText("Any");minimum.setId("minimum-price");maximum.setId("maximum-price");minimum.setPrefWidth(100);maximum.setPrefWidth(100);minimum.setAccessibleText("Minimum price in USD");maximum.setAccessibleText("Maximum price in USD");
        minimum.textProperty().addListener((o,a,b)->Ui.flag(minimum,false));maximum.textProperty().addListener((o,a,b)->Ui.flag(maximum,false));
        rangeApplied.set(!minimum.getText().isBlank()||!maximum.getText().isBlank());
        Button applyRange=Ui.button("Apply range",this::applyRange);applyRange.setId("apply-price-range");minimum.setOnAction(e->applyRange());maximum.setOnAction(e->applyRange());
        Button clearFilters=Ui.link("Clear filters",this::clear);clearFilters.setId("clear-filters");
        category.setPrefWidth(190);budget.setPrefWidth(160);sort.setPrefWidth(195);
        HBox rangeControls=new HBox(8,labeled("From $",minimum),labeled("To $",maximum),applyRange);rangeControls.setAlignment(Pos.BOTTOM_LEFT);
        VBox price=new VBox(6,Ui.label("Custom price range · USD","field-label"),rangeControls);
        VBox availability=new VBox(12,Ui.label("Availability","field-label"),available,clearFilters);
        filters.getChildren().addAll(labeled("Category",category),labeled("Quick budget",budget),price,availability);
        filters.setId("catalog-filters");filters.getStyleClass().add("catalog-filter-panel");filters.setAlignment(Pos.TOP_LEFT);
        filters.setVisible(state.isFiltersExpanded());filters.managedProperty().bind(filters.visibleProperty());
        syncToggle();
        toggle.setOnAction(e->{filters.setVisible(!filters.isVisible());state.setFiltersExpanded(filters.isVisible());syncToggle();});
        HBox tools=new HBox(12,toggle,labeled("Sort by",sort));tools.setAlignment(Pos.BOTTOM_RIGHT);
        HBox toolbar=new HBox(20,collectionTitle,spacer,tools);toolbar.setAlignment(Pos.BOTTOM_LEFT);
        toolbar.getStyleClass().add("catalog-toolbar");
        chips.setId("filter-chips");chips.managedProperty().bind(chips.visibleProperty());
        results.getChildren().add(grid);

        VBox collection=new VBox(20,toolbar,searchRow,filters,chips,feedback,results);collection.getStyleClass().add("collection-section");
        VBox content=new VBox(0);
        hero=new EditorialHeader(()->{
            content.applyCss();content.layout();
            double range=content.getHeight()-scrollViewportHeight();
            scrollTo(range<=0?0:collection.getBoundsInParent().getMinY()/range);
        },()->browseCategory(Category.ELECTRONICS.ordinal()+1),()->{
            clearing=true;category.getSelectionModel().select(Category.ELECTRONICS.ordinal()+1);search.setText("headphones");
            clearing=false;filterChanged();
        });
        hero.visibleProperty().bind(search.textProperty().isEmpty().and(category.getSelectionModel().selectedIndexProperty().isEqualTo(0))
            .and(budget.getSelectionModel().selectedIndexProperty().isEqualTo(0)).and(available.selectedProperty().not()).and(rangeApplied.not()));
        hero.managedProperty().bind(hero.visibleProperty());
        content.getChildren().addAll(hero,collection,collections(),closing());
        scroll=Ui.scroll(content);scroll.setId("catalog-scroll");getStyleClass().add("catalog-page");getChildren().add(scroll);
        scroll.vvalueProperty().addListener((o,a,b)->{if(!restoring)state.setScrollPosition(b.doubleValue());updateHeroViewport();});
        scroll.viewportBoundsProperty().addListener((o,a,b)->updateHeroViewport());
        content.layoutBoundsProperty().addListener((o,a,b)->updateHeroViewport());
        Platform.runLater(this::refresh);
    }
    private static VBox labeled(String title,javafx.scene.control.Control input) {
        Label label=Ui.label(title,"field-label");label.setLabelFor(input);
        VBox field=new VBox(7,label,input);field.setMinWidth(0);return field;
    }
    private void syncToggle() {
        int count=(state.getCategoryIndex()>0?1:0)+(state.getBudgetIndex()>0?1:0)+(state.isInStockOnly()?1:0)
            +(!state.getMinimumPrice().isBlank()||!state.getMaximumPrice().isBlank()?1:0);
        toggle.setText("Filters"+(count>0?" · "+count:"")+(filters.isVisible()?" −":" +"));
        toggle.setAccessibleText((filters.isVisible()?"Hide":"Show")+" product filters; "+count+" active");
    }
    private void applyFilters() { state.setScrollPosition(0);refresh(); }
    private void applyRange() {
        if(!validRange())return;
        clearing=true;budget.getSelectionModel().selectFirst();clearing=false;applyFilters();
    }
    private boolean validRange() {
        boolean valid=true;BigDecimal lower=null,upper=null;
        try{lower=parseRange(minimum.getText());Ui.flag(minimum,false);}catch(RuntimeException e){Ui.flag(minimum,true);valid=false;}
        try{upper=parseRange(maximum.getText());Ui.flag(maximum,false);}catch(RuntimeException e){Ui.flag(maximum,true);valid=false;}
        if(valid&&lower!=null&&upper!=null&&lower.compareTo(upper)>0){Ui.flag(minimum,true);Ui.flag(maximum,true);valid=false;}
        if(!valid){Ui.failure(feedback,new edu.course.shop.exception.ValidationException("Enter a valid price range (minimum cannot exceed maximum)."));
            if(minimum.getStyleClass().contains("invalid"))minimum.requestFocus();else maximum.requestFocus();}
        return valid;
    }
    private double scrollViewportHeight() { return scroll.getViewportBounds().getHeight(); }
    private void updateHeroViewport() {
        double height=scrollViewportHeight();
        double top=Math.max(0,scroll.getContent().getLayoutBounds().getHeight()-height)*scroll.getVvalue();
        hero.setInViewport(height>0&&top<hero.getBoundsInParent().getMaxY()&&top+height>hero.getBoundsInParent().getMinY());
    }
    private void scrollTo(double value) { scroll.setVvalue(Math.max(0,Math.min(1,value))); }
    private VBox collections() {
        FlowPane tiles=new FlowPane(18,18);tiles.setId("collection-links");
        String[] subjects={"Wireless headphones","Everyday cotton tee","Ceramic mug"};
        String[] headlines={"Find your focus.","Wear your everyday.","Make yourself at home."};
        for(Category c:Category.values()) {
            int i=c.ordinal();CollectionArtwork art=new CollectionArtwork(subjects[i]);art.setPrefSize(130,135);art.setMaxWidth(130);
            VBox copy=new VBox(8,Ui.label("0"+(i+1)+" / "+Ui.category(c).toUpperCase(java.util.Locale.ROOT),"eyebrow"),
                Ui.label(headlines[i],"section-title"),Ui.label("Shop "+Ui.category(c).toLowerCase(java.util.Locale.ROOT)+"  →","collection-link"));
            copy.setMinWidth(0);HBox.setHgrow(copy,Priority.ALWAYS);
            HBox graphic=new HBox(8,copy,art);graphic.setAlignment(Pos.CENTER_LEFT);graphic.getStyleClass().add("collection-tile-content");
            graphic.setMinHeight(0);graphic.setPrefHeight(135);graphic.setMaxHeight(135);
            Button tile=Ui.button("",()->browseCategory(i+1));
            tile.setId("collection-"+c.name().toLowerCase(java.util.Locale.ROOT));tile.setGraphic(graphic);
            tile.setAccessibleText("Shop "+Ui.category(c));tile.getStyleClass().add("collection-tile");
            tile.setPrefWidth(365);graphic.prefWidthProperty().bind(tile.widthProperty().subtract(36));
            tile.setMinHeight(171);tile.setPrefHeight(171);tile.setMaxHeight(171);
            tile.setMinWidth(260);tiles.getChildren().add(tile);
        }
        VBox section=new VBox(22,Ui.label("A LITTLE MORE YOU.","collection-title"),tiles);
        section.getStyleClass().add("discovery-section");return section;
    }
    private VBox closing() {
        VBox end=new VBox(12,Ui.label("GOOD THINGS.\nEVERY DAY.","closing-title"),
            Ui.label("For the way you listen, dress, and live.","closing-sub"));
        end.setAlignment(Pos.CENTER);end.getStyleClass().add("closing-section");return end;
    }
    private void filterChanged() { if(clearing)return;state.setScrollPosition(0);refresh(); }
    private void browseCategory(int index) {
        clearing=true;search.clear();category.getSelectionModel().select(index);clearing=false;filterChanged();
    }
    private void clear() {
        clearing=true;search.clear();category.getSelectionModel().selectFirst();sort.getSelectionModel().selectFirst();
        budget.getSelectionModel().selectFirst();minimum.clear();maximum.clear();state.setPriceRange("","");available.setSelected(false);clearing=false;filterChanged();
    }
    private void refresh() {
        if(clearing||(getScene()!=null&&getScene().getRoot().isDisabled()))return;
        String query=search.getText();int index=category.getSelectionModel().getSelectedIndex(),ordering=sort.getSelectionModel().getSelectedIndex(),cap=budget.getSelectionModel().getSelectedIndex();
        Category group=index==0?null:Category.values()[index-1];
        BigDecimal capPrice=cap==0?null:BigDecimal.valueOf(new int[]{0,25,50,100}[cap]);
        if(!validRange())return;
        BigDecimal lower=parseRange(minimum.getText()),upper=parseRange(maximum.getText());
        state.setPriceRange(minimum.getText(),maximum.getText());rangeApplied.set(lower!=null||upper!=null);
        boolean inStock=available.isSelected();double previousScroll=state.getScrollPosition();
        state.setCategoryIndex(index);state.setSortIndex(ordering);state.setBudgetIndex(cap);state.setInStockOnly(inStock);
        syncToggle();
        if(navSync!=null)navSync.run(); // header highlights the category chosen via the filter dropdown too
        javafx.animation.PauseTransition loadingDelay=new javafx.animation.PauseTransition(javafx.util.Duration.millis(250));
        loadingDelay.setOnFinished(e->results.getChildren().setAll(Ui.skeleton()));loadingDelay.play();
        sceneProperty().addListener((o,a,b)->{if(b==null)loadingDelay.stop();});
        Ui.async(this,"Finding products…",()->products.search(user,query,group,inStock,capPrice).stream().filter(p->lower==null||p.getPrice().compareTo(lower)>=0).filter(p->upper==null||p.getPrice().compareTo(upper)<=0).toList(),rows->{
            loadingDelay.stop();List<Product> sorted=new ArrayList<>(rows);
            sorted.sort(Comparator.comparing(Product::getName,String.CASE_INSENSITIVE_ORDER).thenComparingLong(Product::getId));
            if(ordering==3)sorted.sort(Comparator.comparing(Product::getName,String.CASE_INSENSITIVE_ORDER).reversed().thenComparingLong(Product::getId));
            if(ordering==1)sorted.sort(Comparator.comparing(Product::getPrice));
            if(ordering==2)sorted.sort(Comparator.comparing(Product::getPrice).reversed());
            restoring=true;grid.getChildren().clear();
            for(Product p:sorted){ProductCard card=new ProductCard(p,()->add(p),()->details.accept(p));if(saved!=null)card.enableSavedItems(saved);grid.getChildren().add(card);}
            title.setText(!query.isBlank()?"Search results":index==0?"The everyday edit":Ui.category(group));
            result.setText(sorted.size()+(sorted.size()==1?" item":" items")+" · USD");feedback.setText("");
            results.getChildren().setAll(sorted.isEmpty()?Ui.empty("No products found","Try a different search or remove a filter.","Clear filters",this::clear):grid);
            chips.getChildren().clear();
            if(!query.isBlank())chip("Search: "+query,()->{search.clear();filterChanged();});
            if(index>0)chip(Ui.category(group),()->category.getSelectionModel().selectFirst());
            if(cap>0)chip(budget.getValue(),()->budget.getSelectionModel().selectFirst());
            if(lower!=null||upper!=null)chip("Price: $"+(lower==null?"0":lower)+"–"+(upper==null?"any":"$"+upper),()->{minimum.clear();maximum.clear();filterChanged();});
            if(inStock)chip("In stock",()->{available.setSelected(false);filterChanged();});
            if(!chips.getChildren().isEmpty()) {
                Button clear=Ui.link("Clear filters",this::clear);clear.setId("clear-active-filters");chips.getChildren().add(clear);
            }
            chips.setVisible(!chips.getChildren().isEmpty());
            Platform.runLater(()->{scroll.setVvalue(previousScroll);restoring=false;});
        },error->{loadingDelay.stop();results.getChildren().setAll(Ui.empty("Could not load products","Your search is kept. Try again.","Retry",this::refresh));Ui.failure(feedback,error);});
    }
    private void chip(String text,Runnable remove) { Button b=Ui.button(text+"  ×",remove);b.getStyleClass().add("filter-chip");b.setAccessibleText("Remove "+text+" filter");chips.getChildren().add(b); }
    private void add(Product product) {
        Ui.async(this,"Adding to cart…",()->{carts.add(user,cart,product.getId(),1);return true;},done->{
            changed.run();
            if(grid.lookup("#product-"+product.getId()) instanceof ProductCard card)card.confirmAdded();
            // A floating toast is visible wherever the user has scrolled; the inline label can be off-screen.
            if(toast!=null)toast.accept("✓ "+product.getName()+" added to cart");
            else Ui.success(feedback,"✓ "+product.getName()+" added. Open your cart whenever you're ready.");
        },error->Ui.failure(feedback,error));
    }
    private static BigDecimal parseRange(String value) {
        if(value.isBlank())return null;BigDecimal price=new BigDecimal(value.trim());
        if(price.signum()<0||price.scale()>2||price.compareTo(new BigDecimal("1000000"))>0)throw new IllegalArgumentException();return price;
    }
}
