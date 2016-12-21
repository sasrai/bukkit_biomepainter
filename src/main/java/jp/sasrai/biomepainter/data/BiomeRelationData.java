package jp.sasrai.biomepainter.data;

import org.bukkit.block.Biome;

/**
 * Created by sasrai on 2016/12/12.
 */
public class BiomeRelationData {
    public String mcName = "Ocian";
    public Biome biome = Biome.OCEAN;
    public Object biomebase = "";
    public int id = 0;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ID:");
        sb.append(id);
        sb.append(",BUKKITBIOME:");
        sb.append(biome.toString());
        sb.append(",MCBIOME:");
        sb.append(mcName);
        sb.append("}");
        return sb.toString();
    }
}
