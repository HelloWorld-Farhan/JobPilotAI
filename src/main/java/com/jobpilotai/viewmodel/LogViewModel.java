package com.jobpilotai.viewmodel;

import com.jobpilotai.model.LogEntry;
import com.jobpilotai.service.LogService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Logs viewer.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class LogViewModel {

    private final LogService              service = LogService.getInstance();
    private final ObservableList<LogEntry> items  = FXCollections.observableArrayList();
    private final StringProperty          searchQuery   = new SimpleStringProperty("");
    private final StringProperty          levelFilter   = new SimpleStringProperty("All");

    public void loadAll() {
        items.setAll(service.getAll());
    }

    public void search() {
        String q = searchQuery.get();
        if (q == null || q.isBlank()) { applyFilter(); return; }
        items.setAll(service.search(q));
    }

    public void applyFilter() {
        String level = levelFilter.get();
        if (level == null || "All".equalsIgnoreCase(level)) {
            items.setAll(service.getAll());
        } else {
            items.setAll(service.getByLevel(level));
        }
    }

    public void clearAll() {
        service.clearAll();
        items.clear();
    }

    public ObservableList<LogEntry> getItems()       { return items; }
    public StringProperty           searchQueryProperty()  { return searchQuery; }
    public StringProperty           levelFilterProperty()  { return levelFilter; }
}
