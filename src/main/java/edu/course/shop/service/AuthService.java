package edu.course.shop.service;
import edu.course.shop.model.User;
import edu.course.shop.persistence.*;
import edu.course.shop.exception.*;
import java.sql.*;

public final class AuthService {
    private final DatabaseManager database;
    private final UserRepository users;
    public AuthService(DatabaseManager database, UserRepository users) { this.database = database; this.users = users; }
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw new ValidationException("Username and password are required.");
        try (Connection c = database.open()) {
            var credentials = users.find(c, username.trim()).orElseThrow(() -> new ValidationException("Incorrect username or password."));
            if (!Passwords.matches(password, credentials.passwordHash())) throw new ValidationException("Incorrect username or password.");
            return credentials.user();
        } catch (SQLException e) { throw new StorageException(e); }
    }
}