package com.clinicmanager.model.entities;

import com.clinicmanager.repository.ScheduleRepository;
import com.clinicmanager.repository.SlotRepository;
import java.time.LocalDate;
import java.util.List;

public class Schedule {
  private final int id;
  private final int doctorId;

  public Schedule(int id, int doctorId) {
    this.id = id;
    this.doctorId = doctorId;
  }

  public int id() {
    return id;
  }

  public int doctorId() {
    return doctorId;
  }

  public List<Slot> getSlots(LocalDate date, ScheduleRepository scheduleRepository) {
    return scheduleRepository.findSlotsByScheduleAndDate(id, date);
  }

  public void removeSlot(Slot slot, SlotRepository slotRepository) {
    slotRepository.delete(slot);
  }

  public void addSlot(Slot slot, SlotRepository slotRepository) {
    slotRepository.save(slot);
  }
}
