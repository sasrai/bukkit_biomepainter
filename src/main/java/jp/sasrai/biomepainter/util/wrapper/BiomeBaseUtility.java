package jp.sasrai.biomepainter.util.wrapper;

/**
 * Created by sasrai on 2017/03/06.
 */
abstract public class BiomeBaseUtility extends WrapperBase {
    public static BiomeBaseInterface getBiomeBase() {
        return getBiomeBase(null);
    }

    public static BiomeBaseInterface getBiomeBase(Object bbObj) {
        if (isNumericIdStyle())
            return new BiomeBase_1_8();
        else
            return new BiomeBase_1_9();
    }
}
