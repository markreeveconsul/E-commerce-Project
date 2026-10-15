package edu.course.shop.ui;
import javafx.beans.property.*;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
/** One track with two non-crossing, keyboard-accessible price handles. */
public final class PriceRange extends Pane {
 private final double ceiling;
 private final DoubleProperty low=new SimpleDoubleProperty(),high=new SimpleDoubleProperty();
 private final Region track=new Region(),selection=new Region();
 private final Button lower=new Button(),upper=new Button();
 public PriceRange(double ceiling,double from,double to){
  this.ceiling=Math.max(.01,ceiling);setMinHeight(44);setPrefHeight(44);setMaxHeight(44);
  track.getStyleClass().add("range-track");selection.getStyleClass().add("range-selection");getChildren().addAll(track,selection,lower,upper);
  configure(lower,true);configure(upper,false);low.addListener((o,a,b)->update());high.addListener((o,a,b)->update());setRange(from,to);
 }
 private void configure(Button thumb,boolean minimum){
  thumb.setId(minimum?"price-range-low":"price-range-high");thumb.getStyleClass().add("range-thumb");
  thumb.setOnMousePressed(e->{thumb.requestFocus();e.consume();});
  thumb.setOnMouseDragged(e->{double x=sceneToLocal(e.getSceneX(),e.getSceneY()).getX();move(minimum,Math.round(Math.max(0,Math.min(1,(x-12)/Math.max(1,getWidth()-24)))*ceiling*100)/100.0);e.consume();});
  thumb.setOnKeyPressed(e->{double step=e.isShiftDown()?10:1,value=minimum?low.get():high.get();if(e.getCode()==KeyCode.LEFT||e.getCode()==KeyCode.DOWN)move(minimum,value-step);else if(e.getCode()==KeyCode.RIGHT||e.getCode()==KeyCode.UP)move(minimum,value+step);else if(e.getCode()==KeyCode.HOME)move(minimum,0);else if(e.getCode()==KeyCode.END)move(minimum,ceiling);else return;e.consume();});
 }
 private void move(boolean minimum,double value){if(minimum)low.set(Math.max(0,Math.min(high.get(),value)));else high.set(Math.min(ceiling,Math.max(low.get(),value)));}
 public void setRange(double from,double to){high.set(Math.max(0,Math.min(ceiling,to)));low.set(Math.max(0,Math.min(high.get(),from)));update();}
 public DoubleProperty lowProperty(){return low;}public DoubleProperty highProperty(){return high;}
 public double getLow(){return low.get();}public double getHigh(){return high.get();}public double getCeiling(){return ceiling;}
 private void update(){lower.setAccessibleText(String.format("Minimum price $%.2f. Arrow keys adjust.",low.get()));upper.setAccessibleText(String.format("Maximum price $%.2f. Arrow keys adjust.",high.get()));requestLayout();}
 @Override protected void layoutChildren(){double w=Math.max(0,getWidth()-24),a=12+w*low.get()/ceiling,b=12+w*high.get()/ceiling;track.resizeRelocate(12,20,w,4);selection.resizeRelocate(a,20,b-a,4);lower.resizeRelocate(a-10,12,20,20);upper.resizeRelocate(b-10,12,20,20);}
}
