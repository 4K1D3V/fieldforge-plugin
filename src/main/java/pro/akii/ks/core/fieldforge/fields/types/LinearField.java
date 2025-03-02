package pro.akii.ks.core.fieldforge.fields.types;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import pro.akii.ks.core.fieldforge.fields.VectorField;
import pro.akii.ks.core.fieldforge.utils.VectorMath;

import java.util.UUID;

public class LinearField extends VectorField {
    private final Vector direction;

    /**
     * Constructs a new LinearField instance with custom direction, duration, and creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param direction The direction vector of the force.
     * @param creator The UUID of the field creator, or null if API-created.
     * @param durationTicks The duration in ticks before expiration (0 for permanent).
     */
    public LinearField(Location location, double strength, int range, Vector direction, UUID creator, long durationTicks) {
        super(location, strength, range, creator, durationTicks);
        this.direction = direction.clone().normalize();
    }

    /**
     * Applies a constant force in the specified direction.
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
     * Calculates the constant force vector in the specified direction.
     *
     * @param entity The entity to calculate force for.
     * @return The force vector, or null if out of range.
     */
    @Override
    public Vector calculateForce(Entity entity) {
        Location entityLoc = entity.getLocation();
        if (VectorMath.distance(location, entityLoc) > range) return null;
        return direction.clone().multiply(strength);
    }

    /**
     * Renders the field's visual effects using NMS.
     */
    @Override
    public void render() {
        FieldForgePlugin plugin = (FieldForgePlugin) location.getWorld().getPluginManager().getPlugin("FieldForge");
        plugin.getParticleManager().renderLinearField(location, range, direction);
    }

    /**
     * Gets the direction vector of the field.
     *
     * @return The direction vector.
     */
    public Vector getDirection() {
        return direction.clone();
    }
}