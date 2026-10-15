package edu.course.shop;
import edu.course.shop.persistence.*;
import edu.course.shop.service.*;
import edu.course.shop.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.nio.file.Path;

public final class ShopApp extends Application {
    @Override public void start(Stage stage) {
        DatabaseManager database = new DatabaseManager(Path.of(System.getProperty("shop.db", "data/shop.db")));
        ProductRepository products = new ProductRepository();
        OrderRepository orders = new OrderRepository();
        Services services = new Services(new AuthService(database, new UserRepository()),
            new ProductService(database, products), new CheckoutService(database, products, orders),
            new OrderService(database, orders), new AdminService(database, new AdminRepository()));
        stage.setTitle("Market / Java OOP Store");
        stage.setMinWidth(960); stage.setMinHeight(680);
        var loading = new javafx.scene.layout.StackPane(new javafx.scene.control.Label("Preparing your store..."));
        Scene scene = new Scene(loading, 1280, 860);
        scene.getStylesheets().add(getClass().getResource("/shop.css").toExternalForm());
        stage.setScene(scene); stage.show();
        Ui.async(loading, () -> { database.initialize(); return true; },
            ready -> new ShopWindow(stage, services).showStorefront());
    }
    public static void main(String[] args) { launch(args); }
}
