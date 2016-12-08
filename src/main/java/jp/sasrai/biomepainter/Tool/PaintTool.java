package jp.sasrai.biomepainter.Tool;

import com.github.keepoff07.ParticleAPI;
import jp.sasrai.biomepainter.BiomePainter;
import jp.sasrai.biomepainter.data.BiomeCache;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

/**
 * Created by sasrai on 2016/12/08.
 */
public class PaintTool {
    private final BiomePainter plugin;
    private final BiomeCache cache;

    static Material DefaultToolMaterial = Material.ARROW;
    static Material toolItem;

    public PaintTool(BiomePainter plugin) {
        this.plugin = plugin;
        this.cache = new BiomeCache();

        setToolItem();
    }

    private void setToolItem() {
        String toolName = plugin.getConfig().getString("tool.itemName");
        setToolItem(Material.matchMaterial(toolName));
    }
    private void setToolItem(Material item) {
        toolItem = item;
    }
    private Material getToolItem() {
        return (toolItem == null) ? DefaultToolMaterial : toolItem;
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
                && player.getItemInHand().getType() == getToolItem();
    }

    private void showTargetEffect(Player player, Block target, ParticleAPI.EnumParticle particle) {
        Location loc = target.getLocation().add(0.5f, 1f, 0.5f);
        // 上が空気じゃなかったら横にずらす
        if (loc.getBlock().getType() != Material.AIR) {
            org.bukkit.util.Vector v = player.getEyeLocation().getDirection().normalize();
            org.bukkit.util.Vector v2 = new org.bukkit.util.Vector(-0.7f * v.getX(), -0.7f * v.getY(), -0.7f * v.getZ());
            loc.add(v2);
        }
        if (!ParticleAPI.createEffect(particle,
                (float)loc.getX(), (float)loc.getY(), (float)loc.getZ(),
                0.15f, 0.6f, 0.15f, 0.3f, 10)) {
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
            Block b = loc.add(dir).getBlock();
            if (b.getType() != Material.AIR) {
                block = b;
                break;
            }
        }

        return block;
    }
    private Biome getNextBiome(Biome currentBiome, boolean scrollUp) {
        Biome[] allBiomes = Biome.values();
        List<Biome> allBiomesList = Arrays.asList(allBiomes);

        int index = allBiomesList.indexOf(currentBiome);
        if (scrollUp) {
            if (allBiomesList.size() <= ++index) { index = 0; }
        } else {
            if (--index <= 0) { index = allBiomesList.size() - 1; }
        }

        return allBiomesList.get(index);
    }

    private void refreshChunk(Block target) {
        Chunk chunk = target.getChunk();
        target.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
    }

    /***********************************************************************
     * ここからパブリックメソッド
     ***********************************************************************/
    public void recoveryCacheData() {
        cache.loadFromFile(plugin.getDataFolder());
    }
    public void saveCacheData() {
        cache.saveToFile(plugin.getDataFolder());
    }


    public boolean canPickupBlock(Player player, Action action) {
        return action == Action.LEFT_CLICK_BLOCK
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.pickup");
    }
    public boolean pickupBlockInfo(Player player, Block target) {
        cache.setBiome(player, target.getBiome());

        player.sendMessage("[BiomePaint] picked up " + target.getBiome().toString() + " biome.");

        return true;
    }
    public boolean canBiomePainting(Player player, Action action) {
        return action == Action.RIGHT_CLICK_BLOCK
                && !player.isSneaking()
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.paint");
    }
    public boolean replaceBiome(Player player, Block target) {
        Biome newBiome = cache.getBiome(player);
        Location loc = target.getLocation();

        if (newBiome == null) {
            player.sendMessage("[BiomePaint] Don't set biome. Please set from LeftClick or `/biome set <biome_name>` command.");
            return false;
        }

        // バイオーム書き換え
        loc.getWorld().setBiome(loc.getBlockX(), loc.getBlockZ(), newBiome);
        // チャンク再読込
        refreshChunk(target);

        showTargetEffect(player, target, ParticleAPI.EnumParticle.VILLAGER_HAPPY);

        return true;
    }
    public boolean canShowBiomeInfo(Player player, Action action) {
        return action == Action.RIGHT_CLICK_BLOCK
                && player.isSneaking()
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.check");
    }
    public void showBiomeInfo(Player player, Block target) {
        Formatter fm = new Formatter();
        Location loc = target.getLocation();
        fm.format("[BiomePaint] X=%d, Z=%d, Biome=%s", loc.getBlockX(), loc.getBlockZ(), target.getBiome());
        player.sendMessage(fm.toString());

        // おまけのチャンク再読込
        refreshChunk(target);
    }
    public boolean canScrollBiome(Player player) {
        return player.isSneaking()
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.paint");
    }
    public boolean scrollBiome(Player player, int newSlot, int prevSlot) {
        boolean scrollUp = getScrollDirection(newSlot, prevSlot);

        long milliSeconds = System.currentTimeMillis();
        // 前回イベントから245ms未満の場合はバイオーム書き換え処理を行わない
        if (milliSeconds - cache.getWheelMoveTime(player) < 245) { return false; }
        Block target = getTargetBlockForumVer(player, 6);

        // 対象座標のブロックが取得できなかったら空中と判断して中断
        if (target == null) { return false; }

        // 次のバイオームを取得
        Biome currentBiome = target.getBiome();
        Biome nextBiome = getNextBiome(target.getBiome(), scrollUp);

        // バイオームの変更処理
        target.setBiome(nextBiome);

        // エフェクトを表示
        showTargetEffect(player, target, ParticleAPI.EnumParticle.VILLAGER_HAPPY);

        // チャンクを更新
        refreshChunk(target);

        // メッセージ送信
        if (!cache.getDisabledBiomeScrollMessageFlag(player)) {
            player.sendMessage("[BiomePainter] Biome switch. " + currentBiome.toString() + " => " + nextBiome.toString());
        }

        cache.setWheelMoveTime(player, milliSeconds);

        return true;
    }
}
