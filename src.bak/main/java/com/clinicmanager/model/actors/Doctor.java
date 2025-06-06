package com.clinicmanager.model.actors;

import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.entities.TimeRange;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.repository.SlotRepository;

import java.time.LocalDate;
import java.util.List;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.entities.TimeRange;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.repository.SlotRepository;

import java.time.LocalDate;
import java.util.List;

public class Doctor extends Person {
    private final int scheduleId;
    private List<Integer> patients = null;

    private final PatientRepository patientRepository;
    private final SlotRepository slotRepository;

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
        this.patientRepository = AppContext.getInstance().getRepositories().patients;
        this.slotRepository = AppContext.getInstance().getRepositories().slots;
    }

    public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId, List<Integer> patients) {
        super(id, name, dateOfBirth, phoneNumber);
        this.scheduleId = scheduleId;
        this.patients = patients;
        this.patientRepository = AppContext.getInstance().getRepositories().patients;
        this.slotRepository = AppContext.getInstance().getRepositories().slots;
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
        slotRepository.save(slot);
    }

    public void addAvailableSlot(LocalDate date, TimeRange timeRange) {
        Slot slot = new Slot(this.scheduleId, date, timeRange);
        slotRepository.save(slot);
    }
}