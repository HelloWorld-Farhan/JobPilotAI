package com.jobpilotai.events;

/**
 * Base interface for all internal system events.
 */
public interface IEvent {
    String getEventName();
    long getTimestamp();
}
