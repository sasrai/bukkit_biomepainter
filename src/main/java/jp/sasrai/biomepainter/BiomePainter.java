package jp.sasrai.biomepainter;
/**
 * Created by sasrai on 2016/12/2.
 */

import jp.sasrai.biomepainter.Tool.PaintTool;
import org.bukkit.plugin.java.JavaPlugin;

public class BiomePainter extends JavaPlugin {
    PaintTool tool;

    void pluginInitialize() {
        // コンフィグ読み込み設定
        this.getConfig().options().copyDefaults(true);

        // コンフィグが無かったらデフォルトのファイルを保存してくれる神を呼ぶ
        this.saveDefaultConfig();

        // ツールを初期化
        tool = new PaintTool(this);
    }
    public PaintTool getTool() {
        return tool;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // 初期化処理
        pluginInitialize();

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
