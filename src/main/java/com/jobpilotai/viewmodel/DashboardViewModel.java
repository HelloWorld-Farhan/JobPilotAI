package com.jobpilotai.viewmodel;

import com.jobpilotai.service.ApplicationService;
import javafx.beans.property.*;

/**
 * ViewModel for the Dashboard view, exposing observable statistics properties.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class DashboardViewModel {

    private final ApplicationService appService = ApplicationService.getInstance();

    private final IntegerProperty applicationsToday  = new SimpleIntegerProperty(0);
    private final IntegerProperty successful          = new SimpleIntegerProperty(0);
    private final IntegerProperty alreadyApplied     = new SimpleIntegerProperty(0);
    private final IntegerProperty failed              = new SimpleIntegerProperty(0);
    private final IntegerProperty pendingOtp          = new SimpleIntegerProperty(0);
    private final IntegerProperty pendingCaptcha      = new SimpleIntegerProperty(0);
    private final StringProperty  runningTask         = new SimpleStringProperty("Idle");
    private final StringProperty  currentWebsite      = new SimpleStringProperty("—");
    private final StringProperty  currentCompany      = new SimpleStringProperty("—");
    private final StringProperty  currentJob          = new SimpleStringProperty("—");
    private final StringProperty  systemStatus        = new SimpleStringProperty("Ready");
    private final DoubleProperty  progress            = new SimpleDoubleProperty(0.0);

    /** Refreshes all statistics from the database. */
    public void refresh() {
        applicationsToday .set(appService.countToday());
        successful        .set(appService.countTodaySuccess());
        alreadyApplied    .set(appService.countTodayAlreadyApplied());
        failed            .set(appService.countTodayFailed());
        pendingOtp        .set(appService.countTodayPendingOtp());
        pendingCaptcha    .set(appService.countTodayPendingCaptcha());
    }

    // ── Property accessors ──────────────────────────────────────────────────

    public IntegerProperty applicationsTodayProperty() { return applicationsToday; }
    public IntegerProperty successfulProperty()         { return successful; }
    public IntegerProperty alreadyAppliedProperty()    { return alreadyApplied; }
    public IntegerProperty failedProperty()             { return failed; }
    public IntegerProperty pendingOtpProperty()         { return pendingOtp; }
    public IntegerProperty pendingCaptchaProperty()     { return pendingCaptcha; }
    public StringProperty  runningTaskProperty()        { return runningTask; }
    public StringProperty  currentWebsiteProperty()     { return currentWebsite; }
    public StringProperty  currentCompanyProperty()     { return currentCompany; }
    public StringProperty  currentJobProperty()         { return currentJob; }
    public StringProperty  systemStatusProperty()       { return systemStatus; }
    public DoubleProperty  progressProperty()           { return progress; }

    public void setRunningTask(String task)     { runningTask.set(task); }
    public void setCurrentWebsite(String site)  { currentWebsite.set(site); }
    public void setCurrentCompany(String co)    { currentCompany.set(co); }
    public void setCurrentJob(String job)       { currentJob.set(job); }
    public void setSystemStatus(String status)  { systemStatus.set(status); }
    public void setProgress(double value)       { progress.set(value); }
}
