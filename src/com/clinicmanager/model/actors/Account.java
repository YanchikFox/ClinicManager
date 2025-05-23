package com.clinicmanager.model.actors;

import com.clinicmanager.model.enums.Role;

public class Account {
    private final int id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final int ownerId;

    public Account(int id, String email, String passwordHash, Role role, int ownerId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.ownerId = ownerId;
    }

    public int id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    } // ← ВОТ ЭТО ДОБАВЬ

    public Role role() {
        return role;
    }

    public int ownerId() {
        return ownerId;
    }

    public boolean validatePassword(String inputPassword) {
        // TODO: заменить на нормальную проверку хеша
        return inputPassword.equals(passwordHash);
    }
}
