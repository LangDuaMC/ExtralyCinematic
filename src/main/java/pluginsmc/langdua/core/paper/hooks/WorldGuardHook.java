package pluginsmc.langdua.core.paper.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pluginsmc.langdua.core.paper.Core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorldGuardHook implements Listener {
    private final Core instance;
    private final Map<UUID, Set<String>> playerRegions = new HashMap<>();
    private File file;
    private FileConfiguration config;

    public WorldGuardHook(Core instance) {
        this.instance = instance;
        createConfig();
    }

    private void createConfig() {
        file = new File(instance.getDataFolder(), "worldguard.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
            config = YamlConfiguration.loadConfiguration(file);

            // Generate example setup
            config.set("regions.boss_arena", "BossIntro");
            config.set("regions.spawn_city", "WelcomeCine");

            try { config.save(file); } catch (IOException ignored) {}
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        // Tối ưu hóa: Chỉ tính là di chuyển khi thực sự bước qua block khác
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Bỏ qua nếu player đang xem Cinematic rồi (chống kẹt vòng lặp)
        if (instance.getGame().getViewers().contains(uuid)) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(to));

        Set<String> currentRegions = new HashSet<>();
        for (ProtectedRegion region : set) {
            currentRegions.add(region.getId());
        }

        Set<String> oldRegions = playerRegions.getOrDefault(uuid, new HashSet<>());

        for (String region : currentRegions) {
            // Nhận diện người chơi VỪA MỚI bước vào Region
            if (!oldRegions.contains(region)) {
                String cinematicName = config.getString("regions." + region);
                if (cinematicName != null && instance.getGame().getCinematics().containsKey(cinematicName)) {
                    // Ép xem phim luôn!
                    player.performCommand("cinematic play " + player.getName() + " " + cinematicName);
                }
            }
        }

        // Cập nhật lại danh sách Region hiện tại của người chơi
        playerRegions.put(uuid, currentRegions);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerRegions.remove(event.getPlayer().getUniqueId());
    }
}