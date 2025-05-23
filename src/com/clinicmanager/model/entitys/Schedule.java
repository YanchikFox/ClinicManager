package com.clinicmanager.model.entitys;

public class Schedule {
    private final int id;
    private final int doctorId;

    public Schedule(int id, int doctorId) {
        this.id = id;
        this.doctorId = doctorId;
    }

    public int id() { return id; }
    public int doctorId() { return doctorId; }
}
