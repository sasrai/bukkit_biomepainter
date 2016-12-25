package jp.sasrai.biomepainter;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by keiso on 2016/12/03.
 */
public class BPCommandExecutor implements CommandExecutor {
    private final BiomePainter plugin;

    public BPCommandExecutor(BiomePainter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 関係ないコマンドが来たらばいばい
        if (!cmd.getName().equalsIgnoreCase("bpaint")) { return false; }

        // コンソールからは使わせないよ！
        if (!(sender instanceof Player)) { sender.sendMessage("Do not use server console."); return true; }

        // コマンド処理
        plugin.getLogger().info(cmd.toString());
        plugin.getLogger().info(cmd.getName());
        plugin.getLogger().info(label);
        plugin.getLogger().info("length : " + args.length + ((args.length > 0) ? ", " + args[0] : ""));

        if (args.length == 0) { return onNoArgs((Player) sender); }
        else if (args[0].equalsIgnoreCase("set") && args.length >= 2) {
            return onCommandSet((Player) sender, buildLongArgs(args));
        }
        else if (args[0].equalsIgnoreCase("give")) {
            return onCommandGiveTool((Player) sender);
        }

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
        if (player == null || !(player instanceof Player)) { return false; }

        plugin.getTool().showToolInfo(player);

        return true;
    }

    private boolean onCommandSet(Player player, String biomeName) {
        if (player == null || !(player instanceof Player)) { return false; }

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
        if (player == null || !(player instanceof Player)) { return false; }

        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("biomepainter.tool.give")) {
            plugin.getTool().giveToolItem(player);
        }

        return true;
    }
}
