package pro.akii.ks.core.fieldforge.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import pro.akii.ks.core.fieldforge.FieldForgePlugin;
import pro.akii.ks.core.fieldforge.fields.FieldManager;
import pro.akii.ks.core.fieldforge.fields.VectorField;
import pro.akii.ks.core.fieldforge.fields.types.LinearField;
import pro.akii.ks.core.fieldforge.fields.types.RadialField;
import pro.akii.ks.core.fieldforge.fields.types.VortexField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FieldCommand implements CommandExecutor, TabCompleter, Listener {
    private final FieldForgePlugin plugin;
    private final FieldManager fieldManager;

    /**
     * Constructs a new FieldCommand instance.
     *
     * @param plugin The main plugin instance.
     */
    public FieldCommand(FieldForgePlugin plugin) {
        this.plugin = plugin;
        this.fieldManager = plugin.getFieldManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Handles execution of the /fieldforge command.
     *
     * @param sender The command sender.
     * @param command The command object.
     * @param label The command label.
     * @param args The command arguments.
     * @return True if the command was handled successfully.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("fieldforge.use")) {
            sender.sendMessage(ChatColor.RED + "You lack permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /fieldforge <create|remove|list|reload|modify|toggle|activate|deactivate|gui> [args]");
            return true;
        }

        UUID playerUUID = player.getUniqueId();
        switch (args[0].toLowerCase()) {
            case "create":
                handleCreateCommand(player, args, playerUUID);
                break;
            case "remove":
                handleRemoveCommand(player, args, playerUUID);
                break;
            case "list":
                handleListCommand(player, playerUUID);
                break;
            case "reload":
                handleReloadCommand(player);
                break;
            case "modify":
                handleModifyCommand(player, args, playerUUID);
                break;
            case "toggle":
                handleToggleCommand(player, args, playerUUID);
                break;
            case "activate":
                handleActivateCommand(player, args, playerUUID, true);
                break;
            case "deactivate":
                handleActivateCommand(player, args, playerUUID, false);
                break;
            case "gui":
                if (!player.hasPermission("fieldforge.gui")) {
                    player.sendMessage(ChatColor.RED + "You lack permission to use the GUI.");
                    return true;
                }
                handleGuiCommand(player, playerUUID);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }
        return true;
    }

    /**
     * Provides tab completion for the /fieldforge command.
     *
     * @param sender The command sender.
     * @param command The command object.
     * @param alias The command alias.
     * @param args The command arguments.
     * @return List of possible completions.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "remove", "list", "reload", "modify", "toggle", "activate", "deactivate", "gui"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                    completions.addAll(Arrays.asList("radial", "linear", "vortex"));
                    break;
                case "modify":
                    completions.add("strength");
                    break;
                case "remove":
                case "toggle":
                case "activate":
                case "deactivate":
                    for (int i = 0; i < fieldManager.getFields().size(); i++) {
                        completions.add(String.valueOf(i));
                    }
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("create")) {
                completions.add("<strength>");
            } else if (args[0].equalsIgnoreCase("modify") && args[1].equalsIgnoreCase("strength")) {
                for (int i = 0; i < fieldManager.getFields().size(); i++) {
                    completions.add(String.valueOf(i));
                }
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("create")) {
                completions.add("<range>");
            } else if (args[0].equalsIgnoreCase("modify") && args[1].equalsIgnoreCase("strength")) {
                completions.add("<value>");
            }
        } else if (args.length >= 5 && args.length <= 7 && args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("linear")) {
            completions.add(args.length == 5 ? "<x>" : args.length == 6 ? "<y>" : "<z>");
        } else if ((args.length == 8 && args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("linear")) ||
                   (args.length == 5 && args[0].equalsIgnoreCase("create") && !args[1].equalsIgnoreCase("linear"))) {
            completions.add("<duration>");
        }
        return completions;
    }

    private void handleCreateCommand(Player player, String[] args, UUID playerUUID) {
        if (args.length < 4 || (args[1].equalsIgnoreCase("linear") && args.length < 7)) {
            player.sendMessage(ChatColor.RED + "Usage: /fieldforge create <type> <strength> <range> [x y z] [duration]");
            return;
        }

        String type = args[1].toLowerCase();
        try {
            double strength = Double.parseDouble(args[2]);
            int range = Integer.parseInt(args[3]);
            long durationTicks = args.length > (type.equals("linear") ? 7 : 4) ? parseDuration(args[type.equals("linear") ? 7 : 4]) : 0;
            VectorField field;

            switch (type) {
                case "radial":
                    field = new RadialField(player.getLocation(), strength, range, playerUUID, durationTicks);
                    break;
                case "linear":
                    double x = Double.parseDouble(args[4]);
                    double y = Double.parseDouble(args[5]);
                    double z = Double.parseDouble(args[6]);
                    field = new LinearField(player.getLocation(), strength, range, new Vector(x, y, z), playerUUID, durationTicks);
                    break;
                case "vortex":
                    field = new VortexField(player.getLocation(), strength, range, playerUUID, durationTicks);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown field type: radial, linear, vortex expected.");
                    return;
            }

            if (fieldManager.createField(field, playerUUID)) {
                player.sendMessage(ChatColor.GREEN + type.substring(0, 1).toUpperCase() + type.substring(1) + " field created.");
            } else {
                player.sendMessage(ChatColor.RED + "Field limit reached.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid numbers for strength, range, or direction.");
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Failed to create field: " + e.getMessage());
            plugin.getLogger().error("Error creating field", e);
        }
    }

    private void handleRemoveCommand(Player player, String[] args, UUID playerUUID) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /fieldforge remove <index>");
            return;
        }

        try {
            int index = Integer.parseInt(args[1]);
            if (fieldManager.removeField(index, playerUUID)) {
                player.sendMessage(ChatColor.GREEN + "Field at index " + index + " removed.");
            } else {
                player.sendMessage(ChatColor.RED + "Invalid index or permission denied.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Index must be a number.");
        }
    }

    private void handleListCommand(Player player, UUID playerUUID) {
        List<VectorField> playerFields = fieldManager.getFieldsByPlayer(playerUUID);
        if (playerFields.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active fields owned by you.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Your Active Fields:");
        int index = 0;
        for (VectorField field : fieldManager.getFields()) {
            if (field.getCreator() != null && field.getCreator().equals(playerUUID)) {
                Location loc = field.getLocation();
                String type = field.getClass().getSimpleName().replace("Field", "").toLowerCase();
                String direction = field instanceof LinearField ? " Direction: " + ((LinearField) field).getDirection() : "";
                String duration = field.getDurationTicks() > 0 ? " Duration: " + (field.getDurationTicks() / 20) + "s" : "";
                player.sendMessage(ChatColor.GREEN + index + ": " + type + " at (" +
                    loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() +
                    "), strength=" + field.getStrength() + ", range=" + field.getRange() + direction + duration +
                    " [" + (field.isActive() ? "Active" : "Inactive") + "]");
            }
            index++;
        }
        if (player.hasPermission("fieldforge.admin")) {
            player.sendMessage(ChatColor.YELLOW + "All Fields (Admin View):");
            index = 0;
            for (VectorField field : fieldManager.getFields()) {
                Location loc = field.getLocation();
                String type = field.getClass().getSimpleName().replace("Field", "").toLowerCase();
                String owner = field.getCreator() != null ? field.getCreator().toString() : "None";
                player.sendMessage(ChatColor.YELLOW + index + ": " + type + " at (" +
                    loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ") by " + owner);
                index++;
            }
        }
    }

    private void handleReloadCommand(Player player) {
        plugin.reloadConfig();
        plugin.getConfigManager();
        player.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
        plugin.getLogger().info("Configuration reloaded by {}", player.getName());
    }

    private void handleModifyCommand(Player player, String[] args, UUID playerUUID) {
        if (args.length < 4 || !args[1].equalsIgnoreCase("strength")) {
            player.sendMessage(ChatColor.RED + "Usage: /fieldforge modify strength <index> <value>");
            return;
        }

        try {
            int index = Integer.parseInt(args[2]);
            double newStrength = Double.parseDouble(args[3]);
            if (fieldManager.modifyFieldStrength(index, newStrength, playerUUID)) {
                player.sendMessage(ChatColor.GREEN + "Field strength at index " + index + " set to " + newStrength);
            } else {
                player.sendMessage(ChatColor.RED + "Invalid index or permission denied.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Index and value must be numbers.");
        }
    }

    private void handleToggleCommand(Player player, String[] args, UUID playerUUID) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /fieldforge toggle <index>");
            return;
        }

        try {
            int index = Integer.parseInt(args[1]);
            if (index < 0 || index >= fieldManager.getFields().size()) {
                player.sendMessage(ChatColor.RED + "Invalid field index: " + index);
                return;
            }
            VectorField field = fieldManager.getFields().get(index);
            if (field.getCreator() != null && !field.getCreator().equals(playerUUID) && !player.hasPermission("fieldforge.admin")) {
                player.sendMessage(ChatColor.RED + "You don’t own this field.");
                return;
            }
            field.setVisualsEnabled(!field.isVisualsEnabled());
            player.sendMessage(ChatColor.GREEN + "Visuals for field at index " + index + " set to " + (field.isVisualsEnabled() ? "on" : "off"));
            plugin.getLogger().info("Player {} toggled visuals for field at index {} to {}", playerUUID, index, field.isVisualsEnabled());
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Index must be a number.");
        }
    }

    private void handleActivateCommand(Player player, String[] args, UUID playerUUID, boolean activate) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /fieldforge " + (activate ? "activate" : "deactivate") + " <index>");
            return;
        }

        try {
            int index = Integer.parseInt(args[1]);
            if (fieldManager.toggleFieldActive(index, playerUUID)) {
                player.sendMessage(ChatColor.GREEN + "Field at index " + index + " set to " + (activate ? "active" : "inactive"));
            } else {
                player.sendMessage(ChatColor.RED + "Invalid index or permission denied.");
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Index must be a number.");
        }
    }

    private void handleGuiCommand(Player player, UUID playerUUID) {
        Inventory gui = Bukkit.createInventory(player, 27, ChatColor.GREEN + "FieldForge Management");
        List<VectorField> playerFields = fieldManager.getFieldsByPlayer(playerUUID);
        int globalIndex = 0;
        for (VectorField field : fieldManager.getFields()) {
            if (field.getCreator() != null && field.getCreator().equals(playerUUID)) {
                int localIndex = playerFields.indexOf(field);
                ItemStack item = new ItemStack(Material.ENDER_PEARL);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + field.getClass().getSimpleName().replace("Field", "") + " #" + globalIndex);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.YELLOW + "Strength: " + field.getStrength());
                lore.add(ChatColor.YELLOW + "Range: " + field.getRange());
                lore.add(ChatColor.YELLOW + "Active: " + field.isActive());
                lore.add(ChatColor.GRAY + "Left-click: Toggle Active");
                lore.add(ChatColor.GRAY + "Right-click: Remove");
                lore.add(ChatColor.GRAY + "Shift+Left: Increase Strength");
                lore.add(ChatColor.GRAY + "Shift+Right: Decrease Strength");
                meta.setLore(lore);
                item.setItemMeta(meta);
                gui.setItem(localIndex, item);
            }
            globalIndex++;
        }
        player.openInventory(gui);
    }

    /**
     * Handles clicks in the FieldForge GUI.
     *
     * @param event The inventory click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.GREEN + "FieldForge Management")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        int index = Integer.parseInt(clickedItem.getItemMeta().getDisplayName().split("#")[1]);
        VectorField field = fieldManager.getFields().get(index);

        if (field.getCreator() != null && !field.getCreator().equals(playerUUID) && !player.hasPermission("fieldforge.admin")) {
            player.sendMessage(ChatColor.RED + "You don’t own this field.");
            return;
        }

        if (event.isLeftClick() && !event.isShiftClick()) {
            fieldManager.toggleFieldActive(index, playerUUID);
            player.sendMessage(ChatColor.GREEN + "Field at index " + index + " set to " + (field.isActive() ? "active" : "inactive"));
        } else if (event.isRightClick() && !event.isShiftClick()) {
            if (fieldManager.removeField(index, playerUUID)) {
                player.sendMessage(ChatColor.GREEN + "Field at index " + index + " removed.");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to remove field.");
            }
        } else if (event.isShiftClick()) {
            double newStrength = field.getStrength() + (event.isLeftClick() ? 1.0 : -1.0);
            if (newStrength >= 0) {
                fieldManager.modifyFieldStrength(index, newStrength, playerUUID);
                player.sendMessage(ChatColor.GREEN + "Field strength at index " + index + " set to " + newStrength);
            } else {
                player.sendMessage(ChatColor.RED + "Strength cannot be negative.");
            }
        }
        player.closeInventory();
        handleGuiCommand(player, playerUUID); // Refresh GUI
    }

    private long parseDuration(String duration) {
        try {
            if (duration.endsWith("s")) {
                return Long.parseLong(duration.replace("s", "")) * 20;
            } else if (duration.endsWith("t")) {
                return Long.parseLong(duration.replace("t", ""));
            }
            return Long.parseLong(duration) * 20; // Default to seconds
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration format.");
        }
    }
}