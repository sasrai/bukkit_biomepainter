package jp.sasrai.biomepainter;

import jp.sasrai.biomepainter.data.BiomeCache;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Formatter;

/**
 * Created by sasrai on 2016/12/03.
 */
public class BPPlayerEventListener implements Listener {
    private final BiomePainter plugin;

    public BPPlayerEventListener(BiomePainter plugin) {
        this.plugin = plugin;

        registerEvents();
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBiomePickup(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (canPickupBlock(player, event.getAction())) {
            pickupBlockInfo(player, event.getClickedBlock());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBiomePaint(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (canBiomePainting(player, event.getAction())) {
            replaceBiome(player, event.getClickedBlock());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShowBiomeInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (canShowBiomeInfo(player, event.getAction())) {
            showBiomeInfo(player, event.getClickedBlock());
            event.setCancelled(true);
        }
    }

    private boolean shouldAllowedProcessing(Player player, String permission) {
        if (player.hasPermission(permission)) { return true; }
        else {
            player.sendMessage("That operation is not allowed by permission");
            return false;
        }
    }
    private boolean isUsingPlugin(Player player) {
        return player.getGameMode() == GameMode.CREATIVE
                && player.getItemInHand().getType() == BPToolConfig.getInstance().getToolItem();
    }

    private boolean canPickupBlock(Player player, Action action) {
        return action == Action.LEFT_CLICK_BLOCK
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.pickup");
    }
    private boolean pickupBlockInfo(Player player, Block target) {
        BiomeCache.getInstance().setBiome(player, target.getBiome());

        player.sendMessage("[BiomePaint] picked up " + target.getBiome().toString() + " biome.");

        return true;
    }
    private boolean canBiomePainting(Player player, Action action) {
        return action == Action.RIGHT_CLICK_BLOCK
                && !player.isSneaking()
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.paint");
    }
    private boolean replaceBiome(Player player, Block target) {
        Biome newBiome = BiomeCache.getInstance().getBiome(player);
        Location loc = target.getLocation();

        if (newBiome == null) {
            player.sendMessage("[BiomePaint] Don't set biome. Please set from LeftClick or `/biome set <biome_name>` command.");
            return false;
        }

        // バイオーム書き換え
        loc.getWorld().setBiome(loc.getBlockX(), loc.getBlockZ(), newBiome);
        // チャンク再読込
        loc.getWorld().refreshChunk(loc.getChunk().getX(), loc.getChunk().getZ());

        return true;
    }
    private boolean canShowBiomeInfo(Player player, Action action) {
        return action == Action.RIGHT_CLICK_BLOCK
                && player.isSneaking()
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.check");
    }
    private void showBiomeInfo(Player player, Block target) {
        Formatter fm = new Formatter();
        Location loc = target.getLocation();
        fm.format("[BiomePaint] X=%d, Z=%d, Biome=%s", loc.getBlockX(), loc.getBlockZ(), target.getBiome());
        player.sendMessage(fm.toString());

        // おまけのチャンク再読込
        loc.getWorld().refreshChunk(loc.getChunk().getX(), loc.getChunk().getZ());
    }
}
