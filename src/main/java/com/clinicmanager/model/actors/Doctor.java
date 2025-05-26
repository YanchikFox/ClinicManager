package com.clinicmanager.model.actors;

import com.clinicmanager.model.entitys.Slot;
import com.clinicmanager.model.entitys.TimeRange;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.repository.SlotRepository;

import java.time.LocalDate;
import java.util.List;

public class Doctor extends Person {
    private final int scheduleId;
    private List<Integer> patients = null;
    private final PatientRepository patientRepository;
    private final SlotRepository slotRepository;

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId, PatientRepository patientRepository, SlotRepository slotRepository) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
        this.patientRepository = patientRepository;
    }

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId, List<Integer> patients, PatientRepository patientRepository, SlotRepository slotRepository) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
        this.patients = patients;
        this.patientRepository = patientRepository;
    }

    public int scheduleId() {
        return scheduleId;
    }

    public void updateCuredPatientIds() {
        this.patients = patientRepository.getPatientIdsOfDoctor(this.id());
    }

    public List<Integer> getPatientsList() {
        this.patients = patientRepository.getPatientIdsOfDoctor(this.id());
        return patients;
    }

    public void addAvailableSlot(Slot slot) {
        if (slot.scheduleId() != this.scheduleId) {
            throw new IllegalArgumentException("Slot scheduleId must match doctor's scheduleId.");
        }
        // TODO: SlotRepository should be injected as well, not accessed statically
        // AppContext.getRepositories().slots.save(slot);
    }

    public void addAvailableSlot(LocalDate date, TimeRange timeRange) {
        Slot slot = new Slot(this.scheduleId, date, timeRange);
        // TODO: SlotRepository should be injected as well, not accessed statically
        // AppContext.getRepositories().slots.save(slot);
    }
}
