package edu.course.shop.model;
import edu.course.shop.exception.ValidationException;
import java.math.BigDecimal;

/** Validated immutable snapshot; updates replace it through ProductService. */
public final class Product {
    private final long id;
    private final String name;
    private final Category category;
    private final BigDecimal price;
    private final int stock;
    private final boolean active;
    private final String description;
    private final byte[] photo;
    private final java.util.List<byte[]> additionalPhotos;
    private final String specifications;
    private final PhotoFrame photoFrame;
    public PhotoFrame getPhotoFrame() { return photoFrame; }
    public Product(long id, String name, Category category, BigDecimal price, int stock, boolean active) {
        this(id, name, category, price, stock, active, "", null);
    }
    public Product(long id, String name, Category category, BigDecimal price, int stock, boolean active, String description, byte[] photo) {
        this(id,name,category,price,stock,active,description,photo,java.util.List.of(),"");
    }
    public Product(long id,String name,Category category,BigDecimal price,int stock,boolean active,String description,
                   byte[] photo,java.util.List<byte[]> additionalPhotos,String specifications) {
        this(id,name,category,price,stock,active,description,photo,additionalPhotos,specifications,PhotoFrame.AUTO);
    }
    public Product(long id,String name,Category category,BigDecimal price,int stock,boolean active,String description,
                   byte[] photo,java.util.List<byte[]> additionalPhotos,String specifications,PhotoFrame frame) {
        if(frame==null)throw new ValidationException("Choose photo framing.");
        this.photoFrame=frame;
        if(additionalPhotos==null||additionalPhotos.size()>4)throw new ValidationException("Add at most four additional photos.");
        if(photo==null&&!additionalPhotos.isEmpty())throw new ValidationException("Choose a main photo before adding gallery photos.");
        if(specifications==null||specifications.length()>1000)throw new ValidationException("Specifications must be at most 1,000 characters.");
        this.additionalPhotos=additionalPhotos.stream().map(bytes->{
            if(bytes==null||bytes.length==0||bytes.length>5*1024*1024)throw new ValidationException("Choose a nonempty gallery photo smaller than 5 MB.");
            return bytes.clone();
        }).toList();
        this.specifications=specifications.trim();
        if (description == null || description.length() > 1000) throw new ValidationException("Description must be at most 1,000 characters.");
        if (photo != null && photo.length > 5 * 1024 * 1024) throw new ValidationException("Photo must be smaller than 5 MB.");
        this.description = description.trim(); this.photo = photo == null ? null : photo.clone();
        if (name == null || name.isBlank() || name.trim().length() > 80)
            throw new ValidationException("Product name is required and must be at most 80 characters.");
        if (category == null) throw new ValidationException("Choose a category.");
        if (stock < 0 || stock > 100000) throw new ValidationException("Stock must be between 0 and 100,000.");
        this.id = id; this.name = name.trim(); this.category = category;
        this.price = Money.price(price); this.stock = stock; this.active = active;
    }
    public String getDescription() { return description; }
    public byte[] getPhoto() { return photo == null ? null : photo.clone(); }
    public java.util.List<byte[]> getAdditionalPhotos() { return additionalPhotos.stream().map(byte[]::clone).toList(); }
    public String getSpecifications() { return specifications; }
    public long getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isActive() { return active; }
}