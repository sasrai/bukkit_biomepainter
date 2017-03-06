package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.block.Biome;

/**
 * Created by sasrai on 2017/03/04.
 */
public interface CraftBlockInterface { // CraftBlockWrapperInterface
    public Biome BiomeBaseToBiome(BiomeBaseInterface biomebase);

    public Object BiomeToBiomeBase(Biome biome);

    public boolean isAvailable();
}
