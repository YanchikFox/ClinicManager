// package com.clinicmanager.service;
//
// import com.clinicmanager.exception.AuthenticationException;
// import com.clinicmanager.model.actors.Account;
// import com.clinicmanager.model.enums.Role;
// import com.clinicmanager.repository.AccountRepository;
// import com.clinicmanager.security.HashUtil;
// import com.clinicmanager.security.TokenService;
// import org.junit.jupiter.api.Test;
//
// import java.nio.charset.StandardCharsets;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
// import java.util.ArrayList;
// import java.util.List;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// class AccountManagerTest {
//
//    @Test
//    void loginShouldUpgradeLegacyPasswordHash() {
//        String email = "legacy@example.com";
//        String password = "Password!23";
//        String legacyHash = legacySha256(password);
//        Account legacyAccount = new Account(1, email, legacyHash, Role.PATIENT, 42, true);
//        InMemoryAccountRepository repository = new InMemoryAccountRepository(legacyAccount);
//        AccountManager manager = new AccountManager(repository, new TokenService());
//
//        String token = manager.login(email, password);
//
//        assertNotNull(token);
//        Account updated = repository.getAccount();
//        assertFalse(HashUtil.isLegacyHash(updated.passwordHash()));
//        assertTrue(HashUtil.verify(password, updated.passwordHash()));
//        assertNotEquals(legacyHash, updated.passwordHash());
//    }
//
//    @Test
//    void loginWithInvalidCredentialsThrowsException() {
//        Account legacyAccount = new Account(1, "user@example.com", legacySha256("secret"),
// Role.PATIENT, 1, true);
//        InMemoryAccountRepository repository = new InMemoryAccountRepository(legacyAccount);
//        AccountManager manager = new AccountManager(repository, new TokenService());
//
//        assertThrows(AuthenticationException.class, () -> manager.login("user@example.com",
// "wrong"));
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
//
//    private static final class InMemoryAccountRepository extends AccountRepository {
//        private Account account;
//
//        private InMemoryAccountRepository(Account account) {
//            super("jdbc:sqlite::memory:");
//            this.account = account;
//        }
//
//        @Override
//        public int save(Account acc) {
//            this.account = acc;
//            return acc.id();
//        }
//
//        @Override
//        public void delete(Account acc) {
//            this.account = null;
//        }
//
//        @Override
//        public void update(Account acc) {
//            this.account = acc;
//        }
//
//        @Override
//        public Account findById(int id) {
//            return account != null && account.id() == id ? account : null;
//        }
//
//        @Override
//        public List<Account> findAll() {
//            List<Account> result = new ArrayList<>();
//            if (account != null) {
//                result.add(account);
//            }
//            return result;
//        }
//
//        @Override
//        public Account findByEmail(String email) {
//            return account != null && account.email().equals(email) ? account : null;
//        }
//
//        private Account getAccount() {
//            return account;
//        }
//    }
// }
