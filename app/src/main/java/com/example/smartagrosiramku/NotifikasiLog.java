package com.example.smartagrosiramku;

public class NotifikasiLog {
    public String id;
    public String title;
    public String message;
    public String type; // "error", "warning", "info"
    public long timestamp;
    public boolean isRead;

    public NotifikasiLog() {}

    public NotifikasiLog(String title, String message, String type, long timestamp, boolean isRead) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }
}
