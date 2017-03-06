package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.block.Biome;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class BiomeBaseCommonWrapper extends WrapperBase implements BiomeBaseInterface {
    private static Class<?> biomeBase;
    private Object instance;

    static {
        try {
            String biomebasePackage = getNMSPackage() + "BiomeBase";
            biomeBase = Class.forName(biomebasePackage);
        } catch (Exception e) {
            biomeBase = null;
        }
    }

    public BiomeBaseCommonWrapper() {
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
        return new BiomeBaseInterface[0];
    }

    @Override
    public int getId() {
        return -1;
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

    @Override
    public boolean isAvailable() {
        return (biomeBase != null);
    }
}
