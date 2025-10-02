package com.clinicmanager.model.entities;

import com.clinicmanager.repository.MedicalCardRepository;
import com.clinicmanager.repository.MedicalRecordRepository;

import java.util.List;

public class MedicalCard {
    private final int id;
    private final int patientId;

    public MedicalCard(int id, int patientId) {
        this.id = id;
        this.patientId = patientId;
    }

    public int id() {
        return id;
    }

    public int patientId() {
        return patientId;
    }

    public void addRecord(MedicalRecord medicalRecord, MedicalRecordRepository medicalRecordRepository) {
        medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getRecords(MedicalRecordRepository medicalRecordRepository) {
        return medicalRecordRepository.findByMedicalCardId(id);
    }
}
