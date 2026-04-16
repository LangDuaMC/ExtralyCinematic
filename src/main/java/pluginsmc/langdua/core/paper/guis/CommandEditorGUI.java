package pluginsmc.langdua.core.paper.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.util.ArrayList;
import java.util.List;

public class CommandEditorGUI {
    private final Core instance;

    public CommandEditorGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getCommandEditorGUI(Cinematic cinematic, int page, int frameIndex) {
        Inventory inv = Bukkit.createInventory(null, 54, "Cmds: " + cinematic.getName() + " - F" + frameIndex + " - P" + page);
        Frame frame = cinematic.getFrames().get(frameIndex);

        // Display existing commands
        for (int i = 0; i < frame.getCommands().size(); i++) {
            if (i >= 45) break;
            ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + "/" + frame.getCommands().get(i));
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.RED + "Right Click: Delete Command");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        // Info panels for adding new features
        inv.setItem(48, createInfoItem(Material.WRITABLE_BOOK, ChatColor.GREEN + "Add Commands",
                ChatColor.GRAY + "Type in chat:",
                ChatColor.YELLOW + "/cinematic addcmd " + cinematic.getName() + " " + frameIndex + " <cmd>"));

        inv.setItem(49, createInfoItem(Material.NAME_TAG, ChatColor.LIGHT_PURPLE + "Add Titles/Subtitles",
                ChatColor.GRAY + "Type in chat:",
                ChatColor.YELLOW + "/cinematic title " + cinematic.getName() + " " + frameIndex + " <text>"));

        inv.setItem(50, createInfoItem(Material.NOTE_BLOCK, ChatColor.GOLD + "Add Sounds",
                ChatColor.GRAY + "Use playsound command in chat:",
                ChatColor.YELLOW + "/cinematic addcmd " + cinematic.getName() + " " + frameIndex + " playsound <sound> master %player%"));

        inv.setItem(45, createInfoItem(Material.ARROW, ChatColor.RED + "Back to Frames"));

        return inv;
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
}