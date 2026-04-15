package pluginsmc.langdua.core.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pluginsmc.langdua.core.paper.objects.Cinematic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class StorageManager {
    private final Core plugin;
    private final File folder;
    private final Gson gson;

    public StorageManager(Core plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "cinematics");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (!this.folder.exists()) {
            this.folder.mkdirs();
        }
    }

    public void save(HashMap<String, Cinematic> cinematics) {
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Save active cinematics to individual files
        for (Cinematic cinematic : cinematics.values()) {
            File file = new File(folder, cinematic.getName() + ".json");
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(cinematic, writer);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save cinematic: " + cinematic.getName());
                e.printStackTrace();
            }
        }

        // Remove files for cinematics that were deleted in-game
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName().replace(".json", "");
                if (!cinematics.containsKey(name)) {
                    file.delete();
                }
            }
        }
    }

    public HashMap<String, Cinematic> load() {
        HashMap<String, Cinematic> data = new HashMap<>();

        if (!folder.exists() || !folder.isDirectory()) {
            return data;
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return data;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                Cinematic cinematic = gson.fromJson(reader, Cinematic.class);
                if (cinematic != null && cinematic.getName() != null) {
                    data.put(cinematic.getName(), cinematic);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load cinematic file: " + file.getName());
                e.printStackTrace();
            }
        }
        return data;
    }
}