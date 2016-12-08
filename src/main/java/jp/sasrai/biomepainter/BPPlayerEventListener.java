package jp.sasrai.biomepainter;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jp.sasrai.biomepainter.Tool.PaintTool;
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

/**
 * Created by sasrai on 2016/12/03.
 */
class BPPlayerEventListener implements Listener {
    private final BiomePainter plugin;
    private final PaintTool tool;
    private final WorldGuardPlugin worldGuard;

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

        if (worldGuard.canBuild(player, target)) { return true; }
        else {
            player.sendMessage("[BiomePainter] Can't edit Biome.");
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

    @EventHandler(priority = EventPriority.LOW)
    public void onScrollBiome(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Block target = getTargetBlockForumVer(player, 6);

        if (tool.canScrollBiome(player) && shouldEditingEvent(player, target)) {
            tool.scrollBiome(player, target, event.getNewSlot(), event.getPreviousSlot());
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
