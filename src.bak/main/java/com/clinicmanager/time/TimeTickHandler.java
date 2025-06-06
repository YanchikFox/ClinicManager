package com.clinicmanager.time;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.enums.AppointmentStatus;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.service.NotificationManager;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimeTickHandler {
    public static void handleTimeTick(LocalDateTime now) {
        RepositoryManager repos = AppContext.getRepositories();
        NotificationManager notificationManager = new NotificationManager(repos.notifications);

        // 1. Usuwanie starych slotów
        List<Slot> slots = repos.slots.findAll();
        for (Slot slot : slots) {
            LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
            if (slotEnd.isBefore(now)) {
                repos.slots.delete(slot);
            }
        }

        // 2. Zakończenie przeterminowanych wizyt (Appointment)
        List<Appointment> appointments = repos.appointments.findAll();
        for (Appointment app : appointments) {
            Slot slot = app.getSlot();
            if (slot == null) continue;
            LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
            if (!app.status().equals(AppointmentStatus.ENDED) && !app.status().equals(AppointmentStatus.CANCELLED)) {
                if (slotEnd.isBefore(now)) {
                    app.end(repos.appointments);
                }
            }
        }

        // 3. Powiadomienia na 10 minut przed wizytą (tylko CONFIRMED/PENDING)
        for (Appointment app : appointments) {
            Slot slot = app.getSlot();
            if (slot == null) continue;
            LocalDateTime slotStart = LocalDateTime.of(slot.date(), slot.timeRange().start());
            if (!app.status().equals(AppointmentStatus.ENDED) && !app.status().equals(AppointmentStatus.CANCELLED)) {
                if (slotStart.minusMinutes(10).equals(now)) {
                    // Powiadom lekarza i pacjenta
                    notificationManager.createNotification(app.doctorId(), "Za 10 minut rozpocznie się przyjęcie.");
                    notificationManager.createNotification(app.patientId(), "Za 10 minut rozpocznie się Twoja wizyta.");
                }
            }
        }

        // 4. Odśwież UI (wszystkie kontrolery muszą nasłuchiwać TimeManagera)
        // Kontrolery są już podpięte do TimeManagera i same odświeżą UI
    }
}

