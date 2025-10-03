package com.clinicmanager.service;

import com.clinicmanager.exception.AuthenticationException;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.AccountRepository;
import com.clinicmanager.security.HashUtil;
import com.clinicmanager.security.TokenService;

public class AccountManager implements AccountService {
    private final AccountRepository accountRepository;
    private final TokenService tokenService;

    public AccountManager(AccountRepository repo, TokenService tokenService) {
        this.accountRepository = repo;
        this.tokenService = tokenService;
    }

    @Override
    public String login(String email, String password) {
        Account acc = accountRepository.findByEmail(email);
        if (acc == null || !acc.validatePassword(password)) {
            throw new AuthenticationException("Invalid credentials");
        }
        if (acc.requiresPasswordUpgrade()) {
            Account upgraded = acc.withPasswordHash(HashUtil.hash(password));
            accountRepository.update(upgraded);
            acc = upgraded;
        }
        return tokenService.generateToken(acc);
    }

    @Override
    public boolean validateToken(String token) {
        return tokenService.isValid(token);
    }

    @Override
    public Account getAccountByToken(String token) {
        return tokenService.getAccount(token);
    }

    @Override
    public void revokeToken(String token) {
        tokenService.revoke(token);
    }

}
