package jp.sasrai.biomepainter.Tool;

import com.github.keepoff07.ParticleAPI;
import jp.sasrai.biomepainter.BiomePainter;
import jp.sasrai.biomepainter.data.BiomeCache;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Formatter;

/**
 * Created by sasrai on 2016/12/08.
 */
public class PaintTool {
    private final BiomePainter plugin;
    private final BiomeCache cache;

    static final Material DefaultToolMaterial = Material.ARROW;
    static Material toolItem;

    public PaintTool(BiomePainter plugin) {
        this.plugin = plugin;
        this.cache = new BiomeCache();

        setToolItemFromConfiguration();

        if (isCustomizedTool()) {
            for (Player player: plugin.getServer().getOnlinePlayers()) {
                sendChangeToolMessage(player);
            }
        }
    }

    public void setToolItemFromConfiguration() {
        String toolName = plugin.getConfig().getString("tool.itemName", DefaultToolMaterial.name());
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
            player.sendMessage("[BiomePainter] That operation is not allowed by permission.");
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
    private Biome getNextBiome(Biome currentBiome, boolean scrollUp) {
        int currentBiomeId = plugin.getBiomeList().getBiomeId(currentBiome);
        int biomeId = currentBiomeId;

        int append = (scrollUp) ? 1 : -1;

        while (true) {
            biomeId += append;

            if (biomeId >= plugin.getBiomeList().biomesCount()) biomeId = 0;
            if (biomeId < 0) biomeId = plugin.getBiomeList().biomesCount() - 1;

            if (plugin.getBiomeList().biomeExists(biomeId)
                    && !plugin.getBiomeList().getBiome(biomeId).name().equals(currentBiome.name())) break;
        }

        return plugin.getBiomeList().getBiome(biomeId);
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

    public boolean isCustomizedTool() {
        return getToolItem() != DefaultToolMaterial;
    }
    public void sendChangeToolMessage(Player player) {
        player.sendMessage("[BiomePainter] Tool has been changed for " + ChatColor.BOLD + getToolItem().name());
    }
    public void setBiome(Player player, Biome biome) {
        cache.setBiome(player, biome);
    }

    public void giveToolItem(Player player) {
        ItemStack toolItem = new ItemStack(getToolItem());
        ItemMeta meta = toolItem.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "[BiomeTool]");
        toolItem.setItemMeta(meta);
        plugin.getLogger().info("Tool = " + toolItem.toString());
        player.getInventory().addItem(toolItem);
        player.sendMessage("[BiomePainter] Get item for paint tool (" + ChatColor.BOLD + getToolItem().name() + ChatColor.RESET + ")");
    }

    public boolean canPickupBlock(Player player, Action action) {
        return action == Action.LEFT_CLICK_BLOCK
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.pickup");
    }
    public boolean pickupBlockInfo(Player player, Block target) {
        setBiome(player, target.getBiome());

        player.sendMessage("[BiomePainter] picked up " + ChatColor.YELLOW + target.getBiome().toString() + ChatColor.RESET + " biome.");

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
            player.sendMessage("[BiomePaint] Don't set biome. Please set from "
                    + ChatColor.UNDERLINE + "LeftClick" + ChatColor.RESET + " or "
                    + ChatColor.UNDERLINE + "`/biome set <biome_name>`" + ChatColor.RESET + " command.");
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
        fm.format("[BiomePainter] X=%d, Z=%d, Biome=" + ChatColor.YELLOW  + "%s", loc.getBlockX(), loc.getBlockZ(), target.getBiome());
        player.sendMessage(fm.toString());

        // おまけのチャンク再読込
        refreshChunk(target);
    }
    public boolean canShowToolInfo(Player player, Action action) {
        return action == Action.RIGHT_CLICK_AIR
                && !player.isSneaking()
                && isUsingPlugin(player);
    }
    public void showToolInfo(Player player) {
        player.sendMessage("[BiomePainter] The biome set in the tool is " + ChatColor.YELLOW + cache.getBiome(player));
    }
    public boolean canScrollBiome(Player player) {
        return player.isSneaking()
                && isUsingPlugin(player)
                && shouldAllowedProcessing(player, "biomepainter.tool.paint");
    }
    public boolean scrollBiome(Player player, Block target, int newSlot, int prevSlot) {
        boolean scrollUp = getScrollDirection(newSlot, prevSlot);

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
            player.sendMessage("[BiomePainter] Biome switch. " + ChatColor.YELLOW + currentBiome.toString() + ChatColor.RESET + " => " + ChatColor.YELLOW + nextBiome.toString());
        }

        return true;
    }
}
