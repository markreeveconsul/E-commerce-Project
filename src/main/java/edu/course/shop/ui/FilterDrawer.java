package edu.course.shop.ui;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import java.math.BigDecimal;
import java.util.function.Consumer;
/** Edits a draft; only Apply commits it to the catalog. */
final class FilterDrawer {
 record Selection(int category,boolean inStock,String minimum,String maximum){}
 static void show(CatalogView owner,CatalogState state,double ceiling,java.util.List<edu.course.shop.model.Product> matches,Consumer<Selection> apply){
  VBox panel=new VBox(20);panel.setId("catalog-filters");panel.getStyleClass().add("filter-drawer");
  panel.setMinWidth(0);panel.prefWidthProperty().bind(owner.getScene().widthProperty().multiply(.38));panel.setMaxWidth(380);
  Button close=new Button();close.setGraphic(StoreIcon.of("close"));close.getStyleClass().add("filter-close");close.setId("close-filters");close.setAccessibleText("Close filters without applying");
  Region gap=new Region();HBox.setHgrow(gap,Priority.ALWAYS);HBox heading=new HBox(Ui.label("Filters","section-title"),gap,close);heading.setAlignment(Pos.CENTER_LEFT);
  ToggleGroup categories=new ToggleGroup();VBox category=new VBox(4,Ui.label("Category","field-label"));
  for(int i=0;i<=edu.course.shop.model.Category.shopping().size();i++){RadioButton radio=new RadioButton(i==0?"All products":Ui.category(edu.course.shop.model.Category.shopping().get(i-1)));radio.setMaxWidth(Double.MAX_VALUE);radio.setToggleGroup(categories);radio.setUserData(i);radio.setId("filter-category-"+i);radio.setSelected(i==state.getCategoryIndex());category.getChildren().add(radio);}
  double from=state.getMinimumPrice().isBlank()?0:Double.parseDouble(state.getMinimumPrice());
  double to=state.getMaximumPrice().isBlank()?ceiling:Double.parseDouble(state.getMaximumPrice());
  if(state.getBudgetIndex()>0)to=new int[]{0,25,50,100}[state.getBudgetIndex()];
  ceiling=Math.max(ceiling,Math.max(from,to));
  PriceRange range=new PriceRange(ceiling,from,to);range.setId("price-range");
  Label low=new Label(),high=new Label();low.textProperty().bind(range.lowProperty().asString("$%,.2f"));high.textProperty().bind(range.highProperty().asString("$%,.2f"));
  Region space=new Region();HBox.setHgrow(space,Priority.ALWAYS);HBox values=new HBox(low,space,high);
  VBox price=new VBox(10,Ui.label("Price range · USD","field-label"),values,range,Ui.label("Drag either handle to set your range.","range-help"));
  CheckBox stock=new CheckBox("In stock only");stock.setId("filter-in-stock");stock.setSelected(state.isInStockOnly());
  Label count=Ui.label("","filter-match-count");count.setId("filter-match-count");
  VBox body=new VBox(20,category,new Separator(),price,new Separator(),new VBox(12,Ui.label("Availability","field-label"),stock,count));
  ScrollPane scroll=Ui.scroll(body);scroll.setMinHeight(0);VBox.setVgrow(scroll,Priority.ALWAYS);
  Button reset=Ui.link("Clear all",()->{categories.getToggles().getFirst().setSelected(true);range.setRange(0,range.getCeiling());stock.setSelected(false);});reset.setId("clear-filters");
  Button confirm=Ui.primary("Apply filters",()->{});confirm.setId("apply-filters");confirm.getStyleClass().add("purchase-action");
  // Count the draft against the current search without changing the product grid.
  Runnable updateCount=()->{
   int index=(Integer)categories.getSelectedToggle().getUserData();
   var group=index==0?null:edu.course.shop.model.Category.shopping().get(index-1);
   BigDecimal lower=BigDecimal.valueOf(range.getLow()),upper=BigDecimal.valueOf(range.getHigh());
   long total=matches.stream().filter(p->group==null||p.getCategory()==group)
       .filter(p->!stock.isSelected()||p.getStock()>0)
       .filter(p->p.getPrice().compareTo(lower)>=0&&p.getPrice().compareTo(upper)<=0).count();
   count.setText(total+(total==1?" matching product":" matching products"));
   confirm.setText("Apply filters · "+total);
  };
  categories.selectedToggleProperty().addListener((o,a,b)->updateCount.run());
  range.lowProperty().addListener((o,a,b)->updateCount.run());
  range.highProperty().addListener((o,a,b)->updateCount.run());
  stock.selectedProperty().addListener((o,a,b)->updateCount.run());updateCount.run();
  Region footerGap=new Region();HBox.setHgrow(footerGap,Priority.ALWAYS);HBox footer=new HBox(reset,footerGap,confirm);footer.setAlignment(Pos.CENTER_LEFT);
  panel.getChildren().addAll(heading,scroll,new Separator(),footer);
  Runnable dismiss=ShopOverlay.show(owner,panel,false,true,()->{});
  close.setOnAction(e->dismiss.run());
  confirm.setOnAction(e->{Selection result=new Selection((Integer)categories.getSelectedToggle().getUserData(),stock.isSelected(),range.getLow()==0?"":money(range.getLow()),range.getHigh()==range.getCeiling()?"":money(range.getHigh()));dismiss.run();apply.accept(result);});
 }
 private static String money(double value){return BigDecimal.valueOf(value).setScale(2,java.math.RoundingMode.HALF_UP).toPlainString();}
}
