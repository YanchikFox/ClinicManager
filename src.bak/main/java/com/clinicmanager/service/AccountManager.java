package com.clinicmanager.service;

import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.AccountRepository;
import com.clinicmanager.security.TokenService;
import com.clinicmanager.exception.AuthenticationException;

public class AccountManager {
    private final AccountRepository accountRepository;
    private final TokenService tokenService;

    public AccountManager(AccountRepository repo, TokenService tokenService) {
        this.accountRepository = repo;
        this.tokenService = tokenService;
    }

    public String login(String email, String password) {
        Account acc = accountRepository.findByEmail(email);
        if (acc == null || !acc.validatePassword(password)) {
            throw new AuthenticationException("Invalid credentials");
        }
        return tokenService.generateToken(acc);
    }

    public boolean validateToken(String token) {
        return tokenService.isValid(token);
    }

    public Account getAccountByToken(String token) {
        return tokenService.getAccount(token);
    }

    public void revokeToken(String token) {
        tokenService.revoke(token);
    }

}
