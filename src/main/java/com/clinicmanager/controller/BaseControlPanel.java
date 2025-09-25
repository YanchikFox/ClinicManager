package com.clinicmanager.controller;

import com.clinicmanager.exception.InvalidTokenException;
import com.clinicmanager.service.AccountManager;

public abstract class BaseControlPanel {
    protected final String token;
    protected final AccountManager accountManager;
    protected BaseControlPanel(String token, AccountManager accountManager) {
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
