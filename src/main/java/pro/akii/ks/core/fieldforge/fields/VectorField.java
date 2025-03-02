package pro.akii.ks.core.fieldforge.fields;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.UUID;

@Getter
public abstract class VectorField {
    protected final Location location;
    protected double strength;
    protected final int range;
    private final UUID creator;
    private final long durationTicks;
    @Setter
    private boolean visualsEnabled;
    @Setter
    private boolean active;

    /**
     * Constructs a new VectorField instance with duration and creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param creator The UUID of the field creator, or null if API-created.
     * @param durationTicks The duration in ticks before expiration (0 for permanent).
     */
    public VectorField(Location location, double strength, int range, UUID creator, long durationTicks) {
        this.location = location.clone();
        this.strength = strength;
        this.range = range;
        this.creator = creator;
        this.durationTicks = durationTicks;
        this.visualsEnabled = true;
        this.active = true;
    }

    /**
     * Applies the field's force to the specified entity.
     *
     * @param entity The entity to affect.
     */
    public abstract void applyForce(Entity entity);

    /**
     * Calculates the force vector for the specified entity.
     *
     * @param entity The entity to calculate force for.
     * @return The force vector, or null if out of range.
     */
    public abstract Vector calculateForce(Entity entity);

    /**
     * Renders the field's visual effects using NMS.
     */
    public abstract void render();

    /**
     * Sets the strength of the field.
     *
     * @param newStrength The new strength value.
     */
    protected void setStrength(double newStrength) {
        this.strength = newStrength;
    }
}