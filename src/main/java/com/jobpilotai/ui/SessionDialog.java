package com.jobpilotai.ui;

import com.jobpilotai.model.SavedSession;
import com.jobpilotai.service.SessionService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

/**
 * Modal dialog asking the user whether to resume a previous session or
 * start a new one.
 *
 * @author JobPilotAI Team
 * @version 1.0.0
 */
public final class SessionDialog {

    private SessionDialog() {}

    /**
     * Shows the session dialog centred on the owner stage.
     *
     * @param owner          the parent stage
     * @param previousSession the session to potentially resume
     * @param sessionService  the service used to clear the old session
     */
    public static void show(Stage owner, SavedSession previousSession, SessionService sessionService) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setResizable(false);

        // ── Root ──────────────────────────────────────────────────────────
        VBox root = new VBox(20);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.TOP_LEFT);
        root.setMinWidth(420);
        root.setStyle("""
            -fx-background-color: #1E293B;
            -fx-background-radius: 16;
            -fx-border-color: #334155;
            -fx-border-radius: 16;
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 24, 0, 0, 8);
        """);

        // ── Icon + Title ─────────────────────────────────────────────────
        Label icon = new Label("🔄");
        icon.setStyle("-fx-font-size: 32px;");

        Label title = new Label("Previous Session Found");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label subtitle = new Label("A previous JobPilotAI session was detected.\nWhat would you like to do?");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-wrap-text: true;");

        // ── Session info card ────────────────────────────────────────────
        VBox card = new VBox(6);
        card.setPadding(new Insets(14));
        card.setStyle("""
            -fx-background-color: #0F172A;
            -fx-background-radius: 10;
            -fx-border-color: #334155;
            -fx-border-radius: 10;
            -fx-border-width: 1;
        """);
        Label savedAt = new Label("💾  Saved: " + (previousSession.getSavedAt() != null
                ? previousSession.getSavedAt() : "Unknown"));
        savedAt.setStyle("-fx-font-size: 12px; -fx-text-fill: #CBD5E1;");
        card.getChildren().add(savedAt);

        // ── Buttons ───────────────────────────────────────────────────────
        Button resumeBtn = new Button("▶   Resume Previous Session");
        resumeBtn.setPrefWidth(Double.MAX_VALUE);
        resumeBtn.setPrefHeight(44);
        resumeBtn.setStyle("""
            -fx-background-color: linear-gradient(to right, #3B82F6, #2563EB);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-font-size: 13px;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """);
        resumeBtn.setOnMouseEntered(e ->
            resumeBtn.setStyle(resumeBtn.getStyle().replace("#3B82F6", "#60A5FA").replace("#2563EB", "#3B82F6")));
        resumeBtn.setOnMouseExited(e ->
            resumeBtn.setStyle(resumeBtn.getStyle().replace("#60A5FA", "#3B82F6").replace("#3B82F6, #60A5FA", "#3B82F6, #2563EB")));

        Button newBtn = new Button("✦   Start New Session");
        newBtn.setPrefWidth(Double.MAX_VALUE);
        newBtn.setPrefHeight(44);
        newBtn.setStyle("""
            -fx-background-color: #334155;
            -fx-text-fill: #CBD5E1;
            -fx-font-weight: bold;
            -fx-font-size: 13px;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """);
        newBtn.setOnMouseEntered(e -> newBtn.setStyle(newBtn.getStyle().replace("#334155", "#475569")));
        newBtn.setOnMouseExited(e  -> newBtn.setStyle(newBtn.getStyle().replace("#475569", "#334155")));

        resumeBtn.setOnAction(e -> dialog.close());  // Simply close — session data remains
        newBtn.setOnAction(e -> {
            sessionService.clearSession();
            dialog.close();
        });

        root.getChildren().addAll(icon, title, subtitle, card, resumeBtn, newBtn);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        try {
            scene.getStylesheets().add(Objects.requireNonNull(
                    SessionDialog.class.getResource("/css/base.css")).toExternalForm());
        } catch (Exception ignored) {}

        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.show();
    }
}
