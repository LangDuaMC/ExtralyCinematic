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
import pluginsmc.langdua.core.paper.objects.CinematicTrack;

import java.util.ArrayList;
import java.util.List;

public class CinematicTrackGUI implements InventoryHolder {

    private final Core instance;
    private final Cinematic cinematic;
    private final Inventory inventory;

    public CinematicTrackGUI(Core instance, Cinematic cinematic) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize("<aqua>Tracks: <yellow>" + cinematic.getName()));
        setup();
    }

    private void setup() {
        inventory.clear();
        int slot = 0;

        for (CinematicTrack track : cinematic.getTracks().values()) {
            if (slot >= 45) break;

            ItemStack item = new ItemStack(Material.MINECART);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<aqua>Track: <white>" + track.getId()));

            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Frames: " + track.getFrames().size()));
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Duration: " + track.getDurationTicks() + " ticks"));
            lore.add(MiniMessage.miniMessage().deserialize(""));
            lore.add(MiniMessage.miniMessage().deserialize("<yellow>Click to edit frames"));
            meta.lore(lore);

            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }

        ItemStack addItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.displayName(MiniMessage.miniMessage().deserialize("<green><bold>+ Add New Track"));
        addItem.setItemMeta(addMeta);
        inventory.setItem(49, addItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(MiniMessage.miniMessage().deserialize("<red>Back to Cinematic"));
        backItem.setItemMeta(backMeta);
        inventory.setItem(45, backItem);
    }

    public void handle(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.BARRIER) {
            player.openInventory(new CinematicGUI(instance, cinematic).getInventory());
            return;
        }

        if (clicked.getType() == Material.EMERALD_BLOCK) {
            player.closeInventory();
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Please enter new track name in chat. Type 'cancel' to abort."));
            return;
        }

        if (clicked.getType() == Material.MINECART) {
            String name = MiniMessage.miniMessage().serialize(clicked.getItemMeta().displayName());
            name = name.replaceAll("<[^>]*>", "").replace("\\<", "<").replace("\\>", ">").replace("Track: ", "").trim();

            CinematicTrack track = cinematic.getTracks().get(name);
            if (track != null) {
                player.openInventory(new CinematicFrameGUI(instance, cinematic, track).getInventory());
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}