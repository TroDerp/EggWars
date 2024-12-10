package me.rosillogames.eggwars.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class Colorizer
{
    public static final List<Map<Integer, Material>> COLORABLES = new ArrayList();
    public static final Map<Integer, Integer> COLORED_COLORS = new HashMap();
    public static final Map<Integer, Material> COLORED_BANNERS = new HashMap();
    public static final Map<Integer, Material> COLORED_BEDS = new HashMap();
    public static final Map<Integer, Material> COLORED_BUNDLES = new HashMap();
    public static final Map<Integer, Material> COLORED_CANDLES = new HashMap();
    public static final Map<Integer, Material> COLORED_CARPETS = new HashMap();
    public static final Map<Integer, Material> COLORED_CONCRETE = new HashMap();
    public static final Map<Integer, Material> COLORED_CONCRETE_POWDER = new HashMap();
    public static final Map<Integer, Material> COLORED_DYES = new HashMap();
    public static final Map<Integer, Material> COLORED_GLASS = new HashMap();
    public static final Map<Integer, Material> COLORED_GLASS_PANE = new HashMap();
    public static final Map<Integer, Material> COLORED_GLAZED_TERRACOTTA = new HashMap();
    public static final Map<Integer, Material> COLORED_SHULKER_BOXES = new HashMap();
    public static final Map<Integer, Material> COLORED_TERRACOTAS = new HashMap();
    public static final Map<Integer, Material> COLORED_WOOLS = new HashMap();
    public static final List<Material> LEATHER_ARMOR = new ArrayList();

    public static Material colorize(Material toReColor, int meta)
    {
        Map<Integer, Material> map = null;

        for (Map<Integer, Material> map1 : COLORABLES)
        {
            for (Map.Entry<Integer, Material> material : map1.entrySet())
            {
                if (material.getValue() == toReColor)
                {
                    map = map1;
                    break;
                }
            }
        }

        if (map != null)
        {
            return map.getOrDefault(meta, toReColor);
        }

        return toReColor;
    }

    public static void colorizeItem(ItemStack stack, int meta)
    {
        Material torecolor = stack.getType();
        Material recolored = colorize(torecolor, meta);

        if (recolored != torecolor)
        {
            stack.setType(recolored);
        }

        for (Material material1 : LEATHER_ARMOR)
        {
            if (material1 == stack.getType() && COLORED_COLORS.containsKey(meta))
            {
                LeatherArmorMeta leatherarmormeta = (LeatherArmorMeta)stack.getItemMeta();
                leatherarmormeta.setColor(Color.fromRGB(COLORED_COLORS.get(meta)));
                ReflectionUtils.hideDyeFlag(leatherarmormeta);
                stack.setItemMeta(leatherarmormeta);
                break;
            }
        }
    }

    public static void init()
    {
        COLORED_COLORS.put(0, 16777215);
        COLORED_COLORS.put(1, 16755200);
        COLORED_COLORS.put(2, 11141290);
        COLORED_COLORS.put(3, 5636095);
        COLORED_COLORS.put(4, 16777045);
        COLORED_COLORS.put(5, 5635925);
        COLORED_COLORS.put(6, 16733695);
        COLORED_COLORS.put(7, 5592405);
        COLORED_COLORS.put(8, 11184810);
        COLORED_COLORS.put(9, 43690);
        COLORED_COLORS.put(10, 8991416);
        COLORED_COLORS.put(11, 170);
        COLORED_COLORS.put(12, 8606770);
        COLORED_COLORS.put(13, 43520);
        COLORED_COLORS.put(14, 11141120);
        COLORED_COLORS.put(15, 0);
        COLORED_BANNERS.put(0, Material.WHITE_BANNER);
        COLORED_BANNERS.put(1, Material.ORANGE_BANNER);
        COLORED_BANNERS.put(2, Material.MAGENTA_BANNER);
        COLORED_BANNERS.put(3, Material.LIGHT_BLUE_BANNER);
        COLORED_BANNERS.put(4, Material.YELLOW_BANNER);
        COLORED_BANNERS.put(5, Material.LIME_BANNER);
        COLORED_BANNERS.put(6, Material.PINK_BANNER);
        COLORED_BANNERS.put(7, Material.GRAY_BANNER);
        COLORED_BANNERS.put(8, Material.LIGHT_GRAY_BANNER);
        COLORED_BANNERS.put(9, Material.CYAN_BANNER);
        COLORED_BANNERS.put(10, Material.PURPLE_BANNER);
        COLORED_BANNERS.put(11, Material.BLUE_BANNER);
        COLORED_BANNERS.put(12, Material.BROWN_BANNER);
        COLORED_BANNERS.put(13, Material.GREEN_BANNER);
        COLORED_BANNERS.put(14, Material.RED_BANNER);
        COLORED_BANNERS.put(15, Material.BLACK_BANNER);
        COLORED_BEDS.put(0, Material.WHITE_BED);
        COLORED_BEDS.put(1, Material.ORANGE_BED);
        COLORED_BEDS.put(2, Material.MAGENTA_BED);
        COLORED_BEDS.put(3, Material.LIGHT_BLUE_BED);
        COLORED_BEDS.put(4, Material.YELLOW_BED);
        COLORED_BEDS.put(5, Material.LIME_BED);
        COLORED_BEDS.put(6, Material.PINK_BED);
        COLORED_BEDS.put(7, Material.GRAY_BED);
        COLORED_BEDS.put(8, Material.LIGHT_GRAY_BED);
        COLORED_BEDS.put(9, Material.CYAN_BED);
        COLORED_BEDS.put(10, Material.PURPLE_BED);
        COLORED_BEDS.put(11, Material.BLUE_BED);
        COLORED_BEDS.put(12, Material.BROWN_BED);
        COLORED_BEDS.put(13, Material.GREEN_BED);
        COLORED_BEDS.put(14, Material.RED_BED);
        COLORED_BEDS.put(15, Material.BLACK_BED);

        if (EggWars.serverVersion.ordinal() >= Versions.V_1_21_R2.ordinal())
        {
            COLORED_BUNDLES.put(-1, Material.BUNDLE);
            COLORED_BUNDLES.put(0, Material.WHITE_BUNDLE);
            COLORED_BUNDLES.put(1, Material.ORANGE_BUNDLE);
            COLORED_BUNDLES.put(2, Material.MAGENTA_BUNDLE);
            COLORED_BUNDLES.put(3, Material.LIGHT_BLUE_BUNDLE);
            COLORED_BUNDLES.put(4, Material.YELLOW_BUNDLE);
            COLORED_BUNDLES.put(5, Material.LIME_BUNDLE);
            COLORED_BUNDLES.put(6, Material.PINK_BUNDLE);
            COLORED_BUNDLES.put(7, Material.GRAY_BUNDLE);
            COLORED_BUNDLES.put(8, Material.LIGHT_GRAY_BUNDLE);
            COLORED_BUNDLES.put(9, Material.CYAN_BUNDLE);
            COLORED_BUNDLES.put(10, Material.PURPLE_BUNDLE);
            COLORED_BUNDLES.put(11, Material.BLUE_BUNDLE);
            COLORED_BUNDLES.put(12, Material.BROWN_BUNDLE);
            COLORED_BUNDLES.put(13, Material.GREEN_BUNDLE);
            COLORED_BUNDLES.put(14, Material.RED_BUNDLE);
            COLORED_BUNDLES.put(15, Material.BLACK_BUNDLE);
        }

        if (EggWars.serverVersion.ordinal() >= Versions.V_1_17.ordinal())
        {
            COLORED_CANDLES.put(-1, Material.CANDLE);
            COLORED_CANDLES.put(0, Material.WHITE_CANDLE);
            COLORED_CANDLES.put(1, Material.ORANGE_CANDLE);
            COLORED_CANDLES.put(2, Material.MAGENTA_CANDLE);
            COLORED_CANDLES.put(3, Material.LIGHT_BLUE_CANDLE);
            COLORED_CANDLES.put(4, Material.YELLOW_CANDLE);
            COLORED_CANDLES.put(5, Material.LIME_CANDLE);
            COLORED_CANDLES.put(6, Material.PINK_CANDLE);
            COLORED_CANDLES.put(7, Material.GRAY_CANDLE);
            COLORED_CANDLES.put(8, Material.LIGHT_GRAY_CANDLE);
            COLORED_CANDLES.put(9, Material.CYAN_CANDLE);
            COLORED_CANDLES.put(10, Material.PURPLE_CANDLE);
            COLORED_CANDLES.put(11, Material.BLUE_CANDLE);
            COLORED_CANDLES.put(12, Material.BROWN_CANDLE);
            COLORED_CANDLES.put(13, Material.GREEN_CANDLE);
            COLORED_CANDLES.put(14, Material.RED_CANDLE);
            COLORED_CANDLES.put(15, Material.BLACK_CANDLE);
        }

        COLORED_CARPETS.put(0, Material.WHITE_CARPET);
        COLORED_CARPETS.put(1, Material.ORANGE_CARPET);
        COLORED_CARPETS.put(2, Material.MAGENTA_CARPET);
        COLORED_CARPETS.put(3, Material.LIGHT_BLUE_CARPET);
        COLORED_CARPETS.put(4, Material.YELLOW_CARPET);
        COLORED_CARPETS.put(5, Material.LIME_CARPET);
        COLORED_CARPETS.put(6, Material.PINK_CARPET);
        COLORED_CARPETS.put(7, Material.GRAY_CARPET);
        COLORED_CARPETS.put(8, Material.LIGHT_GRAY_CARPET);
        COLORED_CARPETS.put(9, Material.CYAN_CARPET);
        COLORED_CARPETS.put(10, Material.PURPLE_CARPET);
        COLORED_CARPETS.put(11, Material.BLUE_CARPET);
        COLORED_CARPETS.put(12, Material.BROWN_CARPET);
        COLORED_CARPETS.put(13, Material.GREEN_CARPET);
        COLORED_CARPETS.put(14, Material.RED_CARPET);
        COLORED_CARPETS.put(15, Material.BLACK_CARPET);
        COLORED_CONCRETE.put(0, Material.WHITE_CONCRETE);
        COLORED_CONCRETE.put(1, Material.ORANGE_CONCRETE);
        COLORED_CONCRETE.put(2, Material.MAGENTA_CONCRETE);
        COLORED_CONCRETE.put(3, Material.LIGHT_BLUE_CONCRETE);
        COLORED_CONCRETE.put(4, Material.YELLOW_CONCRETE);
        COLORED_CONCRETE.put(5, Material.LIME_CONCRETE);
        COLORED_CONCRETE.put(6, Material.PINK_CONCRETE);
        COLORED_CONCRETE.put(7, Material.GRAY_CONCRETE);
        COLORED_CONCRETE.put(8, Material.LIGHT_GRAY_CONCRETE);
        COLORED_CONCRETE.put(9, Material.CYAN_CONCRETE);
        COLORED_CONCRETE.put(10, Material.PURPLE_CONCRETE);
        COLORED_CONCRETE.put(11, Material.BLUE_CONCRETE);
        COLORED_CONCRETE.put(12, Material.BROWN_CONCRETE);
        COLORED_CONCRETE.put(13, Material.GREEN_CONCRETE);
        COLORED_CONCRETE.put(14, Material.RED_CONCRETE);
        COLORED_CONCRETE.put(15, Material.BLACK_CONCRETE);
        COLORED_CONCRETE_POWDER.put(0, Material.WHITE_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(1, Material.ORANGE_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(2, Material.MAGENTA_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(3, Material.LIGHT_BLUE_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(4, Material.YELLOW_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(5, Material.LIME_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(6, Material.PINK_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(7, Material.GRAY_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(8, Material.LIGHT_GRAY_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(9, Material.CYAN_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(10, Material.PURPLE_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(11, Material.BLUE_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(12, Material.BROWN_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(13, Material.GREEN_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(14, Material.RED_CONCRETE_POWDER);
        COLORED_CONCRETE_POWDER.put(15, Material.BLACK_CONCRETE_POWDER);
        COLORED_DYES.put(0, Material.WHITE_DYE);
        COLORED_DYES.put(1, Material.ORANGE_DYE);
        COLORED_DYES.put(2, Material.MAGENTA_DYE);
        COLORED_DYES.put(3, Material.LIGHT_BLUE_DYE);
        COLORED_DYES.put(4, Material.YELLOW_DYE);
        COLORED_DYES.put(5, Material.LIME_DYE);
        COLORED_DYES.put(6, Material.PINK_DYE);
        COLORED_DYES.put(7, Material.GRAY_DYE);
        COLORED_DYES.put(8, Material.LIGHT_GRAY_DYE);
        COLORED_DYES.put(9, Material.CYAN_DYE);
        COLORED_DYES.put(10, Material.PURPLE_DYE);
        COLORED_DYES.put(11, Material.BLUE_DYE);
        COLORED_DYES.put(12, Material.BROWN_DYE);
        COLORED_DYES.put(13, Material.GREEN_DYE);
        COLORED_DYES.put(14, Material.RED_DYE);
        COLORED_DYES.put(15, Material.BLACK_DYE);/* @formatter:off
        COLORED_.put(-1, Material.
        COLORED_.put(0, Material.WHITE_
        COLORED_.put(1, Material.ORANGE_
        COLORED_.put(2, Material.MAGENTA_
        COLORED_.put(3, Material.LIGHT_BLUE_
        COLORED_.put(4, Material.YELLOW_
        COLORED_.put(5, Material.LIME_
        COLORED_.put(6, Material.PINK_
        COLORED_.put(7, Material.GRAY_
        COLORED_.put(8, Material.LIGHT_GRAY_
        COLORED_.put(9, Material.CYAN_
        COLORED_.put(10, Material.PURPLE_
        COLORED_.put(11, Material.BLUE_
        COLORED_.put(12, Material.BROWN_
        COLORED_.put(13, Material.GREEN_
        COLORED_.put(14, Material.RED_
        COLORED_.put(15, Material.BLACK_
        @formatter:on */
        COLORED_GLASS.put(-1, Material.GLASS);//-1 is used to find uncolored block for converting
        COLORED_GLASS.put(0, Material.WHITE_STAINED_GLASS);
        COLORED_GLASS.put(1, Material.ORANGE_STAINED_GLASS);
        COLORED_GLASS.put(2, Material.MAGENTA_STAINED_GLASS);
        COLORED_GLASS.put(3, Material.LIGHT_BLUE_STAINED_GLASS);
        COLORED_GLASS.put(4, Material.YELLOW_STAINED_GLASS);
        COLORED_GLASS.put(5, Material.LIME_STAINED_GLASS);
        COLORED_GLASS.put(6, Material.PINK_STAINED_GLASS);
        COLORED_GLASS.put(7, Material.GRAY_STAINED_GLASS);
        COLORED_GLASS.put(8, Material.LIGHT_GRAY_STAINED_GLASS);
        COLORED_GLASS.put(9, Material.CYAN_STAINED_GLASS);
        COLORED_GLASS.put(10, Material.PURPLE_STAINED_GLASS);
        COLORED_GLASS.put(11, Material.BLUE_STAINED_GLASS);
        COLORED_GLASS.put(12, Material.BROWN_STAINED_GLASS);
        COLORED_GLASS.put(13, Material.GREEN_STAINED_GLASS);
        COLORED_GLASS.put(14, Material.RED_STAINED_GLASS);
        COLORED_GLASS.put(15, Material.BLACK_STAINED_GLASS);
        COLORED_GLASS_PANE.put(-1, Material.GLASS_PANE);
        COLORED_GLASS_PANE.put(0, Material.WHITE_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(1, Material.ORANGE_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(2, Material.MAGENTA_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(3, Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(4, Material.YELLOW_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(5, Material.LIME_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(6, Material.PINK_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(7, Material.GRAY_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(8, Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(9, Material.CYAN_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(10, Material.PURPLE_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(11, Material.BLUE_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(12, Material.BROWN_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(13, Material.GREEN_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(14, Material.RED_STAINED_GLASS_PANE);
        COLORED_GLASS_PANE.put(15, Material.BLACK_STAINED_GLASS_PANE);
        COLORED_GLAZED_TERRACOTTA.put(0, Material.WHITE_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(1, Material.ORANGE_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(2, Material.MAGENTA_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(3, Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(4, Material.YELLOW_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(5, Material.LIME_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(6, Material.PINK_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(7, Material.GRAY_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(8, Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(9, Material.CYAN_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(10, Material.PURPLE_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(11, Material.BLUE_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(12, Material.BROWN_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(13, Material.GREEN_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(14, Material.RED_GLAZED_TERRACOTTA);
        COLORED_GLAZED_TERRACOTTA.put(15, Material.BLACK_GLAZED_TERRACOTTA);
        COLORED_SHULKER_BOXES.put(-1, Material.SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(0, Material.WHITE_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(1, Material.ORANGE_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(2, Material.MAGENTA_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(3, Material.LIGHT_BLUE_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(4, Material.YELLOW_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(5, Material.LIME_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(6, Material.PINK_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(7, Material.GRAY_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(8, Material.LIGHT_GRAY_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(9, Material.CYAN_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(10, Material.PURPLE_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(11, Material.BLUE_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(12, Material.BROWN_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(13, Material.GREEN_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(14, Material.RED_SHULKER_BOX);
        COLORED_SHULKER_BOXES.put(15, Material.BLACK_SHULKER_BOX);
        COLORED_TERRACOTAS.put(-1, Material.TERRACOTTA);
        COLORED_TERRACOTAS.put(0, Material.WHITE_TERRACOTTA);
        COLORED_TERRACOTAS.put(1, Material.ORANGE_TERRACOTTA);
        COLORED_TERRACOTAS.put(2, Material.MAGENTA_TERRACOTTA);
        COLORED_TERRACOTAS.put(3, Material.LIGHT_BLUE_TERRACOTTA);
        COLORED_TERRACOTAS.put(4, Material.YELLOW_TERRACOTTA);
        COLORED_TERRACOTAS.put(5, Material.LIME_TERRACOTTA);
        COLORED_TERRACOTAS.put(6, Material.PINK_TERRACOTTA);
        COLORED_TERRACOTAS.put(7, Material.GRAY_TERRACOTTA);
        COLORED_TERRACOTAS.put(8, Material.LIGHT_GRAY_TERRACOTTA);
        COLORED_TERRACOTAS.put(9, Material.CYAN_TERRACOTTA);
        COLORED_TERRACOTAS.put(10, Material.PURPLE_TERRACOTTA);
        COLORED_TERRACOTAS.put(11, Material.BLUE_TERRACOTTA);
        COLORED_TERRACOTAS.put(12, Material.BROWN_TERRACOTTA);
        COLORED_TERRACOTAS.put(13, Material.GREEN_TERRACOTTA);
        COLORED_TERRACOTAS.put(14, Material.RED_TERRACOTTA);
        COLORED_TERRACOTAS.put(15, Material.BLACK_TERRACOTTA);
        COLORED_WOOLS.put(0, Material.WHITE_WOOL);
        COLORED_WOOLS.put(1, Material.ORANGE_WOOL);
        COLORED_WOOLS.put(2, Material.MAGENTA_WOOL);
        COLORED_WOOLS.put(3, Material.LIGHT_BLUE_WOOL);
        COLORED_WOOLS.put(4, Material.YELLOW_WOOL);
        COLORED_WOOLS.put(5, Material.LIME_WOOL);
        COLORED_WOOLS.put(6, Material.PINK_WOOL);
        COLORED_WOOLS.put(7, Material.GRAY_WOOL);
        COLORED_WOOLS.put(8, Material.LIGHT_GRAY_WOOL);
        COLORED_WOOLS.put(9, Material.CYAN_WOOL);
        COLORED_WOOLS.put(10, Material.PURPLE_WOOL);
        COLORED_WOOLS.put(11, Material.BLUE_WOOL);
        COLORED_WOOLS.put(12, Material.BROWN_WOOL);
        COLORED_WOOLS.put(13, Material.GREEN_WOOL);
        COLORED_WOOLS.put(14, Material.RED_WOOL);
        COLORED_WOOLS.put(15, Material.BLACK_WOOL);
        LEATHER_ARMOR.add(Material.LEATHER_HELMET);
        LEATHER_ARMOR.add(Material.LEATHER_CHESTPLATE);
        LEATHER_ARMOR.add(Material.LEATHER_LEGGINGS);
        LEATHER_ARMOR.add(Material.LEATHER_BOOTS);
        LEATHER_ARMOR.add(Material.LEATHER_HORSE_ARMOR);
        COLORABLES.add(COLORED_BANNERS);
        COLORABLES.add(COLORED_BEDS);
        COLORABLES.add(COLORED_BUNDLES);
        COLORABLES.add(COLORED_CANDLES);
        COLORABLES.add(COLORED_CARPETS);
        COLORABLES.add(COLORED_CONCRETE);
        COLORABLES.add(COLORED_CONCRETE_POWDER);
        COLORABLES.add(COLORED_DYES);
        COLORABLES.add(COLORED_GLASS);
        COLORABLES.add(COLORED_GLASS_PANE);
        COLORABLES.add(COLORED_GLAZED_TERRACOTTA);
        COLORABLES.add(COLORED_SHULKER_BOXES);
        COLORABLES.add(COLORED_TERRACOTAS);
        COLORABLES.add(COLORED_WOOLS);
    }
}
