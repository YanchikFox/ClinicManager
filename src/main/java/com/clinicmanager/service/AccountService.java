package com.clinicmanager.service;

import com.clinicmanager.model.actors.Account;

public interface AccountService {
    String login(String email, String password);

    boolean validateToken(String token);

    Account getAccountByToken(String token);

    void revokeToken(String token);
}
