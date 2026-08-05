package com.jobpilotai.plugins;

/**
 * The standard interface for all JobPilotAI plugins.
 * Any external .jar must implement this interface in its main class
 * to be discovered and loaded by the PluginLoader.
 */
public interface IJobPilotPlugin {
    
    /**
     * @return The unique ID of the plugin (e.g. "com.example.myplugin")
     */
    String getId();

    /**
     * @return The display name of the plugin.
     */
    String getName();

    /**
     * @return The semantic version of the plugin (e.g. "1.0.0")
     */
    String getVersion();

    /**
     * @return A brief description of what this plugin does.
     */
    String getDescription();

    /**
     * Called when the plugin is enabled or the application starts.
     */
    void onEnable();

    /**
     * Called when the plugin is disabled or the application shuts down.
     */
    void onDisable();
}
