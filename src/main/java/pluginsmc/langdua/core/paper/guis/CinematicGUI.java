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

import java.util.ArrayList;
import java.util.List;

public class CinematicGUI implements InventoryHolder {
    private final Core instance;
    private final Inventory inventory;

    public CinematicGUI(Core instance) {
        this.instance = instance;
        this.inventory = Bukkit.createInventory(this, 54, "Cinematics List");
        setup();
    }

    private void setup() {
        inventory.clear();
        for (Cinematic cinematic : instance.getGame().getCinematics().values()) {
            ItemStack item = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + cinematic.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Total Frames: " + cinematic.getFrames().size());
                lore.add(ChatColor.GRAY + "Focus: " + (cinematic.hasFocus() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
                lore.add(ChatColor.GRAY + "Shake: " + cinematic.getShakeIntensity());
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to open Dashboard");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.addItem(item);
        }
    }

    public void handle(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getItemMeta() == null) {
            return;
        }

        String cinematicName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        Cinematic cinematic = instance.getGame().getCinematics().get(cinematicName);
        if (cinematic != null) {
            player.openInventory(new CinematicDashboardGUI(instance, cinematic).getInventory());
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
