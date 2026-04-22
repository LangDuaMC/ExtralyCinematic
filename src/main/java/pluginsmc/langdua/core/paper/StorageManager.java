package pluginsmc.langdua.core.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import pluginsmc.langdua.core.paper.objects.Cinematic;
import pluginsmc.langdua.core.paper.objects.CinematicTrack;
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
    private double asDouble(Object o) {
        return (o instanceof Number) ? ((Number) o).doubleValue() : 0.0;
    }

    private int asInt(Object o) {
        return (o instanceof Number) ? ((Number) o).intValue() : 0;
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
        for (Cinematic cine : cinematics.values()) {
            cine.ensureStructure();
            File file = new File(folder, cine.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("name", cine.getName());
            config.set("bgm", cine.getBgmSound());
            config.set("shake", cine.getShakeIntensity());
            config.set("zoom.start", cine.getStartZoom());
            config.set("zoom.end", cine.getEndZoom());
            config.set("duration", cine.getDuration());
            if (cine.hasFocus()) {
                config.set("focus.world", cine.getFocusWorld());
                config.set("focus.x", cine.getFocusX());
                config.set("focus.y", cine.getFocusY());
                config.set("focus.z", cine.getFocusZ());
            }

            List<Map<String, Object>> legacyFrames = new ArrayList<>();
            for (Frame frame : cine.getFrames()) {
                legacyFrames.add(serializeFrame(frame));
            }
            config.set("frames", legacyFrames);
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
                if (config.isConfigurationSection("focus")) {
                    String world = config.getString("focus.world");
                    if (world != null) {
                        cine.setFocus(
                                world,
                                config.getDouble("focus.x"),
                                config.getDouble("focus.y"),
                                config.getDouble("focus.z")
                        );
                    }
                }

                List<Map<?, ?>> trackMaps = config.getMapList("tracks");
                if (!trackMaps.isEmpty()) {
                    cine.getTracks().clear();
                    for (Map<?, ?> trackMap : trackMaps) {
                        String trackId = (String) trackMap.get("id");
                        if (trackId == null || trackId.isBlank()) {
                            continue;
                        }
                        CinematicTrack track = new CinematicTrack(trackId);
                        track.setDurationTicks(asInt(trackMap.get("durationTicks")));
                        List<Map<?, ?>> frameMaps = (List<Map<?, ?>>) trackMap.get("frames");
                        if (frameMaps != null) {
                            for (Map<?, ?> frameMap : frameMaps) {
                                track.getFrames().add(deserializeFrame(frameMap));
                            }
                        }
                        cine.getTracks().put(trackId, track);
                    }
                } else {
                    List<Map<?, ?>> frameMaps = config.getMapList("frames");
                    for (Map<?, ?> map : frameMaps) {
                        cine.getFrames().add(deserializeFrame(map));
                    }
                }

                cine.ensureStructure();
                cinematics.put(name, cine);
            } catch (Throwable t) {
                instance.getLogger().severe("Failed to load cinematic file '" + file.getName() + "'. Skipping it so the plugin can continue enabling.");
                t.printStackTrace();
            }
        }
        return cinematics;
    }
}
