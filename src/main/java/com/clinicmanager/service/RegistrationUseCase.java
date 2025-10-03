package com.clinicmanager.service;

public interface RegistrationUseCase {
  void registerDoctor(
      String email,
      String rawPassword,
      String name,
      String dateOfBirth,
      String phone,
      String licenseCode);

  void registerPatient(
      String email, String rawPassword, String name, String dateOfBirth, String phone);
}
