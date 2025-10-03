package com.clinicmanager.model.actors;

import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.entities.TimeRange;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.repository.SlotRepository;
import java.time.LocalDate;
import java.util.List;

public class Doctor extends Person {
  private final int scheduleId;
  private List<Integer> patients = null;

  public Doctor(int id, String name, String dateOfBirth, String phoneNumber, int scheduleId) {
    super(id, name, dateOfBirth, phoneNumber);
    this.scheduleId = scheduleId;
  }

  public Doctor(
      int id,
      String name,
      String dateOfBirth,
      String phoneNumber,
      int scheduleId,
      List<Integer> patients) {
    super(id, name, dateOfBirth, phoneNumber);
    this.scheduleId = scheduleId;
    this.patients = patients;
  }

  public int scheduleId() {
    return scheduleId;
  }

  public void updateCuredPatientIds(PatientRepository patientRepository) {
    this.patients = patientRepository.getPatientIdsOfDoctor(this.id());
  }

  public List<Integer> getPatientsList(PatientRepository patientRepository) {
    this.patients = patientRepository.getPatientIdsOfDoctor(this.id());
    return patients;
  }

  public void addAvailableSlot(Slot slot, SlotRepository slotRepository) {
    if (slot.scheduleId() != this.scheduleId) {
      throw new IllegalArgumentException("Slot scheduleId must match doctor's scheduleId.");
    }
    slotRepository.save(slot);
  }

  public void addAvailableSlot(LocalDate date, TimeRange timeRange, SlotRepository slotRepository) {
    Slot slot = new Slot(this.scheduleId, date, timeRange);
    slotRepository.save(slot);
  }
}
