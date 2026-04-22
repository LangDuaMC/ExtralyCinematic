package pluginsmc.langdua.core.paper.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.util.Arrays;

public class CinematicDashboardGUI implements InventoryHolder {
    private final Core instance;
    private final Cinematic cinematic;
    private final Inventory inventory;

    public CinematicDashboardGUI(Core instance, Cinematic cinematic) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.inventory = Bukkit.createInventory(this, 36, "Cine: " + cinematic.getName());
        setup();
    }

    private void setup() {
        inventory.clear();
        inventory.setItem(10, createItem(Material.ITEM_FRAME, ChatColor.AQUA + "Edit Frames & Commands", ChatColor.GRAY + "Edit frame coordinates and commands."));
        inventory.setItem(11, createItem(Material.ENDER_EYE, ChatColor.LIGHT_PURPLE + "Visualize Path", ChatColor.GRAY + "Preview the flight path with particles."));

        inventory.setItem(12, createItem(Material.CLOCK, ChatColor.YELLOW + "Duration",
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + cinematic.getDuration() + "s",
                ChatColor.GRAY + "Click to set cinematic duration."));

        inventory.setItem(13, createItem(Material.JUKEBOX, ChatColor.GOLD + "Background Music (BGM)",
                ChatColor.GRAY + "Current: " + ChatColor.WHITE + (cinematic.getBgmSound() != null ? cinematic.getBgmSound() : "None"),
                ChatColor.GRAY + "Left Click: Set BGM", ChatColor.GRAY + "Right Click: Clear BGM"));

        inventory.setItem(14, createItem(Material.SPYGLASS, ChatColor.DARK_AQUA + "Dolly Zoom (FOV)",
                ChatColor.GRAY + "Start: " + ChatColor.WHITE + cinematic.getStartZoom() + ChatColor.GRAY + " | End: " + ChatColor.WHITE + cinematic.getEndZoom(),
                ChatColor.GRAY + "Left Click: Set Start Zoom", ChatColor.GRAY + "Right Click: Set End Zoom"));

        inventory.setItem(15, createItem(Material.BLAZE_POWDER, ChatColor.RED + "Camera Shake",
                ChatColor.GRAY + "Intensity: " + ChatColor.WHITE + cinematic.getShakeIntensity(),
                ChatColor.GRAY + "Click to type exact intensity."));

        inventory.setItem(16, createItem(Material.COMPASS, ChatColor.GREEN + "Focus Target",
                ChatColor.GRAY + "Status: " + (cinematic.hasFocus() ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"),
                ChatColor.GRAY + "Click to toggle (Locks to your current location)."));
        inventory.setItem(22, createItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Play Cinematic", ChatColor.GRAY + "Play this cinematic instantly."));
        inventory.setItem(27, createItem(Material.OAK_DOOR, ChatColor.RED + "Back", ChatColor.GRAY + "Return to the main list."));
        inventory.setItem(35, createItem(Material.BARRIER, ChatColor.DARK_RED + "Delete Cinematic", ChatColor.GRAY + "Click to delete (Cannot be undone)."));

        ItemStack glass = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }

    public void handle(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.BLACK_STAINED_GLASS_PANE || clicked.getItemMeta() == null) {
            return;
        }

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (itemName.equals("Edit Frames & Commands")) {
            player.openInventory(new CinematicFrameGUI(instance, cinematic, 1).getInventory());
        } else if (itemName.equals("Visualize Path")) {
            player.closeInventory();
            player.performCommand("cinematic path " + cinematic.getName());
        } else if (itemName.equals("Focus Target")) {
            if (cinematic.hasFocus()) {
                player.performCommand("cinematic focus " + cinematic.getName() + " clear");
            } else {
                player.performCommand("cinematic focus " + cinematic.getName() + " set");
            }
            player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
        } else if (itemName.equals("Duration")) {
            player.closeInventory();
            instance.getChatInputManager().requestInput(player, "input.prompt-duration", input -> {
                try {
                    cinematic.setDuration(Integer.parseInt(input));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.generic-updated");
                } catch (NumberFormatException e) {
                    instance.getMessageManager().send(player, "error.invalid-number");
                }
                player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
            });
        } else if (itemName.equals("Background Music (BGM)")) {
            if (event.isLeftClick()) {
                player.closeInventory();
                instance.getChatInputManager().requestInput(player, "input.prompt-bgm", input -> {
                    cinematic.setBgmSound(input);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.generic-updated");
                    player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
                });
            } else if (event.isRightClick()) {
                cinematic.setBgmSound(null);
                instance.getStorageManager().save(instance.getGame().getCinematics());
                player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
            }
        } else if (itemName.equals("Dolly Zoom (FOV)")) {
            player.closeInventory();
            if (event.isLeftClick()) {
                instance.getChatInputManager().requestInput(player, "input.prompt-zoom-start", input -> {
                    try {
                        cinematic.setStartZoom(Integer.parseInt(input));
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.generic-updated");
                    } catch (NumberFormatException e) {
                        instance.getMessageManager().send(player, "error.invalid-number");
                    }
                    player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
                });
            } else if (event.isRightClick()) {
                instance.getChatInputManager().requestInput(player, "input.prompt-zoom-end", input -> {
                    try {
                        cinematic.setEndZoom(Integer.parseInt(input));
                        instance.getStorageManager().save(instance.getGame().getCinematics());
                        instance.getMessageManager().send(player, "edit.generic-updated");
                    } catch (NumberFormatException e) {
                        instance.getMessageManager().send(player, "error.invalid-number");
                    }
                    player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
                });
            }
        } else if (itemName.equals("Camera Shake")) {
            player.closeInventory();
            instance.getChatInputManager().requestInput(player, "input.prompt-shake", input -> {
                try {
                    cinematic.setShakeIntensity(Double.parseDouble(input));
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.generic-updated");
                } catch (NumberFormatException e) {
                    instance.getMessageManager().send(player, "error.invalid-number");
                }
                player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
            });
        } else if (itemName.equals("Play Cinematic")) {
            player.closeInventory();
            player.performCommand("cinematic play " + player.getName() + " " + cinematic.getName());
        } else if (itemName.equals("Delete Cinematic")) {
            player.closeInventory();
            player.performCommand("cinematic delete " + cinematic.getName());
        } else if (itemName.equals("Back")) {
            player.openInventory(new CinematicGUI(instance).getInventory());
        }
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
