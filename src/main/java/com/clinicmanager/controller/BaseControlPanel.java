package com.clinicmanager.controller;

import com.clinicmanager.exception.InvalidTokenException;
import com.clinicmanager.service.AccountService;

public abstract class BaseControlPanel {
  protected final String token;
  protected final AccountService accountManager;

  protected BaseControlPanel(String token, AccountService accountManager) {
    this.token = token;
    this.accountManager = accountManager;
  }

  protected void requireValidToken() {
    if (!accountManager.validateToken(token)) {
      throw new InvalidTokenException("Access denied: invalid token");
    }
  }

  public void revokeToken() {
    accountManager.revokeToken(token);
  }

  public abstract Object currentPerson();
}
