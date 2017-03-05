package jp.sasrai.biomepainter.util.wrapper;

/**
 * Created by sasrai on 2017/03/04.
 */
public interface BiomeBaseInterface { // BiomeBaseWrapperInterface
    public BiomeBaseInterface[] getBiomes();

    public int getId();
    public String getName();

    public String getCanonicalName();

    public Class<?> getBiomeBaseClass();
    public Object getBiomeBaseObject();
    public void setBiomeBaseObject(Object biomeBaseObject);
}
