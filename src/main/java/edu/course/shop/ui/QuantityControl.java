package edu.course.shop.ui;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
/** A bounded, keyboard-friendly quantity stepper. Typed edits commit on Enter or focus loss. */
public final class QuantityControl extends HBox {
    private final IntegerProperty value=new SimpleIntegerProperty(1);
    public QuantityControl(int maximum) {
        super(2);setAlignment(Pos.CENTER);getStyleClass().add("stepper");setMaxWidth(150);
        int limit=Math.max(1,maximum);TextField input=new TextField("1");input.setId("detail-quantity-input");input.setAccessibleText("Quantity");input.setPrefWidth(50);input.setAlignment(Pos.CENTER);input.getStyleClass().add("stepper-input");
        Button minus=Ui.button("−",()->value.set(Math.max(1,value.get()-1))),plus=Ui.button("+",()->value.set(Math.min(limit,value.get()+1)));
        minus.setId("detail-minus");plus.setId("detail-plus");minus.setAccessibleText("Decrease quantity");plus.setAccessibleText("Increase quantity");minus.getStyleClass().add("stepper-button");plus.getStyleClass().add("stepper-button");
        minus.disableProperty().bind(value.lessThanOrEqualTo(1));plus.disableProperty().bind(value.greaterThanOrEqualTo(limit));
        Runnable commit=()->{try {int n=Ui.integer(input.getText());if(n<1||n>limit)throw new IllegalArgumentException();value.set(n);Ui.flag(input,false);}catch(RuntimeException e){input.setText(Integer.toString(value.get()));Ui.flag(input,true);}};
        input.setOnAction(e->commit.run());input.focusedProperty().addListener((o,a,b)->{if(!b)commit.run();});
        value.addListener((o,a,b)->{input.setText(b.toString());Ui.flag(input,false);});getChildren().addAll(minus,input,plus);setDisable(maximum==0);
    }
    public int getValue(){return value.get();}
    public IntegerProperty valueProperty(){return value;}
}
