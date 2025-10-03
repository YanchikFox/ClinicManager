package com.clinicmanager.model.entities;

public class FavoriteDoctor {
  private final int id;
  private final int patientId;
  private final int doctorId;

  public FavoriteDoctor(int id, int patientId, int doctorId) {
    this.id = id;
    this.patientId = patientId;
    this.doctorId = doctorId;
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
}
