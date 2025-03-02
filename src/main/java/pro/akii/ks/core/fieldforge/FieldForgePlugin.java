package pro.akii.ks.core.fieldforge;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.akii.ks.core.fieldforge.api.FieldForgeAPI;
import pro.akii.ks.core.fieldforge.commands.FieldCommand;
import pro.akii.ks.core.fieldforge.fields.FieldManager;
import pro.akii.ks.core.fieldforge.particles.ParticleManager;
import pro.akii.ks.core.fieldforge.utils.ConfigManager;

@Getter
public class FieldForgePlugin extends JavaPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldForgePlugin.class);
    private FieldManager fieldManager;
    private ParticleManager particleManager;
    private ConfigManager configManager;
    private FieldForgeAPI api;

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        LOGGER.info("Starting FieldForge plugin...");
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        fieldManager = new FieldManager(this);
        particleManager = new ParticleManager(this);
        api = new FieldForgeAPI(this);
        FieldCommand command = new FieldCommand(this);
        getCommand("fieldforge").setExecutor(command);
        getCommand("fieldforge").setTabCompleter(command);
        getServer().getScheduler().runTaskTimer(this, fieldManager::updateFields, 0L, 1L);

        fieldManager.loadFields();
        LOGGER.info("FieldForge enabled successfully.");
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        fieldManager.saveFields();
        fieldManager.clearFields();
        LOGGER.info("FieldForge disabled.");
    }
}