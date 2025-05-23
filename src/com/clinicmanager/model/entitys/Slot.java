package com.clinicmanager.model.entitys;

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

    public int id() { return id; }
    public int scheduleId() { return scheduleId; }
    public LocalDate date() { return date; }
    public TimeRange timeRange() { return timeRange; }
}
