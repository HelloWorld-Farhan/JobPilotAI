package com.jobpilotai.viewmodel;

import com.jobpilotai.service.SettingsService;
import javafx.beans.property.*;

public class SettingsViewModel {

    private final SettingsService service = SettingsService.getInstance();

    private final StringProperty  resumePath           = new SimpleStringProperty();
    private final StringProperty  defaultEmail         = new SimpleStringProperty();
    private final StringProperty  gasUrl               = new SimpleStringProperty();
    private final StringProperty  reportFolder         = new SimpleStringProperty();
    private final StringProperty  logFolder            = new SimpleStringProperty();
    private final StringProperty  theme                = new SimpleStringProperty();
    private final BooleanProperty darkMode             = new SimpleBooleanProperty();
    private final BooleanProperty enableNotifications  = new SimpleBooleanProperty();
    private final BooleanProperty enableEmail          = new SimpleBooleanProperty();
    private final BooleanProperty autoSave             = new SimpleBooleanProperty();
    private final BooleanProperty autoGenerateReports  = new SimpleBooleanProperty();
    private final BooleanProperty rememberWindowSize   = new SimpleBooleanProperty();
    private final BooleanProperty rememberWindowPosition = new SimpleBooleanProperty();
    
    private final BooleanProperty headlessMode         = new SimpleBooleanProperty();
    private final BooleanProperty screenshotOnError    = new SimpleBooleanProperty();
    private final StringProperty  timeoutMs            = new SimpleStringProperty();
    private final StringProperty  maxRetries           = new SimpleStringProperty();
    
    private final StringProperty  geminiApiKeys        = new SimpleStringProperty();
    private final StringProperty  groqApiKeys          = new SimpleStringProperty();
    private final BooleanProperty aiEnabled            = new SimpleBooleanProperty();

    private final StringProperty  currentSalary        = new SimpleStringProperty();
    private final StringProperty  expectedSalary       = new SimpleStringProperty();
    private final StringProperty  yearsExperience      = new SimpleStringProperty();
    private final BooleanProperty currentlyEmployed    = new SimpleBooleanProperty();
    private final BooleanProperty requireSponsorship   = new SimpleBooleanProperty();
    
    private final StringProperty  linkedInProfileUrl   = new SimpleStringProperty();

    public void load() {
        resumePath             .set(service.getResumePath());
        defaultEmail           .set(service.getDefaultEmail());
        gasUrl                 .set(service.getGasUrl());
        reportFolder           .set(service.getReportFolder());
        logFolder              .set(service.getLogFolder());
        theme                  .set(service.getTheme());
        darkMode               .set(service.isDarkMode());
        enableNotifications    .set(service.isEnableNotifications());
        enableEmail            .set(service.isEnableEmail());
        autoSave               .set(service.isAutoSave());
        autoGenerateReports    .set(service.isAutoGenerateReports());
        rememberWindowSize     .set(service.isRememberWindowSize());
        rememberWindowPosition .set(service.isRememberWindowPosition());
        
        headlessMode           .set(service.isHeadlessMode());
        screenshotOnError      .set(service.isScreenshotOnError());
        timeoutMs              .set(String.valueOf(service.getTimeoutMs()));
        maxRetries             .set(String.valueOf(service.getMaxRetries()));
        
        geminiApiKeys          .set(service.getGeminiApiKeys());
        groqApiKeys            .set(service.getGroqApiKeys());
        aiEnabled              .set(service.isAiEnabled());
        
        currentSalary          .set(service.getCurrentSalary());
        expectedSalary         .set(service.getExpectedSalary());
        yearsExperience        .set(service.getYearsExperience());
        currentlyEmployed      .set(service.isCurrentlyEmployed());
        requireSponsorship     .set(service.isRequireSponsorship());
        linkedInProfileUrl     .set(service.getLinkedInProfileUrl());
    }

    public void save() {
        service.setResumePath           (resumePath.get());
        service.setDefaultEmail         (defaultEmail.get());
        service.setGasUrl               (gasUrl.get());
        service.setReportFolder         (reportFolder.get());
        service.setLogFolder            (logFolder.get());
        service.setTheme                (theme.get());
        service.setDarkMode             (darkMode.get());
        service.setEnableNotifications  (enableNotifications.get());
        service.setEnableEmail          (enableEmail.get());
        service.setAutoSave             (autoSave.get());
        service.setAutoGenerateReports  (autoGenerateReports.get());
        service.setRememberWindowSize   (rememberWindowSize.get());
        service.setRememberWindowPosition(rememberWindowPosition.get());
        
        service.setHeadlessMode         (headlessMode.get());
        service.setScreenshotOnError    (screenshotOnError.get());
        try {
            service.setTimeoutMs(Integer.parseInt(timeoutMs.get()));
        } catch (NumberFormatException ignored) {}
        try {
            service.setMaxRetries(Integer.parseInt(maxRetries.get()));
        } catch (NumberFormatException ignored) {}
        
        service.setGeminiApiKeys        (geminiApiKeys.get());
        service.setGroqApiKeys          (groqApiKeys.get());
        service.setAiEnabled            (aiEnabled.get());
        
        service.setCurrentSalary        (currentSalary.get());
        service.setExpectedSalary       (expectedSalary.get());
        service.setYearsExperience      (yearsExperience.get());
        service.setCurrentlyEmployed    (currentlyEmployed.get());
        service.setRequireSponsorship   (requireSponsorship.get());
        service.setLinkedInProfileUrl   (linkedInProfileUrl.get());
        
        service.save();
    }

    public StringProperty  resumePathProperty()             { return resumePath; }
    public StringProperty  defaultEmailProperty()           { return defaultEmail; }
    public StringProperty  gasUrlProperty()                 { return gasUrl; }
    public StringProperty  reportFolderProperty()           { return reportFolder; }
    public StringProperty  logFolderProperty()              { return logFolder; }
    public StringProperty  themeProperty()                  { return theme; }
    public BooleanProperty darkModeProperty()               { return darkMode; }
    public BooleanProperty enableNotificationsProperty()    { return enableNotifications; }
    public BooleanProperty enableEmailProperty()            { return enableEmail; }
    public BooleanProperty autoSaveProperty()               { return autoSave; }
    public BooleanProperty autoGenerateReportsProperty()    { return autoGenerateReports; }
    public BooleanProperty rememberWindowSizeProperty()     { return rememberWindowSize; }
    public BooleanProperty rememberWindowPositionProperty() { return rememberWindowPosition; }
    
    public BooleanProperty headlessModeProperty()           { return headlessMode; }
    public BooleanProperty screenshotOnErrorProperty()      { return screenshotOnError; }
    public StringProperty  timeoutMsProperty()              { return timeoutMs; }
    public StringProperty  maxRetriesProperty()             { return maxRetries; }
    
    public StringProperty  geminiApiKeysProperty()          { return geminiApiKeys; }
    public StringProperty  groqApiKeysProperty()            { return groqApiKeys; }
    public BooleanProperty aiEnabledProperty()              { return aiEnabled; }
    
    public StringProperty  currentSalaryProperty()          { return currentSalary; }
    public StringProperty  expectedSalaryProperty()         { return expectedSalary; }
    public StringProperty  yearsExperienceProperty()        { return yearsExperience; }
    public BooleanProperty currentlyEmployedProperty()      { return currentlyEmployed; }
    public BooleanProperty requireSponsorshipProperty()     { return requireSponsorship; }
    
    public StringProperty  linkedInProfileUrlProperty()     { return linkedInProfileUrl; }
}
