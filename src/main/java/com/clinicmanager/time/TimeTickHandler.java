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

        // 1. Удаление старых слотов
        List<Slot> slots = repos.slots.findAll();
        for (Slot slot : slots) {
            LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
            if (slotEnd.isBefore(now)) {
                repos.slots.delete(slot);
            }
        }

        // 2. Завершение просроченных записей (Appointment)
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

        // 3. Уведомления за 10 минут до приёма (только CONFIRMED/PENDING)
        for (Appointment app : appointments) {
            Slot slot = app.getSlot();
            if (slot == null) continue;
            LocalDateTime slotStart = LocalDateTime.of(slot.date(), slot.timeRange().start());
            if (!app.status().equals(AppointmentStatus.ENDED) && !app.status().equals(AppointmentStatus.CANCELLED)) {
                if (slotStart.minusMinutes(10).equals(now)) {
                    // Уведомить доктора и пациента
                    notificationManager.createNotification(app.doctorId(), "Za 10 minut rozpocznie się przyjęcie.");
                    notificationManager.createNotification(app.patientId(), "Za 10 minut rozpocznie się Twoja wizyta.");
                }
            }
        }

        // 4. Обновить UI (все контроллеры должны слушать TimeManager)
        // Контроллеры уже подписаны на TimeManager и обновят UI сами
    }
}

