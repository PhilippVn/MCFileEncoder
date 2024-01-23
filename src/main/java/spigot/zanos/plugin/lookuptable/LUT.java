package spigot.zanos.plugin.lookuptable;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LUT {
    public static final Map<Integer, Material> INDEX_TO_MATERIAL = new HashMap<>();

    static {
        INDEX_TO_MATERIAL.put(0,Material.WHITE_CONCRETE);
        INDEX_TO_MATERIAL.put(1,Material.BLACK_CONCRETE);
        INDEX_TO_MATERIAL.put(2,Material.DIAMOND_BLOCK);
        INDEX_TO_MATERIAL.put(3,Material.IRON_BLOCK);
        INDEX_TO_MATERIAL.put(4,Material.WAXED_COPPER_BLOCK);
        INDEX_TO_MATERIAL.put(5,Material.GOLD_BLOCK);
        INDEX_TO_MATERIAL.put(6,Material.NETHERITE_BLOCK);
        INDEX_TO_MATERIAL.put(7,Material.REDSTONE_BLOCK);
        INDEX_TO_MATERIAL.put(8,Material.BONE_BLOCK);
        INDEX_TO_MATERIAL.put(9,Material.EMERALD_BLOCK);
        INDEX_TO_MATERIAL.put(10,Material.LAPIS_BLOCK);
        INDEX_TO_MATERIAL.put(11,Material.AMETHYST_BLOCK);
        INDEX_TO_MATERIAL.put(12,Material.GLOWSTONE);
        INDEX_TO_MATERIAL.put(13,Material.PRISMARINE);
        INDEX_TO_MATERIAL.put(14,Material.STONE);
        INDEX_TO_MATERIAL.put(15,Material.SEA_LANTERN);
    }

    public static final Map<Material, Integer> MATERIAL_TO_INDEX = new HashMap<>();

    static{
        MATERIAL_TO_INDEX.put(Material.WHITE_CONCRETE,0);
        MATERIAL_TO_INDEX.put(Material.BLACK_CONCRETE,1);
        MATERIAL_TO_INDEX.put(Material.DIAMOND_BLOCK,2);
        MATERIAL_TO_INDEX.put(Material.IRON_BLOCK,3);
        MATERIAL_TO_INDEX.put(Material.WAXED_COPPER_BLOCK,4);
        MATERIAL_TO_INDEX.put(Material.GOLD_BLOCK,5);
        MATERIAL_TO_INDEX.put(Material.NETHERITE_BLOCK,6);
        MATERIAL_TO_INDEX.put(Material.REDSTONE_BLOCK,7);
        MATERIAL_TO_INDEX.put(Material.BONE_BLOCK,8);
        MATERIAL_TO_INDEX.put(Material.EMERALD_BLOCK,9);
        MATERIAL_TO_INDEX.put(Material.LAPIS_BLOCK,10);
        MATERIAL_TO_INDEX.put(Material.AMETHYST_BLOCK,11);
        MATERIAL_TO_INDEX.put(Material.GLOWSTONE,12);
        MATERIAL_TO_INDEX.put(Material.PRISMARINE,13);
        MATERIAL_TO_INDEX.put(Material.STONE,14);
        MATERIAL_TO_INDEX.put(Material.SEA_LANTERN,15);
    }

    public static final Set<Material> materialSet = new HashSet<>(); // used to check for encoding materials efficiently O(1)

    static{
        materialSet.add(Material.WHITE_CONCRETE);
        materialSet.add(Material.BLACK_CONCRETE);
        materialSet.add(Material.DIAMOND_BLOCK);
        materialSet.add(Material.IRON_BLOCK);
        materialSet.add(Material.WAXED_COPPER_BLOCK);
        materialSet.add(Material.GOLD_BLOCK);
        materialSet.add(Material.NETHERITE_BLOCK);
        materialSet.add(Material.REDSTONE_BLOCK);
        materialSet.add(Material.BONE_BLOCK);
        materialSet.add(Material.EMERALD_BLOCK);
        materialSet.add(Material.LAPIS_BLOCK);
        materialSet.add(Material.AMETHYST_BLOCK);
        materialSet.add(Material.GLOWSTONE);
        materialSet.add(Material.PRISMARINE);
        materialSet.add(Material.STONE);
        materialSet.add(Material.SEA_LANTERN);
    }
}
