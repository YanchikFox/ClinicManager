package com.clinicmanager.service;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.entities.TimeRange;
import com.clinicmanager.repository.DoctorRepository;
import com.clinicmanager.repository.SlotRepository;
import com.clinicmanager.time.TimeManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SlotAutoGeneratorService {
    private final DoctorRepository doctorRepository;
    private final SlotRepository slotRepository;

    public SlotAutoGeneratorService() {
        this.doctorRepository = AppContext.getRepositories().doctors;
        this.slotRepository = AppContext.getRepositories().slots;
    }

    public void ensureFutureSlotsForAllDoctors() {
        LocalDate virtualToday = TimeManager.getInstance().getCurrentTime().toLocalDate();
        LocalTime virtualNowTime = TimeManager.getInstance().getCurrentTime().toLocalTime();
        List<Doctor> doctors = doctorRepository.findAll();
        for (Doctor doctor : doctors) {
            final int scheduleId = doctor.scheduleId();
            for (int i = 0; i < 3; i++) {
                LocalDate date = virtualToday.plusDays(i);
                if (isWeekday(date)) {
                    int startHour = (i == 0)
                            ? Math.max(9,
                                    virtualNowTime.getMinute() > 0 ? virtualNowTime.getHour() + 1
                                            : virtualNowTime.getHour())
                            : 9;
                    if (startHour >= 17) {
                        continue;
                    }
                    // Fetch existing slots for this schedule and date
                    List<Slot> slotsForDay = slotRepository.findAll().stream()
                            .filter(s -> s.scheduleId() == scheduleId && s.date().equals(date))
                            .toList();
                    for (int hour = startHour; hour < 17; hour++) {
                        final int slotHour = hour;
                        boolean exists = slotsForDay.stream()
                                .anyMatch(s -> s.timeRange().start().getHour() == slotHour);
                        if (!exists) {
                            TimeRange range = new TimeRange(LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                            Slot slot = new Slot(scheduleId, date, range);
                            slotRepository.save(slot);
                        }
                    }
                }
            }
        }
    }

    private boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
