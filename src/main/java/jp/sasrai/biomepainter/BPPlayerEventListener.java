package jp.sasrai.biomepainter;

import jp.sasrai.biomepainter.Tool.PaintTool;
import org.bukkit.ChatColor;
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

    private int wheelDelayTime = 245;

    private final Map<UUID, Long> scrollEventDelayMap = new HashMap<>();

    BPPlayerEventListener(BiomePainter plugin) {
        this.plugin = plugin;
        this.tool = plugin.getTool();

        setEventConfigFromFile();

        registerEvents();
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void setEventConfigFromFile() {
        String delayTimeValue = plugin.getConfig().getString("tool.wheelBiomeChangeDelay", "0");
        try {
            wheelDelayTime = Integer.valueOf(delayTimeValue);
        } catch (Exception e) {
            plugin.getServer().getConsoleSender().sendMessage("[BiomePainter] wheelBiomeChangeDelay : " + ChatColor.YELLOW + "Characters other than numbers are included.");
            wheelDelayTime = 0;
        }
        if (wheelDelayTime > 1000) wheelDelayTime = 1000; // 長過ぎるディレイは設定させないほうがいいでしょ
        if (wheelDelayTime > 0)
            plugin.getLogger().info("Wheel scroll delay time = " + wheelDelayTime + "ms");
    }

    private boolean isScrollDelayTimeout(UUID player) {
        if (wheelDelayTime < 1 || !scrollEventDelayMap.containsKey(player)) return true;
        return System.currentTimeMillis() - scrollEventDelayMap.get(player) >= wheelDelayTime;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBiomePickup(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tool.pickupBlockInfo(player, event.getMaterial(), event.getAction(), event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBiomePaint(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        tool.replaceBiome(player, event.getMaterial(), event.getAction(), event.getClickedBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShowBiomeInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tool.showBiomeInfo(player, event.getMaterial(), event.getAction(), event.getClickedBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShowToolInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (tool.canShowToolInfo(player, event.getMaterial(), event.getAction())) {
            tool.showToolInfo(player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onScrollBiome(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (tool.canScrollBiome(player) && event.getPreviousSlot() != event.getNewSlot()) {
            if (isScrollDelayTimeout(player.getUniqueId())) {
                if (tool.scrollBiome(player, event.getNewSlot(), event.getPreviousSlot()))
                    scrollEventDelayMap.put(player.getUniqueId(), System.currentTimeMillis());
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
