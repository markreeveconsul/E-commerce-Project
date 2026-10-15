package edu.course.shop.ui;
import edu.course.shop.model.*;
import edu.course.shop.service.ProductService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.BigDecimal;
import java.util.List;

public final class AdminView extends VBox {
    private long editingId;
    private final Label activeCount=Ui.label("","section-title"),lowCount=Ui.label("","section-title"),photoCount=Ui.label("","section-title");

    private List<Product> all=List.of();
    private final TableView<Product> table=Ui.table();
    private final TextField search=new TextField();
    private final ComboBox<String> status=new ComboBox<>(), filterCategory=new ComboBox<>();
    private final Label count=Ui.label("","muted");

    public AdminView(User admin,ProductService products) {
        getStyleClass().add("page"); setId("admin-page"); setSpacing(16);
        table.setId("admin-table"); table.setMinHeight(225); table.setPrefHeight(330);
        TableColumn<Product,String> idColumn=Ui.column("ID",p->Long.toString(p.getId()));idColumn.setMinWidth(45);idColumn.setPrefWidth(50);idColumn.setMaxWidth(65);table.getColumns().add(idColumn);
        TableColumn<Product,Product> productColumn=new TableColumn<>("Product");
        productColumn.setCellValueFactory(row->new javafx.beans.property.ReadOnlyObjectWrapper<>(row.getValue()));
        productColumn.setCellFactory(column->new TableCell<>() {
            @Override protected void updateItem(Product product,boolean empty) {
                super.updateItem(product,empty);
                if(empty||product==null){setGraphic(null);setText(null);return;}
                ProductArtwork photo=new ProductArtwork(product,42,true);photo.setPrefWidth(48);photo.setMinWidth(48);photo.setMaxWidth(48);
                Label name=Ui.label(product.getName(),"admin-product-name");name.setWrapText(false);name.setMaxWidth(Double.MAX_VALUE);name.setTooltip(new Tooltip(product.getName()));
                Label price=Ui.label(Money.format(product.getPrice()),"admin-product-price");
                VBox details=new VBox(2,name,price);HBox.setHgrow(details,Priority.ALWAYS);
                HBox cell=new HBox(10,photo,details);cell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);setGraphic(cell);setText(null);
            }
        });productColumn.setMinWidth(230);table.getColumns().add(productColumn);
        TableColumn<Product,String> categoryColumn=Ui.column("Category",p->Ui.category(p.getCategory()));categoryColumn.setMinWidth(125);categoryColumn.setPrefWidth(165);categoryColumn.setCellFactory(column->new TableCell<>(){@Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);setText(empty?null:item);setTooltip(empty||item==null?null:new Tooltip(item));}});table.getColumns().add(categoryColumn);
        TableColumn<Product,String> priceColumn=Ui.column("Price",p->Money.format(p.getPrice()));priceColumn.setMinWidth(82);priceColumn.setPrefWidth(100);table.getColumns().add(priceColumn);
        TableColumn<Product,String> stockColumn=Ui.column("Stock",p->Integer.toString(p.getStock()));stockColumn.setMinWidth(58);stockColumn.setPrefWidth(70);table.getColumns().add(stockColumn);
        TableColumn<Product,String> statusColumn=Ui.column("Status",p->p.isActive()?"Active":"Inactive");statusColumn.setMinWidth(78);statusColumn.setPrefWidth(90);table.getColumns().add(statusColumn);
        TableColumn<Product,String> contentColumn=new TableColumn<>("Content");
        contentColumn.setCellValueFactory(row->{Product p=row.getValue();int missing=(p.getPhoto()==null?1:0)+(p.getDescription().isBlank()?1:0)+(p.getSpecifications().isBlank()?1:0);return new javafx.beans.property.ReadOnlyStringWrapper(missing==0?"Complete":missing+" missing");});
        contentColumn.setCellFactory(column->new TableCell<>(){@Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);setText(empty?null:item);if(empty||getTableRow()==null||getTableRow().getItem()==null){setTooltip(null);return;}Product p=getTableRow().getItem();java.util.List<String> missing=new java.util.ArrayList<>();if(p.getPhoto()==null)missing.add("photo");if(p.getDescription().isBlank())missing.add("description");if(p.getSpecifications().isBlank())missing.add("specifications");setTooltip(new Tooltip(missing.isEmpty()?"Product photo, description, and specifications are present":"Missing: "+String.join(", ",missing)));}});
        contentColumn.setMinWidth(110);contentColumn.setPrefWidth(135);table.getColumns().add(contentColumn);
        table.widthProperty().addListener((o,a,b)->{
            boolean compact=b.doubleValue()<780;
            idColumn.setMinWidth(compact?38:45);idColumn.setPrefWidth(compact?42:50);
            productColumn.setMinWidth(compact?165:230);productColumn.setPrefWidth(compact?185:245);
            categoryColumn.setMinWidth(compact?90:125);categoryColumn.setPrefWidth(compact?112:165);
            priceColumn.setMinWidth(compact?68:82);priceColumn.setPrefWidth(compact?82:100);
            stockColumn.setMinWidth(compact?56:58);stockColumn.setPrefWidth(compact?62:70);
            statusColumn.setMinWidth(compact?58:78);statusColumn.setPrefWidth(compact?72:90);
            contentColumn.setMinWidth(compact?82:110);contentColumn.setPrefWidth(compact?98:135);
        });
        search.setPromptText("Find a product"); search.setId("admin-search"); search.setAccessibleText("Search inventory"); HBox.setHgrow(search,Priority.ALWAYS);
        status.getItems().addAll("All statuses","Active","Inactive"); status.getSelectionModel().selectFirst(); status.setId("admin-status"); status.setAccessibleText("Filter by status");
        filterCategory.getItems().add("All categories");
        for(Category c:Category.values())filterCategory.getItems().add(Ui.category(c));
        filterCategory.getSelectionModel().selectFirst(); filterCategory.setAccessibleText("Filter inventory by category");
        search.textProperty().addListener((o,a,b)->filter()); status.setOnAction(e->filter()); filterCategory.setOnAction(e->filter());
        TextArea description=new TextArea(); description.setId("product-description"); description.setPrefRowCount(3); description.setWrapText(true);
        description.setPromptText("Describe the product's materials, purpose, or features (up to 1,000 characters).");
        SpecificationsEditor specifications=new SpecificationsEditor();
        ProductPhotoEditor photos=new ProductPhotoEditor();
        TextField name=new TextField(),price=new TextField(),stock=new TextField();
        name.setId("product-name"); price.setId("product-price"); stock.setId("product-stock");
        price.setPromptText("19.99"); stock.setPromptText("0");
        ComboBox<Category> category=new ComboBox<>(FXCollections.observableArrayList(Category.shopping())); category.setId("product-category");category.setPromptText("Choose a tech category"); category.setMaxWidth(Double.MAX_VALUE);
        category.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Category c) { return c==null?"":Ui.category(c); }
            public Category fromString(String value) { return Category.shopping().stream().filter(c->c.label().equals(value)).findFirst().orElse(null); }
        });
        category.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Category value,boolean empty) {
                super.updateItem(value,empty);setText(value==null?"Choose a tech category":Ui.category(value));
            }
        });
        category.getSelectionModel().selectFirst();
        CheckBox active=new CheckBox("Available to customers"); active.setSelected(true); active.setId("product-active");
        javafx.beans.property.BooleanProperty replacementRequired=new javafx.beans.property.SimpleBooleanProperty();
        Label replacement=Ui.label("This item is kept for replacement. Choose a tech category, update its content, then enable Available to customers when ready.","notice");replacement.setId("retired-category-notice");
        replacement.visibleProperty().bind(replacementRequired);replacement.managedProperty().bind(replacement.visibleProperty());
        Label title=Ui.label("New product","section-title"),nameError=Ui.feedback("name-error"),priceError=Ui.feedback("price-error"),
            stockError=Ui.feedback("stock-error"),categoryError=Ui.feedback("category-error"),feedback=Ui.feedback("admin-feedback");
        name.textProperty().addListener((o,a,b)->nameError.setText(""));
        price.textProperty().addListener((o,a,b)->priceError.setText(""));
        stock.textProperty().addListener((o,a,b)->stockError.setText(""));
        VBox form=new VBox(16); form.getStyleClass().add("card"); form.setVisible(false); form.managedProperty().bind(form.visibleProperty());
        Runnable clearErrors=()-> { nameError.setText(""); priceError.setText(""); stockError.setText(""); categoryError.setText(""); };
        Runnable refresh=()->Ui.async(this,"Loading inventory…",()->products.search(admin,"",null),rows->{all=rows;filter();},error->Ui.failure(feedback,error));
        Runnable create=()-> {
            photos.setPhotos(null,List.of());photos.setFraming(PhotoFrame.AUTO); description.clear();specifications.clear();
            replacementRequired.set(false);clearErrors.run(); editingId=0; name.clear(); price.clear(); stock.setText("0"); category.getSelectionModel().selectFirst(); active.setSelected(true);
            title.setText("New product"); form.setVisible(true); name.requestFocus();
        };
        Runnable edit=()-> {
            Product p=table.getSelectionModel().getSelectedItem();
            if(p==null){feedback.setText("Select the product you want to edit.");return;}
            photos.setPhotos(p.getPhoto(),p.getAdditionalPhotos());photos.setFraming(p.getPhotoFrame()); description.setText(p.getDescription());specifications.setText(p.getSpecifications());
            clearErrors.run(); editingId=p.getId(); name.setText(p.getName()); price.setText(p.getPrice().toPlainString());
            stock.setText(Integer.toString(p.getStock()));replacementRequired.set(p.getCategory().isRetired());category.setValue(p.getCategory().isRetired()?null:p.getCategory()); active.setSelected(p.isActive());
            title.setText("Edit product #"+editingId); form.setVisible(true); name.requestFocus();
        };
        Button add=Ui.primary("+ Add product",create); add.setId("new-product");
        Button editButton=Ui.button("Edit selected",edit); editButton.setId("edit-product");
        editButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull().or(form.visibleProperty()));
        add.disableProperty().bind(form.visibleProperty()); search.disableProperty().bind(form.visibleProperty());
        status.disableProperty().bind(form.visibleProperty()); filterCategory.disableProperty().bind(form.visibleProperty());
        table.visibleProperty().bind(form.visibleProperty().not()); table.managedProperty().bind(table.visibleProperty());
        Button save=Ui.primary("Save product",()-> {

            clearErrors.run(); feedback.setText(""); boolean valid=true;
            if(description.getText().length()>1000){Ui.failure(feedback,new edu.course.shop.exception.ValidationException("Description must be at most 1,000 characters."));return;}
            if(name.getText().isBlank()||name.getText().trim().length()>80){nameError.setText("Enter a name of 1–80 characters.");valid=false;}
            BigDecimal amount=null;
            try {amount=Money.parse(price.getText());}catch(RuntimeException e){priceError.setText(Ui.message(e));valid=false;}
            int quantity=0;
            try {quantity=Ui.integer(stock.getText());if(quantity<0||quantity>100000)throw new edu.course.shop.exception.ValidationException("Stock must be between 0 and 100,000.");}
            catch(RuntimeException e){stockError.setText(Ui.message(e));valid=false;}
            if(category.getValue()==null||category.getValue().isRetired()&&active.isSelected()){categoryError.setText("Choose a tech category before making this item available.");valid=false;category.requestFocus();}
            if(!valid)return;
            long id=editingId;String value=name.getText();Category group=category.getValue();BigDecimal cost=amount;int qty=quantity;boolean enabled=active.isSelected();
            String details=description.getText(),specs=specifications.getText();byte[] selectedPhoto=photos.getPhoto();List<byte[]> angles=photos.getAdditionalPhotos();PhotoFrame frame=photos.getFraming();
            Ui.async(this,"Saving product…",()->{products.save(admin,id,value,group,cost,qty,enabled,details,selectedPhoto,angles,specs,frame);return true;},done-> {
                form.setVisible(false);Ui.success(feedback,"✓ "+value.trim()+" saved.");refresh.run();
            },error->Ui.failure(feedback,error));
        });
        save.setId("save-product");
        GridPane fields=new GridPane();fields.setHgap(16);fields.setVgap(14);
        ColumnConstraints left=new ColumnConstraints(),right=new ColumnConstraints();left.setPercentWidth(50);right.setPercentWidth(50);
        fields.getColumnConstraints().addAll(left,right);
        fields.add(Ui.field("Product name",name,nameError),0,0);fields.add(Ui.field("Category",category,categoryError),1,0);
        fields.add(Ui.field("Price (USD)",price,priceError),0,1);fields.add(Ui.field("Stock quantity",stock,stockError),1,1);
        VBox information=new VBox(14,replacement,fields,Ui.field("Description",description,Ui.feedback("description-error")),new VBox(6,Ui.label("Specifications (optional)","field-label"),specifications),active,
            Ui.label("Deactivating a product hides it from the store and preserves existing receipts.","muted"));
        information.setMinWidth(0);HBox.setHgrow(information,Priority.ALWAYS);
        photos.setPrefWidth(240);photos.setMinWidth(210);photos.setMaxWidth(240);
        Button preview=Ui.button("Preview product",()->{
            try {
                Product candidate=new Product(editingId,name.getText(),category.getValue(),Money.parse(price.getText()),Ui.integer(stock.getText()),active.isSelected(),description.getText(),photos.getPhoto(),photos.getAdditionalPhotos(),specifications.getText(),photos.getFraming());
                Dialog<Void> dialog=new Dialog<>();dialog.setTitle("Product preview · Unsaved draft");dialog.initOwner(getScene().getWindow());dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                VBox content=new VBox(16,Ui.label(candidate.getName(),"page-title"),new ProductGallery(candidate),Ui.label(Money.format(candidate.getPrice()),"total"),Ui.label(candidate.getDescription(),"product-description"),Ui.label(candidate.getSpecifications(),"muted"));
                javafx.scene.control.ScrollPane scroll=Ui.scroll(content);scroll.setPrefViewportWidth(650);scroll.setPrefViewportHeight(500);dialog.getDialogPane().setContent(scroll);dialog.showAndWait();
            }catch(RuntimeException error){Ui.failure(feedback,error);}
        });preview.setId("preview-product");
        form.getChildren().addAll(title,Ui.responsiveColumns(information,photos,800,20),
            new HBox(12,preview,save,Ui.button("Cancel",()->form.setVisible(false))));
        HBox stats=new HBox(36,activeCount,lowCount,photoCount);stats.getStyleClass().add("admin-stats");
        stats.visibleProperty().bind(form.visibleProperty().not());stats.managedProperty().bind(stats.visibleProperty());
        count.visibleProperty().bind(form.visibleProperty().not());count.managedProperty().bind(count.visibleProperty());
        Button clearSearch=Ui.button("Clear",()->{search.clear();search.requestFocus();});clearSearch.setId("clear-admin-search");
        clearSearch.disableProperty().bind(search.textProperty().isEmpty());
        HBox inventorySearch=new HBox(8,search,clearSearch);inventorySearch.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        VBox searchField=new VBox(7,Ui.label("Search inventory","field-label"),inventorySearch);HBox.setHgrow(searchField,Priority.ALWAYS);search.setMinWidth(0);
        Label statusLabel=Ui.label("Status","field-label"),categoryLabel=Ui.label("Category","field-label");statusLabel.setLabelFor(status);categoryLabel.setLabelFor(filterCategory);
        HBox filterBar=new HBox(16,searchField,new VBox(7,statusLabel,status),new VBox(7,categoryLabel,filterCategory)),actions=new HBox(12,add,editButton);
        filterBar.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);filterBar.getStyleClass().add("inventory-filter-panel");
        filterBar.visibleProperty().bind(form.visibleProperty().not());filterBar.managedProperty().bind(filterBar.visibleProperty());
        actions.visibleProperty().bind(form.visibleProperty().not());actions.managedProperty().bind(actions.visibleProperty());
        VBox body=new VBox(18,table,form);
        getChildren().addAll(Ui.label("STORE INVENTORY","page-title"),Ui.label("Keep the good things in stock. Edit products, descriptions and photos here.","muted"),stats,
            filterBar,actions,count,feedback,Ui.scroll(body));
        Platform.runLater(refresh);
    }
    private void filter() {
        String term=search.getText().trim().toLowerCase(java.util.Locale.ROOT);
        int categoryIndex=filterCategory.getSelectionModel().getSelectedIndex(),state=status.getSelectionModel().getSelectedIndex();
        List<Product> rows=all.stream().filter(p->p.getName().toLowerCase(java.util.Locale.ROOT).contains(term))
            .filter(p->categoryIndex==0||p.getCategory()==Category.values()[categoryIndex-1])
            .filter(p->state==0||(state==1)==p.isActive()).toList();
        activeCount.setText(all.stream().filter(Product::isActive).count()+" ACTIVE");
        lowCount.setText(all.stream().filter(p->p.isActive()&&p.getStock()<=5).count()+" LOW STOCK");
        photoCount.setText(all.stream().filter(p->p.getPhoto()==null||p.getDescription().isBlank()||p.getSpecifications().isBlank()).count()+" NEED CONTENT");
        table.setItems(FXCollections.observableArrayList(rows));count.setText(rows.size()+(rows.size()==1?" product · ":" products · ")+all.stream().filter(p->p.isActive()&&p.getStock()<=5).count()+" active products low on stock (5 or fewer)");
    }
}
