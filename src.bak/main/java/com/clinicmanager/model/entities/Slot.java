package com.clinicmanager.model.entities;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.repository.AppointmentRepository;
import com.clinicmanager.repository.SlotRepository;

import java.time.LocalDate;

public class Slot {
    private final int id;
    private final int scheduleId;
    private final LocalDate date;
    private final TimeRange timeRange;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    public Slot(int id, int scheduleId, LocalDate date, TimeRange timeRange) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.date = date;
        this.timeRange = timeRange;
        this.slotRepository = AppContext.getInstance().getRepositories().slots;
        this.appointmentRepository = AppContext.getInstance().getRepositories().appointments;
    }

    public Slot(int scheduleId, LocalDate date, TimeRange timeRange) {
        this(-1, scheduleId, date, timeRange);
    }

    public int id() {
        return id;
    }

    public int scheduleId() {
        return scheduleId;
    }

    public LocalDate date() {
        return date;
    }

    public TimeRange timeRange() {
        return timeRange;
    }

    // Sprawdza, czy slot jest wolny (czy nie ma aktywnej wizyty na ten slot)
    public boolean isAvailable() {
        // Slot zajęty, jeśli jest wizyta z tym slotId i status różny od CANCELLED
        return appointmentRepository.findAll().stream()
                .noneMatch(a -> a.slotId() == id
                        && !a.status().equals(com.clinicmanager.model.enums.AppointmentStatus.CANCELLED));
    }
}
