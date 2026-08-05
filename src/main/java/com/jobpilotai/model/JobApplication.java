package com.jobpilotai.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Domain model representing a single job application tracked by JobPilotAI.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class JobApplication {

    private int id;
    private String company;
    private String jobTitle;
    private String website;
    private String jobUrl;
    private String status;
    private String date;
    private String time;
    private String resumeUsed;
    private String notes;
    private int attemptCount;
    private String createdAt;
    
    // V3 Fields
    private String location;
    private String salary;
    private String recruiter;

    public JobApplication() {
        this.status       = "Pending";
        this.attemptCount = 1;
        this.date         = LocalDate.now().toString();
        this.time         = LocalTime.now().toString();
    }

    public JobApplication(String company, String jobTitle, String website,
                          String jobUrl, String status, String date,
                          String time, String resumeUsed, String notes,
                          int attemptCount) {
        this.company      = company;
        this.jobTitle     = jobTitle;
        this.website      = website;
        this.jobUrl       = jobUrl;
        this.status       = status;
        this.date         = date;
        this.time         = time;
        this.resumeUsed   = resumeUsed;
        this.notes        = notes;
        this.attemptCount = attemptCount;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────

    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getCompany()            { return company; }
    public void setCompany(String v)      { this.company = v; }

    public String getJobTitle()           { return jobTitle; }
    public void setJobTitle(String v)     { this.jobTitle = v; }

    public String getWebsite()            { return website; }
    public void setWebsite(String v)      { this.website = v; }

    public String getJobUrl()             { return jobUrl; }
    public void setJobUrl(String v)       { this.jobUrl = v; }

    public String getStatus()             { return status; }
    public void setStatus(String v)       { this.status = v; }

    public String getDate()               { return date; }
    public void setDate(String v)         { this.date = v; }

    public String getTime()               { return time; }
    public void setTime(String v)         { this.time = v; }

    public String getResumeUsed()         { return resumeUsed; }
    public void setResumeUsed(String v)   { this.resumeUsed = v; }

    public String getNotes()              { return notes; }
    public void setNotes(String v)        { this.notes = v; }

    public int getAttemptCount()          { return attemptCount; }
    public void setAttemptCount(int v)    { this.attemptCount = v; }

    public String getCreatedAt()          { return createdAt; }
    public void setCreatedAt(String v)    { this.createdAt = v; }

    public String getLocation()           { return location; }
    public void setLocation(String v)     { this.location = v; }

    public String getSalary()             { return salary; }
    public void setSalary(String v)       { this.salary = v; }

    public String getRecruiter()          { return recruiter; }
    public void setRecruiter(String v)    { this.recruiter = v; }

    @Override
    public String toString() {
        return "JobApplication{id=" + id + ", company='" + company +
               "', jobTitle='" + jobTitle + "', status='" + status + "'}";
    }
}
