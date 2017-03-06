package jp.sasrai.biomepainter.util.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class BiomeBase_1_7 extends BiomeBaseCommonWrapper {
    private Field getIDField() throws NoSuchFieldException {
        return getBiomeBaseClass().getField("id");
    }
    private int getIDFieldData() {
        try {
            return getIDField().getInt(getBiomeBaseObject());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return -1;
        }
    }

    @Override
    public BiomeBaseInterface[] getBiomes() {
        try {
            Method getBiomes = getBiomeBaseClass().getMethod("getBiomes");
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
}
