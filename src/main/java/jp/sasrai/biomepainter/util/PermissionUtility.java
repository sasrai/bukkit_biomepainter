package jp.sasrai.biomepainter.util;

import org.bukkit.entity.Player;

/**
 * Created by sasrai on 2016/12/27.
 */
public class PermissionUtility {
    public static boolean shouldAllowedProcessing(Player player, String permission) {
        if (player.hasPermission(permission)) { return true; }
        else {
            player.sendMessage("[BiomePainter] That operation is not allowed by permission.");
            return false;
        }
    }

}
