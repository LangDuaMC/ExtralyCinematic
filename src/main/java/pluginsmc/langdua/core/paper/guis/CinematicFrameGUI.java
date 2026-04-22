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
import pluginsmc.langdua.core.paper.objects.Frame;

import java.util.ArrayList;
import java.util.List;

public class CinematicFrameGUI implements InventoryHolder {

    private final Core instance;
    private final Cinematic cinematic;
    private final CinematicTrack track;
    private final Inventory inventory;

    public CinematicFrameGUI(Core instance, Cinematic cinematic, CinematicTrack track) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.track = track;
        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize("<green>Frames: <white>" + track.getId()));
        setup();
    }

    private void setup() {
        inventory.clear();
        int slot = 0;

        for (int i = 0; i < track.getFrames().size(); i++) {
            if (slot >= 45) break;
            Frame frame = track.getFrames().get(i);

            ItemStack item = new ItemStack(Material.ITEM_FRAME);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<green>Frame #" + i));

            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Location: " + Math.round(frame.getX()) + " " + Math.round(frame.getY()) + " " + Math.round(frame.getZ())));
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Commands: " + frame.getCommands().size()));
            lore.add(MiniMessage.miniMessage().deserialize(""));
            lore.add(MiniMessage.miniMessage().deserialize("<yellow>Click to edit Commands/Title"));
            lore.add(MiniMessage.miniMessage().deserialize("<red>Shift-Right Click to Delete"));
            meta.lore(lore);

            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.displayName(MiniMessage.miniMessage().deserialize("<red>Back to Tracks"));
        backItem.setItemMeta(backMeta);
        inventory.setItem(45, backItem);
    }

    public void handle(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.BARRIER) {
            player.openInventory(new CinematicTrackGUI(instance, cinematic).getInventory());
            return;
        }

        if (clicked.getType() == Material.ITEM_FRAME) {
            int frameIndex = event.getSlot();
            if (frameIndex >= 0 && frameIndex < track.getFrames().size()) {
                if (event.isShiftClick() && event.isRightClick()) {
                    track.getFrames().remove(frameIndex);
                    setup();
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Frame removed!"));
                } else {
                    Frame frame = track.getFrames().get(frameIndex);
                    player.openInventory(new CommandEditorGUI(instance, cinematic, track, frame, frameIndex).getInventory());
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}