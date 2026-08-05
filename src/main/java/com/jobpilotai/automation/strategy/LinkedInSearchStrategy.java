package com.jobpilotai.automation.strategy;

import com.jobpilotai.automation.queue.QueueService;
import com.jobpilotai.logs.AppLogger;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.HashSet;
import java.util.Set;

public class LinkedInSearchStrategy implements JobApplyStrategy {

    @Override
    public boolean apply(Page page, String url) throws Exception {
        AppLogger.info("Executing LinkedIn Search & Scrape Strategy for: " + url);

        try {
            // Give the search results time to load initially
            Thread.sleep(3000);
            
            // Look for the main list container to scroll
            Locator listContainer = page.locator(".jobs-search-results-list");
            if (listContainer.count() == 0) {
                AppLogger.warn("Could not find jobs-search-results-list. Searching for alternative layout...");
                listContainer = page.locator(".scaffold-layout__list");
            }
            
            if (listContainer.count() == 0) {
                AppLogger.error("Could not find the scrollable job list container.");
                return false;
            }

            // LinkedIn lazy-loads jobs. We must scroll down the list container to load all ~25 jobs.
            AppLogger.info("Scrolling down job list to load all jobs...");
            for (int i = 0; i < 5; i++) {
                // Execute JS to scroll the container
                listContainer.evaluate("el => el.scrollBy(0, 1000)");
                Thread.sleep(1500); // Wait for lazy load
            }

            // Extract all job links
            Locator jobLinks = page.locator("a.job-card-container__link");
            int count = jobLinks.count();
            AppLogger.info("Found " + count + " job links on the search page.");
            
            if (count == 0) {
                return false;
            }

            Set<String> uniqueUrls = new HashSet<>();
            
            for (int i = 0; i < count; i++) {
                String href = jobLinks.nth(i).getAttribute("href");
                if (href != null && href.contains("/jobs/view/")) {
                    // Clean URL (remove tracking parameters)
                    int queryIdx = href.indexOf('?');
                    if (queryIdx > 0) {
                        href = href.substring(0, queryIdx);
                    }
                    
                    // Make absolute if relative
                    if (href.startsWith("/")) {
                        href = "https://www.linkedin.com" + href;
                    }
                    
                    uniqueUrls.add(href);
                }
            }
            
            AppLogger.info("Extracted " + uniqueUrls.size() + " unique job URLs. Adding to queue...");
            
            for (String jobUrl : uniqueUrls) {
                QueueService.getInstance().addJob("LinkedIn", "Auto Scraped", "Pending Data Extraction", jobUrl);
            }
            
            AppLogger.info("Successfully added jobs to queue. This Search Task is now complete.");
            return true;

        } catch (Exception e) {
            AppLogger.error("Error during LinkedIn Search & Scrape flow.", e);
            return false;
        }
    }
}
