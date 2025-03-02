package pro.akii.ks.core.fieldforge.fields.types;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import pro.akii.ks.core.fieldforge.fields.VectorField;
import pro.akii.ks.core.fieldforge.utils.VectorMath;

import java.util.UUID;

public class RadialField extends VectorField {
    /**
     * Constructs a new RadialField instance with duration and creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param creator The UUID of the field creator, or null if API-created.
     * @param durationTicks The duration in ticks before expiration (0 for permanent).
     */
    public RadialField(Location location, double strength, int range, UUID creator, long durationTicks) {
        super(location, strength, range, creator, durationTicks);
    }

    /**
     * Applies a radial force based on inverse square law: F = strength / r^2.
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
     * Calculates the radial force vector based on inverse square law.
     *
     * @param entity The entity to calculate force for.
     * @return The force vector, or null if out of range.
     */
    @Override
    public Vector calculateForce(Entity entity) {
        Location entityLoc = entity.getLocation();
        double distance = VectorMath.distance(location, entityLoc);
        if (distance > range || distance < 0.1) return null;

        double forceMagnitude = strength / (distance * distance);
        Vector direction = location.toVector().subtract(entityLoc.toVector()).normalize();
        return direction.multiply(forceMagnitude);
    }

    /**
     * Renders the field's visual effects using NMS.
     */
    @Override
    public void render() {
        FieldForgePlugin plugin = (FieldForgePlugin) location.getWorld().getPluginManager().getPlugin("FieldForge");
        plugin.getParticleManager().renderRadialField(location, range);
    }
}