package edu.course.shop;
import edu.course.shop.exception.*;
import edu.course.shop.model.*;
import edu.course.shop.persistence.*;
import edu.course.shop.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AccountAndRepeatTest {
    @TempDir Path temp;
    DatabaseManager db;AccountService accounts;ProductService products;CartService carts;User customer,admin,other;
    @BeforeEach void setup() throws Exception {
        db=new DatabaseManager(temp.resolve("account.db"));db.initialize();AuthService auth=new AuthService(db,new UserRepository());
        customer=auth.login("customer","customer123");admin=auth.login("admin","admin123");accounts=auth.accounts();products=new ProductService(db,new ProductRepository());carts=new CartService(products);
        try(Connection c=db.open();PreparedStatement p=c.prepareStatement("INSERT INTO users(id,username,password_hash,role) VALUES(99,'other',?,'CUSTOMER')")){p.setString(1,Passwords.hash("test"));p.executeUpdate();}other=new Customer(99,"other");
    }
    @Test void savedItemsAreIdempotentPrivateAndPersistent() {
        accounts.saveItem(customer,1,true);accounts.saveItem(customer,1,true);assertEquals(Set.of(1L),accounts.saved(customer));assertTrue(accounts.saved(other).isEmpty());
        db.initialize();assertEquals(Set.of(1L),new AccountService(new DatabaseManager(temp.resolve("account.db"))).saved(customer));
        accounts.saveItem(other,1,false);assertEquals(Set.of(1L),accounts.saved(customer));accounts.saveItem(customer,1,false);assertTrue(accounts.saved(customer).isEmpty());
    }
    @Test void accountOperationsRejectGuestsAdminsAndUnknownCustomers() {
        for(User denied:List.of(new Guest(),admin,new Customer(admin.getId(),"fake"),new Customer(999,"missing"))) {
            assertThrows(ValidationException.class,()->accounts.saved(denied));assertThrows(ValidationException.class,()->accounts.saveItem(denied,1,true));assertThrows(ValidationException.class,()->accounts.saveName(denied,"Changed"));assertThrows(ValidationException.class,()->accounts.addresses(denied));
        }
    }
    @Test void unavailableProductsCannotBeSavedButCanBeRemoved() {
        accounts.saveItem(customer,1,true);Product p=products.get(1);products.save(admin,p.getId(),p.getName(),p.getCategory(),p.getPrice(),p.getStock(),false);
        assertThrows(ValidationException.class,()->accounts.saveItem(customer,1,true));assertThrows(ValidationException.class,()->accounts.saveItem(customer,999,true));accounts.saveItem(customer,1,false);assertTrue(accounts.saved(customer).isEmpty());
    }
    private DeliveryAddress address(String street){return new DeliveryAddress("Test Recipient",street,"Test City","12345");}
    @Test void profileAndAddressBookPersistWithSingleDefault() {
        accounts.saveName(customer,"  Shopper  ");accounts.saveAddress(customer,0,address("123 First Street"),true);accounts.saveAddress(customer,0,address("456 Second Street"),true);
        db.initialize();assertEquals("Shopper",accounts.displayName(customer));var rows=accounts.addresses(customer);assertEquals(2,rows.size());assertEquals(1,rows.stream().filter(AccountService.SavedAddress::preferred).count());assertEquals("456 Second Street",rows.getFirst().address().street());assertTrue(accounts.addresses(other).isEmpty());
    }
    @Test void addressOwnershipFailureRollsBackDefaultChange() {
        accounts.saveAddress(customer,0,address("123 Owned Street"),true);accounts.saveAddress(other,0,address("456 Other Street"),true);long foreign=accounts.addresses(other).getFirst().id();
        assertThrows(ValidationException.class,()->accounts.saveAddress(customer,foreign,address("789 Attempt Street"),true));assertTrue(accounts.addresses(customer).getFirst().preferred());assertEquals("456 Other Street",accounts.addresses(other).getFirst().address().street());
        assertThrows(ValidationException.class,()->accounts.removeAddress(customer,foreign));assertEquals(1,accounts.addresses(other).size());
    }
    @Test void failedAddressWriteRollsBackAndLimitsAreValidated() throws Exception {
        accounts.saveAddress(customer,0,address("123 Main Street"),true);
        try(Connection c=db.open();Statement s=c.createStatement()){s.executeUpdate("CREATE TRIGGER reject_address BEFORE INSERT ON customer_addresses BEGIN SELECT RAISE(ABORT,'fixture'); END");}
        assertThrows(StorageException.class,()->accounts.saveAddress(customer,0,address("456 Other Street"),true));assertTrue(accounts.addresses(customer).getFirst().preferred());
        try(Connection c=db.open();Statement s=c.createStatement()){s.executeUpdate("DROP TRIGGER reject_address");}
        for(int i=0;i<4;i++)accounts.saveAddress(customer,0,address("Other street "+i),false);
        assertThrows(ValidationException.class,()->accounts.saveAddress(customer,0,address("Sixth street"),false));assertEquals(5,accounts.addresses(customer).size());
        assertThrows(ValidationException.class,()->accounts.saveName(customer," "));assertThrows(ValidationException.class,()->accounts.saveName(customer,"x".repeat(81)));assertThrows(ValidationException.class,()->accounts.saveAddress(customer,0,null,false));
    }
    @Test void framingPersistsAndLegacyEditsRetainIt() {
        Product p=products.get(1);PhotoFrame frame=new PhotoFrame(PhotoFrame.Mode.COVER,.2,.8);
        products.save(admin,p.getId(),p.getName(),p.getCategory(),p.getPrice(),p.getStock(),true,p.getDescription(),null,List.of(),"",frame);db.initialize();assertEquals(frame,products.get(1).getPhotoFrame());
        products.save(admin,p.getId(),p.getName(),p.getCategory(),p.getPrice(),5,true);assertEquals(frame,products.get(1).getPhotoFrame());
        assertThrows(ValidationException.class,()->new PhotoFrame(PhotoFrame.Mode.COVER,Double.NaN,.5));assertThrows(ValidationException.class,()->new PhotoFrame(PhotoFrame.Mode.COVER,2,.5));
    }
    private Order order(User owner){return new Order(1,owner.getId(),"2026-10-14T00:00:00Z","123 Test Street","Cash on delivery","PENDING",new BigDecimal("72.80"),List.of(new OrderItem(1,"Old headphones",new BigDecimal("59.90"),1),new OrderItem(4,"Old tote",new BigDecimal("12.90"),1)));}
    @Test void repeatPlanUsesCurrentPricesAndExplainsUnavailableLines() {
        Product p=products.get(1),tote=products.get(4);products.save(admin,1,p.getName(),p.getCategory(),new BigDecimal("65.00"),p.getStock(),true);products.save(admin,4,tote.getName(),tote.getCategory(),tote.getPrice(),0,true);
        Cart cart=new Cart(customer.getId());var plan=carts.prepareRepeat(customer,cart,order(customer));assertEquals(1,plan.lines().size());assertEquals(new BigDecimal("65.00"),plan.lines().getFirst().product().getPrice());assertEquals(1,plan.unavailable().size());assertTrue(cart.getItems().isEmpty());carts.applyRepeat(customer,cart,plan);assertEquals(new BigDecimal("65.00"),cart.getTotal());
        assertThrows(ValidationException.class,()->carts.prepareRepeat(customer,cart,order(other)));
    }
    @Test void repeatCommitIsAtomicWhenLaterLineStockOrPriceChanges() {
        Cart cart=new Cart(customer.getId());carts.add(customer,cart,3,1);var before=cart.getItems();var plan=carts.prepareRepeat(customer,cart,order(customer));
        Product tote=products.get(4);products.save(admin,4,tote.getName(),tote.getCategory(),tote.getPrice(),0,true);
        assertThrows(OutOfStockException.class,()->carts.applyRepeat(customer,cart,plan));assertEquals(before,cart.getItems());
        products.save(admin,4,tote.getName(),tote.getCategory(),new BigDecimal("20.00"),20,true);assertThrows(ValidationException.class,()->carts.applyRepeat(customer,cart,plan));assertEquals(before,cart.getItems());
    }
}
