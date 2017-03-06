package jp.sasrai.biomepainter;

import jp.sasrai.biomepainter.util.PermissionUtility;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static jp.sasrai.biomepainter.util.PermissionUtility.shouldAllowedProcessing;

/**
 * Created by sasrai on 2016/12/03.
 */
public class BPCommandExecutor implements CommandExecutor {
    private final BiomePainter plugin;

    BPCommandExecutor(BiomePainter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 関係ないコマンドが来たらばいばい
        if (!cmd.getName().equalsIgnoreCase("bpaint")) { return false; }

        // コマンド処理
        if (args.length == 0) { return onNoArgs((Player) sender); }
        else if (args[0].equalsIgnoreCase("set") && isSendPlayer(sender) && args.length >= 2) {
            return onCommandSet((Player) sender, buildLongArgs(args));
        }
        else if (args[0].equalsIgnoreCase("give") && isSendPlayer(sender)) {
            return onCommandGiveTool((Player) sender);
        }
        else if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("biomes")) {
            int page = 1;
            try { page = Integer.parseInt(args[1]); } catch (Exception ignored) {}
            return onCommandShowBiomes(sender, page);
        }

        return false;
    }

    private boolean isSendPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) { sender.sendMessage("[BiomePainter] " + ChatColor.DARK_RED + "Do not use server console."); return false; }
        return true;
    }

    private String buildLongArgs(String args[]) { return buildLongArgs(args, 1); }
    private String buildLongArgs(String args[], int index) {
        StringBuilder sb = new StringBuilder();

        for (int i = index; i < args.length; i++) {
            if (i != index) sb.append(" ");
            sb.append(args[i]);
        }

        return sb.toString();
    }

    private boolean onNoArgs(Player player) {
        if (player == null) { return false; }

        plugin.getTool().showToolInfo(player);

        return true;
    }

    private boolean onCommandSet(Player player, String biomeName) {
        if (player == null) { return false; }

        // パーミッション確認
        if (!shouldAllowedProcessing(player, "biomepainter.tool.pickup")) { return true; }

        try {
            int biomeId = Integer.parseInt(biomeName);
            plugin.getTool().setBiome(player, plugin.getBiomeList().getBiome(biomeId));
        } catch (NumberFormatException e) {
            try {
                if (plugin.getBiomeList().biomeExists(biomeName)) {
                    Biome newBiome = plugin.getBiomeList().getBiome(biomeName);
                    plugin.getTool().setBiome(player, newBiome);
                    player.sendMessage("[BiomePainter] Set " + ChatColor.YELLOW + plugin.getBiomeList().getBiomeMCName(newBiome) + ChatColor.RESET + " biome. ");
                } else {
                    player.sendMessage("[BiomePainter] `" + ChatColor.YELLOW + biomeName + ChatColor.RESET + "` : Unknown biome.");
                }
            } catch (Exception ex) {
                player.sendMessage("[BiomePainter] Don't set biome.");
                return false;
            }
        }

        return true;
    }

    private boolean onCommandGiveTool(Player player) {
        if (player == null) { return false; }

        // パーミッション確認
        if (!shouldAllowedProcessing(player, "biomepainter.give")) { return true; }

        if (player.getGameMode() == GameMode.CREATIVE) {
            plugin.getTool().giveToolItem(player);
        }

        return true;
    }

    private boolean onCommandShowBiomes(CommandSender sender, int page) {

        // パーミッション確認
        if (sender instanceof Player && !shouldAllowedProcessing((Player)sender, "biomepainter.give")) { return true; }

        Integer[] ids = plugin.getBiomeList().getBiomeIDs();
        int pageLines = (sender instanceof Player) ? 9 : 20;

        // 範囲外ページ数調整
        if (page < 1) page = 1;
        else if (page > ids.length / pageLines) page = (int)Math.ceil((double)ids.length / (double)pageLines);

        int offset = (page -1) * pageLines;
        if (offset < 0) offset = 0;

        StringBuilder sb = new StringBuilder();

        sb.append(ChatColor.DARK_PURPLE)
            .append("---------- Available Biomes (page ")
            .append(page).append("/").append((int)Math.ceil((double)ids.length / (double)pageLines))
            .append(" ) --------------------")
            .append(ChatColor.RESET);
        for (int i = offset; i < ids.length && i < offset + pageLines; i++) {
            sb.append("\n");
            // ID出力
            sb.append(ChatColor.DARK_AQUA).append("[");
            if (ids[i] < 10) sb.append("  ");
            else if (ids[i] < 100) sb.append(" ");
            sb.append(ids[i]).append("] ").append(ChatColor.RESET);

            // バイオーム名出力
            sb.append(ChatColor.YELLOW).append(plugin.getBiomeList().getBiome(ids[i]).name()).append(ChatColor.RESET)
                .append(" / ")
                .append(ChatColor.YELLOW).append(plugin.getBiomeList().getBiomeMCName(ids[i])).append(ChatColor.RESET);
        }
        sender.sendMessage(sb.toString());

        return true;
    }
}
