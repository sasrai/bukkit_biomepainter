package jp.sasrai.biomepainter;
/**
 * Created by sasrai on 2016/12/2.
 */

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BiomePainter extends JavaPlugin {
    @Override
    public void onEnable() {
        Logger logger = this.getLogger();
        logger.log(Level.INFO, "Load BiomePainter");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}