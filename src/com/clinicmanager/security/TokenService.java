package com.clinicmanager.security;

import com.clinicmanager.model.actors.Account;
import java.util.*;

public class TokenService {
    private static final Map<String, Account> tokens = new HashMap<>();

    public static String issueToken(Account account) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, account);
        return token;
    }

    public static boolean isValid(String token) {
        return tokens.containsKey(token);
    }

    public static Account resolve(String token) {
        return tokens.get(token);
    }

    public static void revoke(String token) {
        tokens.remove(token);
    }
}
