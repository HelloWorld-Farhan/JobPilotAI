package com.jobpilotai.events;

public class WorkspaceChangedEvent implements IEvent {
    private final long timestamp;
    private final String newWorkspaceName;

    public WorkspaceChangedEvent(String newWorkspaceName) {
        this.timestamp = System.currentTimeMillis();
        this.newWorkspaceName = newWorkspaceName;
    }

    @Override
    public String getEventName() {
        return "WorkspaceChangedEvent";
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getNewWorkspaceName() {
        return newWorkspaceName;
    }
}
