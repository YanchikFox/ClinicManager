package com.clinicmanager.model.actors;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.model.entitys.Slot;
import com.clinicmanager.model.entitys.TimeRange;
import com.clinicmanager.repository.PatientRepository;

import java.time.LocalDate;
import java.util.List;

public class Doctor extends Person {
    private final int scheduleId;
    private List<Integer> patients = null;
    PatientRepository repo = AppContext.getRepositories().patients;// FIXME: this should not be hardcoded, but rather
                                                                   // injected or passed as a parameter

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
    }

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId, List<Integer> patients) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
        this.patients = patients;
    }

    public int scheduleId() {
        return scheduleId;
    }

    public void updateCuredPatientIds() {
        this.patients = repo.getPatientIdsOfDoctor(this.id());
    }

    public List<Integer> getPatientsList() {
        this.patients = repo.getPatientIdsOfDoctor(this.id()); // auto-update za każdym razem
        return patients;
    }

    public void addAvailableSlot(Slot slot) {
        if (slot.scheduleId() != this.scheduleId) {
            throw new IllegalArgumentException("Slot scheduleId must match doctor's scheduleId.");
        }
        AppContext.getRepositories().slots.save(slot);
    }

    public void addAvailableSlot(LocalDate date, TimeRange timeRange) {
        Slot slot = new Slot(this.scheduleId, date, timeRange);
        AppContext.getRepositories().slots.save(slot);
    }
}
