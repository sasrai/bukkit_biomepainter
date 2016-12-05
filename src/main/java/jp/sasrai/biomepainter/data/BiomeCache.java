package jp.sasrai.biomepainter.data;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sasrai on 2016/12/03.
 */

class BiomeCacheData {
    boolean disabledBiomeScrollMessage;
    long lastWheelMoveTime;
    Biome biome;
}

public class BiomeCache {
    private static BiomeCache ourInstance = new BiomeCache();

    public static BiomeCache getInstance() {
        return ourInstance;
    }

    private static HashMap<UUID, BiomeCacheData> biomeCache = new HashMap<>();

    private BiomeCache() {
    }

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

    public void LoadFromCache() {

    }
}
