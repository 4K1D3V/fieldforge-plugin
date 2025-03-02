package pro.akii.ks.core.fieldforge.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pro.akii.ks.core.fieldforge.fields.VectorField;

/**
 * Event triggered when an entity exits a vector field.
 */
public class FieldExitEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Entity entity;
    private final VectorField field;
    private boolean cancelled;

    /**
     * Constructs a new FieldExitEvent.
     *
     * @param entity The entity exiting the field.
     * @param field The vector field exited.
     */
    public FieldExitEvent(Entity entity, VectorField field) {
        this.entity = entity;
        this.field = field;
        this.cancelled = false;
    }

    /**
     * Gets the entity that exited the field.
     *
     * @return The entity.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the vector field exited.
     *
     * @return The vector field.
     */
    public VectorField getField() {
        return field;
    }

    /**
     * Checks if the event is cancelled.
     *
     * @return True if cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of the event.
     *
     * @param cancel True to cancel the event, false to allow it.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return The handler list.
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static handler list for this event.
     *
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}