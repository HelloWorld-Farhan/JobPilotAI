package com.jobpilotai.viewmodel;

import com.jobpilotai.service.SettingsService;
import javafx.beans.property.*;

/**
 * ViewModel for the Settings view, exposing all configurable properties.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
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

    /** Loads current settings from the service into properties. */
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
    }

    /** Writes the current property values back to the service and persists them. */
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
        service.save();
    }

    // ── Property accessors ──────────────────────────────────────────────────

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
}
