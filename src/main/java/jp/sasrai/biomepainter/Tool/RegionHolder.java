package jp.sasrai.biomepainter.Tool;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by sasrai on 2016/12/09.
 */
public class RegionHolder {
    private static boolean canBuildRangeHeight(LocalPlayer player, RegionQuery query, Location location, int start, int end, boolean isIncrement) {
        boolean canBuild = true;

        for (int i = start; (isIncrement && i <= end) || (!isIncrement && i >= end); ) {
            location.setY(i);
            if (!query.testState(location, player, DefaultFlag.BUILD)) {
                canBuild = false;
                break;
            }

            if (isIncrement) { i++; } else { i--; }
        }

        return canBuild;
    }
    public static boolean canBuildAllHeight(WorldGuardPlugin worldGuard, Player player, Location location) {
        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
        RegionContainer container = worldGuard.getRegionContainer();
        RegionQuery query = container.createQuery();
        Location targetLocation = location.clone();
        boolean canBuild;

        // 利用率が高そうなY=80以下を先行してチェック、チェック通過時に残りの上空もチェックする
        canBuild = canBuildRangeHeight(localPlayer, query, targetLocation, 80, 0, false);
        if (canBuild) { canBuild = canBuildRangeHeight(localPlayer, query, targetLocation, 81, 255, true); }

        return canBuild;
    }
}
