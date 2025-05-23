package com.clinicmanager.model.entitys;


import java.time.LocalTime;

public class TimeRange {
    private final LocalTime start;
    private final LocalTime end;

    public TimeRange(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalTime start() { return start; }
    public LocalTime end() { return end; }
}