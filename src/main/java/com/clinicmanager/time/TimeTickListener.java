package com.clinicmanager.time;

import java.time.LocalDateTime;

public interface TimeTickListener {
    void onTick(LocalDateTime now);
}
