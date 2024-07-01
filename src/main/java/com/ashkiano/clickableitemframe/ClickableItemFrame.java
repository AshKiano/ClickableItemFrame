package com.ashkiano.clickableitemframe;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;

public class ClickableItemFrame extends JavaPlugin implements Listener {

    private Map<Location, String> frameCommands;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        loadCommandsFromConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        Metrics metrics = new Metrics(this, 22240);
    }

    private void loadCommandsFromConfig() {
        frameCommands = new HashMap<>();
        if (getConfig().contains("itemframes")) {
            for (String key : getConfig().getConfigurationSection("itemframes").getKeys(false)) {
                Location location = stringToLocation(key);
                String command = getConfig().getString("itemframes." + key);
                frameCommands.put(location, command);
            }
        }
    }

    private void saveCommandsToConfig() {
        for (Map.Entry<Location, String> entry : frameCommands.entrySet()) {
            String locString = locationToString(entry.getKey());
            getConfig().set("itemframes." + locString, entry.getValue());
        }
        saveConfig();
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
            Location location = itemFrame.getLocation();
            if (frameCommands.containsKey(location)) {
                String command = frameCommands.get(location);
                Player player = event.getPlayer();
                Bukkit.dispatchCommand(player, command.replace("{player}", player.getName()));
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setitemframecommand")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("clickableitemframe.setcommand")) {
                    if (args.length > 0) {
                        StringBuilder command = new StringBuilder();
                        for (String arg : args) {
                            command.append(arg).append(" ");
                        }
                        command = new StringBuilder(command.toString().trim());
                        ItemFrame itemFrame = getTargetItemFrame(player);
                        if (itemFrame != null) {
                            Location location = itemFrame.getLocation();
                            frameCommands.put(location, command.toString());
                            saveCommandsToConfig();
                            player.sendMessage("Command set for ItemFrame at " + location.toString());
                        } else {
                            player.sendMessage("You must be looking at an ItemFrame to set a command.");
                        }
                    } else {
                        player.sendMessage("Usage: /setitemframecommand <command>");
                    }
                } else {
                    player.sendMessage("You do not have permission to use this command.");
                }
            } else {
                sender.sendMessage("Only players can use this command.");
            }
            return true;
        }
        return false;
    }

    private ItemFrame getTargetItemFrame(Player player) {
        RayTraceResult result = player.getWorld().rayTraceEntities(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5, entity -> entity instanceof ItemFrame);
        if (result != null && result.getHitEntity() != null && result.getHitEntity() instanceof ItemFrame) {
            return (ItemFrame) result.getHitEntity();
        }
        return null;
    }

    private String locationToString(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private Location stringToLocation(String str) {
        String[] parts = str.split(",");
        return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }
}