package com.walletwise.util;

import com.walletwise.dao.Database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

public class PinService {

    public static boolean isEnabled() {
        return getStored() != null;
    }

    public static void setPin(String pin) {
        save("pin_hash", hash(pin));
    }

    public static void removePin() {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM settings WHERE key = 'pin_hash'")) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean verify(String pin) {
        String stored = getStored();
        if (stored == null) {
            return true;
        }
        return stored.equals(hash(pin));
    }

    private static String getStored() {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT value FROM settings WHERE key = 'pin_hash'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String v = rs.getString("value");
                rs.close();
                return v;
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void save(String key, String value) {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO settings(key, value) VALUES(?, ?) " +
                             "ON CONFLICT(key) DO UPDATE SET value = excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String hash(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}