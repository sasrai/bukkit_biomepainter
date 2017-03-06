package jp.sasrai.biomepainter.util.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class BiomeBase_1_8 extends BiomeBaseCommonWrapper {
    private Field getIDField() throws NoSuchFieldException {
        return getBiomeBaseClass().getField("id");
    }
    private int getIDFieldData() {
        try {
            return getIDField().getInt(getBiomeBaseObject());
        } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
            return -1;
        }
    }

    private boolean isBBNameField(Field field) {
        return (field.getType() == String.class);
    }
    private Field getNameField() throws NoSuchFieldException {
        for (Field field: getBiomeBaseClass().getDeclaredFields()) {
            if (isBBNameField(field)) {
                return field;
            }
        }
        throw new NoSuchFieldException();
    }
    private String getNameFieldData() {
        try {
            return (String)getNameField().get(getBiomeBaseObject());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return "OCEAN";
        }
    }

    @Override
    public BiomeBaseInterface[] getBiomes() {
        try {
            Method getBiomes = getBiomeBaseClass().getMethod("getBiomes");
            Object biomesObj[] = (Object[]) getBiomes.invoke(null);
            BiomeBase_1_8 biomes[] = new BiomeBase_1_8[biomesObj.length];

            for (int i = 0; i < biomesObj.length; i++) {
                biomes[i] = new BiomeBase_1_8();
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
    public boolean isAvailable() {
        try {
            return super.isAvailable() && getIDField() != null;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
