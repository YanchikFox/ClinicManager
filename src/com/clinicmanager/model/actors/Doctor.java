package com.clinicmanager.model.actors;


public class Doctor extends Person {
    private final int scheduleId;

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
    }

    public int scheduleId() { return scheduleId; }
}
