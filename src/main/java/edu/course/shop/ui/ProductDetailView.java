package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.service.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public final class ProductDetailView extends VBox {
    public ProductDetailView(Product product,User user,Cart cart,CartService carts,ProductService products,
                             Runnable changed,Runnable back,Runnable openCart,Consumer<Product> openProduct) {
        setId("product-detail");getStyleClass().add("page");setSpacing(18);
        Label feedback=Ui.feedback("detail-feedback");
        Spinner<Integer> quantity=new Spinner<>(1,Math.max(1,product.getStock()),1);quantity.setId("detail-quantity");
        quantity.setAccessibleText("Quantity");quantity.setPrefWidth(100);
        // One dominant purchase action beside the price and stock status.
        Button add=Ui.primary(product.getStock()==0?"Sold out":"Add to cart",()->Ui.async(this,"Adding to cart…",()->{
            carts.add(user,cart,product.getId(),quantity.getValue());return true;
        },done->{changed.run();Ui.success(feedback,"✓ Added to your cart.");},error->Ui.failure(feedback,error)));
        add.setId("detail-add");add.setDisable(!product.isActive()||product.getStock()==0);add.setMaxWidth(Double.MAX_VALUE);
        add.getStyleClass().addAll("cta","purchase-action");
        quantity.setDisable(product.getStock()==0);
        TitledPane description=section("Product details",product.getDescription().isBlank()?
            "The seller has not added a description yet.":product.getDescription());description.setExpanded(true);description.setId("product-details-panel");
        TitledPane delivery=section("Delivery & payment","Shipping is free in this course demo. Choose cash on delivery or a simulated wallet at checkout. No real payment or delivery is processed.");
        TitledPane stock=section("Availability",product.getStock()+" units currently available. Items in a cart are not reserved; stock is checked again at checkout.");

        boolean soldOut=product.getStock()==0,low=product.getStock()<=5;
        Label availability=Ui.label(soldOut?"Sold out":low?"Only "+product.getStock()+" left":"In stock",soldOut?"stock-out":"stock");
        availability.setWrapText(false);availability.setMaxWidth(Region.USE_PREF_SIZE);
        HBox priceRow=new HBox(12,Ui.label(Money.format(product.getPrice()),"total"),availability);priceRow.setAlignment(Pos.CENTER_LEFT);
        HBox quantityRow=new HBox(12,Ui.label("Quantity","field-label"),quantity);quantityRow.setAlignment(Pos.CENTER_LEFT);
        Button viewCart=Ui.link("View cart",openCart);viewCart.setId("detail-view-cart");

        // Reading order: category, name, price + stock, quantity, Add to cart, then the fine print.
        String details=product.getDescription();
        Label preview=Ui.label(details.length()>160?details.substring(0,160)+"…":details,"product-description");
        preview.setVisible(!details.isBlank());preview.managedProperty().bind(preview.visibleProperty());
        VBox info=new VBox(14,Ui.label(Ui.category(product.getCategory())+" · #"+product.getId(),"eyebrow"),
            Ui.label(product.getName(),"page-title"),priceRow,preview,quantityRow,add,feedback,viewCart,description,specifications(product),delivery,stock);
        info.getStyleClass().add("detail-info");info.setMinWidth(300);info.setPrefWidth(420);info.setMaxWidth(460);

        ProductGallery gallery=new ProductGallery(product);HBox.setHgrow(gallery,Priority.ALWAYS);
        HBox columns=new HBox(32,gallery,info);columns.setAlignment(Pos.TOP_LEFT);

        // Related products reuse ProductCard, so they look and behave exactly like the storefront grid.
        FlowPane suggestions=new FlowPane(20,20);suggestions.setId("related-products");
        VBox related=new VBox(14,Ui.label("More from this category","section-title"),suggestions);related.setVisible(false);related.managedProperty().bind(related.visibleProperty());
        VBox content=new VBox(32,columns,related);
        Button backButton=Ui.link("← Back to results",back);backButton.setMaxWidth(Region.USE_PREF_SIZE);
        getChildren().addAll(backButton,Ui.scroll(content));
        Platform.runLater(()->Ui.async(this,"Finding related products…",()->products.search(user,"",product.getCategory()),rows->{
            for(Product p:rows.stream().filter(p->p.getId()!=product.getId()).limit(4).toList()){
                ProductCard card=new ProductCard(p,()->Ui.async(this,"Adding to cart…",()->{
                    carts.add(user,cart,p.getId(),1);return true;
                },done->{changed.run();if(suggestions.lookup("#product-"+p.getId()) instanceof ProductCard added)added.confirmAdded();Ui.success(feedback,"✓ "+p.getName()+" added to your cart.");},error->Ui.failure(feedback,error)),
                    ()->openProduct.accept(p));
                card.setPrefWidth(220);suggestions.getChildren().add(card);
            }
            related.setVisible(!suggestions.getChildren().isEmpty());
        },error->Ui.failure(feedback,error)));
    }
    private TitledPane specifications(Product product) {
        VBox values=new VBox(12);
        values.getChildren().addAll(Ui.row("Category",Ui.category(product.getCategory()),"muted","field-label"),
            Ui.row("Product ID",Long.toString(product.getId()),"muted","field-label"));
        for(String line:product.getSpecifications().lines().filter(s->!s.isBlank()).toList()) {
            int separator=line.indexOf(':');
            if(separator>0) {
                Label key=Ui.label(line.substring(0,separator).trim(),"muted"),value=Ui.label(line.substring(separator+1).trim(),"field-label");
                key.setMinWidth(0);value.setMinWidth(0);key.setPrefWidth(130);value.setPrefWidth(180);
                HBox.setHgrow(value,Priority.ALWAYS);values.getChildren().add(new HBox(16,key,value));
            } else values.getChildren().add(Ui.label(line,"product-description"));
        }
        TitledPane pane=new TitledPane("Specifications",values);pane.setId("product-specifications");pane.setAnimated(false);pane.setExpanded(true);return pane;
    }
    private TitledPane section(String title,String text) {
        TitledPane pane=new TitledPane(title,Ui.label(text,"product-description"));pane.setExpanded(false);pane.setAnimated(false);return pane;
    }
}
