package com.clinicmanager.model.entities;

import com.clinicmanager.model.enums.AppointmentStatus;
import com.clinicmanager.repository.AppointmentRepository;
import java.time.LocalDate;

public class Slot {
  private final int id;
  private final int scheduleId;
  private final LocalDate date;
  private final TimeRange timeRange;

  public Slot(int id, int scheduleId, LocalDate date, TimeRange timeRange) {
    this.id = id;
    this.scheduleId = scheduleId;
    this.date = date;
    this.timeRange = timeRange;
  }

  public Slot(int scheduleId, LocalDate date, TimeRange timeRange) {
    this(-1, scheduleId, date, timeRange);
  }

  public int id() {
    return id;
  }

  public int scheduleId() {
    return scheduleId;
  }

  public LocalDate date() {
    return date;
  }

  public TimeRange timeRange() {
    return timeRange;
  }

  // Check whether the slot is free (no active appointment for this slot)
  public boolean isAvailable(AppointmentRepository appointmentRepository) {
    // The slot is occupied when an appointment with this slotId exists and is not CANCELLED
    return appointmentRepository.findAll().stream()
        .noneMatch(a -> a.slotId() == id && !a.status().equals(AppointmentStatus.CANCELLED));
  }
}
