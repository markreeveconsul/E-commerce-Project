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
    private final TilePane grid=new TilePane(20,24);
    private final TextField search;
    private final CatalogState state;
    private final Consumer<Product> details;
    private final ComboBox<String> category=new ComboBox<>(),sort=new ComboBox<>(),budget=new ComboBox<>();
    private final CheckBox available=new CheckBox("In stock only");
    // Sentence case reads calmer and more modern than ALL CAPS; the hero stays uppercase as the one brand moment.
    private final Label feedback=Ui.feedback("catalog-feedback"),result=Ui.label("","muted"),title=Ui.label("All products","collection-title");
    private final FlowPane chips=new FlowPane(8,8);
    private final VBox results=new VBox(16);
    private final ScrollPane scroll;
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
        sort.getItems().addAll("Name: A–Z","Price: low to high","Price: high to low");sort.getSelectionModel().select(state.getSortIndex());
        sort.setId("sort");sort.setAccessibleText("Sort products");
        budget.getItems().addAll("Any price","Up to $25","Up to $50","Up to $100");budget.getSelectionModel().select(state.getBudgetIndex());
        budget.setId("budget");budget.setAccessibleText("Maximum price");available.setId("in-stock");available.setSelected(state.isInStockOnly());
        category.setOnAction(e->filterChanged());sort.setOnAction(e->filterChanged());budget.setOnAction(e->filterChanged());available.setOnAction(e->filterChanged());
        grid.setId("product-grid");
        grid.widthProperty().addListener((o,a,b)->{
            int columns=Math.max(1,Math.min(4,(int)((b.doubleValue()+20)/270)));
            grid.setPrefTileWidth(Math.max(180,Math.floor((b.doubleValue()-20*(columns-1))/columns)-2));
        });

        // Slim hero: one headline, one line of support, one badge. Products now start much higher on screen.
        VBox words=new VBox(4,Ui.label("GOOD THINGS. EVERY DAY.","hero-title"),
            Ui.label("Find your next favorite for home, work, and everywhere.","muted"));
        HBox.setHgrow(words,Priority.ALWAYS);
        // The old "MAKE IT YOURS ↗" badge had no action and got clipped; this one states a real benefit.
        VBox sticker=new VBox(0,Ui.label("Free shipping","section-title"),Ui.label("on every order","muted"));
        sticker.getStyleClass().add("hero-sticker");sticker.setAlignment(Pos.CENTER);sticker.setMaxHeight(USE_PREF_SIZE);
        HBox hero=new HBox(24,words,sticker);hero.setAlignment(Pos.CENTER_LEFT);hero.getStyleClass().add("hero");
        hero.visibleProperty().bind(heightProperty().greaterThan(600));hero.managedProperty().bind(hero.visibleProperty());

        // Category tiles removed: the header nav and the "All categories" filter already do this job.

        Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);
        HBox heading=new HBox(12,title,spacer,result);heading.setAlignment(Pos.CENTER_LEFT);
        FlowPane filters=new FlowPane(10,10,category,budget,available,sort,Ui.button("Clear filters",this::clear));filters.getStyleClass().add("filter-bar");
        filters.setAlignment(Pos.CENTER_LEFT);
        chips.setId("filter-chips");chips.managedProperty().bind(chips.visibleProperty());
        results.getChildren().add(grid);
        VBox content=new VBox(14,hero,heading,filters,chips,feedback,results);
        scroll=Ui.scroll(content);scroll.setId("catalog-scroll");getChildren().add(scroll);
        scroll.vvalueProperty().addListener((o,a,b)->{if(!restoring)state.setScrollPosition(b.doubleValue());});
        Platform.runLater(this::refresh);
    }
    private void filterChanged() { if(clearing)return;state.setScrollPosition(0);refresh(); }
    private void clear() {
        clearing=true;search.clear();category.getSelectionModel().selectFirst();sort.getSelectionModel().selectFirst();
        budget.getSelectionModel().selectFirst();available.setSelected(false);clearing=false;filterChanged();
    }
    private void refresh() {
        if(clearing||(getScene()!=null&&getScene().getRoot().isDisabled()))return;
        String query=search.getText();int index=category.getSelectionModel().getSelectedIndex(),ordering=sort.getSelectionModel().getSelectedIndex(),cap=budget.getSelectionModel().getSelectedIndex();
        Category group=index==0?null:Category.values()[index-1];
        BigDecimal maximum=cap==0?null:BigDecimal.valueOf(new int[]{0,25,50,100}[cap]);
        boolean inStock=available.isSelected();double previousScroll=state.getScrollPosition();
        state.setCategoryIndex(index);state.setSortIndex(ordering);state.setBudgetIndex(cap);state.setInStockOnly(inStock);
        if(navSync!=null)navSync.run(); // header highlights the category chosen via the filter dropdown too
        Ui.async(this,"Finding products…",()->products.search(user,query,group,inStock,maximum),rows->{
            List<Product> sorted=new ArrayList<>(rows);
            if(ordering==1)sorted.sort(Comparator.comparing(Product::getPrice));
            if(ordering==2)sorted.sort(Comparator.comparing(Product::getPrice).reversed());
            restoring=true;grid.getChildren().clear();
            for(Product p:sorted)grid.getChildren().add(new ProductCard(p,()->add(p),()->details.accept(p)));
            title.setText(!query.isBlank()?"Search results":index==0?"All products":Ui.category(group));
            result.setText(sorted.size()+(sorted.size()==1?" item":" items")+" · USD");feedback.setText("");
            results.getChildren().setAll(sorted.isEmpty()?Ui.empty("No products found","Try a different search or remove a filter.","Reset search & filters",this::clear):grid);
            chips.getChildren().clear();
            if(!query.isBlank())chip("Search: "+query,()->{search.clear();filterChanged();});
            if(index>0)chip(Ui.category(group),()->category.getSelectionModel().selectFirst());
            if(cap>0)chip(budget.getValue(),()->budget.getSelectionModel().selectFirst());
            if(inStock)chip("In stock",()->{available.setSelected(false);filterChanged();});
            chips.setVisible(!chips.getChildren().isEmpty());
            Platform.runLater(()->{scroll.setVvalue(previousScroll);restoring=false;});
        },error->Ui.failure(feedback,error));
    }
    private void chip(String text,Runnable remove) { Button b=Ui.button(text+"  ×",remove);b.getStyleClass().add("filter-chip");b.setAccessibleText("Remove "+text+" filter");chips.getChildren().add(b); }
    private void add(Product product) {
        Ui.async(this,"Adding to cart…",()->{carts.add(user,cart,product.getId(),1);return true;},done->{
            changed.run();
            // A floating toast is visible wherever the user has scrolled; the inline label can be off-screen.
            if(toast!=null)toast.accept("✓ "+product.getName()+" added to cart");
            else Ui.success(feedback,"✓ "+product.getName()+" added. Open your cart whenever you're ready.");
        },error->Ui.failure(feedback,error));
    }
}