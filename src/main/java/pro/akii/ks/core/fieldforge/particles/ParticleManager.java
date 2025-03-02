package pro.akii.ks.core.fieldforge.particles;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.akii.ks.core.fieldforge.FieldForgePlugin;

public class ParticleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticleManager.class);
    private final FieldForgePlugin plugin;
    private final NMSUtil nmsUtil;
    private int tickCounter;

    /**
     * Constructs a new ParticleManager instance.
     *
     * @param plugin The main plugin instance.
     */
    public ParticleManager(FieldForgePlugin plugin) {
        this.plugin = plugin;
        this.nmsUtil = new NMSUtil();
        this.tickCounter = 0;
    }

    /**
     * Renders particle effects for a radial field using NMS.
     *
     * @param location The field's center.
     * @param range The field's range.
     */
    public void renderRadialField(Location location, int range) {
        tickCounter++;
        if (tickCounter % 5 != 0) return;
        double density = plugin.getConfigManager().getParticleDensity();
        String particleType = plugin.getConfigManager().getParticleType("radial");
        for (double i = 0; i < range; i += density) {
            double x = Math.cos(i) * i;
            double z = Math.sin(i) * i;
            nmsUtil.spawnParticle(location.clone().add(x, 0, z), particleType);
        }
        if (tickCounter % 20 == 0) {
            nmsUtil.playSound(location, plugin.getConfigManager().getSoundEffect("radial"), 1.0F, 1.0F);
        }
    }

    /**
     * Renders particle effects for a linear field using NMS.
     *
     * @param location The field's center.
     * @param range The field's range.
     * @param direction The field's direction.
     */
    public void renderLinearField(Location location, int range, Vector direction) {
        tickCounter++;
        if (tickCounter % 5 != 0) return;
        double density = plugin.getConfigManager().getParticleDensity();
        String particleType = plugin.getConfigManager().getParticleType("linear");
        Vector step = direction.clone().multiply(density);
        Location current = location.clone();
        for (double i = 0; i < range; i += density) {
            nmsUtil.spawnParticle(current, particleType);
            current.add(step);
        }
        if (tickCounter % 20 == 0) {
            nmsUtil.playSound(location, plugin.getConfigManager().getSoundEffect("linear"), 1.0F, 1.0F);
        }
    }

    /**
     * Renders particle effects for a vortex field using NMS, including environmental effects.
     *
     * @param location The field's center.
     * @param range The field's range.
     */
    public void renderVortexField(Location location, int range) {
        tickCounter++;
        if (tickCounter % 5 != 0) return;
        double density = plugin.getConfigManager().getParticleDensity();
        String particleType = plugin.getConfigManager().getParticleType("vortex");
        for (double i = 0; i < range * 2 * Math.PI; i += density) {
            double x = Math.cos(i) * (range / 2.0);
            double z = Math.sin(i) * (range / 2.0);
            nmsUtil.spawnParticle(location.clone().add(x, i / 2.0, z), particleType);
        }
        if (plugin.getConfigManager().isVortexLeavesEnabled()) {
            nmsUtil.spawnParticle(location, "minecraft:falling_obsidian_tear");
        }
        if (tickCounter % 20 == 0) {
            nmsUtil.playSound(location, plugin.getConfigManager().getSoundEffect("vortex"), 1.0F, 1.0F);
        }
    }
}