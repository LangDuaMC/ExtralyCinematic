package pluginsmc.langdua.core.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StorageManager {
    private final Core instance;
    private final File folder;

    public StorageManager(Core instance) {
        this.instance = instance;
        this.folder = new File(instance.getDataFolder(), "cinematics");
        if (!folder.exists()) folder.mkdirs();
    }

    // Xử lý an toàn lỗi ép kiểu số
    private double asDouble(Object o) {
        return (o instanceof Number) ? ((Number) o).doubleValue() : 0.0;
    }

    public void save(Map<String, Cinematic> cinematics) {
        for (Cinematic cine : cinematics.values()) {
            File file = new File(folder, cine.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("name", cine.getName());
            config.set("bgm", cine.getBgmSound());
            config.set("shake", cine.getShakeIntensity());
            config.set("zoom.start", cine.getStartZoom());
            config.set("zoom.end", cine.getEndZoom());
            config.set("duration", cine.getDuration());

            List<Map<String, Object>> frameList = new ArrayList<>();
            for (Frame f : cine.getFrames()) {
                Map<String, Object> fMap = new HashMap<>();
                fMap.put("w", f.getWorld());
                fMap.put("x", f.getX());
                fMap.put("y", f.getY());
                fMap.put("z", f.getZ());
                fMap.put("yaw", (double) f.getYaw());
                fMap.put("pitch", (double) f.getPitch());
                fMap.put("cmds", f.getCommands());
                fMap.put("title", f.getTitle());
                fMap.put("subtitle", f.getSubtitle());
                frameList.add(fMap);
            }
            config.set("frames", frameList);
            try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    public Map<String, Cinematic> load() {
        Map<String, Cinematic> cinematics = new HashMap<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return cinematics;

        for (File file : files) {
            try {
                // BỌC TRY-CATCH CHỐNG SẬP
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                if (name == null) continue;

                Cinematic cine = new Cinematic(name);
                cine.setBgmSound(config.getString("bgm"));
                cine.setShakeIntensity(config.getDouble("shake"));
                cine.setStartZoom(config.getInt("zoom.start", 0));
                cine.setEndZoom(config.getInt("zoom.end", 0));
                cine.setDuration(config.getInt("duration", 0));

                List<Map<?, ?>> frameMaps = config.getMapList("frames");
                for (Map<?, ?> m : frameMaps) {
                    Frame f = new Frame((String) m.get("w"), asDouble(m.get("x")), asDouble(m.get("y")), asDouble(m.get("z")), (float) asDouble(m.get("yaw")), (float) asDouble(m.get("pitch")));
                    if (m.containsKey("cmds")) f.setCommands((List<String>) m.get("cmds"));
                    if (m.containsKey("title")) f.setTitle((String) m.get("title"));
                    if (m.containsKey("subtitle")) f.setSubtitle((String) m.get("subtitle"));
                    cine.getFrames().add(f);
                }
                cinematics.put(name, cine);
            } catch (Exception e) {
                instance.getLogger().severe("Lỗi định dạng khi đọc file: " + file.getName() + ". Đã bỏ qua để chống sập plugin!");
                e.printStackTrace();
            }
        }
        return cinematics;
    }
}