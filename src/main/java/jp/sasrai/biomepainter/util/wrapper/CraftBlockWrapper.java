package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.block.Biome;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class CraftBlockWrapper extends WrapperBase implements CraftBlockInterface {
    private static Class<?> craftBlock;
    static {
        try {
            String craftblockPackage = getOBCPackage() + "block.CraftBlock";
            craftBlock = Class.forName(craftblockPackage);
        } catch (Exception e) {
            craftBlock = null;
        }
    }

    private Method biomeBaseToBiomeMethod;
    private Method biomeToBiomeBaseMethod;

    public CraftBlockWrapper() {
    }

    private Method getBBtoBiomeMethod(Class<?> biomebase) throws NoSuchMethodException {
        return craftBlock.getMethod("biomeBaseToBiome", biomebase);
    }

    private Method getBiomeToBBMethod() throws NoSuchMethodException {
        return craftBlock.getMethod("biomeToBiomeBase", Biome.class);
    }

    @Override
    public Biome BiomeBaseToBiome(BiomeBaseInterface biomebase) {
        try {
            if (null == biomeBaseToBiomeMethod)
                biomeBaseToBiomeMethod = getBBtoBiomeMethod(biomebase.getBiomeBaseClass());
            return (Biome)biomeBaseToBiomeMethod.invoke(null, biomebase.getBiomeBaseObject());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public Object BiomeToBiomeBase(Biome biome) {
        try {
            if (null == biomeToBiomeBaseMethod)
                biomeToBiomeBaseMethod = getBiomeToBBMethod();
            Object bbObj = biomeToBiomeBaseMethod.invoke(null, biome);
            return bbObj;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        return (craftBlock != null);
    }
}
