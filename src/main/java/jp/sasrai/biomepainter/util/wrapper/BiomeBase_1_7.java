package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.block.Biome;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class BiomeBase_1_7 extends WrapperBase implements BiomeBaseInterface {
    private final Class<?> biomeBase;
    private Object instance;

    public BiomeBase_1_7() {
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

    private Field getIDField() throws NoSuchFieldException {
        return biomeBase.getField("id");
    }
    private int getIDFieldData() {
        try {
            return getIDField().getInt(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
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
            Method getBiomes = biomeBase.getMethod("getBiomes");
            Object biomesObj[] = (Object[]) getBiomes.invoke(null);
            BiomeBase_1_7 biomes[] = new BiomeBase_1_7[biomesObj.length];

            for (int i = 0; i < biomesObj.length; i++) {
                biomes[i].setBiomeBaseObject(biomesObj[i]);
            }

            return biomes;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return new BiomeBaseInterface[0];
        }
    }

    @Override
    public int getId() {
        return getIDFieldData();
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
