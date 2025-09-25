package com.clinicmanager.model.entities;

import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.enums.AppointmentStatus;
import com.clinicmanager.gui.AppContext;
import com.clinicmanager.repository.DoctorRepository;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.repository.SlotRepository;
import com.clinicmanager.repository.AppointmentRepository;

public class Appointment {
    private final int id;
    private final int patientId;
    private final int doctorId;
    private int slotId;
    private AppointmentStatus status;
    private final String problemDescription;

    public Appointment(int id, int patientId, int doctorId, int slotId, AppointmentStatus status,
            String problemDescription) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.slotId = slotId;
        this.status = status;
        this.problemDescription = problemDescription;
    }

    // Legacy constructor maintained for backward compatibility
    public Appointment(int id, int patientId, int doctorId, int slotId, AppointmentStatus status) {
        this(id, patientId, doctorId, slotId, status, "");
    }

    public int id() {
        return id;
    }

    public int patientId() {
        return patientId;
    }

    public int doctorId() {
        return doctorId;
    }

    public int slotId() {
        return slotId;
    }

    public AppointmentStatus status() {
        return status;
    }

    public String problemDescription() {
        return problemDescription;
    }

    // Methods for updating the status
    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public void reschedule(int newSlotId) {
        this.slotId = newSlotId;
        this.status = AppointmentStatus.PENDING;
    }

    // Methods to cancel, reschedule, and confirm the appointment
    public void cancel(AppointmentRepository repo) {
        this.status = AppointmentStatus.CANCELLED;
        repo.update(this);
    }

    public void reschedule(int newSlotId, AppointmentRepository repo) {
        this.slotId = newSlotId;
        this.status = AppointmentStatus.PENDING;
        repo.update(this);
    }

    public void confirm(AppointmentRepository repo) {
        this.status = AppointmentStatus.CONFIRMED;
        repo.update(this);
    }

    public void end(AppointmentRepository repo) {
        this.status = AppointmentStatus.ENDED;
        repo.update(this);
    }

    // Methods for retrieving related objects by id
    public Patient getPatient() {
        PatientRepository repo = AppContext.getInstance().getRepositories().patients;
        return repo.findById(patientId);
    }

    public Doctor getDoctor() {
        DoctorRepository repo = AppContext.getInstance().getRepositories().doctors;
        return repo.findById(doctorId);
    }

    public Slot getSlot() {
        SlotRepository repo = AppContext.getInstance().getRepositories().slots;
        return repo.findById(slotId);
    }
}
