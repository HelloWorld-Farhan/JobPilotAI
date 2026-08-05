package com.jobpilotai.service;

import com.jobpilotai.logs.AppLogger;
import com.jobpilotai.model.SavedSession;
import com.jobpilotai.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Service for saving and restoring application sessions.
 * <p>
 * Session data is serialised to JSON and stored in the database.
 * On startup the service checks for a previous session and, if found,
 * prompts the user to resume or start fresh.
 * </p>
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class SessionService {

    private static SessionService instance;
    private final SessionRepository repo   = new SessionRepository();
    private final ObjectMapper      mapper = new ObjectMapper();

    private SessionService() {}

    public static synchronized SessionService getInstance() {
        if (instance == null) instance = new SessionService();
        return instance;
    }

    /**
     * Checks for a previous session and shows a resume/new dialog if one exists.
     *
     * @param owner the owner stage for the dialog
     */
    public void checkAndPromptSession(Stage owner) {
        Optional<SavedSession> previous = repo.findLatest();
        if (previous.isPresent()) {
            AppLogger.info("Previous session found. Prompting user.");
            javafx.application.Platform.runLater(() ->
                    com.jobpilotai.ui.SessionDialog.show(owner, previous.get(), this));
        } else {
            AppLogger.info("No previous session found. Starting fresh.");
        }
    }

    /**
     * Saves the current session state to the database.
     *
     * @param note an optional human-readable label for the session
     */
    public void saveCurrentSession(String note) {
        try {
            ObjectNode node = mapper.createObjectNode();
            node.put("note",        note != null ? note : "");
            node.put("saved_at",    java.time.LocalDateTime.now().toString());
            node.put("app_count",   ApplicationService.getInstance().countAll());

            SavedSession session = new SavedSession(mapper.writeValueAsString(node));
            repo.save(session);
            AppLogger.info("Session saved.");
        } catch (Exception e) {
            AppLogger.error("SessionService.saveCurrentSession failed.", e);
        }
    }

    /** Clears saved session data (user chose "Start New Session"). */
    public void clearSession() {
        try {
            repo.clearAll();
            AppLogger.info("Session cleared.");
        } catch (Exception e) {
            AppLogger.error("SessionService.clearSession failed.", e);
        }
    }

    /** Returns the latest saved session, if any. */
    public Optional<SavedSession> getLatestSession() {
        return repo.findLatest();
    }
}
