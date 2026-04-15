package me.aleiv.core.paper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.aleiv.core.paper.objects.Cinematic;
import me.aleiv.core.paper.objects.Frame;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class StorageManager {
    private final Core plugin;
    private final File file;
    private final Gson gson;

    public StorageManager(Core plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "cinematics.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void save(HashMap<String, Cinematic> cinematics) {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            if (!file.exists()) file.createNewFile();
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(cinematics, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Cinematic> load() {
        if (!file.exists()) return new HashMap<>();
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<HashMap<String, Cinematic>>() {
            }.getType();
            HashMap<String, Cinematic> data = gson.fromJson(reader, type);
            return data != null ? data : new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();//Nejigay
        }
    }
}