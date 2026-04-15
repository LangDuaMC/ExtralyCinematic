package me.aleiv.core.paper.guis;

import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.objects.Cinematic;
import me.aleiv.core.paper.objects.Frame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CommandEditorGUI {

    private Core instance;
    private Cinematic cinematic;
    private int frameIndex;

    public CommandEditorGUI(Core instance, Cinematic cinematic, int frameIndex) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.frameIndex = frameIndex;
    }

    public Inventory getGUI() {
        Frame frame = cinematic.getFrames().get(frameIndex);
        Inventory gui = Bukkit.createInventory(null, 54, "Commands for Frame " + frameIndex);

        // Display existing commands
        List<String> commands = frame.getCommands();
        for (int i = 0; i < commands.size() && i < 45; i++) {
            String command = commands.get(i);
            ItemStack commandItem = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta meta = commandItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "/" + command);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.RED + "Right-click to delete");
            meta.setLore(lore);
            commandItem.setItemMeta(meta);
            gui.setItem(i, commandItem);
        }

        // "Add Command" button
        ItemStack addCommandButton = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta addMeta = addCommandButton.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Add New Command");
        addCommandButton.setItemMeta(addMeta);
        gui.setItem(45, addCommandButton);

        // Back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Frame List");
        backButton.setItemMeta(backMeta);
        gui.setItem(48, backButton);

        return gui;
    }
}
