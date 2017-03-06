package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.block.Biome;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class BiomeBase_1_9 extends BiomeBaseCommonWrapper {
    private Method getIDMethod() throws NoSuchMethodException {
        return getBiomeBaseClass().getMethod("a", getBiomeBaseClass());
    }
    private int getIDMethodData() {
        try {
            return (int) getIDMethod().invoke(null, getBiomeBaseObject());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return -1;
        }
    }

    @Override
    public BiomeBaseInterface[] getBiomes() {
        try {
            Biome bukkitBiomes[] = Biome.values();
            BiomeBase_1_9 biomes[] = new BiomeBase_1_9[256];
            CraftBlockInterface craftBlock = new CraftBlockWrapper();

            for (Biome biome: bukkitBiomes) {
                BiomeBase_1_9 bb = new BiomeBase_1_9();
                bb.setBiomeBaseObject(craftBlock.BiomeToBiomeBase(biome));
                biomes[bb.getId()] = bb;
            }

            return biomes;
        } catch (ArrayIndexOutOfBoundsException e) {
            return new BiomeBaseInterface[0];
        }
    }

    @Override
    public int getId() {
        return getIDMethodData();
    }
}
