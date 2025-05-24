package com.clinicmanager.model.actors;

import com.clinicmanager.repository.PatientRepository;

import java.util.List;

public class Doctor extends Person {
    private final int scheduleId;
private List<Integer> curedPatientIds = null;

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
    }

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId, List<Integer> curedPatientIds) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
        this.curedPatientIds = curedPatientIds;
    }

    public int scheduleId() {
        return scheduleId;
    }


public void updateCuredPatientIds(PatientRepository repo) {
    this.curedPatientIds = repo.getPatientIdsOfDoctor(this.id());

}
}
