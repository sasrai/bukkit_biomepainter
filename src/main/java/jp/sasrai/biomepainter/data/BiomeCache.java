package jp.sasrai.biomepainter.data;

import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by sasrai on 2016/12/03.
 */

class BiomeCacheData {
    private enum KeyName {
        BIOME("biome"),
        DISABLED_SCROLL_MSG("disabledScrollMessage"),
        ;

        private final String key;

        KeyName(final String key) {
            this. key = key;
        }

        public String getString() {
            return this.key;
        }
    }
    boolean disabledBiomeScrollMessage;
    long lastWheelMoveTime;
    Biome biome;

    BiomeCacheData() {
        this.biome = null;
        disabledBiomeScrollMessage = false;
        lastWheelMoveTime = 0;
    }
    BiomeCacheData(ConfigurationSection section) {
        try { // バイオーム名の読み込み失敗時はnullにする
            this.biome = Biome.valueOf(section.getString(KeyName.BIOME.getString(), Biome.PLAINS.name()).toUpperCase());
        } catch (Exception e) {
            this.biome = null;
        }
        this.disabledBiomeScrollMessage = section.getBoolean(KeyName.DISABLED_SCROLL_MSG.getString(), false);
        this.lastWheelMoveTime = 0;
    }
    Map<String, String> toConfigurationSection() {
        Map<String, String> result = new HashMap<>();
        result.put(KeyName.BIOME.getString(), biome.toString());
        result.put(KeyName.DISABLED_SCROLL_MSG.getString(), String.valueOf(disabledBiomeScrollMessage));
        return result;
    }
}

public class BiomeCache {
    // 定数
    final String cacheFilename = "usercache.yml";

    private static Map<UUID, BiomeCacheData> biomeCache = new HashMap<>();

    private BiomeCacheData getData(UUID player) {
        if (biomeCache.containsKey(player)) {
            return biomeCache.get(player);
        } else {
            return new BiomeCacheData();
        }
    }

    public void setBiome(Player player, Biome biome) {
        setBiome(player.getUniqueId(), biome);
    }
    public void setBiome(UUID player, Biome biome) {
        BiomeCacheData data = getData(player);

        data.biome = biome;
        biomeCache.put(player, data);
    }

    public Biome getBiome(Player player) {
        return getBiome(player.getUniqueId());
    }
    public Biome getBiome(UUID player) {
        if (biomeCache.containsKey(player)) return biomeCache.get(player).biome;
        else return null;
    }

    public void setWheelMoveTime(Player player, long time) {
        setWheelMoveTime(player.getUniqueId(), time);
    }
    public void setWheelMoveTime(UUID player, long time) {
        BiomeCacheData data = getData(player);

        data.lastWheelMoveTime = time;
        biomeCache.put(player, data);
    }

    public long getWheelMoveTime(Player player) {
        return getWheelMoveTime(player.getUniqueId());
    }
    public long getWheelMoveTime(UUID player) {
        if (biomeCache.containsKey(player)) return biomeCache.get(player).lastWheelMoveTime;
        else return 0;
    }

    public void setDisabledBiomeScrollMessageFlag(Player player, boolean flag) {
        setDisabledBiomeScrollMessageFlag(player.getUniqueId(), flag);
    }
    public void setDisabledBiomeScrollMessageFlag(UUID player, boolean flag) {
        BiomeCacheData data = getData(player);

        data.disabledBiomeScrollMessage = flag;
        biomeCache.put(player, data);
    }

    public boolean getDisabledBiomeScrollMessageFlag(Player player) {
        return getDisabledBiomeScrollMessageFlag(player.getUniqueId());
    }
    public boolean getDisabledBiomeScrollMessageFlag(UUID player) {
        return biomeCache.containsKey(player) && biomeCache.get(player).disabledBiomeScrollMessage;
    }

    public void loadFromFile(File dataDirectory) {
        YamlConfiguration userCache = YamlConfiguration.loadConfiguration(new File(dataDirectory, cacheFilename));

        if (userCache.contains("users")) {
            ConfigurationSection users = userCache.getConfigurationSection("users");

            for (String uuidString: users.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                biomeCache.put(uuid, new BiomeCacheData(users.getConfigurationSection(uuidString)));
            }
        }
    }

    public void saveToFile(File dataDirectory) {
        YamlConfiguration userCache = new YamlConfiguration();

        Map<String, Map<String, String>> users = new HashMap<>();
        for (Map.Entry<UUID, BiomeCacheData> userdata: biomeCache.entrySet()) {
            users.put(userdata.getKey().toString(), userdata.getValue().toConfigurationSection());
        }

        userCache.set("users", users);

        try {
            userCache.save(new File(dataDirectory, cacheFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
