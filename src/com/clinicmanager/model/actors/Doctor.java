package com.clinicmanager.model.actors;


public class Doctor extends Person {
    private final int scheduleId;
    private List<Integer> curedPatientIds;

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
        this.curedPatientIds = curedPatientIds;

    }


    public int scheduleId() { return scheduleId; }

    public List<Integer> curedPatientIds() {
        return curedPatientIds;
    }
}
