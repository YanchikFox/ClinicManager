package com.clinicmanager.app;

import com.clinicmanager.app.Clinic;
import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.exception.RegistrationException;
import com.clinicmanager.repository.*;
import com.clinicmanager.security.TokenService;
import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.RegistrationService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String dbUrl = "jdbc:sqlite:clinic.db";

        // Database initialization
        DatabaseInitializer.initialize(dbUrl, "src/main/resources/schema.sql");
        RepositoryManager repos = new RepositoryManager(dbUrl);
        AccountManager accountManager = new AccountManager(repos.accounts, new TokenService());
        RegistrationService registrationService = new RegistrationService(repos.accounts, repos.doctors, repos.patients);
        Clinic clinic = new Clinic(accountManager);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose an action: 1 - Register, 2 - Login");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.println("Register as: 1 - Doctor, 2 - Patient");
            String role = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Date of birth (YYYY-MM-DD): ");
            String dob = scanner.nextLine();
            System.out.print("Phone: ");
            String phone = scanner.nextLine();

            try {
                if (role.equals("1")) {
                    System.out.print("License code: ");
                    String license = scanner.nextLine();
                    registrationService.registerDoctor(email, password, name, dob, phone, license);
                    System.out.println("✅ Doctor registered!");
                } else if (role.equals("2")) {
                    registrationService.registerPatient(email, password, name, dob, phone);
                    System.out.println("✅ Patient registered!");
                } else {
                    System.out.println("❌ Unknown role");
                }
            } catch (RegistrationException e) {
                System.out.println("❌ Registration error: " + e.getMessage());
            }
        }

        if (choice.equals("2")) {
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            try {
                BaseControlPanel panel = clinic.login(email, password);
                System.out.println("✅ Login successful. Token is active!");
                panel.demoAction(); // token check
                System.out.println("🎉 Control panel started: " + panel.getClass().getSimpleName());
            } catch (Exception e) {
                System.out.println("❌ Login error: " + e.getMessage());
            }
        }

        repos.closeAll();
    }
}

