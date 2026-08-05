package com.jobpilotai.viewmodel;

import com.jobpilotai.model.JobApplication;
import com.jobpilotai.service.ApplicationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * ViewModel for the Applications and History views.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ApplicationViewModel {

    private final ApplicationService             service = ApplicationService.getInstance();
    private final ObservableList<JobApplication> items   = FXCollections.observableArrayList();
    private final StringProperty                 searchQuery = new SimpleStringProperty("");
    private final StringProperty                 statusFilter = new SimpleStringProperty("All");

    /** Loads all applications from the database into the observable list. */
    public void loadAll() {
        items.setAll(service.getAll());
    }

    /** Searches by the current search query. */
    public void search() {
        String q = searchQuery.get();
        if (q == null || q.isBlank()) {
            loadAll();
        } else {
            items.setAll(service.search(q));
        }
    }

    /** Filters by the current status filter. */
    public void applyFilter() {
        String f = statusFilter.get();
        if (f == null || "All".equalsIgnoreCase(f)) {
            loadAll();
        } else {
            items.setAll(service.getByStatus(f));
        }
    }

    /**
     * Adds a new application.
     *
     * @param app the application to add
     * @throws Exception on validation or database error
     */
    public void add(JobApplication app) throws Exception {
        service.add(app);
        loadAll();
    }

    /**
     * Updates an existing application.
     *
     * @param app the application to update
     * @throws Exception on database error
     */
    public void update(JobApplication app) throws Exception {
        service.update(app);
        loadAll();
    }

    /**
     * Deletes an application by ID.
     *
     * @param id the application ID
     * @throws Exception on database error
     */
    public void delete(int id) throws Exception {
        service.delete(id);
        loadAll();
    }

    /** Deletes all applications. */
    public void deleteAll() throws Exception {
        service.deleteAll();
        items.clear();
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public ObservableList<JobApplication> getItems()         { return items; }
    public StringProperty                 searchQueryProperty()  { return searchQuery; }
    public StringProperty                 statusFilterProperty() { return statusFilter; }
}
