package org.correomqtt.plugin.update;

import org.correomqtt.plugin.manager.PluginSystem;
import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.UpdateManager;
import org.pf4j.update.UpdateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class PluginUpdateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUpdateManager.class);

    private final PluginSystem pluginSystem;

    public PluginUpdateManager(PluginSystem pluginSystem) {
        this.pluginSystem = pluginSystem;
    }

    public void updateSystem() throws MalformedURLException {

        LOGGER.info("Start Plugin Update");

        String jarFilePath = PluginUpdateManager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String path =  new File(jarFilePath).getParentFile().getPath() + File.separator + "plugins";
        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        if (new File(decodedPath + File.separator + "plugins.json").exists()) {
            List<UpdateRepository> repos = Collections.singletonList(new DefaultUpdateRepository("bundled", new File(decodedPath).toURI().toURL()));
            UpdateManager updateManager = new UpdateManager(pluginSystem, repos);
            updateExisitingPlugins(updateManager);
            installNewPlugins(updateManager);
        }else{
            LOGGER.info("No default plugins available.");
        }
    }

    private void installNewPlugins(UpdateManager updateManager) {
        // check for available (new) plugins
        if (updateManager.hasAvailablePlugins()) {
            List<PluginInfo> availablePlugins = updateManager.getAvailablePlugins();
            LOGGER.info("Found {} available plugins", availablePlugins.size());
            for ( PluginInfo plugin : availablePlugins ) {
                LOGGER.info("Found available plugin '{}'", plugin.id);
                PluginInfo.PluginRelease lastRelease = updateManager.getLastPluginRelease(plugin.id);
                String lastVersion = lastRelease.version;
                LOGGER.info("Install plugin '{}' with version {}", plugin.id, lastVersion);
                boolean installed = updateManager.installPlugin(plugin.id, lastVersion);
                if (installed) {
                    LOGGER.info("Installed plugin '{}'", plugin.id);
                } else {
                    LOGGER.error("Cannot install plugin '{}'", plugin.id);
                }
            }
        } else {
            LOGGER.info("No available plugins found");
        }
    }

    private void updateExisitingPlugins(UpdateManager updateManager){
        // check for updates
        if (updateManager.hasUpdates()) {
            List<PluginInfo> updates = updateManager.getUpdates();
            LOGGER.info("Found {} plugin updates", updates.size());
            for ( PluginInfo plugin : updates ) {
                LOGGER.info("Found update for plugin '{}'", plugin.id);
                PluginInfo.PluginRelease lastRelease = updateManager.getLastPluginRelease(plugin.id);
                String lastVersion = lastRelease.version;
                String installedVersion = pluginSystem.getPlugin(plugin.id).getDescriptor().getVersion();
                LOGGER.info("Update plugin '{}' from version {} to version {}", plugin.id, installedVersion, lastVersion);
                boolean updated = updateManager.updatePlugin(plugin.id, lastVersion);
                if (updated) {
                    LOGGER.info("Updated plugin '{}'", plugin.id);
                } else {
                    LOGGER.error("Cannot update plugin '{}'", plugin.id);
                }
            }
        } else {
            LOGGER.info("No updates found");
        }
    }
}