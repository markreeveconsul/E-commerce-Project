package edu.course.shop.ui;
import edu.course.shop.model.Category;
import java.util.EnumMap;
import java.util.Locale;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.scene.effect.DropShadow;

/** Shared category campaigns; the Shop all hero remains a separate component. */
public final class CategoryBanner extends Pane {
    private record Campaign(String headline,String support,String image,double rotation) {}
    private record Artwork(Image image,double left,double top,double width,double height) {}
    private static final EnumMap<Category,Artwork> ART=new EnumMap<>(Category.class);
    private final Canvas canvas=new Canvas();
    private final Label eyebrow=Ui.label("","category-banner-eyebrow");
    private final Label headline=Ui.label("","category-banner-headline");
    private final Label support=Ui.label("","category-banner-support");
    private final Label pending=Ui.label("","category-banner-pending");
    private final VBox copy=new VBox(8,eyebrow,headline,support);
    private Category category;
    private Campaign campaign;

    public CategoryBanner(){
        setId("category-banner");setMinWidth(0);setPrefWidth(0);canvas.setMouseTransparent(true);copy.setMouseTransparent(true);pending.setMouseTransparent(true);
        getChildren().addAll(canvas,copy,pending);
        widthProperty().addListener((o,a,b)->{double height=b.doubleValue()<1200?242:260;setMinHeight(height);setPrefHeight(height);setMaxHeight(height);});
        setMinHeight(260);setPrefHeight(260);setMaxHeight(260);
        javafx.scene.shape.Rectangle clip=new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(widthProperty());clip.heightProperty().bind(heightProperty());setClip(clip);
    }
    public void showCategory(Category value){
        category=value;
        campaign=switch(value){
            case PC_SETS -> new Campaign("YOUR\nNEXT SETUP.","Built for work. Ready for play.","category-pc.png",0);
            case PC_PARTS -> new Campaign("BUILD YOUR\nADVANTAGE.","The parts behind your next upgrade.","category-gpu.png",50);
            case LAPTOPS -> new Campaign("POWER\nTHAT MOVES.","Performance, wherever you go.","category-laptop.png",0);
            case PHONES_TABLETS -> new Campaign("STAY\nCONNECTED.","Everyday essentials. Extraordinary possibilities.","category-phones.png",0);
            case PERIPHERALS -> new Campaign("MAKE IT\nYOURS.","The finishing touches for your setup.","category-keyboard.png",0);
            case ELECTRONICS -> new Campaign("DISCOVER\nWHAT'S NEXT.","More possibilities for your everyday.","category-controller.png",0);
            default -> throw new IllegalArgumentException("Unsupported category");
        };
        eyebrow.setText(value.label().toUpperCase(Locale.ROOT));headline.setText(campaign.headline());support.setText(campaign.support());
        pending.setText(value==Category.PC_SETS?"PC set model\nto be selected":"Electronics model\nto be selected");
        pending.setVisible(campaign.image()==null);
        if(campaign.image()!=null)ART.computeIfAbsent(value,c->load(campaign));
        setAccessibleText(value.label()+". "+campaign.headline().replace('\n',' ')+". "+campaign.support());
        requestLayout();
    }
    /** Measure visible pixels after rotation, avoiding empty margins around supplied cutouts. */
    private static Artwork load(Campaign campaign){
        Image image=new Image(java.util.Objects.requireNonNull(CategoryBanner.class.getResource("/images/"+campaign.image())).toExternalForm());
        if(image.isError())throw new IllegalStateException("Category artwork could not load",image.getException());
        double rad=Math.toRadians(campaign.rotation()),cos=Math.cos(rad),sin=Math.sin(rad);
        double left=Double.MAX_VALUE,top=Double.MAX_VALUE,right=-Double.MAX_VALUE,bottom=-Double.MAX_VALUE;
        var pixels=image.getPixelReader();
        for(int y=0;y<(int)image.getHeight();y++)for(int x=0;x<(int)image.getWidth();x++){
            if((pixels.getArgb(x,y)>>>24)<32)continue;
            double px=x*cos-y*sin,py=x*sin+y*cos;
            left=Math.min(left,px);right=Math.max(right,px);top=Math.min(top,py);bottom=Math.max(bottom,py);
        }
        if(right<left)return new Artwork(image,0,0,image.getWidth(),image.getHeight());
        return new Artwork(image,left,top,right-left+1,bottom-top+1);
    }
    @Override protected void layoutChildren(){
        double w=getWidth(),h=getHeight(),nav=w<1200?52:0,body=h-nav;
        canvas.setWidth(w);canvas.setHeight(h);var g=canvas.getGraphicsContext2D();
        g.setFill(Color.web("#111214"));g.fillRect(0,0,w,h);
        if(campaign==null)return;
        double cx=w*.78,cy=nav+body*.5;
        g.setFill(new RadialGradient(0,0,cx,cy,body*1.2,false,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#8A3817",.72)),new Stop(.5,Color.web("#492516",.45)),new Stop(1,Color.TRANSPARENT)));
        g.fillRect(w*.43,nav,w*.57,body);
        g.setStroke(Color.web("#E96020",.55));g.setLineWidth(.8);
        for(double r:new double[]{body*.62,body*.83,body*1.08})g.strokeOval(cx-r,cy-r,r*2,r*2);
        g.setStroke(Color.web("#E96020",.30));
        for(int i=0;i<5;i++){double x=w*.44+i*body*.31;g.strokeLine(x,h,x+body*.85,nav);}
        if(nav>0){g.setFill(Color.web("#111214"));g.fillRect(0,0,w,nav);}
        double left=w*.045,copyWidth=w*.45;
        headline.setStyle("-fx-font-size: "+(w<1200?32:52)+"px;");
        copy.resize(copyWidth,copy.prefHeight(copyWidth));copy.relocate(left,nav+(body-copy.getHeight())/2);
        double artLeft=w*.52,artWidth=w*.45;
        if(campaign.image()!=null){
            Artwork art=ART.get(category);double scale=Math.min(artWidth/art.width(),(body-12)/art.height());
            g.save();g.translate(artLeft+artWidth/2,cy+3);g.scale(scale,scale);
            g.translate(-(art.left()+art.width()/2),-(art.top()+art.height()/2));g.rotate(campaign.rotation());
            g.setEffect(new DropShadow(18,0,10,Color.web("#000000",.7)));g.drawImage(art.image(),0,0);g.restore();
        }else{
            double bw=artWidth*.74,bh=body*.74,x=artLeft+(artWidth-bw)/2,y=nav+(body-bh)/2;
            g.setStroke(Color.web("#919191",.6));g.setLineDashes(6,5);g.strokeRoundRect(x,y,bw,bh,10,10);g.setLineDashes();
            pending.resizeRelocate(x+12,y,bw-24,bh);
        }
    }
}
