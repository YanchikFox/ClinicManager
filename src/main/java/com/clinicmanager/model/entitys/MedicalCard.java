package com.clinicmanager.model.entitys;

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
}
