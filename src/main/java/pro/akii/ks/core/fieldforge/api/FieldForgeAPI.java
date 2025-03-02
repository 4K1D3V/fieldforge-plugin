package pro.akii.ks.core.fieldforge.api;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import pro.akii.ks.core.fieldforge.FieldForgePlugin;
import pro.akii.ks.core.fieldforge.fields.FieldManager;
import pro.akii.ks.core.fieldforge.fields.VectorField;
import pro.akii.ks.core.fieldforge.fields.types.LinearField;
import pro.akii.ks.core.fieldforge.fields.types.RadialField;
import pro.akii.ks.core.fieldforge.fields.types.VortexField;

import java.util.List;
import java.util.UUID;

/**
 * Public API for interacting with FieldForge.
 */
public class FieldForgeAPI {
    private final FieldForgePlugin plugin;
    private final FieldManager fieldManager;

    /**
     * Constructs a new FieldForgeAPI instance.
     *
     * @param plugin The main plugin instance.
     */
    public FieldForgeAPI(FieldForgePlugin plugin) {
        this.plugin = plugin;
        this.fieldManager = plugin.getFieldManager();
    }

    /**
     * Creates a radial field programmatically with a creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param durationTicks The duration in ticks (0 for permanent).
     * @param creator The UUID of the field creator, or null if API-created.
     * @return True if created, false if limit reached.
     */
    public boolean createRadialField(Location location, double strength, int range, long durationTicks, UUID creator) {
        return fieldManager.createField(new RadialField(location, strength, range, creator, durationTicks), creator);
    }

    /**
     * Creates a radial field programmatically without a creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param durationTicks The duration in ticks (0 for permanent).
     * @return True if created, false if limit reached.
     */
    public boolean createRadialField(Location location, double strength, int range, long durationTicks) {
        return createRadialField(location, strength, range, durationTicks, null);
    }

    /**
     * Creates a linear field programmatically with a custom direction and creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param direction The direction vector.
     * @param durationTicks The duration in ticks (0 for permanent).
     * @param creator The UUID of the field creator, or null if API-created.
     * @return True if created, false if limit reached.
     */
    public boolean createLinearField(Location location, double strength, int range, Vector direction, long durationTicks, UUID creator) {
        return fieldManager.createField(new LinearField(location, strength, range, direction, creator, durationTicks), creator);
    }

    /**
     * Creates a linear field programmatically without a creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param direction The direction vector.
     * @param durationTicks The duration in ticks (0 for permanent).
     * @return True if created, false if limit reached.
     */
    public boolean createLinearField(Location location, double strength, int range, Vector direction, long durationTicks) {
        return createLinearField(location, strength, range, direction, durationTicks, null);
    }

    /**
     * Creates a vortex field programmatically with a creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param durationTicks The duration in ticks (0 for permanent).
     * @param creator The UUID of the field creator, or null if API-created.
     * @return True if created, false if limit reached.
     */
    public boolean createVortexField(Location location, double strength, int range, long durationTicks, UUID creator) {
        return fieldManager.createField(new VortexField(location, strength, range, creator, durationTicks), creator);
    }

    /**
     * Creates a vortex field programmatically without a creator.
     *
     * @param location The center of the field.
     * @param strength The force magnitude.
     * @param range The effective radius in blocks.
     * @param durationTicks The duration in ticks (0 for permanent).
     * @return True if created, false if limit reached.
     */
    public boolean createVortexField(Location location, double strength, int range, long durationTicks) {
        return createVortexField(location, strength, range, durationTicks, null);
    }

    /**
     * Removes a field by its index.
     *
     * @param index The index of the field to remove.
     * @param requester The UUID of the requester, or null for API.
     * @return True if removed, false if index invalid or permission denied.
     */
    public boolean removeField(int index, UUID requester) {
        return fieldManager.removeField(index, requester);
    }

    /**
     * Removes a field by its index without a requester (API use).
     *
     * @param index The index of the field to remove.
     * @return True if removed, false if index invalid.
     */
    public boolean removeField(int index) {
        return removeField(index, null);
    }

    /**
     * Modifies the strength of a field at the given index.
     *
     * @param index The index of the field to modify.
     * @param newStrength The new strength value.
     * @param requester The UUID of the requester, or null for API.
     * @return True if modified, false if index invalid or permission denied.
     */
    public boolean modifyFieldStrength(int index, double newStrength, UUID requester) {
        return fieldManager.modifyFieldStrength(index, newStrength, requester);
    }

    /**
     * Modifies the strength of a field at the given index without a requester (API use).
     *
     * @param index The index of the field to modify.
     * @param newStrength The new strength value.
     * @return True if modified, false if index invalid.
     */
    public boolean modifyFieldStrength(int index, double newStrength) {
        return modifyFieldStrength(index, newStrength, null);
    }

    /**
     * Toggles the active state of a field.
     *
     * @param index The index of the field to toggle.
     * @param requester The UUID of the requester, or null for API.
     * @return True if toggled, false if index invalid or permission denied.
     */
    public boolean toggleFieldActive(int index, UUID requester) {
        return fieldManager.toggleFieldActive(index, requester);
    }

    /**
     * Toggles the active state of a field without a requester (API use).
     *
     * @param index The index of the field to toggle.
     * @return True if toggled, false if index invalid.
     */
    public boolean toggleFieldActive(int index) {
        return toggleFieldActive(index, null);
    }

    /**
     * Gets all active fields.
     *
     * @return List of all vector fields.
     */
    public List<VectorField> getFields() {
        return fieldManager.getFields();
    }

    /**
     * Gets fields owned by a specific player.
     *
     * @param playerUUID The UUID of the player.
     * @return List of fields owned by the player.
     */
    public List<VectorField> getFieldsByPlayer(UUID playerUUID) {
        return fieldManager.getFieldsByPlayer(playerUUID);
    }
}