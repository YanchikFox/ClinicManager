package com.clinicmanager.time;

import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.enums.AppointmentStatus;
import com.clinicmanager.repository.AppointmentRepository;
import com.clinicmanager.repository.SlotRepository;
import com.clinicmanager.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TimeTickHandler implements TimeTickListener {
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public TimeTickHandler(SlotRepository slotRepository,
            AppointmentRepository appointmentRepository,
            NotificationService notificationService) {
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void onTick(LocalDateTime now) {
        // 1. Remove outdated slots
        List<Slot> slots = slotRepository.findAll();
        for (Slot slot : slots) {
            LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
            if (slotEnd.isBefore(now)) {
                slotRepository.delete(slot);
            }
        }

        // 2. Finish overdue appointments
        List<Appointment> appointments = appointmentRepository.findAll();
        for (Appointment app : appointments) {
            Slot slot = app.getSlot(slotRepository);
            if (slot == null)
                continue;
            LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
            if (!app.status().equals(AppointmentStatus.ENDED) && !app.status().equals(AppointmentStatus.CANCELLED)) {
                if (slotEnd.isBefore(now)) {
                    app.end(appointmentRepository);
                }
            }
        }

        // 3. Notifications ten minutes before an appointment (only CONFIRMED/PENDING)
        for (Appointment app : appointments) {
            Slot slot = app.getSlot(slotRepository);
            if (slot == null)
                continue;
            LocalDateTime slotStart = LocalDateTime.of(slot.date(), slot.timeRange().start());
            if (!app.status().equals(AppointmentStatus.ENDED) && !app.status().equals(AppointmentStatus.CANCELLED)) {
                if (slotStart.minusMinutes(10).equals(now)) {
                    // Notify the doctor and the patient
                    notificationService.createNotification(app.doctorId(), "The appointment starts in 10 minutes.");
                    notificationService.createNotification(app.patientId(), "Your appointment starts in 10 minutes.");
                }
            }
        }

        // 4. Refresh the UI (all controllers listen to the TimeManager)
        // Controllers are already registered with the TimeManager and refresh themselves
    }
}

