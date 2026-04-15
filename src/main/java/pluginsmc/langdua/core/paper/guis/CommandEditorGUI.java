package pluginsmc.langdua.core.paper.guis;

import pluginsmc.langdua.core.paper.Core;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CommandEditorGUI {

    private final Core instance;

    public CommandEditorGUI(Core instance) {
        this.instance = instance;
    }

    public Inventory getCommandEditorGUI(Cinematic cinematic, int page, int frameIndex) {
        // Title format: "Cmds: <cineName> - F<frame> - P<page>"
        Inventory gui = Bukkit.createInventory(null, 54, "Cmds: " + cinematic.getName() + " - F" + frameIndex + " - P" + page);

        Frame frame = cinematic.getFrames().get(frameIndex);
        List<String> commands = frame.getCommands();

        for (int i = 0; i < commands.size() && i < 45; i++) {
            ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "/" + commands.get(i));
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.RED + "Right-click to delete command");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(i, item);
        }

        // Hướng dẫn thêm lệnh bằng command
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.AQUA + "How to add a command?");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Use the following command:");
            lore.add(ChatColor.WHITE + "/cinematic addcmd " + cinematic.getName() + " " + frameIndex + " <command>");
            lore.add("");
            lore.add(ChatColor.GRAY + "Note: Do not include the '/' slash.");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        gui.setItem(49, info);

        // Nút quay lại Frame GUI
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back to Frames");
            back.setItemMeta(backMeta);
        }
        gui.setItem(45, back);

        return gui;
    }
}