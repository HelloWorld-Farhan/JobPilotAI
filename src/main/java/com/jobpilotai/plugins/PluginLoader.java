package com.jobpilotai.plugins;

import com.jobpilotai.logs.AppLogger;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Discovers, loads, and manages plugins dynamically from the plugins folder.
 */
public class PluginLoader {
    private static final String PLUGINS_DIR = "plugins";
    private static final List<IJobPilotPlugin> activePlugins = new ArrayList<>();

    public static void initialize() {
        File dir = new File(PLUGINS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            AppLogger.info("No external plugins found.");
            return;
        }

        for (File file : files) {
            loadPlugin(file);
        }
    }

    private static void loadPlugin(File jarFile) {
        try {
            URL[] urls = { jarFile.toURI().toURL() };
            try (URLClassLoader classLoader = new URLClassLoader(urls, PluginLoader.class.getClassLoader());
                 JarFile jar = new JarFile(jarFile)) {

                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                        continue;
                    }

                    String className = entry.getName().substring(0, entry.getName().length() - 6);
                    className = className.replace('/', '.');

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (IJobPilotPlugin.class.isAssignableFrom(clazz) && !clazz.isInterface() && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                            
                            IJobPilotPlugin plugin = (IJobPilotPlugin) clazz.getDeclaredConstructor().newInstance();
                            activePlugins.add(plugin);
                            
                            AppLogger.info("Loaded Plugin: " + plugin.getName() + " v" + plugin.getVersion());
                            plugin.onEnable();
                        }
                    } catch (Exception e) {
                        // Class might not be the plugin entry point, ignore safely
                    }
                }
            }
        } catch (Exception e) {
            AppLogger.error("Failed to load plugin from: " + jarFile.getName(), e);
        }
    }

    public static List<IJobPilotPlugin> getActivePlugins() {
        return activePlugins;
    }
    
    public static void shutdown() {
        for (IJobPilotPlugin plugin : activePlugins) {
            try {
                plugin.onDisable();
            } catch (Exception e) {
                AppLogger.error("Error shutting down plugin: " + plugin.getName(), e);
            }
        }
    }
}
