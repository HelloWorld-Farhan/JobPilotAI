package com.jobpilotai.automation.strategy;

import com.microsoft.playwright.Page;

/**
 * Interface for different job board automation strategies.
 */
public interface JobApplyStrategy {
    
    /**
     * Attempts to automatically apply to the job on the current page.
     * 
     * @param page The Playwright page instance
     * @param url The URL of the job
     * @return true if applied successfully, false otherwise
     */
    boolean apply(Page page, String url) throws Exception;
}
