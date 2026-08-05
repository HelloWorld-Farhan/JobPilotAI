package com.jobpilotai.automation.strategy;

import com.jobpilotai.logs.AppLogger;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LinkedInEasyApplyStrategy implements JobApplyStrategy {

    @Override
    public boolean apply(Page page, String url) throws Exception {
        AppLogger.info("Attempting LinkedIn Easy Apply for: " + url);

        try {
            // 1. Wait for and click the Easy Apply button
            Locator easyApplyBtn = page.locator("button.jobs-apply-button");
            easyApplyBtn.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
            
            if (easyApplyBtn.count() == 0) {
                AppLogger.warn("Easy Apply button not found. This might be an external application.");
                return false;
            }
            
            easyApplyBtn.click();
            AppLogger.info("Clicked Easy Apply button.");
            
            // 2. Step through the modal
            for (int i = 0; i < 15; i++) {
                // Wait briefly for modal transitions
                Thread.sleep(1500);
                
                // Check for Submit button first
                Locator submitBtn = page.locator("button[aria-label='Submit application']");
                if (submitBtn.count() > 0 && submitBtn.isVisible()) {
                    submitBtn.click();
                    AppLogger.info("Clicked Submit Application!");
                    // Wait for the success modal
                    Thread.sleep(3000);
                    return true;
                }
                
                // Check for Review button
                Locator reviewBtn = page.locator("button[aria-label='Review your application']");
                if (reviewBtn.count() > 0 && reviewBtn.isVisible()) {
                    reviewBtn.click();
                    AppLogger.info("Clicked Review button.");
                    continue;
                }
                
                // Check for Next button
                Locator nextBtn = page.locator("button[aria-label='Continue to next step']");
                if (nextBtn.count() > 0 && nextBtn.isVisible()) {
                    nextBtn.click();
                    AppLogger.info("Clicked Next button.");
                    continue;
                }
                
                // If we get here and none of the buttons are found or visible, we might be stuck
                // Look for generic primary buttons in the modal footer
                Locator genericPrimary = page.locator(".artdeco-modal__actionbar .artdeco-button--primary");
                if (genericPrimary.count() > 0 && genericPrimary.isVisible()) {
                    // Check if it's disabled (means a required field is empty)
                    boolean isDisabled = (boolean) genericPrimary.evaluate("el => el.disabled");
                    if (isDisabled) {
                        AppLogger.warn("Primary button is disabled. Stuck on a required field.");
                        return false;
                    }
                    
                    genericPrimary.click();
                    AppLogger.info("Clicked generic primary button.");
                    continue;
                }
                
                // If we didn't click anything in this loop iteration, we are stuck
                AppLogger.warn("Could not find any navigation buttons in modal. Stuck.");
                return false;
            }
            
            AppLogger.warn("Exceeded maximum steps in Easy Apply modal.");
            return false;

        } catch (Exception e) {
            AppLogger.error("Error during LinkedIn Easy Apply flow.", e);
            return false;
        }
    }
}
