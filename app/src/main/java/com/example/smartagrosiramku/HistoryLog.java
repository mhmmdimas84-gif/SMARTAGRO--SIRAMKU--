package com.example.smartagrosiramku;

public class HistoryLog {
    public float tds;
    public int waterLevel;
    public long timestamp;

    public HistoryLog() {}

    public HistoryLog(float tds, int waterLevel, long timestamp) {
        this.tds = tds;
        this.waterLevel = waterLevel;
        this.timestamp = timestamp;
    }
}
