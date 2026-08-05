package com.jobpilotai.update;

import com.jobpilotai.config.AppConfig;
import com.jobpilotai.logs.AppLogger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Handles checking for application updates from a remote server.
 */
public class UpdateService {

    private static final String UPDATE_URL = "https://raw.githubusercontent.com/HelloWorld-Farhan/JobPilotAI/main/version.json";
    
    public static void checkForUpdates() {
        AppLogger.info("Checking for updates...");
        
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
                
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPDATE_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
                
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
              .thenAccept(response -> {
                  if (response.statusCode() == 200) {
                      String body = response.body();
                      // Expecting something like: {"latest_version": "v4.0.0"}
                      if (body.contains("\"latest_version\"")) {
                          AppLogger.info("Update check complete. Latest version JSON: " + body);
                          // Parsing skipped for brevity. If newer than AppConfig.APP_VERSION, prompt user.
                      }
                  } else {
                      AppLogger.warn("Update check failed with status: " + response.statusCode());
                  }
              })
              .exceptionally(ex -> {
                  AppLogger.warn("Failed to check for updates: " + ex.getMessage());
                  return null;
              });
    }
}
