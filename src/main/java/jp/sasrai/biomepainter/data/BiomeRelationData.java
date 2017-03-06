package jp.sasrai.biomepainter.data;

import jp.sasrai.biomepainter.util.wrapper.BiomeBaseInterface;
import org.bukkit.block.Biome;

/**
 * Created by sasrai on 2016/12/12.
 */
public class BiomeRelationData {
    String mcName = "Ocian";
    Biome biome = Biome.OCEAN;
    BiomeBaseInterface biomebase = null;
    int id = 0;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ID:")
            .append(id)
            .append(",BUKKITBIOME:")
            .append(biome.toString())
            .append(",MCBIOME:")
            .append(mcName)
            .append("}");
        return sb.toString();
    }
}
