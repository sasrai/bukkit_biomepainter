package jp.sasrai.biomepainter.data;

import jp.sasrai.biomepainter.BiomePainter;
import jp.sasrai.biomepainter.util.wrapper.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sasrai on 2016/12/12.
 */
public class BiomeList {
    private static final String DEFAULT_BIOMES_FILE = "biomes.yml";

    private final BiomePainter plugin;

    private BiomeRelationData[] biomes = new BiomeRelationData[256];

    private BiomeBaseInterface biomebase;
    private CraftBlockInterface craftblock;

    public BiomeList(BiomePainter plugin) throws NoSuchFileException {
        this.plugin = plugin;
        String mcversion = WrapperBase.getVersionString();
        if (null == mcversion || plugin.getConfig().getBoolean("biomes.useCustomFile", false)) {
            generateBiomeListFromYaml();
        } else {
            biomebase = BiomeBaseUtility.getBiomeBase();
            craftblock = new CraftBlockWrapper();

            if (!biomebase.isAvailable() || !craftblock.isAvailable()) generateBiomeListFromYaml();
            else generateBiomeList();
        }
    }

    private boolean generateBiomeList() {
        if (null == biomebase || null == craftblock) return false;

        plugin.getLogger().info("Load biomes from " + biomebase.getCanonicalName());

        BiomeBaseInterface[] biomes = biomebase.getBiomes();

        for (int i = 0; i < biomes.length; i++) {
            BiomeBaseInterface biomebase = biomes[i];
            if (null != biomebase) {
                BiomeRelationData biomeData = new BiomeRelationData();

                biomeData.id = biomebase.getId();
                if (biomeData.id < 0) continue;
                biomeData.mcName = biomebase.getName();//biomebase, bbNameField);
                biomeData.biome = craftblock.BiomeBaseToBiome(biomebase);
                biomeData.biomebase = biomebase;

                this.biomes[i] = biomeData;
            }
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
