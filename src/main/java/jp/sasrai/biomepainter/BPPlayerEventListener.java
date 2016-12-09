package jp.sasrai.biomepainter;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jp.sasrai.biomepainter.Tool.PaintTool;
import jp.sasrai.biomepainter.Tool.RegionHolder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by sasrai on 2016/12/03.
 */
class BPPlayerEventListener implements Listener {
    private final BiomePainter plugin;
    private final PaintTool tool;
    private final WorldGuardPlugin worldGuard;

    private final Map<UUID, Long> scrollEventDelayMap = new HashMap<>();

    BPPlayerEventListener(BiomePainter plugin) {
        this.plugin = plugin;
        this.tool = plugin.getTool();
        this.worldGuard = plugin.getWorldGuard();

        plugin.getLogger().info("WorldGuard = " + worldGuard);

        registerEvents();
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private boolean shouldEditingEvent(Player player, Block target) {
        if (worldGuard == null) { return true; } // WorldGuard無しは常に許可

        // WorldGuard
        boolean canBuild = false;
        try {
            canBuild = RegionHolder.canBuildAllHeight(worldGuard, player, target.getLocation());
        } catch (NoClassDefFoundError e) { }

        if (canBuild) { return true; }
        else {
            player.sendMessage("[BiomePainter] Regions protected by WorldGuard can not be edited.");
            return false;
        }
    }
    private Block getTargetBlockForumVer(Player player, int range) {
        Location loc = player.getEyeLocation();
        org.bukkit.util.Vector dir = loc.getDirection().normalize();

        Block block = null;

        for (int i = 0; i <= range; i++) {
            Block b = loc.add(dir).getBlock();
            if (b.getType() != Material.AIR) {
                block = b;
                break;
            }
        }

        return block;
    }
    private boolean isScrollDelayTimeout(UUID player) {
        if (!scrollEventDelayMap.containsKey(player)) { return true; }
        return System.currentTimeMillis() - scrollEventDelayMap.get(player) >= 245;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBiomePickup(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tool.canPickupBlock(player, event.getAction())) {
            tool.pickupBlockInfo(player, event.getClickedBlock());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBiomePaint(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tool.canBiomePainting(player, event.getAction()) && shouldEditingEvent(player, event.getClickedBlock())) {
            tool.replaceBiome(player, event.getClickedBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShowBiomeInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tool.canShowBiomeInfo(player, event.getAction())) {
            tool.showBiomeInfo(player, event.getClickedBlock());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShowToolInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tool.canShowToolInfo(player, event.getAction())) {
            tool.showToolInfo(player);
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onScrollBiome(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Block target = getTargetBlockForumVer(player, 6);

        if (tool.canScrollBiome(player)) {
            if (isScrollDelayTimeout(player.getUniqueId())) {
                scrollEventDelayMap.put(player.getUniqueId(), System.currentTimeMillis());
                if (shouldEditingEvent(player, target)) {
                    tool.scrollBiome(player, target, event.getNewSlot(), event.getPreviousSlot());
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoginMessage(PlayerJoinEvent event) {
        if (tool.isCustomizedTool()) {
            tool.sendChangeToolMessage(event.getPlayer());
        }
    }
}
