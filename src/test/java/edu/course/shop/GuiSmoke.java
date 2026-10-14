package edu.course.shop;
import edu.course.shop.model.*;
import edu.course.shop.persistence.*;
import edu.course.shop.service.*;
import edu.course.shop.ui.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** Exercises the real controls with a fresh SQLite file. Does not use the user's store database. */
public final class GuiSmoke {
    private static Scene scene;
    private static Path evidence;
    private static int checks;
    private static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task=new FutureTask<>(()->{if(scene!=null){scene.getRoot().applyCss();scene.getRoot().layout();}return action.call();});Platform.runLater(task);return task.get(20,TimeUnit.SECONDS);
    }
    private static void waitFor(BooleanSupplier condition) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(20);
        while(!fx(()->{scene.getRoot().applyCss();scene.getRoot().layout();return condition.getAsBoolean();})){
            if(System.nanoTime()>deadline)throw new AssertionError("GUI condition timed out");
            Thread.sleep(50);
        }
    }
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static Button button(String text){
        return scene.getRoot().lookupAll(".button").stream().filter(n->n instanceof Button b && b.getText().equals(text))
            .map(n->(Button)n).findFirst().orElseThrow();
    }
    private static void click(String text)throws Exception{fx(()->{button(text).fire();return null;});}
    private static void press(String id)throws Exception{
        fx(()->{((Button)scene.lookup("#"+id)).fire();return null;});
        if(id.equals("nav-cart"))fx(()->{((Button)scene.lookup("#drawer-review")).fire();return null;});
    }
    private static void text(String id,String value)throws Exception{fx(()->{((TextInputControl)scene.lookup("#"+id)).setText(value);return null;});}
    private static String label(String id)throws Exception{return fx(()->((Label)scene.lookup("#"+id)).getText());}
    private static void idle()throws Exception{waitFor(()->!scene.getRoot().isDisabled());}
    private static void catalog()throws Exception{waitFor(()->scene.lookup("#product-grid") instanceof TilePane p && !p.getChildren().isEmpty()&&!scene.getRoot().isDisabled());}
    private static void screenshot(String name,int width,int height)throws Exception{
        fx(()->{
            scene.getRoot().resize(width,height);scene.getRoot().applyCss();scene.getRoot().layout();
            var image=scene.getRoot().snapshot(null,null);
            BufferedImage png=new BufferedImage((int)image.getWidth(),(int)image.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<png.getHeight();y++)for(int x=0;x<png.getWidth();x++)png.setRGB(x,y,image.getPixelReader().getArgb(x,y));
            ImageIO.write(png,"png",evidence.resolve(name).toFile());return null;
        });
    }
    private static void login(String user,String password)throws Exception{text("username",user);text("password",password);press("login");}
    public static void main(String[] args)throws Exception{
        evidence=Path.of(args[0]);Files.createDirectories(evidence);
        DatabaseManager db=new DatabaseManager(Path.of(args[1]));db.initialize();
        ProductRepository pr=new ProductRepository();OrderRepository or=new OrderRepository();
        Services services=new Services(new AuthService(db,new UserRepository()),new ProductService(db,pr),
            new CheckoutService(db,pr,or),new OrderService(db,or));
        User admin=services.auth().login("admin","admin123");
        Product lamp=services.products().get(6);
        services.products().save(admin,6,lamp.getName(),lamp.getCategory(),lamp.getPrice(),0,true);
        CountDownLatch ready=new CountDownLatch(1);Platform.startup(ready::countDown);ready.await();Platform.setImplicitExit(false);
        try{
            fx(()->{
                Stage stage=new Stage();scene=new Scene(new StackPane(),1280,860);
                scene.getStylesheets().add(GuiSmoke.class.getResource("/shop.css").toExternalForm());
                stage.setScene(scene);new ShopWindow(stage,services).showLogin();return null;
            });
            screenshot("01-login.png",1280,860);
            screenshot("24-login-compact.png",960,680);screenshot("01-login.png",1280,860);
            press("login");check(!label("username-error").isBlank(),"Missing username needs field error");
            login("customer","incorrect");idle();check(label("login-feedback").contains("Incorrect"),"Wrong password needs inline error");
            click("← Continue browsing");catalog();
            check(fx(()->((Button)scene.lookup("#account")).getText()).equals("Sign in"),"Guests browse without login");
            check(fx(()->scene.lookup("#search").isVisible()&&scene.lookup("#catalog-search")!=null),"Catalog search is reachable");
            check(fx(()->((Button)scene.lookup("#hero-electronics")).getGraphic().getLayoutBounds().getHeight())>=300,"Campaign artwork fills the hero height");
            check(fx(()->scene.lookup("#product-4").lookup(".collection-artwork")!=null),"Seed product renders original offline artwork");
            press("hero-shop");
            check(fx(()->((ScrollPane)scene.lookup("#catalog-scroll")).getVvalue()>0),"Hero shopping action scrolls to products");
            screenshot("22-product-collection.png",1280,860);
            fx(()->{((ScrollPane)scene.lookup("#catalog-scroll")).setVvalue(0);return null;});
            screenshot("13-guest-storefront.png",1280,860);
            screenshot("21-storefront-compact.png",960,680);screenshot("13-guest-storefront.png",1280,860);
            fx(()->{
                ScrollPane scroll=(ScrollPane)scene.lookup("#catalog-scroll");
                double start=scene.lookup("#collection-links").getParent().getBoundsInParent().getMinY();
                scroll.setVvalue(start/(scroll.getContent().getLayoutBounds().getHeight()-scroll.getViewportBounds().getHeight()));return null;
            });
            screenshot("23-collection-discovery.png",1280,860);
            check(fx(()->scene.lookup("#collection-home").getLayoutBounds().getHeight())<=200,"Collection tiles stay compact");
            fx(()->{((ScrollPane)scene.lookup("#catalog-scroll")).setVvalue(0);return null;});
            press("hero-electronics");catalog();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==2,"Campaign action opens electronics");
            check(fx(()->!scene.lookup("#editorial-hero").isManaged()),"Campaign hides while browsing a category");
            press("collection-home");catalog();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==2&&
                fx(()->((ComboBox<?>)scene.lookup("#category")).getSelectionModel().getSelectedIndex())==3,"Collection shortcut opens home products");
            text("search","mug");click("Search");catalog();
            press("collection-home");catalog();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==2&&
                fx(()->((TextField)scene.lookup("#search")).getText()).isEmpty(),"Same-category shortcut refreshes after clearing search");
            click("Shop all");catalog();
            fx(()->{var card=scene.lookup("#product-4");((Button)card.lookupAll(".button").stream().filter(n->n instanceof Button b&&b.getText().equals("View product")).findFirst().orElseThrow()).fire();return null;});
            waitFor(()->scene.lookup("#product-detail")!=null&&!scene.getRoot().isDisabled());idle();screenshot("14-product-detail.png",1280,860);
            screenshot("15-product-detail-compact.png",960,680);screenshot("14-product-detail.png",1280,860);
            fx(()->{((Spinner<Integer>)scene.lookup("#detail-quantity")).getValueFactory().setValue(2);return null;});
            press("detail-add");idle();
            fx(()->{((Button)scene.lookup("#nav-cart")).fire();return null;});
            check(fx(()->scene.lookup("#cart-drawer")!=null),"Cart opens as drawer without leaving details");
            waitFor(()->Math.abs(scene.lookup("#cart-drawer").getTranslateX())<0.01);
            screenshot("18-cart-drawer.png",1280,860);screenshot("19-cart-drawer-compact.png",960,680);screenshot("18-cart-drawer.png",1280,860);
            press("drawer-plus-4");idle();
            check(fx(()->((Button)scene.lookup("#nav-cart")).getText()).equals("Cart · 3"),"Drawer changes update header");
            press("drawer-minus-4");idle();
            fx(()->{scene.lookup("#cart-drawer").fireEvent(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED,"","",javafx.scene.input.KeyCode.ESCAPE,false,false,false,false));return null;});
            waitFor(()->scene.lookup("#cart-drawer")==null);
            check(fx(()->scene.lookup("#cart-drawer")==null&&scene.lookup("#product-detail")!=null),"Escape closes drawer and preserves product page");
            fx(()->{
                ((Button)scene.lookup("#nav-cart")).fire();
                ((Button)scene.lookup("#close-cart-drawer")).fire();
                ((Button)scene.lookup("#close-cart-drawer")).fire();
                return null;
            });
            waitFor(()->scene.lookup("#cart-drawer")==null);
            check(fx(()->!scene.lookup("#nav-cart").isDisabled()),"Repeated close during opening restores navigation");
            fx(()->{
                ((Button)scene.lookup("#nav-cart")).fire();
                ((Button)scene.lookup("#close-cart-drawer")).fire();
                ((Button)scene.lookup("#drawer-review")).fire();
                return null;
            });
            // Allow a stale completion callback to surface if navigation failed to cancel it.
            Thread.sleep(300);
            check(fx(()->scene.lookup("#cart-items")!=null&&scene.lookup("#cart-drawer")==null&&!scene.lookup("#nav-cart").isDisabled()),
                "Navigation during drawer closing cancels motion and keeps the destination usable");
            press("checkout");
            check(fx(()->scene.lookup("#login")!=null),"Guest checkout requires login");
            login("customer","customer123");waitFor(()->scene.lookup("#cart-items")!=null&&!scene.getRoot().isDisabled());
            check(fx(()->((TextField)scene.lookup("#cart-quantity-4")).getText()).equals("2"),"Sign-in preserves guest quantities");
            press("remove-4");click("Browse products");catalog();
            check(fx(()->((Button)scene.lookup("#add-6")).isDisabled()),"Sold-out product must be disabled");
            screenshot("02-catalog.png",1280,860);screenshot("08-catalog-compact.png",960,680);
            // Restore the regular viewport before subsequent control interactions.
            screenshot("02-catalog.png",1280,860);
            text("search","does not exist");click("Search");
            waitFor(()->scene.getRoot().lookupAll(".empty-state").size()==1);
            check(label("catalog-feedback").isBlank(),"Empty results are not a failure");
            check(fx(()->!scene.lookup("#catalog-filters").isVisible()),"Filters begin collapsed");
            press("toggle-filters");
            check(fx(()->scene.lookup("#catalog-filters").isVisible()),"Filter toggle reveals controls");
            click("Clear filters");catalog();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==6,"Clear filters restores catalog");
            fx(()->{((ComboBox<?>)scene.lookup("#budget")).getSelectionModel().select(2);return null;});idle();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==4,"Price cap filters products");
            fx(()->{((CheckBox)scene.lookup("#in-stock")).fire();return null;});idle();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==3,"Stock and price filters combine");
            press("toggle-filters");
            check(fx(()->!scene.lookup("#catalog-filters").isManaged()&&((TilePane)scene.lookup("#product-grid")).getChildren().size()==3),"Collapsing filters retains active selection and results");
            press("toggle-filters");
            screenshot("20-catalog-filtered.png",1280,860);
            press("view-4");waitFor(()->scene.lookup("#product-detail")!=null&&!scene.getRoot().isDisabled());idle();
            check(fx(()->scene.lookup("#related-products") instanceof FlowPane box&&!box.getChildren().isEmpty()),"Related products use current category");
            fx(()->{((TitledPane)scene.lookup("#product-details-panel")).setExpanded(false);return null;});
            check(!fx(()->((TitledPane)scene.lookup("#product-details-panel")).isExpanded()),"Product information can collapse");
            click("← Back to results");catalog();
            check(fx(()->((ComboBox<?>)scene.lookup("#budget")).getSelectionModel().getSelectedIndex())==2&&fx(()->((CheckBox)scene.lookup("#in-stock")).isSelected()),"Back retains price and stock filters");
            click("Up to $50  ×");idle();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==5,"Removing one chip preserves other filters");
            click("Clear filters");catalog();click("Home");catalog();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==2,"Header categories navigate to their collection");
            click("Clear filters");catalog();
            fx(()->{((ComboBox<?>)scene.lookup("#sort")).getSelectionModel().select(1);return null;});idle();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().getFirst().getId()).equals("product-4"),"Price ascending order");
            press("nav-cart");check(fx(()->scene.getRoot().lookupAll(".empty-state").size())==1,"Empty cart is actionable");
            click("Browse products");catalog();press("add-4");idle();
            check(fx(()->((Button)scene.lookup("#nav-cart")).getText()).equals("Cart · 1"),"Cart badge after add");
            press("nav-cart");press("plus-4");idle();
            check(fx(()->((Button)scene.lookup("#nav-cart")).getText()).equals("Cart · 2"),"Cart badge after plus");
            press("minus-4");idle();
            text("cart-quantity-4","0");click("Update");idle();
            check(label("cart-feedback").contains("Quantity"),"Invalid quantity shows useful error");
            text("cart-quantity-4","2");click("Update");idle();
            screenshot("03-cart.png",1280,860);screenshot("09-cart-compact.png",960,680);
            screenshot("03-cart.png",1280,860);
            press("remove-4");check(fx(()->scene.getRoot().lookupAll(".empty-state").size())==1,"Removing last item shows empty state");
            click("Browse products");catalog();press("add-4");idle();press("add-1");idle();press("nav-cart");
            press("checkout");
            check(fx(()->scene.lookup(".toast").getOpacity()==0),"Catalog toast is cleared when navigating to checkout");
            fx(()->{((ComboBox<?>)scene.lookup("#payment")).getSelectionModel().clearSelection();((Label)scene.lookup("#payment-error")).setText("Choose a payment method.");Ui.flag((Control)scene.lookup("#payment"),true);return null;});
            fx(()->{((ComboBox<?>)scene.lookup("#payment")).getSelectionModel().selectFirst();return null;});
            check(label("payment-error").isBlank()&&fx(()->!scene.lookup("#payment").getStyleClass().contains("invalid")),"Choosing payment clears its validation feedback");
            press("place-order");check(!label("address-error").isBlank(),"Missing address is inline");
            text("address","123 Sample Street");press("place-order");
            check(!label("recipient-error").isBlank()&&!label("city-error").isBlank()&&!label("postal-error").isBlank(),"Delivery fields require meaningful values");
            text("recipient","Demo Customer");text("city","Demo City");text("postal-code","12345");
            text("address","123 Sample Street, Demo City");click("← Back to cart");press("checkout");
            check(fx(()->((TextArea)scene.lookup("#address")).getText()).equals("123 Sample Street, Demo City"),"Back navigation preserves draft");
            screenshot("04-checkout.png",1280,860);screenshot("10-checkout-compact.png",960,680);
            screenshot("04-checkout.png",1280,860);
            fx(()->{((ComboBox<?>)scene.lookup("#payment")).getSelectionModel().select(2);return null;});
            press("place-order");idle();check(label("checkout-feedback").contains("declined"),"Decline stays on checkout with feedback");
            check(services.orders().history(services.auth().login("customer","customer123")).isEmpty(),"Decline must not create order");
            fx(()->{((ComboBox<?>)scene.lookup("#payment")).getSelectionModel().select(1);return null;});
            press("place-order");waitFor(()->scene.lookup("#confirmation")!=null);
            check(fx(()->((Button)scene.lookup("#nav-cart")).getText()).equals("Cart · 0"),"Success clears badge");
            screenshot("07-confirmation.png",1280,860);
            click("View my orders");waitFor(()->scene.lookup("#orders-table") instanceof TableView<?> t&&t.getItems().size()==1);
            screenshot("05-orders.png",1280,860);
            click("Sign out");catalog();press("account");login("admin","admin123");
            waitFor(()->scene.lookup("#admin-table") instanceof TableView<?> t&&t.getItems().size()==6);
            check(fx(()->((Button)scene.lookup("#edit-product")).isDisabled()),"Edit unavailable until selection");
            press("new-product");press("save-product");
            check(!label("name-error").isBlank()&&!label("price-error").isBlank(),"Admin field validation");
            text("product-name","GUI smoke product");text("product-price","9.99");text("product-stock","4");
            text("product-description","A sample description entered by the store administrator.");
            Path photo=Path.of(args[1]).toAbsolutePath().getParent().resolve("upload-test.png");
            BufferedImage fixture=new BufferedImage(240,160,BufferedImage.TYPE_INT_RGB);
            var graphics=fixture.createGraphics();graphics.setColor(new java.awt.Color(222,233,217));graphics.fillRect(0,0,240,160);
            graphics.setColor(new java.awt.Color(38,92,72));graphics.fillRoundRect(75,35,90,85,12,12);
            graphics.setColor(java.awt.Color.WHITE);graphics.drawString("TEST PHOTO",82,80);graphics.dispose();ImageIO.write(fixture,"png",photo.toFile());
            fx(()->{((ProductPhotoEditor)scene.lookup("#photo-editor")).importPhoto(photo);return null;});idle();
            check(!label("photo-feedback").isBlank(),"Photo upload provides draft feedback");
            check(fx(()->scene.lookup("#photo-preview").lookupAll(".product-photo").size())==1,"Uploaded photo renders in preview");
            screenshot("16-photo-upload.png",1280,860);
            press("save-product");
            waitFor(()->((TableView<?>)scene.lookup("#admin-table")).getItems().size()==7);
            Product uploaded=services.products().search(admin,"GUI smoke",null).getFirst();
            check(uploaded.getPhoto()!=null,"Admin save persists photo");
            Files.delete(photo);
            check(services.products().get(uploaded.getId()).getPhoto()!=null,"Deleting original file keeps saved photo");
            text("admin-search","GUI smoke");check(fx(()->((TableView<?>)scene.lookup("#admin-table")).getItems().size())==1,"Admin search");
            fx(()->{((TableView<?>)scene.lookup("#admin-table")).getSelectionModel().selectFirst();return null;});
            press("edit-product");
            check(fx(()->((TextField)scene.lookup("#product-name")).getText()).equals("GUI smoke product"),"Explicit edit fills form");
            screenshot("11-admin-editor.png",1280,860);screenshot("17-admin-editor-compact.png",960,680);screenshot("11-admin-editor.png",1280,860);
            check(fx(()->scene.lookup("#photo-preview").lookupAll(".product-photo").size())==1,"Reopening edit restores uploaded photo");
            press("remove-photo");click("Cancel");press("edit-product");
            check(fx(()->scene.lookup("#photo-preview").lookupAll(".product-photo").size())==1,"Cancel preserves saved photo");
            Path bad=Path.of(args[1]).toAbsolutePath().getParent().resolve("bad-upload.jpg");Files.writeString(bad,"not a photo");
            fx(()->{((ProductPhotoEditor)scene.lookup("#photo-editor")).importPhoto(bad);return null;});idle();
            check(label("photo-feedback").contains("valid"),"Invalid upload shows inline error");
            check(fx(()->scene.lookup("#photo-preview").lookupAll(".product-photo").size())==1,"Invalid upload keeps existing photo");
            press("remove-photo");press("save-product");idle();press("edit-product");
            check(services.products().get(uploaded.getId()).getPhoto()==null,"Saving removal clears persistent photo");
            fx(()->{((CheckBox)scene.lookup("#product-active")).setSelected(false);return null;});press("save-product");
            waitFor(()->!scene.getRoot().isDisabled()&&((TableView<?>)scene.lookup("#admin-table")).getItems().size()==1);
            text("admin-search","");
            fx(()->{((ComboBox<?>)scene.lookup("#admin-status")).getSelectionModel().select(1);return null;});
            check(fx(()->((TableView<?>)scene.lookup("#admin-table")).getItems().size())==6,"Active filter hides deactivated item");
            screenshot("06-admin.png",1280,860);screenshot("12-admin-compact.png",960,680);
            check(services.products().search(services.auth().login("customer","customer123"),"GUI smoke",null).isEmpty(),"Inactive hidden from customer");
            System.out.println("GUI SMOKE PASS: "+checks+" assertions; editorial actions, artwork, catalog, filters, sold-out state, cart edits, draft retention, declined/approved payment, confirmation, history, admin create/edit/filter; 24 snapshots.");
        }finally{Platform.exit();}
    }
}
