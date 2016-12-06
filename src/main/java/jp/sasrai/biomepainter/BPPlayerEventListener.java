package jp.sasrai.biomepainter;

import com.github.keepoff07.ParticleAPI;
import jp.sasrai.biomepainter.data.BiomeCache;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.util.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Vector;

/**
 * Created by sasrai on 2016/12/03.
 */
class BPPlayerEventListener implements Listener {
    private final BiomePainter plugin;

    BPPlayerEventListener(BiomePainter plugin) {
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

    @EventHandler(priority = EventPriority.LOW)
    public void onScrollBiome(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (canScrollBiome(player)) {
            scrollBiome(player, getScrollDirection(event.getNewSlot(), event.getPreviousSlot()));
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

        showEffect(player, target);

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
    private boolean canScrollBiome(Player player) {
        return player.isSneaking()
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.paint");
    }
    private boolean scrollBiome(Player player, boolean scrollUp) {
        long milliSeconds = System.currentTimeMillis();
        // 前回イベントから245ms未満の場合はバイオーム書き換え処理を行わない
        if (milliSeconds - BiomeCache.getInstance().getWheelMoveTime(player) < 245) { return false; }
        Block target = getTargetBlockForumVer(player, 6);

        // 現在のバイオームを取得
        if (target == null) { return false; }
        Biome currentBiome = target.getBiome();

        // 次のバイオームを取得
        Biome nextBiome;
        Biome[] allBiomes = Biome.values();
        List<Biome> allBiomesList = Arrays.asList(allBiomes);
        int index = allBiomesList.indexOf(currentBiome);
        if (scrollUp) {
            if (allBiomesList.size() <= ++index) { index = 0; }
        } else {
            if (--index <= 0) { index = allBiomesList.size() - 1; }
        }
        nextBiome = allBiomesList.get(index);
        // バイオームの変更処理
        target.setBiome(nextBiome);

        showEffect(player, target);

        // チャンクを更新
        Chunk chunk = target.getChunk();
        target.getWorld().refreshChunk(chunk.getX(), chunk.getZ());

        // メッセージ送信
        if (!BiomeCache.getInstance().getDisabledBiomeScrollMessageFlag(player)) {
            player.sendMessage("[BiomePainter] Biome switch. " + currentBiome.toString() + " => " + nextBiome.toString());
        }

        BiomeCache.getInstance().setWheelMoveTime(player, milliSeconds);

        return true;
    }

    private void showEffect(Player player, Block target) {
        Location loc = target.getLocation().add(0.5f, 1f, 0.5f);
        // 上が空気じゃなかったら横にずらす
        if (loc.getBlock().getType() != Material.AIR) {
            org.bukkit.util.Vector v = player.getEyeLocation().getDirection().normalize();
            org.bukkit.util.Vector v2 = new org.bukkit.util.Vector(-0.7f * v.getX(), -0.7f * v.getY(), -0.7f * v.getZ());
            loc.add(v2);
        }
        if (!ParticleAPI.createEffect(ParticleAPI.EnumParticle.VILLAGER_HAPPY, (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(), 0.15f, 0.6f, 0.15f, 0.3f, 10)) {
            plugin.getLogger().warning("send effect packet error...");
        }
        target.getWorld().playSound(loc, Sound.CLICK, 10f, 10f);
    }

    private boolean getScrollDirection(int newSlot, int prevSlot) {
        int index = newSlot - prevSlot;
        return (index == -1 || index == 8);
    }
    private Block getTargetBlockForumVer(Player player, int range) {
        Location loc = player.getEyeLocation();
        org.bukkit.util.Vector dir = loc.getDirection().normalize();

        Block block = null;

        for (int i = 0; i <= range; i++) {
            block = loc.add(dir).getBlock();
            if (block.getType() != Material.AIR)
                break;
        }

        return block;
    }
}
