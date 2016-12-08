package jp.sasrai.biomepainter;

import jp.sasrai.biomepainter.Tool.PaintTool;
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

    BPPlayerEventListener(BiomePainter plugin) {
        this.plugin = plugin;
        this.tool = plugin.getTool();

        registerEvents();
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

        if (tool.canBiomePainting(player, event.getAction())) {
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

        if (tool.canScrollBiome(player)) {
            tool.scrollBiome(player, event.getNewSlot(), event.getPreviousSlot());
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
