package com.jobpilotai.events;

public class AppStartedEvent implements IEvent {
    private final long timestamp;
    private final String version;

    public AppStartedEvent(String version) {
        this.timestamp = System.currentTimeMillis();
        this.version = version;
    }

    @Override
    public String getEventName() {
        return "AppStartedEvent";
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getVersion() {
        return version;
    }
}
