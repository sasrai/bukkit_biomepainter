package jp.sasrai.biomepainter.data;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.block.Biome;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sasrai on 2016/12/12.
 */
public class BiomeList {
    final String NMSPackage = "net.minecraft.server.";
    final String OBCPackage = "org.bukkit.craftbukkit.";

    private BiomeRelationData[] biomes = new BiomeRelationData[256];
    private final String nmsPackage;
    private final String obcPackage;

    private Class<?> biomebase;
    private Class<?> craftblock;

    public BiomeList() {
        String mcversion = getVersionString();
        nmsPackage = NMSPackage + mcversion + ".";
        obcPackage = OBCPackage + mcversion + ".";

        biomebase = getNMSBiomeBase();
        craftblock = getOBCCraftBlock();

        generateBiomeList();
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
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    private Class<?> getOBCCraftBlock() {
        try {
            String craftblockPackage = obcPackage + "block.CraftBlock";
            return Class.forName(craftblockPackage);
        } catch (ClassNotFoundException e) {
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

        try {
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
        } catch (NoSuchMethodException e) {
            Bukkit.getLogger().info(e.toString());
            return false;
        } catch (IllegalAccessException e) {
            Bukkit.getLogger().info(e.toString());
            return false;
        } catch (InvocationTargetException e) {
            Bukkit.getLogger().info(e.toString());
            return false;
        } catch (NoSuchFieldException e) {
            Bukkit.getLogger().info(e.toString());
            return false;
        }
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
        return getBiomeMCName(getBiome(id));
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
        for (int i = 0; i < biomes.length; i++) {
            if (biomes[i] != null) biomeIdList.add(biomes[i].id);
        }
        return biomeIdList.toArray(new Integer[0]);
    }
}
