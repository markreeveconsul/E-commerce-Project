package edu.course.shop;
import edu.course.shop.model.*;
import edu.course.shop.persistence.*;
import edu.course.shop.service.*;
import edu.course.shop.ui.*;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.input.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;

/** New-feature smoke checks on a shown desktop window and an isolated synthetic catalog. */
public final class NextUiSmoke {
    private static Scene scene;private static Stage stage;private static Path evidence;private static int checks;
    private static <T> T fx(Callable<T> action)throws Exception{FutureTask<T> task=new FutureTask<>(()->{if(scene!=null){scene.getRoot().applyCss();scene.getRoot().layout();}return action.call();});Platform.runLater(task);return task.get(20,TimeUnit.SECONDS);}
    private static void waitFor(BooleanSupplier test)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(20);while(!fx(()->test.getAsBoolean())){if(System.nanoTime()>end)throw new AssertionError("Condition timed out");Thread.sleep(40);}}
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static void idle()throws Exception{waitFor(()->!scene.getRoot().isDisabled());}
    private static void catalog()throws Exception{waitFor(()->scene.lookup("#product-grid") instanceof TilePane p&&!p.getChildren().isEmpty()&&!scene.getRoot().isDisabled());}
    private static void press(String id)throws Exception{fx(()->{((Button)scene.lookup("#"+id)).fire();return null;});}
    private static void text(String id,String value)throws Exception{fx(()->{((TextInputControl)scene.lookup("#"+id)).setText(value);return null;});}
    private static void click(String value)throws Exception{fx(()->{scene.getRoot().lookupAll(".button").stream().filter(n->n instanceof Button b&&b.getText().equals(value)).map(n->(Button)n).findFirst().orElseThrow().fire();return null;});}
    private static String label(String id)throws Exception{return fx(()->((Label)scene.lookup("#"+id)).getText());}
    private static void resize(double width,double height)throws Exception {
        fx(()->{stage.setWidth(width+stage.getWidth()-scene.getWidth());stage.setHeight(height+stage.getHeight()-scene.getHeight());return null;});
        waitFor(()->Math.abs(scene.getWidth()-width)<2&&Math.abs(scene.getHeight()-height)<2);
    }
    private static void screenshot(String name)throws Exception{
        waitFor(()->scene.getRoot().lookupAll(".page").stream().allMatch(n->n.getOpacity()==1));
        fx(()->{write(scene.getRoot(),evidence.resolve(name));return null;});
    }
    private static void write(Node node,Path file)throws Exception{var im=node.snapshot(null,null);BufferedImage out=new BufferedImage((int)im.getWidth(),(int)im.getHeight(),BufferedImage.TYPE_INT_ARGB);for(int y=0;y<out.getHeight();y++)for(int x=0;x<out.getWidth();x++)out.setRGB(x,y,im.getPixelReader().getArgb(x,y));ImageIO.write(out,"png",file.toFile());}
    private static DialogPane dialog() {return Window.getWindows().stream().filter(w->w.isShowing()&&w!=stage&&w.getScene().getRoot() instanceof DialogPane).map(w->(DialogPane)w.getScene().getRoot()).findFirst().orElse(null);}
    private static void openDialog(String id)throws Exception{fx(()->{Platform.runLater(()->((Button)scene.lookup("#"+id)).fire());return null;});waitFor(()->dialog()!=null);}
    private static void closeDialog()throws Exception{fx(()->{((Button)dialog().lookupButton(ButtonType.CLOSE)).fire();return null;});waitFor(()->dialog()==null);}
    private static byte[] photo(int color)throws Exception{BufferedImage img=new BufferedImage(1200,900,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();g.setColor(new java.awt.Color(237,238,239));g.fillRect(0,0,1200,900);g.setColor(new java.awt.Color(color));g.fillRoundRect(350,150,500,600,60,60);g.setColor(java.awt.Color.WHITE);g.drawString("SYNTHETIC PRODUCT FIXTURE",465,460);g.dispose();var bytes=new java.io.ByteArrayOutputStream();ImageIO.write(img,"png",bytes);return bytes.toByteArray();}
    public static void main(String[] args)throws Exception{
        evidence=Path.of(args[0]);Files.createDirectories(evidence);DatabaseManager db=new DatabaseManager(Path.of(args[1]));db.initialize();ProductRepository pr=new ProductRepository();OrderRepository or=new OrderRepository();
        Services services=new Services(new AuthService(db,new UserRepository()),new ProductService(db,pr),new CheckoutService(db,pr,or),new OrderService(db,or));User admin=services.auth().login("admin","admin123"),customer=services.auth().login("customer","customer123");
        TechTestCatalog.prepare(services.products(),admin);
        byte[] main=photo(0x245C70),angle=photo(0xDB6939);Product p=services.products().get(1);services.products().save(admin,1,p.getName(),p.getCategory(),p.getPrice(),12,true,"A synthetic fixture used to verify product details and gallery controls.",main,List.of(angle),"Material: Test fixture\nIncluded items: One unit");
        CountDownLatch ready=new CountDownLatch(1);Platform.startup(ready::countDown);ready.await();Platform.setImplicitExit(false);
        try {
            fx(()->{stage=new Stage();scene=new Scene(new StackPane(),1280,860);scene.getStylesheets().add(NextUiSmoke.class.getResource("/shop.css").toExternalForm());stage.setScene(scene);stage.show();new ShopWindow(stage,services).showStorefront();return null;});catalog();
            check(fx(()->((Button)scene.lookup("#nav-saved")).getText()).equals("Saved items"),"Saved items has a visible navigation label");
            press("save-1");check(fx(()->((Button)scene.lookup("#save-1")).getText()).equals("♥"),"Guest heart toggles without signing in");
            press("nav-saved");waitFor(()->scene.lookup("#saved-grid") instanceof TilePane t&&t.getChildren().size()==1&&!scene.getRoot().isDisabled());check(label("saved-feedback").isEmpty(),"Guest saved list opens without errors");
            press("account");check(fx(()->!((TitledPane)scene.lookup("#demo-accounts")).isExpanded()),"Demo accounts are collapsed by default");
            text("username","customer");text("password","customer123");press("login");catalog();check(services.accounts().saved(customer).contains(1L),"Guest selections persist after customer sign-in");
            press("nav-profile");waitFor(()->scene.lookup("#display-name")!=null&&!scene.getRoot().isDisabled());text("display-name","");press("save-profile");idle();
            check(!label("profile-error").isEmpty()&&fx(()->scene.getFocusOwner()==scene.lookup("#display-name")),"Invalid profile points to display name inline");
            text("display-name","Smoke Shopper");check(label("profile-error").isEmpty(),"Editing profile clears its own error");press("save-profile");idle();check(services.accounts().displayName(customer).equals("Smoke Shopper"),"Profile save reaches persistent service");
            text("saved-recipient","Smoke Shopper");press("save-address");
            check(!label("saved-street-error").isEmpty()&&fx(()->scene.getFocusOwner()==scene.lookup("#saved-street")),"Address validation focuses the actual invalid street field");
            text("saved-street","123 Test Street");text("saved-city","Test City");text("saved-postal","!");press("save-address");
            check(!label("saved-postal-error").isEmpty()&&fx(()->scene.getFocusOwner()==scene.lookup("#saved-postal")),"Postal validation is beside its own field");
            text("saved-recipient","Smoke Shopper");text("saved-street","123 Test Street");text("saved-city","Test City");text("saved-postal","12345");press("save-address");waitFor(()->scene.lookup("#address-list") instanceof VBox v&&v.getChildren().size()==1&&!scene.getRoot().isDisabled());check(services.accounts().addresses(customer).size()==1,"Address save persists validated fields");
            click("Edit");check(label("address-form-title").equals("Edit address"),"Address editor identifies editing mode");click("New address");check(label("address-form-title").equals("Add an address"),"New address resets editing mode");
            screenshot("01-account.png");press("nav-saved");waitFor(()->scene.lookup("#saved-grid")!=null&&!scene.getRoot().isDisabled());screenshot("02-saved-items.png");press("save-1");idle();check(services.accounts().saved(customer).isEmpty(),"Removing saved item updates storage");check(fx(()->scene.lookup("#saved-grid").lookupAll(".empty-state").size())==1,"Removing last favorite shows recovery state");click("Explore products");catalog();
            fx(()->{((TextField)scene.lookup("#search")).requestFocus();return null;});text("search","head");
            waitFor(()->Window.getWindows().stream().anyMatch(w->w.isShowing()&&w instanceof PopupWindow));
            check(fx(()->Window.getWindows().stream().anyMatch(w->w.isShowing()&&w instanceof PopupWindow)),"Debounced search suggestions open on real window");
            press("catalog-search");idle();
            Thread.sleep(350);check(fx(()->Window.getWindows().stream().noneMatch(w->w.isShowing()&&w instanceof PopupWindow)),"Submitting search dismisses suggestions without a stale reopen");
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==1,"Submitted name search returns the matching product");
            press("clear-search");idle();check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==6,"Clear search restores the collection");
            press("toggle-filters");
            fx(()->{((ComboBox<?>)scene.lookup("#budget")).getSelectionModel().select(1);return null;});idle();
            text("minimum-price","50");text("maximum-price","70");press("apply-price-range");idle();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==1,"Custom price range replaces quick budget instead of silently intersecting it");
            check(fx(()->((ComboBox<?>)scene.lookup("#budget")).getSelectionModel().getSelectedIndex())==0,"Custom price range clears quick budget selection");
            check(fx(()->!scene.lookup("#editorial-hero").isVisible()),"Applied custom price range keeps results ahead of campaign");
            check(fx(()->((Button)scene.lookup("#toggle-filters")).getText()).contains("1"),"Filter toggle shows number of applied filters");
            text("minimum-price","80");press("apply-price-range");check(label("catalog-feedback").contains("range"),"Invalid price range reports inline error");
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==1,"Invalid range retains previously loaded products");
            fx(()->{((ComboBox<?>)scene.lookup("#budget")).getSelectionModel().select(1);return null;});idle();
            check(fx(()->((TextField)scene.lookup("#minimum-price")).getText()).isEmpty()&&fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().size())==3,"Quick budget replaces stale custom range and recovers invalid draft");
            text("search","thermal");press("catalog-search");idle();press("clear-search");idle();
            check(fx(()->((ComboBox<?>)scene.lookup("#budget")).getSelectionModel().getSelectedIndex())==1,"Clear search preserves the shopper's budget");
            screenshot("10-catalog-filters.png");
            resize(960,680);
            screenshot("11-catalog-compact.png");
            check(fx(()->{Node n=scene.lookup("#apply-price-range");var bounds=n.localToScene(n.getBoundsInLocal());return scene.getWidth()<=961&&bounds.getMaxX()<=scene.getWidth()&&bounds.getMinX()>=0;}),"Compact price controls stay inside the viewport");
            check(fx(()->{Node n=scene.lookup("#nav-cart");var bounds=n.localToScene(n.getBoundsInLocal());return scene.getWidth()<=961&&bounds.getMaxX()<=scene.getWidth();}),"Labeled header stays inside a compact window");
            resize(1280,860);press("clear-filters");idle();
            fx(()->{((ComboBox<?>)scene.lookup("#sort")).getSelectionModel().select(3);return null;});idle();
            check(fx(()->((TilePane)scene.lookup("#product-grid")).getChildren().getFirst().getId()).equals("product-1"),"Name Z–A places Wireless headphones first");
            press("view-1");waitFor(()->scene.lookup("#detail-plus")!=null&&!scene.getRoot().isDisabled());click("← Back to results");catalog();
            check(fx(()->((ComboBox<?>)scene.lookup("#sort")).getSelectionModel().getSelectedIndex())==3,"Name sorting survives detail navigation");
            press("clear-filters");idle();
            press("view-1");waitFor(()->scene.lookup("#detail-plus")!=null&&!scene.getRoot().isDisabled());press("detail-plus");check(fx(()->((QuantityControl)scene.lookup("#detail-quantity")).getValue())==2,"Custom quantity plus updates purchase amount");
            text("detail-quantity-input","999");fx(()->{((TextField)scene.lookup("#detail-quantity-input")).fireEvent(new javafx.event.ActionEvent());return null;});check(fx(()->((QuantityControl)scene.lookup("#detail-quantity")).getValue())==2,"Invalid typed quantity cannot bypass stock bound");
            press("gallery-photo-1");check(fx(()->scene.lookup("#gallery-photo-1").getStyleClass().contains("gallery-selected")),"Gallery angle gets selected state");screenshot("03-product-details.png");
            fx(()->{scene.lookup("#zoom-photo").requestFocus();return null;});openDialog("zoom-photo");fx(()->{((Button)dialog().lookup("#image-zoom-in")).fire();return null;});check(fx(()->((Label)dialog().lookup("#image-zoom-caption")).getText()).contains("Photo 2 of 2"),"Zoom opens the selected real angle");
            check(fx(()->dialog().getStylesheets().stream().anyMatch(sheet->sheet.endsWith("shop.css"))),"Image viewer inherits the Market visual system");
            fx(()->{((Button)dialog().lookup("#image-zoom-reset")).fire();dialog().applyCss();dialog().layout();return null;});
            check(fx(()->{ScrollPane viewport=(ScrollPane)dialog().lookup("#image-zoom-viewport");Node image=dialog().lookup("#image-zoom-photo");return Math.abs(image.getBoundsInParent().getCenterX()-viewport.getContent().getLayoutBounds().getCenterX())<2;}),"Fit image centers a photo smaller than its viewport");
            fx(()->{dialog().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.PLUS,false,false,false,false));return null;});
            check(fx(()->dialog().lookupAll(".label").stream().anyMatch(n->n instanceof Label l&&l.getText().equals("150%"))),"Keyboard plus changes zoom after Fit image resets it");
            fx(()->{for(int i=0;i<5;i++)((Button)dialog().lookup("#image-zoom-in")).fire();dialog().applyCss();dialog().layout();return null;});
            check(fx(()->{ScrollPane viewport=(ScrollPane)dialog().lookup("#image-zoom-viewport");return viewport.getContent().getLayoutBounds().getWidth()>viewport.getViewportBounds().getWidth();}),"Zoomed photo expands scrollable content beyond the viewport");
            fx(()->{((Button)dialog().lookup("#image-zoom-reset")).fire();return null;});
            check(fx(()->((ScrollPane)dialog().lookup("#image-zoom-viewport")).isPannable()),"Zoom supports image panning");fx(()->{write(dialog(),evidence.resolve("04-image-zoom.png"));return null;});closeDialog();check(fx(()->scene.getFocusOwner()==scene.lookup("#zoom-photo")),"Closing zoom restores gallery trigger focus");
            press("detail-add");idle();check(fx(()->((Button)scene.lookup("#nav-cart")).getText()).contains("2"),"Detail purchase adds selected quantity");
            press("nav-cart");press("drawer-remove-1");check(fx(()->scene.lookup("#drawer-undo-removal").isVisible()),"Drawer offers removal undo");press("drawer-undo-removal");idle();check(fx(()->((Button)scene.lookup("#nav-cart")).getText()).contains("2"),"Drawer undo restores quantity");press("drawer-review");
            press("remove-1");press("undo-cart-removal");idle();check(fx(()->((TextField)scene.lookup("#cart-quantity-1")).getText()).equals("2"),"Full cart undo restores quantity");
            press("checkout");waitFor(()->scene.lookup("#checkout-saved-address") instanceof ComboBox<?> combo&&!combo.getItems().isEmpty()&&!scene.getRoot().isDisabled());
            fx(()->{((ComboBox<?>)scene.lookup("#checkout-saved-address")).getSelectionModel().selectFirst();return null;});check(fx(()->((TextArea)scene.lookup("#address")).getText()).equals("123 Test Street"),"Saved address fills checkout without changing stored address");
            fx(()->{((ToggleButton)scene.lookup("#payment-wallet")).fire();return null;});check(fx(()->((ComboBox<?>)scene.lookup("#payment")).getSelectionModel().getSelectedIndex())==1,"Payment card selects supported wallet method");screenshot("05-checkout.png");
            fx(()->{Node parent=scene.lookup("#checkout-columns");while(parent!=null&&!(parent instanceof ScrollPane))parent=parent.getParent();((ScrollPane)parent).setVvalue(.35);return null;});
            check(fx(()->scene.lookup("#place-order").getParent().getTranslateY())==0,"Short checkout viewport uses normal scrolling for the full summary");
            resize(1280,960);
            fx(()->{Node parent=scene.lookup("#checkout-columns");while(parent!=null&&!(parent instanceof ScrollPane))parent=parent.getParent();((ScrollPane)parent).setVvalue(.35);return null;});
            check(fx(()->scene.lookup("#place-order").getParent().getTranslateY())>0,"Wide summary follows scrolling beside checkout form");
            press("place-order");waitFor(()->scene.lookup("#confirmation")!=null&&!scene.getRoot().isDisabled());click("View my orders");waitFor(()->scene.lookup("#order-card-1")!=null&&!scene.getRoot().isDisabled());screenshot("06-order-history.png");
            fx(()->{Platform.runLater(()->((Button)scene.lookup("#buy-again-1")).fire());return null;});waitFor(()->dialog()!=null);check(fx(()->dialog().getContentText()).contains("Current catalog prices"),"Buy again asks customer to review current prices");fx(()->{((Button)dialog().lookupButton(ButtonType.YES)).fire();return null;});waitFor(()->scene.lookup("#cart-quantity-1")!=null&&!scene.getRoot().isDisabled());check(fx(()->((TextField)scene.lookup("#cart-quantity-1")).getText()).equals("2"),"Confirmed repeat order populates cart");
            press("remove-1");click("Sign out");catalog();press("account");text("username","admin");text("password","admin123");press("login");waitFor(()->scene.lookup("#admin-table") instanceof TableView<?> t&&!t.getItems().isEmpty()&&!scene.getRoot().isDisabled());
            fx(()->{TableView<Product> table=(TableView<Product>)scene.lookup("#admin-table");table.getSelectionModel().select(table.getItems().stream().filter(row->row.getId()==1).findFirst().orElseThrow());return null;});press("edit-product");
            fx(()->{((ProductPhotoEditor)scene.lookup("#photo-editor")).setFraming(new PhotoFrame(PhotoFrame.Mode.COVER,.15,.75));return null;});press("make-main-0");check(fx(()->Arrays.equals(((ProductPhotoEditor)scene.lookup("#photo-editor")).getPhoto(),angle)),"Make main swaps draft gallery and primary photo");click("Cancel");check(Arrays.equals(services.products().get(1).getPhoto(),main),"Cancel preserves original stored image");press("edit-product");
            fx(()->{((ProductPhotoEditor)scene.lookup("#photo-editor")).setFraming(new PhotoFrame(PhotoFrame.Mode.COVER,.15,.75));return null;});openDialog("preview-photo-frames");check(fx(()->dialog().lookupAll(".product-photo").size())==2,"Framing preview shows catalog and full image");closeDialog();
            press("save-product");waitFor(()->scene.lookup("#admin-table").isVisible()&&!scene.getRoot().isDisabled());check(services.products().get(1).getPhotoFrame().equals(new PhotoFrame(PhotoFrame.Mode.COVER,.15,.75)),"Photo framing persists after Save");press("edit-product");openDialog("preview-product");check(fx(()->dialog().getContent().lookup("#product-gallery")!=null),"Unsaved product preview opens complete gallery");closeDialog();screenshot("07-admin-editor.png");
            resize(960,680);screenshot("08-admin-compact.png");
            check(fx(()->scene.lookup("#photo-editor").getBoundsInParent().getWidth())>0,"Compact admin editor keeps photo tools available");
            fx(()->{Motion.reducedProperty().set(true);return null;});click("Cancel");click("Sign out");catalog();press("view-1");waitFor(()->scene.lookup("#detail-columns")!=null&&!scene.getRoot().isDisabled());screenshot("09-details-compact.png");check(fx(()->((VBox)scene.lookup("#detail-columns")).getChildren().getFirst() instanceof ProductGallery),"Compact details stack gallery before purchase panel");check(fx(()->scene.lookup("#gallery-main").getBoundsInParent().getHeight())<=360,"Compact gallery reserves space for purchase controls");check(fx(()->scene.lookup("#product-detail").getOpacity())==1,"Reduced motion uses immediate page state");
            System.out.println("NEXT UI SMOKE PASS: "+checks+" assertions; saved items, profile, address book, search popup, price range, quantity, zoom, undo, payment, repeat order and draft framing; 11 snapshots.");
        }finally{Platform.exit();}
    }
}
