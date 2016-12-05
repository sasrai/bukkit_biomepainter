package jp.sasrai.biomepainter;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

/**
 * Created by sasrai on 2016/12/03.
 */
public class BPToolConfig {
    private static BPToolConfig ourInstance = new BPToolConfig();

    public static BPToolConfig getInstance() {
        return ourInstance;
    }

    static Material DefaultToolMaterial = Material.ARROW;
    static Material toolItem;

    private BPToolConfig() {
    }

    public void setToolItem(Material toolItem) {
        BPToolConfig.toolItem = toolItem;
    }

    public void setToolItemFromConfiguration(Configuration config) {
        String toolName = config.getString("tool.itemName", DefaultToolMaterial.name());

        Material newToolMaterial = Material.matchMaterial(toolName);
        if (newToolMaterial == null) { newToolMaterial = DefaultToolMaterial; }

        setToolItem(newToolMaterial);
    }

    public Material getToolItem() {
        return (toolItem == null) ? DefaultToolMaterial : BPToolConfig.toolItem;
    }
}
