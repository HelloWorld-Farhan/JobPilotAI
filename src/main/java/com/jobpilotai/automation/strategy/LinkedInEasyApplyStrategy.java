package com.jobpilotai.automation.strategy;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.service.SettingsService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;

public class LinkedInEasyApplyStrategy implements JobApplyStrategy {

    @Override
    public boolean apply(Page page, String url) throws Exception {
        AppLogger.info("Attempting LinkedIn Easy Apply for: " + url);

        try {
            // 1. Wait for and click the Easy Apply button
            String currentUrl = page.url().toLowerCase();
            if (currentUrl.contains("/login") || currentUrl.contains("/checkpoint") || currentUrl.contains("/auth/")) {
                com.jobpilotai.service.NotificationService.getInstance().notify("JobPilotAI: Security Challenge", 
                    "Automation is paused. Please complete the login or 2FA challenge on LinkedIn.");
                
                AppLogger.info("Waiting for you to complete login/2FA. Automation will resume instantly once done...");
                long maxWaitTime = System.currentTimeMillis() + 180000;
                while (System.currentTimeMillis() < maxWaitTime) {
                    currentUrl = page.url().toLowerCase();
                    if (!currentUrl.contains("/login") && !currentUrl.contains("/checkpoint") && !currentUrl.contains("/auth/")) {
                        AppLogger.info("Login/2FA completed! Navigating back to target URL...");
                        page.navigate(url);
                        Thread.sleep(3000);
                        break;
                    }
                    Thread.sleep(2000);
                }
            }
            
            AppLogger.info("Waiting for the Easy Apply button. You have 3 minutes.");
            boolean clicked = false;
            long maxWait = System.currentTimeMillis() + 180000;
            
            while (System.currentTimeMillis() < maxWait) {
                try {
                    // Find any button with the class OR containing the text Easy Apply
                    Locator buttons = page.locator("button.jobs-apply-button, button:has-text('Easy Apply')");
                    int count = buttons.count();
                    
                    for (int i = 0; i < count; i++) {
                        Locator btn = buttons.nth(i);
                        try {
                            if (btn.isVisible()) {
                                // Force click bypasses any overlapping elements (like cookie banners)
                                btn.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
                                clicked = true;
                                break;
                            }
                        } catch (Exception e) {
                            AppLogger.warn("Attempted to click a button but failed: " + e.getMessage());
                        }
                    }
                    
                    if (clicked) {
                        break;
                    }
                } catch (Exception e) {
                    AppLogger.warn("Error finding buttons: " + e.getMessage());
                }
                Thread.sleep(500); // Poll very fast
            }
            
            if (!clicked) {
                AppLogger.warn("Easy Apply button not found or not visible after 3 minutes. This might be an external application.");
                return false;
            }
            
            AppLogger.info("Clicked Easy Apply button.");
            
            // 2. Step through the modal
            for (int i = 0; i < 30; i++) {
                // Wait very briefly for modal transitions
                Thread.sleep(200);
                
                // Check for Submit button first
                Locator submitBtn = page.locator("button[aria-label='Submit application']");
                if (submitBtn.count() > 0 && submitBtn.isVisible()) {
                    submitBtn.click();
                    AppLogger.info("Clicked Submit Application!");
                    // Wait for the success modal
                    Thread.sleep(3000);
                    return true;
                }
                
                // Before clicking Next/Review, try to auto-fill visible form fields
                autoFillFormFields(page);
                
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
    
    private void autoFillFormFields(Page page) {
        SettingsService settings = SettingsService.getInstance();
        
        try {
            // 1. Text Inputs (Salary, Experience)
            Locator textInputs = page.locator("div.jobs-easy-apply-form-section__grouping input[type='text']");
            for (int i = 0; i < textInputs.count(); i++) {
                Locator input = textInputs.nth(i);
                if (!input.isVisible()) continue;
                
                // Get the corresponding label
                String labelId = input.getAttribute("id");
                Locator labelNode = page.locator("label[for='" + labelId + "']");
                if (labelNode.count() == 0) continue;
                
                String labelText = labelNode.innerText().toLowerCase();
                String currentValue = input.inputValue();
                
                // Only fill if empty
                if (currentValue != null && !currentValue.trim().isEmpty()) continue;
                
                if (labelText.contains("salary") || labelText.contains("compensation")) {
                    input.fill(settings.getExpectedSalary() != null ? settings.getExpectedSalary() : "");
                    AppLogger.info("Auto-filled salary input.");
                } else if (labelText.contains("experience") || labelText.contains("years")) {
                    input.fill(settings.getYearsExperience() != null ? settings.getYearsExperience() : "");
                    AppLogger.info("Auto-filled experience input.");
                }
            }
            
            // 2. Radio Buttons / Selects (Sponsorship, Citizenship, Employment)
            Locator fieldsets = page.locator("fieldset");
            for (int i = 0; i < fieldsets.count(); i++) {
                Locator fieldset = fieldsets.nth(i);
                if (!fieldset.isVisible()) continue;
                
                Locator legend = fieldset.locator("legend");
                if (legend.count() == 0) continue;
                String questionText = legend.innerText().toLowerCase();
                
                String targetAnswer = null;
                if (questionText.contains("sponsorship") || questionText.contains("visa")) {
                    targetAnswer = settings.isRequireSponsorship() ? "yes" : "no";
                } else if (questionText.contains("currently employed") || questionText.contains("employment")) {
                    targetAnswer = settings.isCurrentlyEmployed() ? "yes" : "no";
                }
                
                if (targetAnswer != null) {
                    // Check if already answered by seeing if a radio is checked
                    Locator checkedRadio = fieldset.locator("input[type='radio']:checked");
                    if (checkedRadio.count() > 0) continue; // Already answered
                    
                    // Find the label matching target answer
                    Locator labels = fieldset.locator("label");
                    for (int j = 0; j < labels.count(); j++) {
                        Locator lbl = labels.nth(j);
                        if (lbl.innerText().toLowerCase().trim().equals(targetAnswer)) {
                            lbl.click();
                            AppLogger.info("Auto-selected '" + targetAnswer + "' for question: " + questionText);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.warn("Error during autoFillFormFields: " + e.getMessage());
        }
    }
}
