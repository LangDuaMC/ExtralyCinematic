package pluginsmc.langdua.core.paper.guis;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.util.ArrayList;
import java.util.List;

public class CinematicGUI implements InventoryHolder {

    private final Core instance;
    private final Cinematic cinematic;
    private final Inventory inventory;

    public CinematicGUI(Core instance, Cinematic cinematic) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.inventory = Bukkit.createInventory(this, 45, MiniMessage.miniMessage().deserialize("<gold>Editing: <yellow>" + cinematic.getName()));
        setup();
    }

    private void setup() {
        inventory.clear();

        // 1. Core Functions
        inventory.setItem(11, createItem(Material.MINECART, "<aqua>Manage Tracks", "<gray>Total Tracks: " + cinematic.getTracks().size()));
        inventory.setItem(13, createItem(Material.CLOCK, "<yellow>Manage Timeline", "<gray>Clips: " + cinematic.getTimeline().size()));
        inventory.setItem(15, createItem(Material.EMERALD_BLOCK, "<green>Play / Preview", "<gray>Click to spectate"));

        // 2. Deep Settings
        inventory.setItem(28, createItem(Material.JUKEBOX, "<light_purple>Edit BGM", "<gray>Current: " + (cinematic.getBgmSound() != null ? cinematic.getBgmSound() : "None")));
        inventory.setItem(29, createItem(Material.SPYGLASS, "<blue>Edit Zoom", "<gray>Start: " + cinematic.getStartZoom() + " | End: " + cinematic.getEndZoom()));
        inventory.setItem(30, createItem(Material.BLAZE_POWDER, "<gold>Edit Shake", "<gray>Intensity: " + cinematic.getShakeIntensity()));
        inventory.setItem(31, createItem(Material.ENDER_EYE, "<dark_purple>Set Focus", "<gray>Click to set camera look-at to your location"));
        inventory.setItem(32, createItem(Material.ENDER_PEARL, "<red>Clear Focus", "<gray>Remove focus point"));
        inventory.setItem(33, createItem(Material.COMPARATOR, "<white>Edit Duration", "<gray>Current: " + cinematic.getDuration() + "s"));
        inventory.setItem(34, createItem(Material.NAME_TAG, "<yellow>Rename Cinematic", "<gray>Current: " + cinematic.getName()));

        // 3. System
        inventory.setItem(40, createItem(Material.BARRIER, "<red>Back to Dashboard", ""));
    }

    private ItemStack createItem(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(name));
        if (!loreLine.isEmpty()) {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize(loreLine));
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void handle(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        switch (clicked.getType()) {
            case MINECART -> player.openInventory(new CinematicTrackGUI(instance, cinematic).getInventory());
            case CLOCK -> player.openInventory(new CinematicTimelineGUI(instance, cinematic).getInventory());
            case EMERALD_BLOCK -> {
                player.closeInventory();
                instance.getGame().getPlayManager().play(player, player, cinematic.getName());
            }
            case JUKEBOX -> {
                player.closeInventory();
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Type BGM sound name in chat (type 'clear' to remove):"));
                // Hook ChatInputManager here
            }
            case SPYGLASS -> {
                player.closeInventory();
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Type Zoom Start and End in chat (e.g. '1 5'):"));
                // Hook ChatInputManager here
            }
            case BLAZE_POWDER -> {
                player.closeInventory();
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Type Shake Intensity in chat (e.g. '0.5'):"));
                // Hook ChatInputManager here
            }
            case ENDER_EYE -> {
                org.bukkit.Location loc = player.getLocation();
                cinematic.setFocus(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
                instance.getStorageManager().save(instance.getGame().getCinematics());
                setup();
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Focus set to your current location!"));
            }
            case ENDER_PEARL -> {
                cinematic.clearFocus();
                instance.getStorageManager().save(instance.getGame().getCinematics());
                setup();
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Focus cleared!"));
            }
            case COMPARATOR -> {
                player.closeInventory();
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Type new Duration in seconds in chat:"));
                // Hook ChatInputManager here
            }
            case NAME_TAG -> {
                player.closeInventory();
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Type new Cinematic name in chat:"));
                // Hook ChatInputManager here
            }
            case BARRIER -> player.openInventory(new CinematicDashboardGUI(instance).getInventory());
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Cinematic getCinematic() {
        return cinematic;
    }
}