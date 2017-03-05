package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.block.Biome;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class BiomeBase_1_9 extends WrapperBase implements BiomeBaseInterface {
    private final Class<?> biomeBase;
    private Object instance;

    public BiomeBase_1_9() {
        biomeBase = getNMSBiomeBase();
    }

    private Class<?> getNMSBiomeBase() {
        try {
            String biomebasePackage = getNMSPackage() + "BiomeBase";
            return Class.forName(biomebasePackage);
        } catch (Exception e) {
            return null;
        }
    }

    private Method getIDMethod() throws NoSuchMethodException {
        return biomeBase.getMethod("a", biomeBase);
    }
    private int getIDMethodData() {
        try {
            instance.toString();
            return (int) getIDMethod().invoke(null, instance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return -1;
        }
    }

    private boolean isBBNameField(Field field) {
        return (field.getType() == String.class);
    }
    private Field getNameField() throws NoSuchFieldException {
        for (Field field: biomeBase.getDeclaredFields()) {
            if (isBBNameField(field)) {
                return field;
            }
        }
        throw new NoSuchFieldException();
    }
    private String getNameFieldData() {
        try {
            return (String)getNameField().get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return "OCEAN";
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

    @Override
    public String getName() {
        return getNameFieldData();
    }

    @Override
    public String getCanonicalName() {
        return biomeBase.getCanonicalName();
    }

    @Override
    public Class<?> getBiomeBaseClass() {
        return biomeBase;
    }

    @Override
    public Object getBiomeBaseObject() {
        return instance;
    }

    @Override
    public void setBiomeBaseObject(Object biomeBaseObject) {
        instance = biomeBaseObject;
    }
}
