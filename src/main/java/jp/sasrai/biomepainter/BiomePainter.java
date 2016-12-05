package jp.sasrai.biomepainter;
/**
 * Created by sasrai on 2016/12/2.
 */

import org.bukkit.plugin.java.JavaPlugin;

public class BiomePainter extends JavaPlugin {
    @Override
    public void onEnable() {
        super.onEnable();

        // コマンド処理を登録
        getCommand("bpaint").setExecutor(new BPCommandExecutor(this));

        // プレイヤーイベントを登録
        new BPPlayerEventListener(this);

        // コンフィグからツールを設定
        BPToolConfig.getInstance().setToolItemFromConfiguration(getConfig());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
