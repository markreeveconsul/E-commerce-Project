package edu.course.shop.service;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Standard JDK hashing. The demo account passwords are intentionally public. */
public final class Passwords {
    private Passwords() { }
    public static String hash(String password) {
        byte[] salt = new byte[16]; new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(derive(password, salt));
    }
    public static boolean matches(String password, String stored) {
        String[] parts = stored.split(":");
        return MessageDigest.isEqual(derive(password, Base64.getDecoder().decode(parts[0])), Base64.getDecoder().decode(parts[1]));
    }
    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (java.security.GeneralSecurityException e) { throw new IllegalStateException("Password hashing is unavailable.", e); }
        finally { spec.clearPassword(); }
    }
}