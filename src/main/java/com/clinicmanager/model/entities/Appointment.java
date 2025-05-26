package com.clinicmanager.model.entities;

import com.clinicmanager.model.enums.AppointmentStatus;

public class Appointment {
    private final int id;
    private final int patientId;
    private final int doctorId;
    private int slotId;
    private AppointmentStatus status;

    public Appointment(int id, int patientId, int doctorId, int slotId, AppointmentStatus status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.status = status;
    }

    public int id() { return id; }
    public int patientId() { return patientId; }
    public int doctorId() { return doctorId; }
    public int slotId() { return slotId; }
    public AppointmentStatus status() { return status; }

    public void confirm() { this.status = AppointmentStatus.CONFIRMED; }
    public void cancel() { this.status = AppointmentStatus.CANCELLED; }
    public void reschedule(int newSlotId) {
        this.slotId = newSlotId;
        this.status = AppointmentStatus.PENDING;
    }
}

