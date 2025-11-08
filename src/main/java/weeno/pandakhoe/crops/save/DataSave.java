package weeno.pandakhoe.crops.save;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import weeno.pandakhoe.PandakHoe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataSave {

    private final PandakHoe plugin;
    private final File file;
    private final Gson gson;
    private BukkitTask task;

    public DataSave(PandakHoe plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "crops.json.gz");
        this.gson = new Gson();
    }

    public void startAutoSave(Map<Long, Byte> data) {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> saveAsync(data), 288000L, 288000L);
    }

    public void stopAutoSave() {
        if (task != null) task.cancel();
    }

    public Map<Long, Byte> load() {
        if (!file.exists()) return new ConcurrentHashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(file.toPath())), StandardCharsets.UTF_8))) {
            Map<Long, Byte> data = gson.fromJson(br, new TypeToken<ConcurrentHashMap<Long, Byte>>(){}.getType());
            plugin.getLogger().info("Loaded " + data.size() + " crops");
            return data != null ? data : new ConcurrentHashMap<>();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load crops", e);
            return new ConcurrentHashMap<>();
        }
    }

    public void saveAsync(Map<Long, Byte> data) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveSync(data));
    }

    public void saveSync(Map<Long, Byte> data) {
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(file.toPath())), StandardCharsets.UTF_8))) {
                gson.toJson(data, bw);
            }

            plugin.getLogger().info("Saved " + data.size() + " crops");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save crops", e);
        }
    }
}