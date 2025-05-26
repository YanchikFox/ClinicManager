package com.clinicmanager.model.entities;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.repository.ScheduleRepository;
import com.clinicmanager.repository.SlotRepository;

import java.time.LocalDate;
import java.util.List;

public class Schedule {
    private final int id;
    private final int doctorId;
    private final ScheduleRepository scheduleRepository;

    public Schedule(int id, int doctorId) {
        this.id = id;
        this.doctorId = doctorId;
        this.scheduleRepository = AppContext.getInstance().getRepositories().schedules;
    }

    public int id() {
        return id;
    }

    public int doctorId() {
        return doctorId;
    }


    // TODO: Anton shis should not be hardcoded, but rather injected or passed as a parameter
    public List<Slot> getSlots(LocalDate date) {
        return ScheduleRepository.getDoctorSlots(doctorId, date);
//                slots.getSlots(doctorId, date);
    }
    // TODO: getUpgoningSlots should be implemented
}
