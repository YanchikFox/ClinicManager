package com.clinicmanager.model.actors;

public class Patient extends Person {
    private final int medicalCardId;

    public Patient(int id, String name, String dateOfBirth, String phoneNumber, int medicalCardId) {
        super(id, name, dateOfBirth, phoneNumber);
        this.medicalCardId = medicalCardId;
    }

    public int medicalCardId() { return medicalCardId; }
}
