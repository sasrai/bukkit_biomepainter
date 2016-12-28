package jp.sasrai.biomepainter;
/**
 * Created by sasrai on 2016/12/2.
 */

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jp.sasrai.biomepainter.Tool.PaintTool;
import jp.sasrai.biomepainter.data.BiomeList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiomePainter extends JavaPlugin {
    private PaintTool tool;
    private BiomeList biomelist;

    public PaintTool getTool() { return tool; }
    public BiomeList getBiomeList() { return biomelist; }

    private boolean pluginInitialize() {
        // コンフィグ読み込み設定
        this.getConfig().options().copyDefaults(true);

        // コンフィグが無かったらデフォルトのファイルを保存してくれる神を呼ぶ
        this.saveDefaultConfig();

        // ツールを初期化
        tool = new PaintTool(this);

        // バイオームリストを初期化
        try {
            biomelist = new BiomeList(this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }

    public String getMCVersion() {
        String serverVersion = Bukkit.getVersion();
        String regex = "MC: *([0-9\\.]+)";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(serverVersion);
        if (matcher.find()) return matcher.group(1);
        else return null;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // 初期化処理
        if (!pluginInitialize()) return;

        // コマンド処理を登録
        getCommand("bpaint").setExecutor(new BPCommandExecutor(this));

        // プレイヤーイベントを登録
        new BPPlayerEventListener(this);

        // 各ユーザのキャッシュデータを読み込む
        tool.recoveryCacheData();
    }

    @Override
    public void onDisable() {
        tool.saveCacheData();
        super.onDisable();
    }
}
