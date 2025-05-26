package com.clinicmanager.model.entities;

import java.time.LocalDate;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.repository.MedicalRecordRepository;

public class MedicalRecord {
    private final int id;
    private final int medicalCardId;
    private final int doctorId;
    private final LocalDate date;
    private final String description;
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecord(int id, int medicalCardId, int doctorId, LocalDate date, String description) {
        this.id = id;
        this.medicalCardId = medicalCardId;
        this.doctorId = doctorId;
        this.date = date;
        this.description = description;
        this.medicalRecordRepository = AppContext.getInstance().getRepositories().records;
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

    // Przykład użycia repo
    // public void save() {
    //     medicalRecordRepository.save(this);
    // }
}
