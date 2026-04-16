package pluginsmc.langdua.core.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private final Core instance;
    private final File folder;

    public StorageManager(Core instance) {
        this.instance = instance;
        this.folder = new File(instance.getDataFolder(), "cinematics");
        if (!folder.exists()) folder.mkdirs();
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
            config.set("duration", cine.getDuration()); // Save duration

            if (cine.hasFocus()) {
                config.set("focus.world", cine.getFocusWorld());
                config.set("focus.x", cine.getFocusX());
                config.set("focus.y", cine.getFocusY());
                config.set("focus.z", cine.getFocusZ());
            }

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

            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Cinematic> load() {
        Map<String, Cinematic> cinematics = new HashMap<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return cinematics;

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = config.getString("name");
            Cinematic cine = new Cinematic(name);

            cine.setBgmSound(config.getString("bgm"));
            cine.setShakeIntensity(config.getDouble("shake"));
            cine.setStartZoom(config.getInt("zoom.start"));
            cine.setEndZoom(config.getInt("zoom.end"));
            cine.setDuration(config.getInt("duration", 0)); // Load duration

            if (config.contains("focus")) {
                cine.setFocus(config.getString("focus.world"), config.getDouble("focus.x"), config.getDouble("focus.y"), config.getDouble("focus.z"));
            }

            List<Map<?, ?>> frameMaps = config.getMapList("frames");
            List<Frame> frames = new ArrayList<>();
            for (Map<?, ?> m : frameMaps) {
                Frame f = new Frame((String) m.get("w"), (Double) m.get("x"), (Double) m.get("y"), (Double) m.get("z"), ((Double) m.get("yaw")).floatValue(), ((Double) m.get("pitch")).floatValue());
                if (m.containsKey("cmds")) f.setCommands((List<String>) m.get("cmds"));
                if (m.containsKey("title")) f.setTitle((String) m.get("title"));
                if (m.containsKey("subtitle")) f.setSubtitle((String) m.get("subtitle"));
                frames.add(f);
            }
            cine.setFrames(frames);
            cinematics.put(name, cine);
        }
        return cinematics;
    }
}