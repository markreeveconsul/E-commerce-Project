package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.service.ProductService;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.geometry.Side;
import javafx.util.Duration;
import java.util.List;
import java.util.function.Consumer;

/** Debounced read-only suggestions; stale tasks cannot reopen a detached page's popup. */
public final class SearchSuggestions {
    private final ContextMenu popup=new ContextMenu();
    private final PauseTransition debounce=new PauseTransition(Duration.millis(220));
    private Task<List<Product>> pending;
    public void dismiss() {generation++;debounce.stop();if(pending!=null)pending.cancel();popup.hide();}
    private long generation;
    private javafx.beans.value.ChangeListener<javafx.scene.Scene> lifecycle;
    public SearchSuggestions(TextField search,User user,ProductService products,Consumer<Product> details,Runnable results) {
        popup.setId("search-suggestions");popup.setStyle("-fx-min-width: 350;");
        javafx.event.EventHandler<javafx.scene.input.KeyEvent> keyboard=e->{
            if(e.getCode()==javafx.scene.input.KeyCode.ENTER||e.getCode()==javafx.scene.input.KeyCode.ESCAPE)dismiss();
        };
        search.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,keyboard);
        javafx.beans.value.ChangeListener<String> listener=(o,a,b)->{generation++;debounce.stop();if(pending!=null)pending.cancel();popup.hide();if(b.trim().length()>=2&&search.isFocused())debounce.playFromStart();};
        debounce.setOnFinished(e->{
            String query=search.getText().trim();long request=generation;
            Task<List<Product>> task=new Task<>(){protected List<Product> call(){return products.search(user,query,null);}};pending=task;
            task.setOnSucceeded(done->{
                if(request!=generation||search.getScene()==null||!search.isFocused())return;
                popup.getItems().clear();
                for(Product p:task.getValue().stream().limit(5).toList()) {
                    MenuItem item=new MenuItem(p.getName()+"  ·  "+Money.format(p.getPrice()));item.setOnAction(action->{popup.hide();details.accept(p);});popup.getItems().add(item);
                }
                MenuItem all=new MenuItem("View all results for “"+query+"”");all.setOnAction(action->{popup.hide();results.run();});popup.getItems().add(all);
                popup.show(search,Side.BOTTOM,0,3);
            });
            task.setOnFailed(done->{if(request==generation)popup.hide();});Thread worker=new Thread(task,"product-suggestions");worker.setDaemon(true);worker.start();
        });
        search.textProperty().addListener(listener);
        javafx.beans.value.ChangeListener<Boolean> focused=(o,a,b)->{if(!b){debounce.stop();}else if(search.getText().trim().length()>=2)debounce.playFromStart();};search.focusedProperty().addListener(focused);
        lifecycle=(o,a,b)->{if(b==null){generation++;debounce.stop();if(pending!=null)pending.cancel();popup.hide();search.textProperty().removeListener(listener);search.focusedProperty().removeListener(focused);search.sceneProperty().removeListener(lifecycle);search.removeEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED,keyboard);}};search.sceneProperty().addListener(lifecycle);
    }
}
