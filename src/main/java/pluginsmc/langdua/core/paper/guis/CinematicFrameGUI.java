package pluginsmc.langdua.core.paper.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.util.ArrayList;
import java.util.List;

public class CinematicFrameGUI implements InventoryHolder {
    private final Core instance;
    private final Cinematic cinematic;
    private final int page;
    private final Inventory inventory;

    public CinematicFrameGUI(Core instance, Cinematic cinematic, int page) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54, "Frames: " + cinematic.getName() + " - P" + page);
        setup();
    }

    private void setup() {
        inventory.clear();

        int maxFramesPerPage = 45;
        List<Frame> frames = cinematic.getFrames();
        int totalFrames = frames.size();
        int startIndex = (page - 1) * maxFramesPerPage;
        int endIndex = Math.min(startIndex + maxFramesPerPage, totalFrames);

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Frame " + i);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Commands: " + frames.get(i).getCommands().size());

                String title = frames.get(i).getTitle();
                if (title != null && !title.isEmpty()) {
                    lore.add(ChatColor.GRAY + "Title: " + ChatColor.translateAlternateColorCodes('&', title));
                }

                lore.add("");
                lore.add(ChatColor.GREEN + "Left Click: Teleport & Edit");
                lore.add(ChatColor.RED + "Right Click: Delete Frame");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(i - startIndex, item);
        }
        if (page > 1) {
            inventory.setItem(45, createControlItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }
        if (endIndex < totalFrames) {
            inventory.setItem(53, createControlItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }

        inventory.setItem(49, createControlItem(Material.OAK_DOOR, ChatColor.RED + "Back", ChatColor.GRAY + "Return to Dashboard"));
        inventory.setItem(48, createControlItem(Material.ENDER_EYE, ChatColor.LIGHT_PURPLE + "Visualize Path", ChatColor.GRAY + "Show path with particles"));
    }

    public void handle(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        List<Frame> frames = cinematic.getFrames();
        if (clicked.getType() == Material.ARROW && clicked.getItemMeta() != null) {
            String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (itemName.equals("Next Page")) {
                player.openInventory(new CinematicFrameGUI(instance, cinematic, page + 1).getInventory());
            } else if (itemName.equals("Previous Page")) {
                player.openInventory(new CinematicFrameGUI(instance, cinematic, page - 1).getInventory());
            }
            return;
        }

        if (clicked.getType() == Material.OAK_DOOR) {
            player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
            return;
        }

        if (clicked.getType() == Material.ENDER_EYE) {
            player.closeInventory();
            player.performCommand("cinematic path " + cinematic.getName());
            return;
        }

        if (clicked.getType() != Material.PAPER || clicked.getItemMeta() == null) {
            return;
        }

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (!itemName.startsWith("Frame ")) {
            return;
        }

        int frameIndex;
        try {
            frameIndex = Integer.parseInt(itemName.replace("Frame ", ""));
        } catch (NumberFormatException e) {
            return;
        }

        if (frameIndex < 0 || frameIndex >= frames.size()) {
            return;
        }

        if (event.isLeftClick()) {
            Frame frame = frames.get(frameIndex);
            World world = Bukkit.getWorld(frame.getWorld());
            if (world != null) {
                Location loc = new Location(world, frame.getX(), frame.getY(), frame.getZ(), frame.getYaw(), frame.getPitch());
                player.teleport(loc);
                player.sendMessage(ChatColor.GREEN + "Teleported to frame " + frameIndex + ".");
            }
            player.openInventory(new CommandEditorGUI(instance, cinematic, page, frameIndex).getInventory());
        } else if (event.isRightClick()) {
            frames.remove(frameIndex);
            instance.getStorageManager().save(instance.getGame().getCinematics());
            player.sendMessage(ChatColor.RED + "Deleted frame " + frameIndex);
            int remaining = cinematic.getFrames().size();
            int maxPage = Math.max(1, (int) Math.ceil(Math.max(remaining, 1) / 45.0));
            player.openInventory(new CinematicFrameGUI(instance, cinematic, Math.min(page, maxPage)).getInventory());
        }
    }

    private ItemStack createControlItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
