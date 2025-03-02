package pro.akii.ks.core.fieldforge.utils;

import lombok.Getter;
import pro.akii.ks.core.fieldforge.FieldForgePlugin;

@Getter
public class ConfigManager {
    private final FieldForgePlugin plugin;
    private final int maxFieldsPerPlayer;
    private final double particleDensity;
    private final double maxForce;
    private final boolean vortexLeavesEnabled;

    /**
     * Constructs a new ConfigManager instance.
     *
     * @param plugin The main plugin instance.
     */
    public ConfigManager(FieldForgePlugin plugin) {
        this.plugin = plugin;
        this.maxFieldsPerPlayer = plugin.getConfig().getInt("vector-fields.max-per-player", 3);
        this.particleDensity = plugin.getConfig().getDouble("vector-fields.particle-density", 0.5);
        this.maxForce = plugin.getConfig().getDouble("vector-fields.max-force", 5.0);
        this.vortexLeavesEnabled = plugin.getConfig().getBoolean("vector-fields.environmental-effects.vortex-leaves", true);
    }

    /**
     * Retrieves the configured sound effect for a field type.
     *
     * @param fieldType The type of field (radial, linear, vortex).
     * @return The sound identifier.
     */
    public String getSoundEffect(String fieldType) {
        return plugin.getConfig().getString("vector-fields.sound-effects." + fieldType, "minecraft:entity.ender_eye.ambient");
    }

    /**
     * Retrieves the configured particle type for a field type.
     *
     * @param fieldType The type of field (radial, linear, vortex).
     * @return The particle type identifier.
     */
    public String getParticleType(String fieldType) {
        return plugin.getConfig().getString("vector-fields.particle-types." + fieldType, "ELECTRIC_SPARK").toLowerCase().replace("minecraft:", "");
    }
}