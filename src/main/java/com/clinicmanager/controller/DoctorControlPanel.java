package com.clinicmanager.controller;

import com.clinicmanager.model.entities.Schedule;
import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.RepositoryManager;

public class DoctorControlPanel extends BaseControlPanel {
    private Doctor doctor;

    public DoctorControlPanel(String token, AccountManager accountManager, NotificationManager notificationManager) {
        super(token, accountManager, notificationManager);
        // Получаем аккаунт по токену
        Account acc = accountManager.getAccountByToken(token);
        // Получаем доктора по ownerId
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        this.doctor = repos.doctors.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        requireValidToken();
        requireDoctorRole();
        return doctor;
    }

    public Schedule viewMySchedule() {
        requireValidToken();
        requireDoctorRole();
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        return repos.schedules.findByDoctorId(doctor.id());
    }

    // Пример защищённого метода: получить список пациентов
    public java.util.List<Integer> getPatientsList() {
        requireValidToken();
        requireDoctorRole();
        return doctor.getPatientsList();
    }

    // Пример защищённого метода: добавить слот
    public void addAvailableSlot(com.clinicmanager.model.entities.Slot slot) {
        requireValidToken();
        requireDoctorRole();
        doctor.addAvailableSlot(slot);
    }

    // Пример защищённого метода: завершить приём
    public void endAppointment(com.clinicmanager.model.entities.Appointment appointment) {
        requireValidToken();
        requireDoctorRole();
        appointment.end(com.clinicmanager.gui.AppContext.getRepositories().appointments);
    }

    private void requireDoctorRole() {
        if (!(accountManager.getAccountByToken(token).role().name().equals("DOCTOR"))) {
            throw new com.clinicmanager.exception.InvalidTokenException("Access denied: not a doctor");
        }
    }

    // ...другие методы
}

