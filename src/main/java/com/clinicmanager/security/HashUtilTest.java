//package com.clinicmanager.security;
//
//import org.junit.jupiter.api.Test;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class HashUtilTest {
//
//    @Test
//    void hashShouldVerifyWithCorrectPassword() {
//        String password = "S3cureP@ss";
//        String hash = HashUtil.hash(password);
//
//        assertTrue(HashUtil.verify(password, hash));
//        assertFalse(HashUtil.verify("wrong", hash));
//    }
//
//    @Test
//    void hashShouldUseDifferentSaltEachTime() {
//        String password = "RepeatPassword";
//        String hash1 = HashUtil.hash(password);
//        String hash2 = HashUtil.hash(password);
//
//        assertNotEquals(hash1, hash2);
//    }
//
//    @Test
//    void verifyShouldAcceptLegacySha256Hashes() {
//        String password = "legacy";
//        String legacyHash = legacySha256(password);
//
//        assertTrue(HashUtil.verify(password, legacyHash));
//        assertTrue(HashUtil.isLegacyHash(legacyHash));
//        assertTrue(HashUtil.shouldRehash(legacyHash));
//    }
//
//    private static String legacySha256(String input) {
//        try {
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
//            StringBuilder hex = new StringBuilder();
//            for (byte b : hashBytes) {
//                hex.append(String.format("%02x", b));
//            }
//            return hex.toString();
//        } catch (NoSuchAlgorithmException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//}
