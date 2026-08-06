package com.jobpilotai.automation.strategy;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.service.SettingsService;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.List;

public class LinkedInEasyApplyStrategy implements JobApplyStrategy {

    private void updateStatus(String statusMsg) {
        com.jobpilotai.model.QueueItem currentTask = com.jobpilotai.automation.workflow.WorkflowEngine.getInstance().getCurrentTask();
        if (currentTask != null) {
            com.jobpilotai.automation.queue.QueueService.getInstance().updateStatus(currentTask.getId(), statusMsg);
        }
    }

    @Override
    public boolean apply(Page page, String url) throws Exception {
        updateStatus("Navigating to Job Page...");
        AppLogger.info("Attempting LinkedIn Easy Apply for: " + url);

        try {
            // 1. Wait for and click the Easy Apply button
            String currentUrl = page.url().toLowerCase();
            if (currentUrl.contains("/login") || currentUrl.contains("/checkpoint") || currentUrl.contains("/auth/")) {
                com.jobpilotai.service.NotificationService.getInstance().notify("JobPilotAI: Security Challenge", 
                    "Automation is paused. Please complete the login or 2FA challenge on LinkedIn.");
                
                updateStatus("Waiting for 2FA / Login...");
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
            
            // Wait for the page to fully load AND network to be idle
            try { page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(15000)); } catch (Exception ignored) {}
            Thread.sleep(1500); // Extra wait for LinkedIn's late-rendering JS

            AppLogger.info("Page URL after load: " + page.url());
            AppLogger.info("Page title: " + page.title());

            updateStatus("Looking for Easy Apply button...");
            AppLogger.info("Searching for Easy Apply button using all strategies...");
            boolean clicked = false;
            long maxWait = System.currentTimeMillis() + 60000; // 60 second timeout
            boolean dumpedHtml = false;

            while (System.currentTimeMillis() < maxWait) {
                try { page.evaluate("window.scrollTo(0, 0)"); } catch (Exception ignored) {}

                // --- STRATEGY 1: Playwright getByRole (most reliable, uses ARIA) ---
                try {
                    Locator btn = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Easy Apply").setExact(false));
                    if (btn.count() > 0) {
                        AppLogger.info("Found via getByRole (count=" + btn.count() + ")");
                        btn.first().scrollIntoViewIfNeeded();
                        Thread.sleep(200);
                        btn.first().click(new Locator.ClickOptions().setForce(true));
                        clicked = true;
                    }
                } catch (Exception e) { /* try next */ }

                if (clicked) break;

                // --- STRATEGY 2: getByText exact ---
                try {
                    Locator btn = page.getByText("Easy Apply", new Page.GetByTextOptions().setExact(true));
                    if (btn.count() > 0) {
                        AppLogger.info("Found via getByText exact (count=" + btn.count() + ")");
                        btn.first().scrollIntoViewIfNeeded();
                        Thread.sleep(200);
                        btn.first().click(new Locator.ClickOptions().setForce(true));
                        clicked = true;
                    }
                } catch (Exception e) { /* try next */ }

                if (clicked) break;

                // --- STRATEGY 3: XPath with text contains (most broad) ---
                try {
                    Locator btn = page.locator("//button[contains(., 'Easy Apply')]");
                    if (btn.count() > 0) {
                        AppLogger.info("Found via XPath contains (count=" + btn.count() + ")");
                        btn.first().scrollIntoViewIfNeeded();
                        Thread.sleep(200);
                        btn.first().click(new Locator.ClickOptions().setForce(true));
                        clicked = true;
                    }
                } catch (Exception e) { /* try next */ }

                if (clicked) break;

                // --- STRATEGY 4: Span inside button (LinkedIn wraps text in span) ---
                try {
                    Locator btn = page.locator("button:has(span:text-is('Easy Apply'))");
                    if (btn.count() > 0) {
                        AppLogger.info("Found via span text-is (count=" + btn.count() + ")");
                        btn.first().scrollIntoViewIfNeeded();
                        Thread.sleep(200);
                        btn.first().click(new Locator.ClickOptions().setForce(true));
                        clicked = true;
                    }
                } catch (Exception e) { /* try next */ }

                if (clicked) break;

                // After first 5 seconds of failure, log all buttons on the page for debugging
                if (!dumpedHtml) {
                    dumpedHtml = true;
                    try {
                        String allButtons = (String) page.evaluate(
                            "() => Array.from(document.querySelectorAll('button')).map(b => b.innerText.trim() + ' | class=' + b.className).join('\\n')");
                        AppLogger.info("=== ALL BUTTONS ON PAGE ===\n" + allButtons);
                        AppLogger.info("=== PAGE URL: " + page.url() + " ===");
                    } catch (Exception e) { /* ignore */ }
                }

                Thread.sleep(500);
            }

            if (!clicked) {
                AppLogger.warn("Easy Apply button not found after 60 seconds. Check logs above for button list.");
                return false;
            }


            
            updateStatus("Navigating Easy Apply Modal...");
            AppLogger.info("Clicked Easy Apply button.");
            
            // 2. Step through the modal
            for (int i = 0; i < 30; i++) {
                // Wait very briefly for modal transitions
                Thread.sleep(200);
                
                // Check for Submit button first
                Locator submitBtn = page.locator("button[aria-label='Submit application']");
                if (submitBtn.count() > 0 && submitBtn.first().isVisible()) {
                    updateStatus("Submitting Application...");
                    submitBtn.first().click();
                    AppLogger.info("Clicked Submit Application!");
                    // Wait for the success modal
                    Thread.sleep(3000);
                    return true;
                }
                
                // Before clicking Next/Review, try to auto-fill visible form fields
                autoFillFormFields(page);
                
                // Check for Review button
                Locator reviewBtn = page.locator("button[aria-label='Review your application']");
                if (reviewBtn.count() > 0 && reviewBtn.first().isVisible()) {
                    updateStatus("Reviewing Application...");
                    reviewBtn.first().click();
                    AppLogger.info("Clicked Review button.");
                    continue;
                }
                
                // Check for Next button
                Locator nextBtn = page.locator("button[aria-label='Continue to next step']");
                if (nextBtn.count() > 0 && nextBtn.first().isVisible()) {
                    updateStatus("Proceeding to Next Step...");
                    nextBtn.first().click();
                    AppLogger.info("Clicked Next button.");
                    continue;
                }
                
                // If we get here and none of the buttons are found or visible, we might be stuck
                // Look for generic primary buttons in the modal footer
                Locator genericPrimary = page.locator(".artdeco-modal__actionbar .artdeco-button--primary");
                if (genericPrimary.count() > 0 && genericPrimary.first().isVisible()) {
                    // Check if it's disabled (means a required field is empty)
                    Boolean isDisabledObj = (Boolean) genericPrimary.first().evaluate(
                        "el => el.disabled === true || el.getAttribute('aria-disabled') === 'true' || el.classList.contains('disabled')"
                    );
                    if (isDisabledObj != null && isDisabledObj) {
                        AppLogger.warn("Primary button is disabled. Stuck on a required field.");
                        return false;
                    }
                    
                    genericPrimary.first().click();
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
            StringBuilder sb = new StringBuilder();
            sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
            for (StackTraceElement el : e.getStackTrace()) {
                sb.append("  at ").append(el.toString()).append("\n");
            }
            AppLogger.error("Error during LinkedIn Easy Apply flow. Details:\n" + sb.toString());
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
