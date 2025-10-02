package com.clinicmanager.model.entities;

import java.time.LocalDate;

public class MedicalRecord {
    private final int id;
    private final int medicalCardId;
    private final int doctorId;
    private final LocalDate date;
    private final String description;
    private final Integer appointmentId;

    public MedicalRecord(int id, int medicalCardId, int doctorId, LocalDate date, String description,
            Integer appointmentId) {
        this.id = id;
        this.medicalCardId = medicalCardId;
        this.doctorId = doctorId;
        this.date = date;
        this.description = description;
        this.appointmentId = appointmentId;
    }

    public MedicalRecord(int id, int medicalCardId, int doctorId, LocalDate date, String description) {
        this(id, medicalCardId, doctorId, date, description, null);
    }

    public int id() {
        return id;
    }

    public int medicalCardId() {
        return medicalCardId;
    }

    public int doctorId() {
        return doctorId;
    }

    public LocalDate date() {
        return date;
    }

    public String description() {
        return description;
    }

    public Integer appointmentId() {
        return appointmentId;
    }

}
