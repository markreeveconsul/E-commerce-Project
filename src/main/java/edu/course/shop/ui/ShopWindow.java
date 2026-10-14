package edu.course.shop.ui;
import java.util.ArrayList;
import java.util.List;

import edu.course.shop.Services;
import edu.course.shop.model.Cart;
import edu.course.shop.model.CartItem;
import edu.course.shop.model.Category;
import edu.course.shop.model.Guest;
import edu.course.shop.model.Product;
import edu.course.shop.model.User;
import edu.course.shop.service.CartService;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;


import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Navigation and transient session state, never SQL or pricing rules. */
public final class ShopWindow {
    private final Stage stage;private final Services services;
    private final CatalogState catalogState=new CatalogState();
    private final TextField search=new TextField();
    private User user=new Guest();private Cart cart=new Cart(0),pendingGuestCart;
    private final CheckoutDraft draft=new CheckoutDraft();
    private BorderPane shell;private StackPane window,overlay;private Button cartButton;
    private Node previousFocus;
    // Header selection and brief action-feedback animations.
    private final List<Button> navButtons=new ArrayList<>();
    private int lastCartCount=-1;
    private Label toast;private SequentialTransition toastAnimation;
    private ScaleTransition cartPulse;
    private ParallelTransition drawerAnimation;
    private CartDrawer activeDrawer;
    private Region drawerScrim;
    private boolean drawerClosing;

    public ShopWindow(Stage stage,Services services){this.stage=stage;this.services=services;search.setId("search");}
    public void showStorefront(){buildShell();showCatalog();}
    public void showLogin(){
        dismissDrawer();stopFeedbackAnimations();
        BorderPane login=new BorderPane(new LoginView(services.auth(),this::signedIn));
        Button browse=Ui.button("← Continue browsing",this::showStorefront);browse.getStyleClass().add("header-link");
        HBox back=new HBox(browse);back.getStyleClass().add("store-footer");login.setBottom(back);stage.getScene().setRoot(login);
    }
    private void signedIn(User signedIn){
        CartService carts=new CartService(services.products());Cart previous=cart;
        if(signedIn.canManageProducts()){
            pendingGuestCart=previous.getCustomerId()==0?previous:null;user=signedIn;cart=new Cart(user.getId());buildShell();showAdmin();return;
        }
        cart=previous.getCustomerId()==0?carts.transferGuest(previous,signedIn):new Cart(signedIn.getId());user=signedIn;buildShell();
        if(cart.getItems().isEmpty())showCatalog();
        else{
            showCart();Label feedback=(Label)shell.lookup("#cart-feedback");
            Ui.async(shell,"Reviewing your saved cart…",()->carts.review(cart),message->Ui.success(feedback,message),error->Ui.failure(feedback,error));
        }
    }
    private void signOut(){
        if(user.canShop()&&!cart.getItems().isEmpty()){
            Alert confirm=new Alert(Alert.AlertType.CONFIRMATION,"Sign out and discard this cart?",ButtonType.YES,ButtonType.NO);
            confirm.initOwner(stage);if(confirm.showAndWait().orElse(ButtonType.NO)!=ButtonType.YES)return;
        }
        user=new Guest();cart=pendingGuestCart==null?new Cart(0):pendingGuestCart;pendingGuestCart=null;draft.clear();showStorefront();
    }
    private Button link(String text,Runnable action){Button b=Ui.button(text,action);b.getStyleClass().add("header-link");return b;}
    private void buildShell(){
        dismissDrawer();stopFeedbackAnimations();
        shell=new BorderPane();
        // The toast sits in the window stack, above the page and below the cart drawer.
        toast=new Label();toast.getStyleClass().add("toast");toast.setOpacity(0);toast.setMouseTransparent(true);
        StackPane.setAlignment(toast,Pos.BOTTOM_CENTER);StackPane.setMargin(toast,new Insets(0,0,64,0));
        window=new StackPane(shell,toast);overlay=null;navButtons.clear();lastCartCount=-1;
        Label operation=Ui.feedback("operation-status");operation.getStyleClass().add("operation");
        if(user.canManageProducts()){
            Button inventory=Ui.button("Inventory",this::showAdmin);inventory.setId("nav-inventory");
            VBox nav=new VBox(22,Ui.label("MARKET","brand"),Ui.label("BEHIND THE COUNTER","eyebrow"),inventory,
                Ui.label("PRODUCTS\nPHOTOS\nSTOCK","section-title"));
            Region space=new Region();VBox.setVgrow(space,Priority.ALWAYS);
            nav.getChildren().addAll(space,Ui.label(user.getUsername(),"account"),Ui.button("Sign out",this::signOut));
            nav.getStyleClass().add("sidebar");nav.setPrefWidth(190);shell.setLeft(nav);shell.setTop(operation);cartButton=null;
        }else{
            search.setPromptText("Find something good…");search.setAccessibleText("Search products");search.setMinWidth(130);search.setPrefWidth(260);
            Runnable find=()->{catalogState.setScrollPosition(0);showCatalog();};search.setOnAction(e->find.run());
            Button logo=Ui.button("MARKET",()->category(0));logo.getStyleClass().add("brand-link");
            cartButton=link("Cart · 0",this::showCartDrawer);cartButton.setId("nav-cart");
            Button account=link(user instanceof Guest?"Sign in":"My orders",user instanceof Guest?this::showLogin:this::showOrders);account.setId("account");
            HBox right=new HBox(4,account,cartButton);right.setAlignment(Pos.CENTER_RIGHT);
            if(!(user instanceof Guest))right.getChildren().add(link("Sign out",this::signOut));
            Button all=Ui.button("Shop all",()->category(0));navButtons.add(all);
            HBox categories=new HBox(6,all);categories.setAlignment(Pos.CENTER_LEFT);
            categories.getStyleClass().addAll("category-nav","integrated-categories");
            for(Category c:Category.values()){
                Button b=Ui.button(Ui.category(c),()->category(c.ordinal()+1));navButtons.add(b);categories.getChildren().add(b);
            }
            Region spacer=new Region();HBox.setHgrow(spacer,Priority.ALWAYS);
            HBox header=new HBox(20,logo,categories,spacer,right);
            header.setAlignment(Pos.CENTER_LEFT);header.getStyleClass().add("store-header");
            Label announcement=Ui.label("Free shipping on every order in this demo","store-announcement");announcement.setMaxWidth(Double.MAX_VALUE);
            shell.setTop(new VBox(announcement,header,operation));updateCart();
        }
        // Footer: two real labels pushed apart, instead of one label padded with spaces.
        Region footerSpace=new Region();HBox.setHgrow(footerSpace,Priority.ALWAYS);
        HBox footer=new HBox(12,Ui.label("MARKET · Good things, every day.","muted"),footerSpace,link("About",this::showInfo),Ui.label("USD · Course demo · Simulated payments","muted"));
        footer.getStyleClass().add("store-footer");footer.setAlignment(Pos.CENTER_LEFT);
        shell.setBottom(footer);stage.getScene().setRoot(window);
    }
    private void showInfo(){
        Alert info=new Alert(Alert.AlertType.INFORMATION,"Browse without an account. Sign in as a customer to place a simulated order.\n\nShipping is free and taxes are omitted in this demo. No real money or delivery is involved.\n\nAdmins manage descriptions, photos and inventory. Your cart is temporary; completed orders are saved.",ButtonType.OK);
        info.initOwner(stage);info.setTitle("About Market");info.setHeaderText("A small shop. Good things.");info.showAndWait();
    }
    private void category(int index){catalogState.setCategoryIndex(index);catalogState.setScrollPosition(0);search.clear();showCatalog();}
    private void show(Node page){dismissDrawer();stopFeedbackAnimations();shell.setCenter(page);updateNav();}

    /** Highlights the header button for the current category, only while the storefront is showing. */
    private void updateNav(){
        boolean onCatalog=shell!=null&&shell.getCenter() instanceof CatalogView;
        int active=catalogState.getCategoryIndex();
        for(int i=0;i<navButtons.size();i++){
            List<String> styles=navButtons.get(i).getStyleClass();styles.remove("category-active");
            if(onCatalog&&i==active)styles.add("category-active");
        }
    }

    private void updateCart(){
        if(cartButton==null)return;
        int count=cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
        cartButton.setText("Cart · "+count);
        // One gentle pulse per addition; a new addition replaces any unfinished pulse.
        if(lastCartCount>=0&&count>lastCartCount){
            if(cartPulse!=null)cartPulse.stop();
            cartButton.setScaleX(1);cartButton.setScaleY(1);
            cartPulse=new ScaleTransition(Duration.millis(90),cartButton);
            cartPulse.setFromX(1);cartPulse.setFromY(1);cartPulse.setToX(1.06);cartPulse.setToY(1.06);
            cartPulse.setCycleCount(2);cartPulse.setAutoReverse(true);cartPulse.play();
        }
        lastCartCount=count;
    }

    /** Floating confirmation at the bottom of the window; fades in, holds, fades out. */
    private void showToast(String message){
        if(toast==null)return;
        toast.setText(message);if(toastAnimation!=null)toastAnimation.stop();
        FadeTransition in=new FadeTransition(Duration.millis(150),toast);in.setToValue(1);
        PauseTransition hold=new PauseTransition(Duration.seconds(2.2));
        FadeTransition out=new FadeTransition(Duration.millis(150),toast);out.setToValue(0);
        toastAnimation=new SequentialTransition(in,hold,out);toastAnimation.play();
    }

    private void stopFeedbackAnimations(){
        if(toastAnimation!=null){toastAnimation.stop();toastAnimation=null;}
        if(toast!=null)toast.setOpacity(0);
        if(cartPulse!=null){cartPulse.stop();cartPulse=null;}
        if(cartButton!=null){cartButton.setScaleX(1);cartButton.setScaleY(1);}
    }
    public void showCatalog(){show(new CatalogView(user,cart,services.products(),new CartService(services.products()),(Runnable)this::updateCart,(Runnable)this::showCartDrawer,(java.util.function.Consumer<Product>)this::showDetails,search,catalogState,(java.util.function.Consumer<String>)this::showToast,(Runnable)this::updateNav));}
    private void showDetails(Product product){
        dismissDrawer();Ui.async(shell,"Opening product…",()->services.products().get(product.getId()),latest->{
            if(!latest.isActive()){showCatalog();return;}
            show(new ProductDetailView(latest,user,cart,new CartService(services.products()),services.products(),this::updateCart,this::showCatalog,this::showCartDrawer,this::showDetails));
        },Ui::error);
    }
    public void showCartDrawer(){
        if(overlay!=null)return;previousFocus=stage.getScene().getFocusOwner();
        drawerClosing=false;
        drawerScrim=new Region();drawerScrim.getStyleClass().add("drawer-scrim");
        drawerScrim.setOnMouseClicked(e->closeDrawer());drawerScrim.setOpacity(0);
        activeDrawer=new CartDrawer(user,cart,new CartService(services.products()),this::updateCart,this::closeDrawer,this::showCart,this::showCheckout);
        overlay=new StackPane(drawerScrim,activeDrawer);StackPane.setAlignment(activeDrawer,Pos.CENTER_RIGHT);
        overlay.setOnKeyPressed(e->{if(e.getCode()==KeyCode.ESCAPE){closeDrawer();e.consume();}});
        shell.setDisable(true);window.getChildren().add(overlay);
        // Lay out first so the slide distance also works in a smaller window.
        window.applyCss();window.layout();activeDrawer.setTranslateX(activeDrawer.getWidth());
        animateDrawer(0,1,null);
        activeDrawer.lookup("#close-cart-drawer").requestFocus();
    }
    private void animateDrawer(double x,double opacity,Runnable finished){
        if(drawerAnimation!=null)drawerAnimation.stop();
        TranslateTransition slide=new TranslateTransition(Duration.millis(200),activeDrawer);
        slide.setToX(x);slide.setInterpolator(Interpolator.EASE_BOTH);
        FadeTransition fade=new FadeTransition(Duration.millis(200),drawerScrim);fade.setToValue(opacity);
        drawerAnimation=new ParallelTransition(slide,fade);
        drawerAnimation.setOnFinished(e->{drawerAnimation=null;if(finished!=null)finished.run();});
        drawerAnimation.play();
    }
    /** Navigation cancels pending motion immediately, so old callbacks cannot change the next page. */
    private void dismissDrawer(){
        if(drawerAnimation!=null){drawerAnimation.stop();drawerAnimation=null;}
        if(overlay!=null){window.getChildren().remove(overlay);overlay=null;shell.setDisable(false);}
        activeDrawer=null;drawerScrim=null;drawerClosing=false;
    }
    private void closeDrawer(){
        if(overlay==null||drawerClosing)return;
        drawerClosing=true;
        animateDrawer(activeDrawer.getWidth(),0,()->{
            dismissDrawer();
            // Reflect drawer edits without losing the checkout draft.
            if(shell.getCenter() instanceof CartView)showCart();else if(shell.getCenter() instanceof CheckoutView)showCheckout();
            if(previousFocus!=null&&previousFocus.getScene()==stage.getScene())previousFocus.requestFocus();
        });
    }
    public void showCart(){show(new CartView(user,cart,new CartService(services.products()),this::updateCart,this::showCheckout,this::showCatalog));}
    public void showCheckout(){
        dismissDrawer();if(user instanceof Guest){showLogin();return;}
        if(cart.getItems().isEmpty()){showCart();return;}
        show(new CheckoutView(user,cart,services.checkout(),draft,order->{updateCart();show(new ConfirmationView(order,this::showOrders,this::showCatalog));},this::showCart));
    }
    public void showOrders(){show(new OrdersView(user,services.orders(),this::showCatalog));}
    public void showAdmin(){show(new AdminView(user,services.products()));}
}