package pluginsmc.langdua.core.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.Frame;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StorageManager {
    private final Core instance;
    private final File folder;

    public StorageManager(Core instance) {
        this.instance = instance;
        this.folder = new File(instance.getDataFolder(), "cinematics");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private double asDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    private Frame deserializeFrame(Map<?, ?> map) {
        Frame frame = new Frame(
                (String) map.get("w"),
                asDouble(map.get("x")),
                asDouble(map.get("y")),
                asDouble(map.get("z")),
                (float) asDouble(map.get("yaw")),
                (float) asDouble(map.get("pitch"))
        );
        if (map.containsKey("cmds")) {
            frame.setCommands(new ArrayList<>((List<String>) map.get("cmds")));
        }
        if (map.containsKey("title")) {
            frame.setTitle((String) map.get("title"));
        }
        if (map.containsKey("subtitle")) {
            frame.setSubtitle((String) map.get("subtitle"));
        }
        return frame;
    }

    private Map<String, Object> serializeFrame(Frame frame) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("w", frame.getWorld());
        map.put("x", frame.getX());
        map.put("y", frame.getY());
        map.put("z", frame.getZ());
        map.put("yaw", (double) frame.getYaw());
        map.put("pitch", (double) frame.getPitch());
        map.put("cmds", new ArrayList<>(frame.getCommands()));
        map.put("title", frame.getTitle());
        map.put("subtitle", frame.getSubtitle());
        return map;
    }

    public void save(Map<String, Cinematic> cinematics) {
        for (Cinematic cinematic : cinematics.values()) {
            cinematic.ensureStructure();
            File file = new File(folder, cinematic.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("name", cinematic.getName());
            config.set("bgm", cinematic.getBgmSound());
            config.set("shake", cinematic.getShakeIntensity());
            config.set("zoom.start", cinematic.getStartZoom());
            config.set("zoom.end", cinematic.getEndZoom());
            config.set("duration", cinematic.getDuration());
            if (cinematic.hasFocus()) {
                config.set("focus.world", cinematic.getFocusWorld());
                config.set("focus.x", cinematic.getFocusX());
                config.set("focus.y", cinematic.getFocusY());
                config.set("focus.z", cinematic.getFocusZ());
            }

            List<Map<String, Object>> frames = new ArrayList<>();
            for (Frame frame : cinematic.getFrames()) {
                frames.add(serializeFrame(frame));
            }
            config.set("frames", frames);

            try {
                config.save(file);
            } catch (IOException e) {
                instance.getLogger().severe("Failed to save cinematic file '" + file.getName() + "'.");
                e.printStackTrace();
            }
        }
    }

    public Map<String, Cinematic> load() {
        Map<String, Cinematic> cinematics = new HashMap<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return cinematics;
        }

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                if (name == null || name.isBlank()) {
                    continue;
                }

                Cinematic cinematic = new Cinematic(name);
                cinematic.setBgmSound(config.getString("bgm"));
                cinematic.setShakeIntensity(config.getDouble("shake"));
                cinematic.setStartZoom(config.getInt("zoom.start", 0));
                cinematic.setEndZoom(config.getInt("zoom.end", 0));
                cinematic.setDuration(config.getInt("duration", 0));
                if (config.isConfigurationSection("focus")) {
                    String world = config.getString("focus.world");
                    if (world != null) {
                        cinematic.setFocus(
                                world,
                                config.getDouble("focus.x"),
                                config.getDouble("focus.y"),
                                config.getDouble("focus.z")
                        );
                    }
                }

                List<Map<?, ?>> frameMaps = config.getMapList("frames");
                if (frameMaps.isEmpty()) {
                    frameMaps = readLegacyTrackFrames(config.getMapList("tracks"));
                }
                for (Map<?, ?> map : frameMaps) {
                    cinematic.getFrames().add(deserializeFrame(map));
                }

                cinematic.ensureStructure();
                cinematics.put(name, cinematic);
            } catch (Throwable t) {
                instance.getLogger().severe("Failed to load cinematic file '" + file.getName() + "'. Skipping it so the plugin can continue enabling.");
                t.printStackTrace();
            }
        }
        return cinematics;
    }

    public boolean delete(String name) {
        File file = new File(folder, name + ".yml");
        return !file.exists() || file.delete();
    }

    private List<Map<?, ?>> readLegacyTrackFrames(List<Map<?, ?>> trackMaps) {
        if (trackMaps == null || trackMaps.isEmpty()) {
            return List.of();
        }

        Map<?, ?> selected = null;
        for (Map<?, ?> trackMap : trackMaps) {
            if ("main".equals(trackMap.get("id"))) {
                selected = trackMap;
                break;
            }
        }
        if (selected == null) {
            selected = trackMaps.getFirst();
        }

        Object frames = selected.get("frames");
        if (frames instanceof List<?> list) {
            List<Map<?, ?>> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    result.add(map);
                }
            }
            return result;
        }
        return List.of();
    }
}
