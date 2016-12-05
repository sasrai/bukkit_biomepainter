package jp.sasrai.biomepainter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by keiso on 2016/12/03.
 */
public class BPCommandExecutor implements CommandExecutor {
    private final BiomePainter instance;

    public BPCommandExecutor(BiomePainter plugin) {
        this.instance = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 関係ないコマンドが来たらばいばい
        if (!cmd.getName().equalsIgnoreCase("bpaint")) { return false; }

        // コンソールからは使わせないよ！
        if (!(sender instanceof Player)) { sender.sendMessage("サーバーコンソールからは利用できません。"); return false; }

        // コマンド処理
        instance.getLogger().info("exec command");

        return true;
    }
}
