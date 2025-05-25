package com.clinicmanager.model.entitys;


import java.time.LocalDateTime;

public class Notification {
    private final int id;
    private final int personId;
    private final String message;
    private final LocalDateTime timestamp;
    private boolean read;

    public Notification(int id, int personId, String message, LocalDateTime timestamp, boolean read) {
        this.id = id;
        this.personId = personId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
    }

    public int id() { return id; }
    public int personId() { return personId; }
    public String message() { return message; }
    public LocalDateTime timestamp() { return timestamp; }
    public boolean read() { return read; }

    public void markAsRead() { this.read = true; }

    public boolean isRead() {
        return this.read;
    }
}

