package com.clinicmanager.model.actors;

import com.clinicmanager.model.enums.Role;
import com.clinicmanager.security.HashUtil;

public class Account {
  private final int id;
  private final String email;
  private final String passwordHash;
  private final Role role;
  private final int ownerId;

  public Account(int id, String email, String rawPassword, Role role, int ownerId) {
    this(id, email, rawPassword, role, ownerId, false);
  }

  public Account(
      int id, String email, String passwordOrHash, Role role, int ownerId, boolean hashed) {
    this.id = id;
    this.email = email;
    this.passwordHash = hashed ? passwordOrHash : HashUtil.hash(passwordOrHash);
    this.role = role;
    this.ownerId = ownerId;
  }

  public boolean validatePassword(String rawPassword) {
    return HashUtil.verify(rawPassword, this.passwordHash);
  }

  public boolean requiresPasswordUpgrade() {
    return HashUtil.shouldRehash(this.passwordHash);
  }

  public Account withPasswordHash(String newHash) {
    return new Account(this.id, this.email, newHash, this.role, this.ownerId, true);
  }

  // Getters for the fields
  public int id() {
    return id;
  }

  public String email() {
    return email;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public Role role() {
    return role;
  }

  public int ownerId() {
    return ownerId;
  }
}
