package pluginsmc.langdua.core.paper;

import org.bukkit.configuration.file.YamlConfiguration;
import pluginsmc.langdua.core.paper.objects.TimelineDefinition;
import pluginsmc.langdua.core.paper.objects.TimelineEntry;
import pluginsmc.langdua.core.paper.objects.TransitionEffect;
import pluginsmc.langdua.core.paper.objects.TransitionMetadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimelineStorageManager {
    private final Core instance;
    private final File folder;

    public TimelineStorageManager(Core instance) {
        this.instance = instance;
        this.folder = new File(instance.getDataFolder(), "timelines");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    public void save(Map<String, TimelineDefinition> timelines) {
        for (TimelineDefinition timeline : timelines.values()) {
            timeline.ensureStructure();
            File file = new File(folder, timeline.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("name", timeline.getName());

            List<Map<String, Object>> entries = new ArrayList<>();
            for (TimelineEntry entry : timeline.getEntries()) {
                Map<String, Object> entryMap = new LinkedHashMap<>();
                entryMap.put("name", entry.getName());
                entryMap.put("cinematic", entry.getCinematicName());
                entryMap.put("transition.effect", entry.getTransition().getEffect().name());
                entryMap.put("transition.durationTicks", entry.getTransition().getDurationTicks());
                entryMap.put("transition.strength", entry.getTransition().getStrength());
                entries.add(entryMap);
            }
            config.set("entries", entries);

            try {
                config.save(file);
            } catch (IOException e) {
                instance.getLogger().severe("Failed to save timeline file '" + file.getName() + "'.");
                e.printStackTrace();
            }
        }
    }

    public Map<String, TimelineDefinition> load() {
        Map<String, TimelineDefinition> timelines = new HashMap<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return timelines;
        }

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                if (name == null || name.isBlank()) {
                    continue;
                }

                TimelineDefinition timeline = new TimelineDefinition(name);
                List<Map<?, ?>> entryMaps = config.getMapList("entries");
                for (Map<?, ?> entryMap : entryMaps) {
                    String entryName = (String) entryMap.get("name");
                    String cinematicName = (String) entryMap.get("cinematic");
                    if (entryName == null || entryName.isBlank() || cinematicName == null || cinematicName.isBlank()) {
                        continue;
                    }

                    TimelineEntry entry = new TimelineEntry(entryName, cinematicName);
                    TransitionMetadata transition = new TransitionMetadata();
                    Object effectValue = entryMap.get("transition.effect");
                    if (effectValue instanceof String effectName) {
                        try {
                            transition.setEffect(TransitionEffect.valueOf(effectName));
                        } catch (IllegalArgumentException ignored) {
                            transition.setEffect(TransitionEffect.NONE);
                        }
                    }
                    transition.setDurationTicks(asInt(entryMap.get("transition.durationTicks")));
                    transition.setStrength(Math.max(1, asInt(entryMap.get("transition.strength"))));
                    entry.setTransition(transition);
                    timeline.getEntries().add(entry);
                }
                timeline.ensureStructure();
                timelines.put(name, timeline);
            } catch (Throwable t) {
                instance.getLogger().severe("Failed to load timeline file '" + file.getName() + "'. Skipping it so the plugin can continue enabling.");
                t.printStackTrace();
            }
        }
        return timelines;
    }

    public boolean delete(String name) {
        File file = new File(folder, name + ".yml");
        return !file.exists() || file.delete();
    }
}
