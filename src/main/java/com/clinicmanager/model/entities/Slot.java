package com.clinicmanager.model.entities;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.repository.AppointmentRepository;
import com.clinicmanager.repository.SlotRepository;
import com.clinicmanager.repository.AppointmentRepository;

import java.time.LocalDate;

public class Slot {
    private final int id;
    private final int scheduleId;
    private final LocalDate date;
    private final TimeRange timeRange;
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private Appointment appointment;

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

    public boolean isAvailable() {
        slotRepository.findById(id)
        // Sprawdza, czy slot jest dostępny, czyli czy nie ma przypisanego spotkania
         appointment = appointmentRepository.findById(id);
         if(appointment == null) {
            return true;
        } else {
            return false;
        }
    }
    // Przykład metody wykorzystującej slotRepository
    // TODO: Można dodać więcej metod operujących na slotach
}
