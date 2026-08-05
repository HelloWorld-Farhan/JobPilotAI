package com.jobpilotai.automation.browser;

import com.jobpilotai.logs.AppLogger;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Manages the Playwright browser lifecycle.
 */
public class BrowserManager {
    private static BrowserManager instance;
    private Playwright playwright;
    private BrowserContext context;
    private Page page;

    private boolean headless = false;
    private int timeoutMs = 30000;

    private BrowserManager() {}

    public static synchronized BrowserManager getInstance() {
        if (instance == null) {
            instance = new BrowserManager();
        }
        return instance;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public void setTimeout(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public synchronized void start() {
        if (playwright == null) {
            AppLogger.info("Starting Playwright (headless=" + headless + ")…");
            playwright = Playwright.create();
            Path userDataDir = Paths.get("browser_data");
            
            context = playwright.chromium().launchPersistentContext(userDataDir, 
                new BrowserType.LaunchPersistentContextOptions()
                    .setHeadless(headless)
                    .setChannel("chrome"));
            context.setDefaultTimeout(timeoutMs);
            
            if (context.pages().isEmpty()) {
                page = context.newPage();
            } else {
                page = context.pages().get(0);
            }
            AppLogger.info("Browser session started successfully.");
        }
    }

    public synchronized void stop() {
        if (playwright != null) {
            AppLogger.info("Stopping Browser session…");
            if (context != null) context.close();
            playwright.close();
            
            page = null;
            context = null;
            playwright = null;
            AppLogger.info("Browser session stopped.");
        }
    }

    public Page getPage() {
        if (page == null) {
            start();
        }
        return page;
    }

    public boolean navigate(String url) {
        try {
            AppLogger.debug("Navigating to: " + url);
            getPage().navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            return true;
        } catch (Exception e) {
            AppLogger.error("Failed to navigate to: " + url, e);
            takeScreenshot("navigation_failed");
            return false;
        }
    }

    public Optional<String> takeScreenshot(String namePrefix) {
        if (page == null) return Optional.empty();
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            Path screenshotsDir = Paths.get("screenshots");
            if (!screenshotsDir.toFile().exists()) {
                screenshotsDir.toFile().mkdirs();
            }
            Path path = screenshotsDir.resolve(namePrefix + "_" + timestamp + ".png");
            page.screenshot(new Page.ScreenshotOptions().setPath(path));
            AppLogger.info("Saved screenshot: " + path.toString());
            return Optional.of(path.toString());
        } catch (Exception e) {
            AppLogger.error("Failed to take screenshot.", e);
            return Optional.empty();
        }
    }
}
