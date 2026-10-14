package edu.course.shop.ui;

import edu.course.shop.exception.StorageException;
import edu.course.shop.exception.ValidationException;
import edu.course.shop.model.Category;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public final class Ui {
    private Ui() {
    }

    public static void main(String[] args) {
    }

    public static Label label(String text, String style) {
        Label label = new Label(text);
        // An empty or null style is allowed (JavaFX throws if an empty class is added).
        if (style != null && !style.isBlank()) {
            label.getStyleClass().add(style);
        }
        label.setWrapText(true);
        return label;
    }

    public static String category(Category category) {
        String value = category.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    public static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> attempt(action));
        return button;
    }

    public static Button primary(String text, Runnable action) {
        Button button = button(text, action);
        button.getStyleClass().add("primary");
        return button;
    }

    /** Quiet text-only button for secondary actions such as "View product". */
    public static Button link(String text, Runnable action) {
        Button button = button(text, action);
        button.getStyleClass().add("link-button");
        return button;
    }

    /** Clips any node (e.g. a product photo) to rounded corners; arc is the corner diameter in pixels. */
    public static void round(Node node, double arc) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        node.layoutBoundsProperty().addListener((o, a, b) -> {
            clip.setWidth(b.getWidth());
            clip.setHeight(b.getHeight());
        });
        node.setClip(clip);
    }

    /** Shows or clears the red "invalid" border on a form control. */
    public static void flag(Control control, boolean invalid) {
        control.getStyleClass().remove("invalid");
        if (invalid) {
            control.getStyleClass().add("invalid");
        }
    }

    /** One line with text on the left and a value pinned to the right (receipts, totals). */
    public static HBox row(String left, String right, String leftStyle, String rightStyle) {
        Label name = label(left, leftStyle);
        Label value = label(right, rightStyle);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        value.setWrapText(false);
        HBox row = new HBox(12, name, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public static VBox page(String title, String subtitle, Node... content) {
        VBox page = new VBox(18, label(title, "page-title"), label(subtitle, "muted"));
        page.getStyleClass().add("page");
        page.getChildren().addAll(content);
        return page;
    }

    public static Label feedback(String id) {
        Label label = label("", "feedback");
        label.setId(id);
        label.managedProperty().bind(label.textProperty().isNotEmpty());
        label.visibleProperty().bind(label.managedProperty());
        return label;
    }

    public static VBox field(String title, Control input, Label error) {
        Label label = label(title, "field-label");
        label.setLabelFor(input);
        input.setAccessibleText(title);
        error.getStyleClass().add("field-error");
        return new VBox(6, label, input, error);
    }

    public static ScrollPane scroll(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("clean-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return scroll;
    }

    public static VBox empty(String title, String detail, String action, Runnable callback) {
        VBox box = new VBox(14, label(title, "section-title"), label(detail, "muted"), primary(action, callback));
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("empty-state");
        return box;
    }

    public static <T> TableColumn<T, String> column(String name, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(name);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    public static <T> TableView<T> table() {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(label("No matching products.", "muted"));
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    public static int integer(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Enter a whole number within the allowed range.");
        }
    }

    public static <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new ValidationException("Select a row first.");
        }
        return value;
    }

    public static void attempt(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            error(e);
        }
    }

    public static String message(Throwable error) {
        if (error instanceof ValidationException || error instanceof StorageException) {
            return error.getMessage();
        }
        error.printStackTrace();
        return "Something went wrong. Please try again.";
    }

    public static void failure(Label label, Throwable error) {
        label.getStyleClass().removeAll("error-feedback");
        label.getStyleClass().add("error-feedback");
        label.setText(message(error));
    }

    public static void success(Label label, String text) {
        label.getStyleClass().removeAll("error-feedback");
        label.setText(text);
    }

    public static void error(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message(e), ButtonType.OK);
        alert.setHeaderText("Unable to complete the action");
        alert.showAndWait();
    }

    public static void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText("Market");
        alert.showAndWait();
    }

    public static <T> void async(Node owner, Callable<T> work, Consumer<T> success) {
        async(owner, "Loading…", work, success, Ui::error);
    }

    /** Tasks preserve the FX thread for rendering, and block duplicate mutations. */
    public static <T> void async(Node owner, String busyText, Callable<T> work, Consumer<T> success, Consumer<Throwable> failure) {
        Node lock = owner.getScene() == null ? owner : owner.getScene().getRoot();
        Label status = lock.lookup("#operation-status") instanceof Label l ? l : null;
        if (status != null) {
            status.setText(busyText);
        }
        lock.setDisable(true);
        Runnable unlock = () -> {
            lock.setDisable(false);
            if (status != null) {
                status.setText("");
            }
        };
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        task.setOnSucceeded(e -> {
            unlock.run();
            attempt(() -> success.accept(task.getValue()));
        });
        task.setOnFailed(e -> {
            unlock.run();
            failure.accept(task.getException());
        });
        Thread thread = new Thread(task, "shop-operation");
        thread.setDaemon(true);
        thread.start();
    }
}
