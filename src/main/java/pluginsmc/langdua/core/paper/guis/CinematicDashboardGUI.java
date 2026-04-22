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

import java.util.ArrayList;
import java.util.List;

public class CinematicDashboardGUI implements InventoryHolder {

    private final Core instance;
    private final Inventory inventory;

    public CinematicDashboardGUI(Core instance) {
        this.instance = instance;
        this.inventory = Bukkit.createInventory(this, 54, MiniMessage.miniMessage().deserialize("<gold><bold>Cinematic Dashboard"));
        setup();
    }

    private void setup() {
        inventory.clear();
        int slot = 0;

        for (Cinematic cine : instance.getGame().getCinematics().values()) {
            if (slot >= 54) break;

            ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize("<gold>" + cine.getName()));

            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Tracks: " + cine.getTracks().size()));
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Duration: " + cine.getDuration() + "s"));
            lore.add(MiniMessage.miniMessage().deserialize(""));
            lore.add(MiniMessage.miniMessage().deserialize("<yellow>Click to edit"));
            meta.lore(lore);

            item.setItemMeta(meta);
            inventory.setItem(slot++, item);
        }
    }

    public void handle(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String name = MiniMessage.miniMessage().serialize(clicked.getItemMeta().displayName());
        name = name.replaceAll("<[^>]*>", "").replace("\\<", "<").replace("\\>", ">").trim();

        Cinematic cine = instance.getGame().getCinematics().get(name);
        if (cine != null) {
            player.openInventory(new CinematicGUI(instance, cine).getInventory());
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}