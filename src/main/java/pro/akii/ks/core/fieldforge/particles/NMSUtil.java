package pro.akii.ks.core.fieldforge.particles;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class NMSUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(NMSUtil.class);
    private static final String NMS_VERSION = "v1_21_R1";

    /**
     * Spawns a particle at the specified location using NMS packets.
     *
     * @param location The location to spawn the particle.
     * @param particleType The particle type identifier.
     */
    public void spawnParticle(Location location, String particleType) {
        try {
            Class<?> craftWorldClass = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".CraftWorld");
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutWorldParticles");
            Class<?> particleParamClass = Class.forName("net.minecraft.core.particles.ParticleParam");
            Class<?> particleTypesClass = Class.forName("net.minecraft.core.particles.ParticleTypes");
            Object craftWorld = craftWorldClass.cast(location.getWorld());
            Method getHandle = craftWorldClass.getMethod("getHandle");
            Object nmsWorld = getHandle.invoke(craftWorld);

            Object particle = particleTypesClass.getField(particleType.toUpperCase().replace("minecraft:", "")).get(null);
            Object packet = packetClass.getConstructor(particleParamClass, boolean.class, double.class, double.class, double.class,
                float.class, float.class, float.class, float.class, int.class)
                .newInstance(particle, true, location.getX(), location.getY(), location.getZ(), 0, 0, 0, 0, 1);

            for (Player player : location.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(location) > 256) continue;
                Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object connection = nmsPlayer.getClass().getField("b").get(nmsPlayer);
                connection.getClass().getMethod("sendPacket", packetClass).invoke(connection, packet);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to spawn particle: {}", e.getMessage(), e);
        }
    }

    /**
     * Plays a sound at the specified location using NMS packets.
     *
     * @param location The location to play the sound.
     * @param sound The sound identifier.
     * @param volume The sound volume.
     * @param pitch The sound pitch.
     */
    public void playSound(Location location, String sound, float volume, float pitch) {
        try {
            Class<?> craftWorldClass = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".CraftWorld");
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect");
            Object craftWorld = craftWorldClass.cast(location.getWorld());
            Method getHandle = craftWorldClass.getMethod("getHandle");
            Object nmsWorld = getHandle.invoke(craftWorld);

            Object packet = packetClass.getConstructor(
                String.class, double.class, double.class, double.class, float.class, float.class
            ).newInstance(sound, location.getX(), location.getY(), location.getZ(), volume, pitch);

            for (Player player : location.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(location) > 256) continue;
                Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object connection = nmsPlayer.getClass().getField("b").get(nmsPlayer);
                connection.getClass().getMethod("sendPacket", packetClass).invoke(connection, packet);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to send sound packet: {}", e.getMessage(), e);
        }
    }

    /**
     * Applies a force to an entity using NMS for direct velocity manipulation.
     *
     * @param entity The entity to affect.
     * @param force The force vector to apply.
     */
    public void applyForce(Entity entity, Vector force) {
        try {
            Object nmsEntity = entity.getClass().getMethod("getHandle").invoke(entity);
            Class<?> nmsEntityClass = nmsEntity.getClass();
            Vector currentVelocity = entity.getVelocity();
            Vector newVelocity = currentVelocity.add(force);
            nmsEntityClass.getMethod("setDeltaMovement", double.class, double.class, double.class)
                .invoke(nmsEntity, newVelocity.getX(), newVelocity.getY(), newVelocity.getZ());
            nmsEntityClass.getField("hasImpulse").set(nmsEntity, true);
        } catch (Exception e) {
            LOGGER.error("Failed to apply force to entity: {}", e.getMessage(), e);
            entity.setVelocity(entity.getVelocity().add(force)); // Fallback to Spigot API
        }
    }
}