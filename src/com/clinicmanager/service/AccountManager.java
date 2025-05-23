package com.clinicmanager.service;

import com.clinicmanager.repository.AccountRepository;
import com.clinicmanager.security.TokenService;
import com.clinicmanager.model.actors.Account;
public class AccountManager {
    final AccountRepository accountRepository;
    public AccountManager(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
}

    public String generateAccessToken(Account account) {
        // Например, JWT или UUID + hash(accountId + secret)
        return TokenService.issueToken(account);
    }

    boolean validateToken(String token) {
        return TokenService.isValid(token);
    }

    public Account login(String email, String password) {
        Account acc = accountRepository.findByEmail(email);
        if (acc != null && acc.validatePassword(password)) {
            return acc;
        }
        return null;
    }
}

