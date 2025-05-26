package com.clinicmanager.model.entities;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.repository.SlotRepository;

import java.time.LocalDate;

public class Slot {
    private final int id;
    private final int scheduleId;
    private final LocalDate date;
    private final TimeRange timeRange;
    private final SlotRepository slotRepository;

    public Slot(int id, int scheduleId, LocalDate date, TimeRange timeRange) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.date = date;
        this.timeRange = timeRange;
        this.slotRepository = AppContext.getInstance().getRepositories().slots;
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

    // Przykład metody wykorzystującej slotRepository
    // TODO: Można dodać więcej metod operujących na slotach
}
