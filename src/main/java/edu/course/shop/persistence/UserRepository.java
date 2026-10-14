package edu.course.shop.persistence;
import edu.course.shop.model.*;
import java.sql.*;
import java.util.Optional;

public final class UserRepository {
    public record Credentials(User user, String passwordHash) { }
    public Optional<Credentials> find(Connection c, String username) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            p.setString(1, username);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();
                User user = r.getString("role").equals("ADMIN") ? new Admin(r.getLong("id"), r.getString("username")) : new Customer(r.getLong("id"), r.getString("username"));
                return Optional.of(new Credentials(user, r.getString("password_hash")));
            }
        }
    }
}