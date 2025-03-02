package pro.akii.ks.core.fieldforge.fields;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.akii.ks.core.fieldforge.FieldForgePlugin;
import pro.akii.ks.core.fieldforge.events.FieldEnterEvent;
import pro.akii.ks.core.fieldforge.events.FieldExitEvent;
import pro.akii.ks.core.fieldforge.particles.NMSUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class FieldManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldManager.class);
    private final FieldForgePlugin plugin;
    private final List<VectorField> fields;
    private final File fieldsFile;
    private final Map<UUID, Integer> playerFieldCounts;
    private final Map<Entity, Set<Integer>> entitiesInFields;
    private final NMSUtil nmsUtil;

    /**
     * Constructs a new FieldManager instance.
     *
     * @param plugin The main plugin instance.
     */
    public FieldManager(FieldForgePlugin plugin) {
        this.plugin = plugin;
        this.fields = new ArrayList<>();
        this.fieldsFile = new File(plugin.getDataFolder(), "fields.yml");
        this.playerFieldCounts = new HashMap<>();
        this.entitiesInFields = new HashMap<>();
        this.nmsUtil = new NMSUtil();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Creates a new vector field if within configured limits for the player.
     *
     * @param field The vector field to add.
     * @param creator The UUID of the player creating the field, or null if API-created.
     * @return True if the field was created, false if limit reached.
     */
    public boolean createField(VectorField field, UUID creator) {
        int maxFields = plugin.getConfigManager().getMaxFieldsPerPlayer();
        int currentCount = creator != null ? playerFieldCounts.getOrDefault(creator, 0) : 0;
        if (creator != null && currentCount >= maxFields) {
            LOGGER.warn("Player {} reached field limit: {}", creator, maxFields);
            return false;
        }
        fields.add(field);
        if (creator != null) {
            playerFieldCounts.put(creator, currentCount + 1);
        }
        LOGGER.info("Created field: {} at {} by {}", field.getClass().getSimpleName(), field.getLocation(), creator);
        if (field.getDurationTicks() > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> removeField(fields.indexOf(field), null), field.getDurationTicks());
        }
        return true;
    }

    /**
     * Removes a field by its index if the player has permission.
     *
     * @param index The index of the field to remove.
     * @param requester The UUID of the player requesting removal, or null for API.
     * @return True if removed, false if index invalid or permission denied.
     */
    public boolean removeField(int index, UUID requester) {
        if (index < 0 || index >= fields.size()) {
            LOGGER.warn("Invalid field index for removal: {}", index);
            return false;
        }
        VectorField field = fields.get(index);
        if (requester != null && field.getCreator() != null && !field.getCreator().equals(requester) &&
            !plugin.getServer().getPlayer(requester).hasPermission("fieldforge.admin")) {
            LOGGER.warn("Player {} lacks permission to remove field at index {}", requester, index);
            return false;
        }
        fields.remove(index);
        if (field.getCreator() != null) {
            playerFieldCounts.compute(field.getCreator(), (k, v) -> v == null || v <= 1 ? null : v - 1);
        }
        LOGGER.info("Removed field: {} at {} by {}", field.getClass().getSimpleName(), field.getLocation(), requester);
        return true;
    }

    /**
     * Updates all fields, applying forces with collision detection and rendering visuals using NMS.
     */
    public void updateFields() {
        Map<Entity, Vector> entityForces = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            VectorField field = fields.get(i);
            if (!field.isActive()) continue;
            if (!field.getLocation().getWorld().isChunkLoaded(field.getLocation().getBlockX() >> 4, field.getLocation().getBlockZ() >> 4)) {
                continue;
            }
            List<Entity> entities = field.getLocation().getWorld()
                .getNearbyEntities(field.getLocation(), field.getRange(), field.getRange(), field.getRange())
                .stream()
                .filter(e -> !e.isDead())
                .toList();
            for (Entity entity : entities) {
                Vector currentForce = entityForces.getOrDefault(entity, new Vector(0, 0, 0));
                Vector newForce = field.calculateForce(entity);
                if (newForce != null) {
                    entityForces.put(entity, currentForce.add(newForce));
                    Set<Integer> currentFields = entitiesInFields.getOrDefault(entity, new HashSet<>());
                    if (!currentFields.contains(i)) {
                        currentFields.add(i);
                        entitiesInFields.put(entity, currentFields);
                        Bukkit.getPluginManager().callEvent(new FieldEnterEvent(entity, field));
                    }
                }
            }
            entitiesInFields.entrySet().removeIf(entry -> {
                Set<Integer> fieldIndices = entry.getValue();
                if (fieldIndices.contains(i) && !entities.contains(entry.getKey())) {
                    fieldIndices.remove(i);
                    Bukkit.getPluginManager().callEvent(new FieldExitEvent(entry.getKey(), field));
                    return fieldIndices.isEmpty();
                }
                return false;
            });
            if (field.isVisualsEnabled()) {
                plugin.getParticleManager().renderField(field);
            }
        }
        double maxForce = plugin.getConfigManager().getMaxForce();
        for (Map.Entry<Entity, Vector> entry : entityForces.entrySet()) {
            Vector force = entry.getValue();
            if (force.lengthSquared() > maxForce * maxForce) {
                force.normalize().multiply(maxForce);
            }
            nmsUtil.applyForce(entry.getKey(), force);
        }
    }

    /**
     * Removes all active fields.
     */
    public void clearFields() {
        fields.clear();
        playerFieldCounts.clear();
        entitiesInFields.clear();
        LOGGER.info("All fields cleared.");
    }

    /**
     * Saves all active fields to fields.yml.
     */
    public void saveFields() {
        YamlConfiguration config = new YamlConfiguration();
        List<String> fieldData = new ArrayList<>();
        for (VectorField field : fields) {
            String type = field.getClass().getSimpleName().toLowerCase().replace("field", "");
            Location loc = field.getLocation();
            String direction = field instanceof LinearField ? ((LinearField) field).getDirection().toString() : "none";
            String creator = field.getCreator() != null ? field.getCreator().toString() : "none";
            String entry = String.format("%s,%s,%f,%d,%s,%s,%d,%b",
                type, loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ(),
                field.getStrength(), field.getRange(), direction, creator, field.getDurationTicks(), field.isActive());
            fieldData.add(entry);
        }
        config.set("fields", fieldData);
        try {
            config.save(fieldsFile);
            LOGGER.info("Saved {} fields to fields.yml", fields.size());
        } catch (IOException e) {
            LOGGER.error("Failed to save fields: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads fields from fields.yml.
     */
    public void loadFields() {
        if (!fieldsFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(fieldsFile);
        List<String> fieldData = config.getStringList("fields");
        for (String entry : fieldData) {
            try {
                String[] parts = entry.split(",");
                String type = parts[0];
                Location loc = new Location(
                    plugin.getServer().getWorld(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]),
                    Double.parseDouble(parts[4])
                );
                double strength = Double.parseDouble(parts[5]);
                int range = Integer.parseInt(parts[6]);
                UUID creator = "none".equals(parts[8]) ? null : UUID.fromString(parts[8]);
                long duration = Long.parseLong(parts[9]);
                boolean active = Boolean.parseBoolean(parts[10]);
                VectorField field;
                switch (type) {
                    case "radial":
                        field = new RadialField(loc, strength, range, creator, duration);
                        break;
                    case "linear":
                        String[] dirParts = parts[7].replace("Vector(", "").replace(")", "").split(", ");
                        Vector direction = new Vector(
                            Double.parseDouble(dirParts[0].split("=")[1]),
                            Double.parseDouble(dirParts[1].split("=")[1]),
                            Double.parseDouble(dirParts[2].split("=")[1])
                        );
                        field = new LinearField(loc, strength, range, direction, creator, duration);
                        break;
                    case "vortex":
                        field = new VortexField(loc, strength, range, creator, duration);
                        break;
                    default:
                        continue;
                }
                field.setActive(active);
                fields.add(field);
                if (creator != null) {
                    playerFieldCounts.compute(creator, (k, v) -> v == null ? 1 : v + 1);
                }
                if (duration > 0) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> removeField(fields.indexOf(field), null), duration);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load field: {}", entry, e);
            }
        }
        LOGGER.info("Loaded {} fields from fields.yml", fields.size());
    }

    /**
     * Modifies the strength of a field at the given index.
     *
     * @param index The index of the field to modify.
     * @param newStrength The new strength value.
     * @param requester The UUID of the player requesting the change, or null for API.
     * @return True if modified, false if invalid or permission denied.
     */
    public boolean modifyFieldStrength(int index, double newStrength, UUID requester) {
        if (index < 0 || index >= fields.size()) {
            LOGGER.warn("Invalid field index for modification: {}", index);
            return false;
        }
        VectorField field = fields.get(index);
        if (requester != null && field.getCreator() != null && !field.getCreator().equals(requester) &&
            !plugin.getServer().getPlayer(requester).hasPermission("fieldforge.admin")) {
            LOGGER.warn("Player {} lacks permission to modify field at index {}", requester, index);
            return false;
        }
        field.setStrength(newStrength);
        LOGGER.info("Modified field strength at index {} to {} by {}", index, newStrength, requester);
        return true;
    }

    /**
     * Gets fields owned by a specific player.
     *
     * @param playerUUID The UUID of the player.
     * @return List of fields owned by the player.
     */
    public List<VectorField> getFieldsByPlayer(UUID playerUUID) {
        return fields.stream()
            .filter(f -> f.getCreator() != null && f.getCreator().equals(playerUUID))
            .toList();
    }

    /**
     * Toggles the active state of a field.
     *
     * @param index The index of the field to toggle.
     * @param requester The UUID of the player requesting the toggle, or null for API.
     * @return True if toggled, false if invalid or permission denied.
     */
    public boolean toggleFieldActive(int index, UUID requester) {
        if (index < 0 || index >= fields.size()) {
            LOGGER.warn("Invalid field index for toggle: {}", index);
            return false;
        }
        VectorField field = fields.get(index);
        if (requester != null && field.getCreator() != null && !field.getCreator().equals(requester) &&
            !plugin.getServer().getPlayer(requester).hasPermission("fieldforge.admin")) {
            LOGGER.warn("Player {} lacks permission to toggle field at index {}", requester, index);
            return false;
        }
        field.setActive(!field.isActive());
        LOGGER.info("Field at index {} set to active: {} by {}", index, field.isActive(), requester);
        return true;
    }
}