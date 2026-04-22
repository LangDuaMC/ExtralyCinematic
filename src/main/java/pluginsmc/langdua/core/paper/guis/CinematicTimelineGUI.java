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
import pluginsmc.langdua.core.paper.objects.TimelineClip;

import java.util.ArrayList;
import java.util.List;

public class CinematicTimelineGUI implements InventoryHolder {

    private final Core instance;
    private final Cinematic cinematic;
    private final Inventory inventory;

    public CinematicTimelineGUI(Core instance, Cinematic cinematic) {
        this.instance = instance;
        this.cinematic = cinematic;
        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize("<yellow>Timeline: <gold>" + cinematic.getName()));
        setup();
    }

    private void setup() {
        inventory.clear();
        int slot = 0;

        for (TimelineClip clip : cinematic.getTimeline()) {
            if (slot >= 45) break;

            ItemStack item = new ItemStack(Material.REPEATER);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<yellow>Clip: <white>" + clip.getTrackId()));

            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Transition: " + clip.getTransition().getEffect().name()));
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Duration: " + clip.getTransition().getDurationTicks() + " ticks"));
            lore.add(MiniMessage.miniMessage().deserialize(""));
            lore.add(MiniMessage.miniMessage().deserialize("<red>Shift-Right Click to Delete"));
            meta.lore(lore);

            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }

        ItemStack addItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.displayName(MiniMessage.miniMessage().deserialize("<green><bold>+ Add Clip"));
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

        if (clicked.getType() == Material.REPEATER) {
            if (event.isShiftClick() && event.isRightClick()) {
                int index = event.getSlot();
                if (index >= 0 && index < cinematic.getTimeline().size()) {
                    cinematic.getTimeline().remove(index);
                    setup(); // Load lại GUI ngay lập tức sau khi xóa
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Clip removed!"));
                }
            }
        }

        if (clicked.getType() == Material.EMERALD_BLOCK) {
            player.closeInventory();
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Enter track name for new clip in chat:"));
            // (Chỗ này sau này bro có thể nối với ChatInputManager)
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}