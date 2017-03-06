package jp.sasrai.biomepainter.util.wrapper;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Method;

/**
 * Created by sasrai on 2017/03/04.
 */
public class WrapperBase {
    private final static String NMSPackage = "net.minecraft.server.";
    private final static String OBCPackage = "org.bukkit.craftbukkit.";

    private final static String mcVersion;
    private final static String nmsPackage;
    private final static String obcPackage;

    private final static boolean numericIdStyle;

    static {
        mcVersion = getVersionString();

        numericIdStyle = (Integer.parseInt(mcVersion.split("_")[1]) < 8);

        nmsPackage = NMSPackage + mcVersion + ".";
        obcPackage = OBCPackage + mcVersion + ".";
    }

    public static String getVersionString() {
        try {
            Server server = Bukkit.getServer();
            Method getHandler = server.getClass().getMethod("getHandle");
            String nmsPackage = getHandler.invoke(server).getClass().getPackage().getName();

            return nmsPackage.substring(nmsPackage.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return null;
        }
    }

    protected static boolean isNumericIdStyle() {
        return numericIdStyle;
    }

    protected static String getNMSPackage() {
        return nmsPackage;
    }
    protected static String getOBCPackage() {
        return obcPackage;
    }
}
