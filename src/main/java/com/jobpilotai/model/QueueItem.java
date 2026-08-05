package com.jobpilotai.model;

import javafx.beans.property.*;

public class QueueItem {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty website = new SimpleStringProperty();
    private final StringProperty company = new SimpleStringProperty();
    private final StringProperty jobTitle = new SimpleStringProperty();
    private final StringProperty jobUrl = new SimpleStringProperty();
    private final IntegerProperty queuePosition = new SimpleIntegerProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final IntegerProperty retryCount = new SimpleIntegerProperty();
    private final StringProperty createdAt = new SimpleStringProperty();
    private final StringProperty updatedAt = new SimpleStringProperty();

    public QueueItem(String id, String website, String company, String jobTitle, String jobUrl,
                     int queuePosition, String status, int retryCount, String createdAt, String updatedAt) {
        this.id.set(id);
        this.website.set(website);
        this.company.set(company);
        this.jobTitle.set(jobTitle);
        this.jobUrl.set(jobUrl);
        this.queuePosition.set(queuePosition);
        this.status.set(status);
        this.retryCount.set(retryCount);
        this.createdAt.set(createdAt);
        this.updatedAt.set(updatedAt);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getWebsite() { return website.get(); }
    public StringProperty websiteProperty() { return website; }

    public String getCompany() { return company.get(); }
    public StringProperty companyProperty() { return company; }

    public String getJobTitle() { return jobTitle.get(); }
    public StringProperty jobTitleProperty() { return jobTitle; }

    public String getJobUrl() { return jobUrl.get(); }
    public StringProperty jobUrlProperty() { return jobUrl; }

    public int getQueuePosition() { return queuePosition.get(); }
    public IntegerProperty queuePositionProperty() { return queuePosition; }
    public void setQueuePosition(int position) { this.queuePosition.set(position); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public int getRetryCount() { return retryCount.get(); }
    public IntegerProperty retryCountProperty() { return retryCount; }
    public void setRetryCount(int count) { this.retryCount.set(count); }

    public String getCreatedAt() { return createdAt.get(); }
    public StringProperty createdAtProperty() { return createdAt; }

    public String getUpdatedAt() { return updatedAt.get(); }
    public StringProperty updatedAtProperty() { return updatedAt; }
}
