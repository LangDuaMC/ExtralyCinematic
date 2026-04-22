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
import pluginsmc.langdua.core.paper.objects.Frame;

import java.util.ArrayList;
import java.util.List;

public class CommandEditorGUI implements InventoryHolder {
    private final Core instance;
    private final Cinematic cinematic;
    private final int page;
    private final int frameIndex;
    private final Inventory inventory;

    public CommandEditorGUI(Core instance, Cinematic cinematic, int page, int frameIndex) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.page = page;
        this.frameIndex = frameIndex;
        this.inventory = Bukkit.createInventory(this, 54, "Cmds: " + cinematic.getName() + " - F" + frameIndex + " - P" + page);
        setup();
    }

    private void setup() {
        inventory.clear();
        Frame frame = cinematic.getFrames().get(frameIndex);
        for (int i = 0; i < frame.getCommands().size(); i++) {
            if (i >= 45) {
                break;
            }
            ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + "/" + frame.getCommands().get(i));
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.RED + "Right Click: Delete this command");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(i, item);
        }

        inventory.setItem(48, createInfoItem(Material.WRITABLE_BOOK, ChatColor.GREEN + "Add Command",
                ChatColor.GRAY + "Click here to enter a new command."));

        inventory.setItem(49, createInfoItem(Material.NAME_TAG, ChatColor.LIGHT_PURPLE + "Set Title / Subtitle",
                ChatColor.GRAY + "Current title: " + (frame.getTitle() != null ? frame.getTitle() : "Empty"),
                ChatColor.GRAY + "Current subtitle: " + (frame.getSubtitle() != null ? frame.getSubtitle() : "Empty"),
                "",
                ChatColor.YELLOW + "Left Click: Set Title",
                ChatColor.YELLOW + "Right Click: Set Subtitle"));

        inventory.setItem(50, createInfoItem(Material.NOTE_BLOCK, ChatColor.GOLD + "Add Sound",
                ChatColor.GRAY + "Click here to enter a sound name.",
                ChatColor.GRAY + "Example: entity.ender_dragon.growl"));

        inventory.setItem(45, createInfoItem(Material.ARROW, ChatColor.RED + "Back"));
    }

    public void handle(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (frameIndex < 0 || frameIndex >= cinematic.getFrames().size()) {
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        Frame frame = cinematic.getFrames().get(frameIndex);
        if (clicked.getType() == Material.ARROW) {
            player.openInventory(new CinematicFrameGUI(instance, cinematic, page).getInventory());
            return;
        }

        if (clicked.getType() == Material.COMMAND_BLOCK && event.isRightClick()) {
            int cmdIndex = event.getSlot();
            if (cmdIndex >= 0 && cmdIndex < frame.getCommands().size()) {
                String removed = frame.getCommands().remove(cmdIndex);
                instance.getStorageManager().save(instance.getGame().getCinematics());
                player.sendMessage(ChatColor.RED + "Deleted command: /" + removed);
                player.openInventory(new CommandEditorGUI(instance, cinematic, page, frameIndex).getInventory());
            }
            return;
        }

        if (clicked.getType() == Material.WRITABLE_BOOK) {
            player.closeInventory();
            instance.getChatInputManager().requestInput(player, "input.prompt-cmd", input -> {
                frame.getCommands().add(input);
                instance.getStorageManager().save(instance.getGame().getCinematics());
                instance.getMessageManager().send(player, "edit.cmd-added", "cmd", input);
                player.openInventory(new CommandEditorGUI(instance, cinematic, page, frameIndex).getInventory());
            });
        } else if (clicked.getType() == Material.NAME_TAG) {
            player.closeInventory();
            if (event.isLeftClick()) {
                instance.getChatInputManager().requestInput(player, "input.prompt-title", input -> {
                    frame.setTitle(input);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.title-updated");
                    player.openInventory(new CommandEditorGUI(instance, cinematic, page, frameIndex).getInventory());
                });
            } else if (event.isRightClick()) {
                instance.getChatInputManager().requestInput(player, "input.prompt-subtitle", input -> {
                    frame.setSubtitle(input);
                    instance.getStorageManager().save(instance.getGame().getCinematics());
                    instance.getMessageManager().send(player, "edit.subtitle-updated");
                    player.openInventory(new CommandEditorGUI(instance, cinematic, page, frameIndex).getInventory());
                });
            }
        } else if (clicked.getType() == Material.NOTE_BLOCK) {
            player.closeInventory();
            instance.getChatInputManager().requestInput(player, "input.prompt-sound", input -> {
                frame.getCommands().add("playsound " + input + " master %player%");
                instance.getStorageManager().save(instance.getGame().getCinematics());
                instance.getMessageManager().send(player, "edit.sound-added", "sound", input);
                player.openInventory(new CommandEditorGUI(instance, cinematic, page, frameIndex).getInventory());
            });
        }
    }

    private ItemStack createInfoItem(Material mat, String name, String... lore) {
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
