package com.clinicmanager.model.entitys;

import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.repository.ScheduleRepository;
import com.clinicmanager.gui.AppContext;

import java.time.LocalDate;
import java.util.List;

public class Schedule {
    private final int id;
    private final int doctorId;

    public Schedule(int id, int doctorId) {
        this.id = id;
        this.doctorId = doctorId;
    }

    public int id() { return id; }
    public int doctorId() { return doctorId; }


// TODO: Anton shis should not be hardcoded, but rather injected or passed as a parameter
    public List<Slot> getSlots(LocalDate date) {
        return AppContext.getRepositories().schedules.getDoctorSlots(doctorId, date);
//                slots.getSlots(doctorId, date);
    }
}
