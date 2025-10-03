package com.clinicmanager.security;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public final class HashUtil {
  private static final String CONFIG_FILE = "/security.properties";
  private static final String COST_PROPERTY = "hash.cost";
  private static final int DEFAULT_COST = 10;
  private static final int MIN_COST = 4;
  private static final int MAX_COST = 31;
  private static final Pattern HEX_64 = Pattern.compile("^[a-fA-F0-9]{64}$");
  private static final Pattern BCRYPT_PATTERN =
      Pattern.compile("^\\$2[abxy]?\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

  private static final int COST = loadCost();

  private HashUtil() {}

  private static int loadCost() {
    try (InputStream input = HashUtil.class.getResourceAsStream(CONFIG_FILE)) {
      if (input == null) {
        return DEFAULT_COST;
      }
      Properties properties = new Properties();
      properties.load(input);
      String value = properties.getProperty(COST_PROPERTY);
      if (value == null) {
        return DEFAULT_COST;
      }
      int parsed = Integer.parseInt(value.trim());
      if (parsed < MIN_COST || parsed > MAX_COST) {
        return DEFAULT_COST;
      }
      return parsed;
    } catch (IOException | NumberFormatException e) {
      return DEFAULT_COST;
    }
  }

  public static String hash(String rawPassword) {
    if (rawPassword == null) {
      throw new IllegalArgumentException("Password cannot be null");
    }
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt(COST));
  }

  public static boolean verify(String rawPassword, String storedHash) {
    if (rawPassword == null || storedHash == null) {
      return false;
    }
    if (isBcryptHash(storedHash)) {
      try {
        return BCrypt.checkpw(rawPassword, storedHash);
      } catch (IllegalArgumentException ex) {
        return false;
      }
    }
    if (isLegacySha256Hash(storedHash)) {
      return sha256(rawPassword).equalsIgnoreCase(storedHash);
    }
    return false;
  }

  public static boolean shouldRehash(String storedHash) {
    if (!isBcryptHash(storedHash)) {
      return true;
    }
    return extractCost(storedHash) != COST;
  }

  public static boolean isLegacyHash(String storedHash) {
    return isLegacySha256Hash(storedHash);
  }

  private static boolean isBcryptHash(String hash) {
    return hash != null && BCRYPT_PATTERN.matcher(hash).matches();
  }

  private static boolean isLegacySha256Hash(String hash) {
    return hash != null && HEX_64.matcher(hash).matches();
  }

  private static int extractCost(String hash) {
    if (!isBcryptHash(hash)) {
      return -1;
    }
    return Integer.parseInt(hash.substring(4, 6));
  }

  private static String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hashBytes) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not supported", e);
    }
  }
}
