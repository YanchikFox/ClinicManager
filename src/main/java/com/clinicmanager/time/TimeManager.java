package com.clinicmanager.time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class TimeManager {
    private static TimeManager instance;
    private LocalDateTime currentTime;
    private final List<Consumer<LocalDateTime>> listeners = new ArrayList<>();
    private Timer timer;
    private boolean running = false;
    private int speedSecondsPerTick = 10; // 1 минута = 10 секунд

    private TimeManager() {
        this.currentTime = LocalDateTime.now();
    }

    public static TimeManager getInstance() {
        if (instance == null) instance = new TimeManager();
        return instance;
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalDateTime time) {
        this.currentTime = time;
        notifyListeners();
    }

    public void addListener(Consumer<LocalDateTime> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<LocalDateTime> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (var l : listeners) l.accept(currentTime);
    }

    public void start() {
        if (running) return;
        running = true;
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentTime = currentTime.plusMinutes(1);
                notifyListeners();
                // --- Глобальная обработка времени ---
                com.clinicmanager.time.TimeTickHandler.handleTimeTick(currentTime);
            }
        }, speedSecondsPerTick * 1000, speedSecondsPerTick * 1000);
    }

    public void stop() {
        if (timer != null) timer.cancel();
        running = false;
    }

    public void setSpeedSecondsPerTick(int seconds) {
        this.speedSecondsPerTick = seconds;
        if (running) {
            stop();
            start();
        }
    }
}
