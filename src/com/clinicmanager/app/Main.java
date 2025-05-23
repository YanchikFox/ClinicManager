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

        // Инициализация БД
        DatabaseInitializer.initialize(dbUrl, "src/main/resources/schema.sql");
        RepositoryManager repos = new RepositoryManager(dbUrl);
        AccountManager accountManager = new AccountManager(repos.accounts, new TokenService());
        RegistrationService registrationService = new RegistrationService(repos.accounts, repos.doctors, repos.patients);
        Clinic clinic = new Clinic(accountManager);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Выберите действие: 1 - Регистрация, 2 - Вход");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.println("Регистрация: 1 - Врач, 2 - Пациент");
            String role = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Пароль: ");
            String password = scanner.nextLine();
            System.out.print("Имя: ");
            String name = scanner.nextLine();
            System.out.print("Дата рождения (YYYY-MM-DD): ");
            String dob = scanner.nextLine();
            System.out.print("Телефон: ");
            String phone = scanner.nextLine();

            try {
                if (role.equals("1")) {
                    System.out.print("Код лицензии: ");
                    String license = scanner.nextLine();
                    registrationService.registerDoctor(email, password, name, dob, phone, license);
                    System.out.println("✅ Врач зарегистрирован!");
                } else if (role.equals("2")) {
                    registrationService.registerPatient(email, password, name, dob, phone);
                    System.out.println("✅ Пациент зарегистрирован!");
                } else {
                    System.out.println("❌ Неизвестная роль");
                }
            } catch (RegistrationException e) {
                System.out.println("❌ Ошибка регистрации: " + e.getMessage());
            }
        }

        if (choice.equals("2")) {
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Пароль: ");
            String password = scanner.nextLine();

            try {
                BaseControlPanel panel = clinic.login(email, password);
                System.out.println("✅ Успешный вход. Токен активен!");
                panel.demoAction(); // проверка токена
                System.out.println("🎉 Контрольная панель запущена: " + panel.getClass().getSimpleName());
            } catch (Exception e) {
                System.out.println("❌ Ошибка входа: " + e.getMessage());
            }
        }

        repos.closeAll();
    }
}

