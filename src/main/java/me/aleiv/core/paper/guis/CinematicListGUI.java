package me.aleiv.core.paper.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import me.aleiv.core.paper.Core;
import me.aleiv.core.paper.objects.Cinematic;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CinematicListGUI {

    private final Core instance;
    private final Player player;

    public CinematicListGUI(Core instance, Player player) {
        this.instance = instance;
        this.player = player;
        show();
    }

    public void show() {
        ChestGui gui = new ChestGui(6, "Cinematics");

        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 5);
        List<GuiItem> items = new ArrayList<>();
        instance.getGame().getCinematics().forEach((name, cinematic) -> {
            ItemStack item = new ItemStack(Material.FILM_PROJECTOR);
            var meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Frames: " + cinematic.getFrames().size());
            meta.setLore(lore);
            item.setItemMeta(meta);
            items.add(new GuiItem(item, event -> {
                // TODO: Open cinematic editor GUI
            }));
        });
        paginatedPane.populateWithGuiItems(items);
        gui.addPane(paginatedPane);

        StaticPane navigation = new StaticPane(0, 5, 9, 1);
        navigation.addItem(new GuiItem(new ItemStack(Material.ARROW), event -> {
            if (paginatedPane.getPage() > 0) {
                paginatedPane.setPage(paginatedPane.getPage() - 1);
                gui.update();
            }
        }), 0, 0);
        navigation.addItem(new GuiItem(new ItemStack(Material.ARROW), event -> {
            if (paginatedPane.getPage() < paginatedPane.getPages() - 1) {
                paginatedPane.setPage(paginatedPane.getPage() + 1);
                gui.update();
            }
        }), 8, 0);

        gui.addPane(navigation);
        gui.show(player);
    }
}
