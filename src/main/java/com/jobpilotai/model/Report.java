package com.jobpilotai.model;

/**
 * Domain model representing a generated Excel report.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class Report {

    private int id;
    private String filename;
    private String filepath;
    private String type;       // MANUAL | HOURLY | FINAL
    private String createdAt;

    public Report() {}

    public Report(String filename, String filepath, String type) {
        this.filename  = filename;
        this.filepath  = filepath;
        this.type      = type;
    }

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }

    public String getFilename()      { return filename; }
    public void setFilename(String v){ this.filename = v; }

    public String getFilepath()      { return filepath; }
    public void setFilepath(String v){ this.filepath = v; }

    public String getType()          { return type; }
    public void setType(String v)    { this.type = v; }

    public String getCreatedAt()     { return createdAt; }
    public void setCreatedAt(String v){ this.createdAt = v; }

    @Override
    public String toString() {
        return "Report{id=" + id + ", filename='" + filename + "', type='" + type + "'}";
    }
}
