package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.service.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;
public final class SavedItemsView extends VBox {
    public SavedItemsView(User user,Cart cart,ProductService products,SavedItems saved,Runnable changed,Runnable browse,Consumer<Product> details,Consumer<String> toast) {
        setId("saved-items-page");getStyleClass().add("page");setSpacing(20);Label feedback=Ui.feedback("saved-feedback");TilePane grid=new TilePane(20,24);grid.setPrefColumns(3);grid.setPrefTileWidth(260);grid.setId("saved-grid");
        getChildren().addAll(Ui.label("Saved for later","page-title"),Ui.label(user instanceof Guest?"Your favorites stay here during this visit. Sign in to save them to your account.":"Good finds, kept close. Saving an item does not reserve its stock.","muted"),feedback,Ui.scroll(grid));
        saved.observe(this,()->{
            grid.getChildren().removeIf(node->node instanceof ProductCard&& !saved.snapshot().contains(Long.parseLong(node.getId().substring("product-".length()))));
            if(grid.getChildren().isEmpty())grid.getChildren().add(Ui.empty("No saved items available","Save a product with its heart button. Unavailable products are hidden.","Explore products",browse));
        });
        Platform.runLater(()->Ui.async(this,"Loading saved items…",()->products.search(user,"",null),rows->{
            for(Product p:rows)if(saved.snapshot().contains(p.getId())) {
                ProductCard card=new ProductCard(p,()->Ui.async(this,"Adding to cart…",()->{new CartService(products).add(user,cart,p.getId(),1);return true;},done->{changed.run();if(grid.lookup("#product-"+p.getId()) instanceof ProductCard c)c.confirmAdded();toast.accept("✓ "+p.getName()+" added to cart");},error->Ui.failure(feedback,error)),()->details.accept(p));card.enableSavedItems(saved);card.setPrefWidth(260);grid.getChildren().add(card);
            }
            if(grid.getChildren().isEmpty())grid.getChildren().add(Ui.empty("No saved items available","Save a product with its heart button. Unavailable products are hidden.","Explore products",browse));
        },error->Ui.failure(feedback,error)));
    }
}
