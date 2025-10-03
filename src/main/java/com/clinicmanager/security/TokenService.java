package com.clinicmanager.security;

import com.clinicmanager.model.actors.Account;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenService {
  private final Map<String, Account> tokens = new HashMap<>();

  public String generateToken(Account account) {
    String token = UUID.randomUUID().toString();
    tokens.put(token, account);
    return token;
  }

  public boolean isValid(String token) {
    return tokens.containsKey(token);
  }

  public Account getAccount(String token) {
    return tokens.get(token);
  }

  public void revoke(String token) {
    tokens.remove(token);
  }
}
