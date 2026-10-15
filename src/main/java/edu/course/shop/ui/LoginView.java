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
        TextField visiblePassword=new TextField();visiblePassword.setPromptText("Your password");visiblePassword.setId("password-visible");
        visiblePassword.setAccessibleText("Password, visible");
        visiblePassword.textProperty().bindBidirectional(password.textProperty());
        visiblePassword.setVisible(false);visiblePassword.setManaged(false);
        Button reveal=Ui.link("Show",()->{});reveal.setId("show-password");reveal.getStyleClass().add("password-toggle");
        reveal.setAccessibleText("Show password");
        StackPane passwordInputs=new StackPane(password,visiblePassword);passwordInputs.setMinWidth(0);HBox.setHgrow(passwordInputs,Priority.ALWAYS);
        HBox passwordRow=new HBox(10,passwordInputs,reveal);passwordRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        reveal.setOnAction(e->{
            boolean show=!visiblePassword.isVisible();
            int caret=show?password.getCaretPosition():visiblePassword.getCaretPosition();
            password.setVisible(!show);password.setManaged(!show);visiblePassword.setVisible(show);visiblePassword.setManaged(show);
            reveal.setText(show?"Hide":"Show");reveal.setAccessibleText(show?"Hide password":"Show password");
            TextField active=show?visiblePassword:password;active.requestFocus();active.positionCaret(caret);
        });
        Label usernameError=Ui.feedback("username-error"),passwordError=Ui.feedback("password-error"),feedback=Ui.feedback("login-feedback");
        Label status=Ui.feedback("operation-status");
        // A red border marks the field that needs attention; it clears as soon as the user types.
        username.textProperty().addListener((o,a,b)->{usernameError.setText("");Ui.flag(username,false);});
        password.textProperty().addListener((o,a,b)->{passwordError.setText("");Ui.flag(password,false);Ui.flag(visiblePassword,false);});
        Button login=Ui.primary("Sign in",()-> {
            String name=username.getText(),secret=password.getText();
            usernameError.setText(""); passwordError.setText(""); feedback.setText("");
            if(name.isBlank()) { usernameError.setText("Enter your username."); Ui.flag(username,true); username.requestFocus(); return; }
            if(secret.isBlank()) { passwordError.setText("Enter your password."); Ui.flag(password,true); Ui.flag(visiblePassword,true); (visiblePassword.isVisible()?visiblePassword:password).requestFocus(); return; }
            Ui.async(this,"Signing you in…",()->auth.login(name,secret),user->{ password.clear(); signedIn.accept(user); },
                error->Ui.failure(feedback,error));
        });
        login.setId("login"); login.setDefaultButton(true); login.setMaxWidth(Double.MAX_VALUE); login.getStyleClass().addAll("cta","purchase-action");

        // Demo shortcuts: handy for the project defense, and they only fill the fields (the user still signs in).
        // Remove this block before treating the app as a real store.
        Button fillCustomer=Ui.link("Use customer login",()->{username.setText("customer");password.setText("customer123");});
        Button fillAdmin=Ui.link("Use admin login",()->{username.setText("admin");password.setText("admin123");});
        VBox demo=new VBox(6,Ui.label("Demo accounts","field-label"),
            Ui.label("Customer: customer / customer123\nAdmin: admin / admin123","muted"),new HBox(8,fillCustomer,fillAdmin));
        demo.getStyleClass().add("notice");

        Label passwordLabel=Ui.label("Password","field-label");passwordLabel.setLabelFor(password);
        password.setAccessibleText("Password");passwordError.getStyleClass().add("field-error");
        Label mark=Ui.label("MARK","login-brand-mark"),et=Ui.label("ET","login-brand-et");
        mark.setWrapText(false);et.setWrapText(false);
        HBox wordmark=new HBox(0,mark,et);wordmark.setId("login-brand");wordmark.setAccessibleText("MARKET");wordmark.getStyleClass().add("login-brand");
        TitledPane demoPanel=new TitledPane("Demo accounts",demo);demoPanel.setExpanded(false);demoPanel.setAnimated(false);demoPanel.setId("demo-accounts");
        VBox form=new VBox(14,wordmark,Ui.label("ACCOUNT ACCESS","login-access-label"),Ui.label("Welcome back.","page-title"),
            Ui.label("Sign in to continue shopping or manage your store.","muted"),Ui.field("Username",username,usernameError),
            new VBox(6,passwordLabel,passwordRow,passwordError),feedback,status,login,demoPanel);
        form.getStyleClass().add("login-card"); form.setPrefWidth(500); form.setMaxWidth(520);
        form.setMaxHeight(Region.USE_PREF_SIZE);

        EditorialHeader intro=EditorialHeader.forLogin();
        intro.setMinHeight(0);intro.setPrefHeight(0);intro.setMaxHeight(Double.MAX_VALUE);

        // Full-bleed split: the form is centered in the right half, and both halves share the window width equally.
        ScrollPane formScroll=Ui.scroll(new StackPane(form));formScroll.setFitToHeight(true);
        StackPane formPane=new StackPane(formScroll); formPane.getStyleClass().add("login-form-pane");formPane.setMinWidth(0);
        form.setMinWidth(0);
        intro.setPrefWidth(0); formPane.setPrefWidth(0);
        HBox.setHgrow(intro,Priority.ALWAYS); HBox.setHgrow(formPane,Priority.ALWAYS);
        HBox layout=new HBox(0,intro,formPane);
        layout.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
        getChildren().add(layout);
    }
}
