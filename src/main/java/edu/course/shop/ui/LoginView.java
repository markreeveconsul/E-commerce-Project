package edu.course.shop.ui;
import edu.course.shop.service.AuthService;
import edu.course.shop.model.User;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.function.Consumer;

public final class LoginView extends StackPane {
    public LoginView(AuthService auth,Consumer<User> signedIn) {
        getStyleClass().add("login-background");
        TextField username=new TextField(); username.setPromptText("Your username"); username.setId("username");
        PasswordField password=new PasswordField(); password.setPromptText("Your password"); password.setId("password");
        Label usernameError=Ui.feedback("username-error"),passwordError=Ui.feedback("password-error"),feedback=Ui.feedback("login-feedback");
        Label status=Ui.feedback("operation-status");
        // A red border marks the field that needs attention; it clears as soon as the user types.
        username.textProperty().addListener((o,a,b)->{usernameError.setText("");Ui.flag(username,false);});
        password.textProperty().addListener((o,a,b)->{passwordError.setText("");Ui.flag(password,false);});
        Button login=Ui.primary("Sign in",()-> {
            String name=username.getText(),secret=password.getText();
            usernameError.setText(""); passwordError.setText(""); feedback.setText("");
            if(name.isBlank()) { usernameError.setText("Enter your username."); Ui.flag(username,true); username.requestFocus(); return; }
            if(secret.isBlank()) { passwordError.setText("Enter your password."); Ui.flag(password,true); password.requestFocus(); return; }
            Ui.async(this,"Signing you in…",()->auth.login(name,secret),user->{ password.clear(); signedIn.accept(user); },
                error->Ui.failure(feedback,error));
        });
        login.setId("login"); login.setDefaultButton(true); login.setMaxWidth(Double.MAX_VALUE); login.getStyleClass().add("cta");

        // Demo shortcuts: handy for the project defense, and they only fill the fields (the user still signs in).
        // Remove this block before treating the app as a real store.
        Button fillCustomer=Ui.link("Use customer login",()->{username.setText("customer");password.setText("customer123");});
        Button fillAdmin=Ui.link("Use admin login",()->{username.setText("admin");password.setText("admin123");});
        VBox demo=new VBox(6,Ui.label("Demo accounts","field-label"),
            Ui.label("Customer: customer / customer123\nAdmin: admin / admin123","muted"),new HBox(8,fillCustomer,fillAdmin));
        demo.getStyleClass().add("notice");

        VBox form=new VBox(16,Ui.label("Welcome back.","page-title"),Ui.label("Sign in to check out or manage your store.","muted"),
            Ui.field("Username",username,usernameError),Ui.field("Password",password,passwordError),feedback,status,login,demo);
        form.getStyleClass().add("login-card"); form.setPrefWidth(480); form.setMaxWidth(480);

        VBox intro=new VBox(24,Ui.label("MARKET","brand"),Ui.label("GOOD TO\nSEE YOU.","hero-title"),
            Ui.label("Your favorites. Your cart.\nYour next good find.","muted"),Ui.label("Course demo · All payments are simulated","muted"));
        intro.getStyleClass().add("login-intro");

        // Full-bleed split: the form is centered in the right half, and both halves share the window width equally.
        StackPane formPane=new StackPane(form); formPane.getStyleClass().add("login-form-pane");
        intro.setPrefWidth(0); formPane.setPrefWidth(0);
        HBox.setHgrow(intro,Priority.ALWAYS); HBox.setHgrow(formPane,Priority.ALWAYS);
        getChildren().add(new HBox(0,intro,formPane));
    }
}