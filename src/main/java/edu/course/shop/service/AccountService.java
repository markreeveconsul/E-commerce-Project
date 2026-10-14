package edu.course.shop.service;

import edu.course.shop.exception.*;
import edu.course.shop.model.*;
import edu.course.shop.persistence.DatabaseManager;
import java.sql.*;
import java.util.*;

/** Customer-owned saved items, profile and address book. No cart or order mutations. */
public final class AccountService {
    public record SavedAddress(long id, DeliveryAddress address, boolean preferred) {
        @Override public String toString() { return address.recipient()+" · "+address.street()+(preferred?" (default)":""); }
    }
    private final DatabaseManager database;
    public AccountService(DatabaseManager database) { this.database=database; }
    private void customer(Connection c, User user) throws SQLException {
        if(!(user instanceof Customer))throw new ValidationException("Sign in as a customer to manage your account.");
        try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM users WHERE id=? AND role='CUSTOMER'")) {
            p.setLong(1,user.getId());try(ResultSet r=p.executeQuery()) {if(!r.next())throw new ValidationException("Customer account no longer exists.");}
        }
    }
    public Set<Long> saved(User user) {
        try(Connection c=database.open()) {
            customer(c,user);Set<Long> ids=new HashSet<>();
            try(PreparedStatement p=c.prepareStatement("SELECT product_id FROM saved_items WHERE customer_id=?")) {
                p.setLong(1,user.getId());try(ResultSet r=p.executeQuery()){while(r.next())ids.add(r.getLong(1));}
            }return Set.copyOf(ids);
        }catch(SQLException e){throw new StorageException(e);}
    }
    public void saveItem(User user,long productId,boolean saved) {
        try(Connection c=database.open()) {
            customer(c,user);
            if(saved)try(PreparedStatement exists=c.prepareStatement("SELECT 1 FROM products WHERE id=? AND active=1")) {
                exists.setLong(1,productId);try(ResultSet r=exists.executeQuery()){if(!r.next())throw new ValidationException("This product is no longer available.");}
            }
            try(PreparedStatement p=c.prepareStatement(saved?"INSERT OR IGNORE INTO saved_items(customer_id,product_id) VALUES(?,?)":"DELETE FROM saved_items WHERE customer_id=? AND product_id=?")) {
                p.setLong(1,user.getId());p.setLong(2,productId);p.executeUpdate();
            }
        }catch(SQLException e){throw new StorageException(e);}
    }
    public String displayName(User user) {
        try(Connection c=database.open()) {
            customer(c,user);try(PreparedStatement p=c.prepareStatement("SELECT display_name FROM customer_profiles WHERE customer_id=?")) {
                p.setLong(1,user.getId());try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):user.getUsername();}
            }
        }catch(SQLException e){throw new StorageException(e);}
    }
    public void saveName(User user,String name) {
        if(name==null||name.isBlank()||name.trim().length()>80)throw new ValidationException("Enter a display name of 1–80 characters.");
        try(Connection c=database.open()) {
            customer(c,user);try(PreparedStatement p=c.prepareStatement("INSERT INTO customer_profiles(customer_id,display_name) VALUES(?,?) ON CONFLICT(customer_id) DO UPDATE SET display_name=excluded.display_name")) {
                p.setLong(1,user.getId());p.setString(2,name.trim());p.executeUpdate();
            }
        }catch(SQLException e){throw new StorageException(e);}
    }
    public List<SavedAddress> addresses(User user) {
        try(Connection c=database.open()) {
            customer(c,user);List<SavedAddress> rows=new ArrayList<>();
            try(PreparedStatement p=c.prepareStatement("SELECT * FROM customer_addresses WHERE customer_id=? ORDER BY preferred DESC,id")) {
                p.setLong(1,user.getId());try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new SavedAddress(r.getLong("id"),new DeliveryAddress(r.getString("recipient"),r.getString("street"),r.getString("city"),r.getString("postal")),r.getBoolean("preferred")));}
            }return List.copyOf(rows);
        }catch(SQLException e){throw new StorageException(e);}
    }
    public void saveAddress(User user,long id,DeliveryAddress address,boolean preferred) {
        if(address==null)throw new ValidationException("Enter delivery details.");
        try(Connection c=database.open()) {
            customer(c,user);c.setAutoCommit(false);
            try {
                int count;try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM customer_addresses WHERE customer_id=?")) {
                    p.setLong(1,user.getId());try(ResultSet r=p.executeQuery()){r.next();count=r.getInt(1);}
                }
                if(id==0&&count>=5)throw new ValidationException("Save at most five addresses.");
                if(preferred)try(PreparedStatement p=c.prepareStatement("UPDATE customer_addresses SET preferred=0 WHERE customer_id=?")){p.setLong(1,user.getId());p.executeUpdate();}
                String sql=id==0?"INSERT INTO customer_addresses(recipient,street,city,postal,preferred,customer_id) VALUES(?,?,?,?,?,?)":"UPDATE customer_addresses SET recipient=?,street=?,city=?,postal=?,preferred=? WHERE customer_id=? AND id=?";
                try(PreparedStatement p=c.prepareStatement(sql)) {
                    p.setString(1,address.recipient());p.setString(2,address.street());p.setString(3,address.city());p.setString(4,address.postalCode());p.setBoolean(5,preferred||count==0);p.setLong(6,user.getId());if(id!=0)p.setLong(7,id);
                    if(p.executeUpdate()!=1)throw new ValidationException("Address no longer exists.");
                }c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }catch(SQLException e){throw new StorageException(e);}
    }
    public void removeAddress(User user,long id) {
        try(Connection c=database.open()) {
            customer(c,user);try(PreparedStatement p=c.prepareStatement("DELETE FROM customer_addresses WHERE customer_id=? AND id=?")) {
                p.setLong(1,user.getId());p.setLong(2,id);if(p.executeUpdate()!=1)throw new ValidationException("Address no longer exists.");
            }
        }catch(SQLException e){throw new StorageException(e);}
    }
}
