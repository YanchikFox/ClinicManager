package com.clinicmanager.model.entities;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.repository.MedicalCardRepository;
import com.clinicmanager.repository.MedicalRecordRepository;

import java.util.List;

public class MedicalCard {
    private final int id;
    private final int patientId;
    private final MedicalCardRepository medicalCardRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalCard(int id, int patientId) {
        this.id = id;
        this.patientId = patientId;
        this.medicalCardRepository = AppContext.getRepositories().cards;
        this.medicalRecordRepository = AppContext.getRepositories().records;
    }

    public int id() {
        return id;
    }

    public int patientId() {
        return patientId;
    }

    public void addRecord(MedicalRecord medicalRecord) {
        medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getRecords() {
        return medicalRecordRepository.findByMedicalCardId(id);
    }
}
