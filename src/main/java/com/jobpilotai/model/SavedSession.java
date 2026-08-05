package com.jobpilotai.model;

/**
 * Domain model representing a saved application session snapshot.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class SavedSession {

    private int id;
    private String dataJson;   // JSON blob of session state
    private String savedAt;

    public SavedSession() {}

    public SavedSession(String dataJson) {
        this.dataJson = dataJson;
    }

    public int getId()                { return id; }
    public void setId(int id)         { this.id = id; }

    public String getDataJson()       { return dataJson; }
    public void setDataJson(String v) { this.dataJson = v; }

    public String getSavedAt()        { return savedAt; }
    public void setSavedAt(String v)  { this.savedAt = v; }
}
