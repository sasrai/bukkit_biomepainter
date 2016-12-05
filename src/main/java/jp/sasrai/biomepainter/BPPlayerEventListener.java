package jp.sasrai.biomepainter;

import jp.sasrai.biomepainter.data.BiomeCache;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
        Block target = getTargetBlock(player);

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

    private boolean getScrollDirection(int newSlot, int prevSlot) {
        int index = newSlot - prevSlot;
        return (index == -1 || index == 8);
    }
    private Block getTargetBlock(Player player) {
        // 1.7.10はgetTargetBlockが非推奨仕様の物しか使えない癖に1.8以降だとちゃんと推奨品用意されてるから
        // リフレクションでちゃんと推奨品が使われるように切り替え処理組んだ。なんという無駄。
        Method[] methods = player.getClass().getMethods();
        Method methodGetTargetBlock = null;
        Object dummyNull = null;
        for (Method method : methods) {
            if (method.getName().equals("getTargetBlock")) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length > 0 && params[0] == Set.class) {
                    methodGetTargetBlock = method;
                    dummyNull = (Set<Material>) null;
                    break;
                } else if (methodGetTargetBlock == null && params.length > 0 && params[0] == HashSet.class) {
                    methodGetTargetBlock = method;
                    dummyNull = (HashSet<Byte>) null;
                }
            }
        }
        if (methodGetTargetBlock == null) { return null; }

        try {
            return (Block)methodGetTargetBlock.invoke(player, dummyNull, 5);
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        } catch (InvocationTargetException ex) {
            return null;
        }
    }
}
