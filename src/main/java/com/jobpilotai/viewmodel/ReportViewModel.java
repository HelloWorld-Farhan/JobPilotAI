package com.jobpilotai.viewmodel;

import com.jobpilotai.model.Report;
import com.jobpilotai.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Reports view.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public class ReportViewModel {

    private final ReportService              service = ReportService.getInstance();
    private final ObservableList<Report>     items   = FXCollections.observableArrayList();

    public void loadAll() {
        items.setAll(service.getAllReports());
    }

    public Report generateManual() throws Exception {
        Report r = service.generateManualReport();
        loadAll();
        return r;
    }

    public Report generateHourly() throws Exception {
        Report r = service.generateHourlyReport();
        loadAll();
        return r;
    }

    public Report generateFinal() throws Exception {
        Report r = service.generateFinalReport();
        loadAll();
        return r;
    }

    public void delete(int id) {
        service.deleteReport(id);
        loadAll();
    }

    public ObservableList<Report> getItems() { return items; }
}
