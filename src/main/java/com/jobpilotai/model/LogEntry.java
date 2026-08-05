package com.jobpilotai.model;

/**
 * Domain model representing a single log entry stored in the database.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class LogEntry {

    private int id;
    private String level;     // INFO | WARN | ERROR | DEBUG
    private String message;
    private String timestamp;

    public LogEntry() {}

    public LogEntry(String level, String message) {
        this.level   = level;
        this.message = message;
    }

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getLevel()         { return level; }
    public void setLevel(String v)   { this.level = v; }

    public String getMessage()       { return message; }
    public void setMessage(String v) { this.message = v; }

    public String getTimestamp()     { return timestamp; }
    public void setTimestamp(String v){ this.timestamp = v; }

    @Override
    public String toString() {
        return "[" + timestamp + "] [" + level + "] " + message;
    }
}
