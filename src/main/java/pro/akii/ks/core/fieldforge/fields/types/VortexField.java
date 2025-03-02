package pro.akii.ks.core.fieldforge.fields.types;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import pro.akii.ks.core.fieldforge.fields.VectorField;
import pro.akii.ks.core.fieldforge.utils.VectorMath;

import java.util.UUID;

public class VortexField extends VectorField {
    /**
     * Constructs a new VortexField instance with duration and creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param creator The UUID of the field creator, or null if API-created.
     * @param durationTicks The duration in ticks before expiration (0 for permanent).
     */
    public VortexField(Location location, double strength, int range, UUID creator, long durationTicks) {
        super(location, strength, range, creator, durationTicks);
    }

    /**
     * Applies a tangential force to create a swirling effect around the field's center.
     *
     * @param entity The entity to affect.
     */
    @Override
    public void applyForce(Entity entity) {
        Vector force = calculateForce(entity);
        if (force != null) {
            entity.getWorld().getPluginManager().getPlugin("FieldForge").getFieldManager().getNmsUtil().applyForce(entity, force);
        }
    }

    /**
     * Calculates the tangential force vector for a swirling effect.
     *
     * @param entity The entity to calculate force for.
     * @return The force vector, or null if out of range.
     */
    @Override
    public Vector calculateForce(Entity entity) {
        Location entityLoc = entity.getLocation();
        double distance = VectorMath.distance(location, entityLoc);
        if (distance > range || distance < 0.1) return null;

        Vector toEntity = entityLoc.toVector().subtract(location.toVector());
        Vector tangential = new Vector(-toEntity.getZ(), 0, toEntity.getX()).normalize();
        double forceMagnitude = strength / distance;
        return tangential.multiply(forceMagnitude);
    }

    /**
     * Renders the field's visual effects using NMS, including environmental effects.
     */
    @Override
    public void render() {
        FieldForgePlugin plugin = (FieldForgePlugin) location.getWorld().getPluginManager().getPlugin("FieldForge");
        plugin.getParticleManager().renderVortexField(location, range);
    }
}