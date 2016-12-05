package jp.sasrai.biomepainter.data;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by keiso on 2016/12/03.
 */

class BiomeCacheData {
    public Biome biome;
}

public class BiomeCache {
    private static BiomeCache ourInstance = new BiomeCache();

    public static BiomeCache getInstance() {
        return ourInstance;
    }

    static HashMap<UUID, BiomeCacheData> biomeCache = new HashMap<>();

    private BiomeCache() {
    }

    public void setBiome(Player player, Biome biome) {
        setBiome(player.getUniqueId(), biome);
    }
    public void setBiome(UUID player, Biome biome) {
        BiomeCacheData data;

        if (biomeCache.containsKey(player)) {
            data = biomeCache.get(player);
        } else {
            data = new BiomeCacheData();
        }

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

    public void LoadFromCache() {

    }
}
