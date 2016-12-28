package jp.sasrai.biomepainter.data;

import jp.sasrai.biomepainter.BiomePainter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sasrai on 2016/12/12.
 */
public class BiomeList {
    private static final String DEFAULT_BIOMES_FILE = "biomes.yml";

    private final BiomePainter plugin;

    private final String NMSPackage = "net.minecraft.server.";
    private final String OBCPackage = "org.bukkit.craftbukkit.";

    private BiomeRelationData[] biomes = new BiomeRelationData[256];
    private final String nmsPackage;
    private final String obcPackage;

    private Class<?> biomebase;
    private Class<?> craftblock;

    public BiomeList(BiomePainter plugin) throws NoSuchFileException {
        this.plugin = plugin;
        String mcversion = getVersionString();
        if (null == mcversion || plugin.getConfig().getBoolean("biomes.useCustomFile", false)) {
            nmsPackage = null;
            obcPackage = null;
            generateBiomeListFromYaml();
        } else {
            nmsPackage = NMSPackage + mcversion + ".";
            obcPackage = OBCPackage + mcversion + ".";

            biomebase = getNMSBiomeBase();
            craftblock = getOBCCraftBlock();

            if (biomebase == null || craftblock == null) generateBiomeListFromYaml();
            else generateBiomeList();
        }
    }

    private String getVersionString() {
        try {
            Server server = Bukkit.getServer();
            Method getHandler = server.getClass().getMethod("getHandle");
            String nmsPackage = getHandler.invoke(server).getClass().getPackage().getName();

            return nmsPackage.substring(nmsPackage.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private Class<?> getNMSBiomeBase() {
        try {
            String biomebasePackage = nmsPackage + "BiomeBase";
            return Class.forName(biomebasePackage);
        } catch (Exception e) {
            return null;
        }
    }
    private Class<?> getOBCCraftBlock() {
        try {
            String craftblockPackage = obcPackage + "block.CraftBlock";
            return Class.forName(craftblockPackage);
        } catch (Exception e) {
            return null;
        }
    }

    private String getBiomeBaseName(Object biomebase, Field field) throws IllegalAccessException {
        if (null == biomebase || null == field) return "";
        return (String) field.get(biomebase);
    }
    private boolean isBBNameField(Field field) {
        return (field.getType() == String.class);
    }
    private Field getBiomeBaseNameField() throws NoSuchFieldException {
        for (Field field: biomebase.getDeclaredFields()) {
            if (isBBNameField(field)) {
                return field;
            }
        }
        throw new NoSuchFieldException();
    }
    private Field getBiomeBaseIdField() throws NoSuchFieldException {
        return biomebase.getField("id");
    }
    private boolean generateBiomeList() {
        if (null == biomebase || null == craftblock) return false;

        plugin.getLogger().info("Load biomes from " + biomebase.getCanonicalName());

        try {
            // TODO: MC1.9以降はBiomeBase.REGISTRY_IDで取得する
            Method getBiomes = biomebase.getMethod("getBiomes");
            Method biomeBaseToBiome = craftblock.getMethod("biomeBaseToBiome", biomebase);
            Object[] biomes = (Object[]) getBiomes.invoke(null);
            Field bbNameField = getBiomeBaseNameField();

            for (int i = 0; i < biomes.length; i++) {
                Object biomebase = biomes[i];
                if (null != biomebase) {
                    BiomeRelationData biomeData = new BiomeRelationData();

                    biomeData.id = i;
                    biomeData.mcName = getBiomeBaseName(biomebase, bbNameField);
                    biomeData.biome = (Biome) biomeBaseToBiome.invoke(null, biomebase);
                    biomeData.biomebase = biomebase;

                    this.biomes[i] = biomeData;
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            Bukkit.getLogger().info(e.toString());
            return false;
        }
        return true;
    }
    private boolean generateBiomeListFromYaml() throws NoSuchFileException {
        File biomesFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("biomes.biomesFile", DEFAULT_BIOMES_FILE));
        if (!biomesFile.exists()) {
            if (biomesFile.getName().equals(DEFAULT_BIOMES_FILE)) copyDefaultBiomesYamlFile();
            else {
                plugin.getLogger().warning(biomesFile.getName() + " : biomesFile not found.");
                biomes = new BiomeRelationData[0];
                throw new NoSuchFileException(biomesFile.getAbsolutePath());
            }
        }
        plugin.getLogger().info("Load biomes from " + biomesFile.getAbsolutePath());

        final FileConfiguration biomesConfig = YamlConfiguration.loadConfiguration(biomesFile);

        if (!biomesConfig.contains("biomes")) {
            throw new InternalError("`biomes` does not exist in the configuration file.");
        }

        final ConfigurationSection biomesList = biomesConfig.getConfigurationSection("biomes");
        for (String idStr : biomesList.getKeys(false)) {
            ConfigurationSection biomeRecord = biomesList.getConfigurationSection(idStr);

            final int id;
            try { id = Integer.parseInt(idStr); } catch (Exception e) { continue; }

            BiomeRelationData biomeData = new BiomeRelationData();

            biomeData.id = id;
            biomeData.mcName = biomeRecord.getString("mcname");
            biomeData.biome = Biome.valueOf(biomeRecord.getString("bkname"));

            this.biomes[id] = biomeData;
        }
        return true;
    }
    private boolean copyDefaultBiomesYamlFile() {
        String[] versions = plugin.getMCVersion().split("\\.");
        String targetFilename = ".biomes." + versions[0] + "." + versions[1] + ".yml";
        // copy default file
        plugin.saveResource(targetFilename, false);

        // rename
        String biomesFile = plugin.getConfig().getString("biomes.biomesFile", DEFAULT_BIOMES_FILE);
        File loadedFile = new File(plugin.getDataFolder(), targetFilename);
        File targetFile = new File(plugin.getDataFolder(), biomesFile);
        loadedFile.renameTo(targetFile);

        plugin.getLogger().info("Generate default biome definition file. => " + targetFile.getAbsolutePath());

        return true;
    }

    public int biomesCount() { return biomes.length; }
    public boolean biomeExists(String name) {
        for (BiomeRelationData relation: biomes) {
            if (null != relation && (name.equalsIgnoreCase(relation.mcName) || name.equalsIgnoreCase(relation.biome.name()))) return true;
        }
        return false;
    }
    public boolean biomeExists(int id) {
        if (biomes[id] != null && biomes[id].id == id) return true;

        for (BiomeRelationData relation: biomes) {
            if (null != relation && relation.id == id) return true;
        }
        return false;
    }
    public int getBiomeId(Biome biome) {
        for (BiomeRelationData relation: biomes) {
            if (null != relation && relation.biome == biome) return relation.id;
        }
        return 0;
    }
    public Biome getBiome(String name) {
        for (BiomeRelationData relation: biomes) {
            if (null != relation && (name.equalsIgnoreCase(relation.mcName) || name.equalsIgnoreCase(relation.biome.name()))) return relation.biome;
        }
        return Biome.OCEAN;
    }
    public Biome getBiome(int id) {
        if (biomes[id] != null && biomes[id].id == id) return biomes[id].biome;

        for (BiomeRelationData relation: biomes) {
            if (null != relation && relation.id == id) return relation.biome;
        }
        return Biome.OCEAN;
    }
    public String getBiomeMCName(int id) {
        if (biomes[id] != null && biomes[id].id == id) return biomes[id].mcName;

        for (BiomeRelationData relation: biomes) {
            if (null != relation && relation.id == id) return relation.mcName;
        }
        return "Ocean";
    }
    public String getBiomeMCName(Biome biome) {
        for (BiomeRelationData relation: biomes) {
            if (null != relation && relation.biome.equals(biome)) return relation.mcName;
        }
        return "Ocean";
    }
    public Biome[] getBiomes() {
        Biome[] biomeOnlyList = new Biome[biomes.length];
        for (int i = 0; i < biomes.length; i++) {
            biomeOnlyList[i] = (biomes[i] != null) ? biomes[i].biome : null;
        }
        return biomeOnlyList;
    }
    public Integer[] getBiomeIDs() {
        List<Integer> biomeIdList = new ArrayList<>();
        for (BiomeRelationData biome : biomes) if (biome != null) biomeIdList.add(biome.id);
        return biomeIdList.toArray(new Integer[0]);
    }
}
