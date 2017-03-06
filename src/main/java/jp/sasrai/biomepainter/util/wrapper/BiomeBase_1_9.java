package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.block.Biome;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class BiomeBase_1_9 extends BiomeBaseCommonWrapper {
    private static Method getIdMethod;
    private static Method getNameMethod;

    private Method getIdMethod() throws NoSuchMethodException {
        if (getIdMethod == null)
        {
            // TODO: OceanバイオームからIDと思われるメソッドを探索する処理を追加する必要があるかもしれない
            getIdMethod = getBiomeBaseClass().getMethod("a", getBiomeBaseClass());
        }
        return getIdMethod;
    }
    private int getIdFromInstanceObj() {
        try {
            return (int) getIdMethod().invoke(null, getBiomeBaseObject());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return -1;
        }
    }

    private Method getNameMethod() throws NoSuchMethodException {
        if (getNameMethod == null)
        {
            // TODO: OceanバイオームからIDと思われるメソッドを探索する処理を追加する必要があるかもしれない
            getNameMethod = getBiomeBaseClass().getMethod("l");
        }
        return getNameMethod;
    }
    private String getNameFromInstanceObj() {
        try {
            return (String) getNameMethod().invoke(getBiomeBaseObject());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    @Override
    public BiomeBaseInterface[] getBiomes() {
        try {
            Biome bukkitBiomes[] = Biome.values();
            BiomeBase_1_9 biomes[] = new BiomeBase_1_9[bukkitBiomes.length];
            CraftBlockInterface craftBlock = new CraftBlockWrapper();

            for (int i = 0; i < bukkitBiomes.length; i++) {
                BiomeBase_1_9 bb = new BiomeBase_1_9();
                bb.setBiomeBaseObject(craftBlock.BiomeToBiomeBase(bukkitBiomes[i]));
                biomes[i] = bb;
            }

            return biomes;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return new BiomeBaseInterface[0];
        }
    }

    @Override
    public int getId() {
        return getIdFromInstanceObj();
    }

    @Override
    public String getName() {
        return getNameFromInstanceObj();
    }

    @Override
    public boolean isAvailable() {
        try {
            return super.isAvailable() && getIdMethod() != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
