package me.rosillogames.eggwars.arena;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.mojang.datafixers.util.Pair;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.EwInvType;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamTypes;
import me.rosillogames.eggwars.utils.TeamUtils;

public class SetupGUI
{
    public static void openArenaGUI(Player player1, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(36, (player) -> "Arena Setup Gui");
        tInv.setItem(11, TranslatableItem.fullTranslatable((player) -> new ItemStack(Material.CRAFTING_TABLE, 1), (player) -> "§7Here you can setup some of\n§7the arena's main settings", (player) -> "§e§lMain Setup"));
        tInv.setItem(13, TranslatableItem.fullTranslatable((player) -> new ItemStack(Material.BLACK_BANNER, 1), (player) -> "§7Here you can setup the arena's teams", (player) -> "§e§lTeam Setup"));
        tInv.setItem(15, TranslatableItem.fullTranslatable((player) -> new ItemStack(Material.OAK_SIGN, 1), (player) -> "§7Here you can setup the generators", (player) -> "§e§lGenerator Setup"));
        tInv.setItem(31, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player1, tInv, EwInvType.ARENA_SETUP);
    }

    public static TranslatableItem getSetupGUIItem()
    {
        return TranslatableItem.translatableNameLore(new ItemStack(Material.ITEM_FRAME), (player) -> "§7Right-Click to open!", (player) -> "§a§lSetup GUI");
    }

    private static void openMainSetupGUI(Player player1, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(27, (player) -> "Main Settings");
        tInv.setItem(4, TranslatableItem.fullTranslatable((player) -> new ItemStack(Material.WRITABLE_BOOK, 1), (player) -> "§7Click to show the to-do list on the chat", (player) -> "§a§lTo-Do List"));
        tInv.setItem(10, getTeamSetting("Lobby Location", arena.getLobby(), Material.SANDSTONE));
        tInv.setItem(12, getTeamSetting("Center Location", arena.getCenter(), Material.RED_SANDSTONE));

        if (arena.getBounds() == null)
        {
            arena.setBounds(new Bounds(null, null));
        }

        Bounds bounds = arena.getBounds();
        tInv.setItem(14, getTeamSetting("Bounds Start", bounds.getStart(), bounds.getStart() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        tInv.setItem(15, getTeamSetting("Bounds End", bounds.getEnd(), bounds.getEnd() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        TranslatableItem boundsItem = new TranslatableItem(new ItemStack(Material.TARGET, 1));
        boundsItem.setName((player) -> "§a§lArena Bounds");
        boundsItem.addLoreString("§7Arena Bounds are used to\n§7set the build limits for players" + (bounds.getStart() == null && bounds.getEnd() == null ? "" : "\n\n§4Shift+Click to remove bounds"), false);
        tInv.setItem(16, boundsItem);
        tInv.setItem(22, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player1, tInv, EwInvType.MAIN_SETUP);
    }

    private static void openTeamsSetupGUI(Player player1, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(36, (player) -> "Teams");
        Map<Integer, TeamTypes> map = new HashMap();
        int i = 0;

        for (TeamTypes teamtype : TeamTypes.values())
        {
            Team team = arena.getTeams().get(teamtype);
            TranslatableItem tItem = new TranslatableItem((player) ->
            {
                ItemStack stack = ItemUtils.tryColorizeByTeam(teamtype, new ItemStack(Material.WHITE_WOOL, 1));
                int to_conf = 1;
//don't change this! negative items (INCLUDING zero) don't work
                if (team != null)
                {
                    if (team.getVillager() == null)
                    {
                        to_conf++;
                    }
 
                    if (team.getCages() == null || team.getCages().size() < arena.getMaxTeamPlayers())
                    {
                        to_conf++;
                    }

                    if (team.getRespawn() == null)
                    {
                        to_conf++;
                    }

                    if (team.getEgg() == null)
                    {
                        to_conf++;
                    }

                    if (to_conf == 1)
                    {
                        ItemMeta meta = stack.getItemMeta();
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        stack.setItemMeta(meta);
                        stack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
                    }
                }

                stack.setAmount(to_conf);
                return stack;
            });

            tItem.setName((player) -> TeamUtils.translateTeamType(teamtype, player, false));
            tItem.addLoreString("§7Todo:", false);

            if (team == null)
            {
                tItem.addLoreString("§c - Create team", false);
                tItem.addLoreString("", false);
                tItem.addLoreString("§7Click to create this team!", false);
            }
            else
            {
                boolean done = true;

                if (team.getVillager() == null)
                {
                    done = false;
                    tItem.addLoreString("§6 - Set villager", false);
                }

                if (team.getCages() == null || team.getCages().size() < arena.getMaxTeamPlayers())
                {
                    done = false;
                    tItem.addLoreString("§6 - Add cages", false);
                }

                if (team.getRespawn() == null)
                {
                    done = false;
                    tItem.addLoreString("§6 - Set respawn", false);
                }

                if (team.getEgg() == null)
                {
                    done = false;
                    tItem.addLoreString("§6 - Set egg", false);
                }

                if (done)
                {
                    tItem.addLoreString("§a - Done!", false);
                }

                tItem.addLoreString("", false);
                tItem.addLoreString("§7Click to view settings!", false);
                tItem.addLoreString("§4Shift click to remove this team!", false);
            }

            int slot = (9 * ((i / 7) + 1)) + ((i % 7) + 1);
            map.put(slot, teamtype);
            tInv.setItem(slot, tItem);
            i++;
        }

        tInv.setItem(31, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player1, tInv, EwInvType.TEAMS_SETUP).setExtraData(map);
    }

    private static void openSingleTeamSetupGUI(Player player, Team team, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(27, (player1) -> TeamUtils.translateTeamType(team.getType(), player1, false) + "'s settings");
        tInv.setItem(10, getTeamSetting("Villager Location", team.getVillager(), Material.VILLAGER_SPAWN_EGG));
        tInv.setItem(12, getMultipleSetting("Cage Locations", team.getCages(), Material.GLASS, team.getCages().size(), arena.getMaxTeamPlayers()));
        tInv.setItem(14, getTeamSetting("Respawn Location", team.getRespawn(), Material.RED_WOOL));
        tInv.setItem(16, getTeamSetting("Egg Location", team.getEgg(), Material.DRAGON_EGG));
        tInv.setItem(22, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player, tInv, EwInvType.SINGLE_TEAM_SETUP).setExtraData(team);
    }

    private static TranslatableItem getTeamSetting(String name, Location setting, Material mat)
    {
        TranslatableItem settingItem = new TranslatableItem(new ItemStack(mat, 1));
        settingItem.setName((player) -> "§e§l" + name);

        if (setting != null)
        {
            settingItem.addLoreString("§7Position:", false);
            settingItem.addLoreString("§7- X: " + setting.getBlockX(), false);
            settingItem.addLoreString("§7- Y: " + setting.getBlockY(), false);
            settingItem.addLoreString("§7- Z: " + setting.getBlockZ(), false);
        }
        else
        {
            settingItem.addLoreString("§cNot set!", false);
        }

        settingItem.addLoreString("", false);
        settingItem.addLoreString("§6Click to set location (Your position)", false);
        return settingItem;
    }

    private static TranslatableItem getMultipleSetting(String name, Collection<Location> setting, Material mat, int set, int toSet)
    {
        TranslatableItem settingItem = new TranslatableItem((player) ->
        {
            ItemStack stack = new ItemStack(mat, (toSet - set) <= 0 ? 1 : (toSet - set));
            return stack;
        });

        settingItem.setName((player) -> "§e§l" + name);
        settingItem.addLoreString("§7Positions: §e(" + set + "/" + toSet + ")", false);

        if (setting != null && !setting.isEmpty())
        {
            for (Location loc : setting)
            {
                settingItem.addLoreString("§7- X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ(), false);
            }
        }
        else
        {
            settingItem.addLoreString("§cNot set!", false);
        }

        settingItem.addLoreString("", false);
        settingItem.addLoreString("§6Click to add location (Your position)", false);
        settingItem.addLoreString("§4Shift+Click to remove last location", false);
        return settingItem;
    }

    private static void openGeneratorsSetupGUI(Player player1, int page)
    {
        Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();
        List<EwPlayerMenu.MenuSize> sizes = EwPlayerMenu.MenuSize.fromChestSize(generators.size());
        TranslatableInventory tInv = new TranslatableInventory(sizes.get(page).getSlots(), (player) -> "Generators");

        if (page < (sizes.size() - 1))
        {
            tInv.setItem(sizes.get(page).getSlots() - 1, EwPlayerMenu.getNextItem());
        }

        if (page > 0)
        {
            tInv.setItem(sizes.get(page).getSlots() - 9, EwPlayerMenu.getPreviousItem());
        }

        tInv.setItem(sizes.get(page).getSlots() - 5, EwPlayerMenu.getCloseItem());
        Map<Integer, GeneratorType> typeMap = new HashMap<Integer, GeneratorType>();
        int pages = 0;
        int counter = 0;
        int expected = sizes.get(0).getFilledSlots();

        for (Map.Entry<String, GeneratorType> entry : generators.entrySet())
        {
            if (counter > expected)
            {
                pages++;
                counter = 0;
                expected += sizes.get(pages).getFilledSlots();
            }

            if (pages == page)
            {
                int slot = (9 * ((counter / 7) + 1)) + ((counter % 7) + 1);
                tInv.setItem(slot, TranslatableItem.translatableNameLore(new ItemStack(entry.getValue().droppedToken().getMaterial(), 1), (player) -> "§7Click to choose level", (player) -> entry.getValue().droppedToken().getColor() + TranslationUtils.getMessage(entry.getValue().droppedToken().getTypeName(), player)));
                typeMap.put(slot, entry.getValue());
            }

            counter++;
        }

        InventoryController.openInventory(player1, tInv, EwInvType.GENERATORS_SETUP).setExtraData(new Pair(page, typeMap));
    }

    private static void openGeneratorLevelsGUI(Player player1, GeneratorType type, int page)
    {
        List<EwPlayerMenu.MenuSize> sizes = EwPlayerMenu.MenuSize.fromChestSize(type.getMaxLevel());
        TranslatableInventory tInv = new TranslatableInventory(sizes.get(page).getSlots(), (player) -> "Generators");

        if (page < (sizes.size() - 1))
        {
            tInv.setItem(sizes.get(page).getSlots() - 1, EwPlayerMenu.getNextItem());
        }

        if (page > 0)
        {
            tInv.setItem(sizes.get(page).getSlots() - 9, EwPlayerMenu.getPreviousItem());
        }

        tInv.setItem(sizes.get(page).getSlots() - 5, EwPlayerMenu.getCloseItem());
        Map<Integer, Integer> typeMap = new HashMap<Integer, Integer>();
        int pages = 0;
        int counter = 0;
        int expected = sizes.get(0).getFilledSlots();

        for (int i = 0; i <= type.getMaxLevel(); i++)
        {
            if (counter > expected)
            {
                pages++;
                counter = 0;
                expected += sizes.get(pages).getFilledSlots();
            }

            if (pages == page)
            {
                int slot = (9 * ((counter / 7) + 1)) + ((counter % 7) + 1);
                ItemStack itemstack = new ItemStack(type.droppedToken().getMaterial(), 1);
                ItemMeta meta = itemstack.getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(EggWars.instance, "GEN_TYPE"), PersistentDataType.STRING, type.getId());
                meta.getPersistentDataContainer().set(new NamespacedKey(EggWars.instance, "GEN_LEVEL"), PersistentDataType.INTEGER, i);
                itemstack.setItemMeta(meta);
                final int fnl = i;
                TranslatableItem tItem = new TranslatableItem(itemstack);
                tItem.setName((player) -> type.droppedToken().getColor() + TranslationUtils.getMessage(type.droppedToken().getTypeName(), player) + " - Level " + fnl);
                tItem.addLoreString("§7Click to pick level", false);
                tItem.addLoreString("", false);
                tItem.addLoreString("§7Right click a sign to apply the level!", false);
                tInv.setItem(slot, tItem);
                typeMap.put(slot, i);
            }

            counter++;
        }

        InventoryController.openInventory(player1, tInv, EwInvType.GENERATOR_LEVELS_SETUP).setExtraData(new Pair(new Pair(type, page), typeMap));
    }

    public static class Listener implements org.bukkit.event.Listener
    {
        @EventHandler
        public void arenaGui(InventoryClickEvent clickEvent)
        {
            EwPlayer ewplayer = PlayerUtils.getEwPlayer((Player)clickEvent.getWhoClicked());

            if (ewplayer.getInv() == null || clickEvent.isCancelled() || clickEvent.getCurrentItem() == null || useCloseMenu(clickEvent))
            {
                return;
            }

            if (ewplayer.getInv().getInventoryType() == EwInvType.ARENA_SETUP)
            {
                clickEvent.setCancelled(true);

                if (clickEvent.getRawSlot() == 11)
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openMainSetupGUI(ewplayer.getPlayer(), ewplayer.getSettingArena());
                    return;
                }

                if (clickEvent.getRawSlot() == 13)
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openTeamsSetupGUI(ewplayer.getPlayer(), ewplayer.getSettingArena());
                    return;
                }

                if (clickEvent.getRawSlot() == 15)
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorsSetupGUI(ewplayer.getPlayer(), 0);
                    return;
                }

                return;
            }

            if (ewplayer.getInv().getInventoryType() == EwInvType.MAIN_SETUP)
            {
                clickEvent.setCancelled(true);

                Player player = ewplayer.getPlayer();
                Arena arena = ewplayer.getSettingArena();
                boolean flag = false;

                if (clickEvent.getRawSlot() == 4)
                {
                    arena.sendToDo(player);
                }

                if (clickEvent.getRawSlot() == 10)
                {
                    arena.setLobby(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setArenaLobby.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 12)
                {
                    arena.setCenter(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setCenter.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 14)
                {
                    Bounds builder = arena.getBounds();
                    builder.setStart(player.getLocation().clone());
                    arena.setBounds(builder);
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 15)
                {
                    Bounds builder = arena.getBounds();
                    builder.setEnd(player.getLocation().clone());
                    arena.setBounds(builder);
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 16 && clickEvent.isShiftClick())
                {
                    arena.setBounds(null);
                    TranslationUtils.sendMessage("commands.setBounds.success.removed", player, arena.getName());
                    flag = true;
                }

                if (flag)
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openMainSetupGUI(ewplayer.getPlayer(), arena);
                }

                return;
            }

            if (ewplayer.getInv().getInventoryType() == EwInvType.TEAMS_SETUP)
            {
                clickEvent.setCancelled(true);

                Map<Integer, TeamTypes> map = (Map<Integer, TeamTypes>)ewplayer.getInv().getExtraData();
                TeamTypes teamtype;

                if ((teamtype = map.get(clickEvent.getRawSlot())) != null)
                {
                    Team team = ewplayer.getSettingArena().getTeams().get(teamtype);
                    clickEvent.setCurrentItem(null);

                    if (team == null)
                    {
                        ewplayer.getSettingArena().addTeam(teamtype);
                        SetupGUI.openTeamsSetupGUI(ewplayer.getPlayer(), ewplayer.getSettingArena());
                        TranslationUtils.sendMessage("commands.addTeam.success", ewplayer.getPlayer(), teamtype.id());
                        return;
                    }
                    else if (clickEvent.isShiftClick())
                    {
                        ewplayer.getSettingArena().removeTeam(teamtype);
                        SetupGUI.openTeamsSetupGUI(ewplayer.getPlayer(), ewplayer.getSettingArena());
                        TranslationUtils.sendMessage("commands.removeTeam.success", ewplayer.getPlayer(), teamtype.id());
                        return;
                    }

                    SetupGUI.openSingleTeamSetupGUI(ewplayer.getPlayer(), team, ewplayer.getSettingArena());
                    return;
                }

                return;
            }

            if (ewplayer.getInv().getInventoryType() == EwInvType.SINGLE_TEAM_SETUP)
            {
                clickEvent.setCancelled(true);
                Team team = (Team)ewplayer.getInv().getExtraData();
                Player player = ewplayer.getPlayer();
                boolean flag = false;

                if (clickEvent.getRawSlot() == 10)
                {
                    team.setVillager(player.getLocation());
                    TranslationUtils.sendMessage("commands.setTeamVillager.success", player, team.getType().id());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 12)
                {
                    if (clickEvent.isShiftClick())
                    {
                        boolean removed = team.removeLastCage();
                        TranslationUtils.sendMessage("commands.removeTeamCage." + (removed ? "success" : "failed"), player, team.getType().id());
                    }
                    else
                    {
                        team.addCage(player.getLocation());
                        TranslationUtils.sendMessage("commands.addTeamCage.success", player, team.getType().id());
                    }

                    flag = true;
                }

                if (clickEvent.getRawSlot() == 14)
                {
                    team.setRespawn(player.getLocation());
                    TranslationUtils.sendMessage("commands.setTeamRespawn.success", player, team.getType().id());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 16)
                {
                    team.setEgg(player.getLocation());
                    TranslationUtils.sendMessage("commands.setTeamEgg.success", player, team.getType().id());
                    flag = true;
                }

                if (flag)
                {
                    clickEvent.setCurrentItem(null);
                    //ewplayer.getSettingArena().sendToDo(player);//todo not necessary
                    SetupGUI.openSingleTeamSetupGUI(ewplayer.getPlayer(), team, ewplayer.getSettingArena());
                }

                return;
            }

            if (ewplayer.getInv().getInventoryType() == EwInvType.GENERATORS_SETUP)
            {
                clickEvent.setCancelled(true);
                Pair<Integer, Map<Integer, GeneratorType>> pair = (Pair<Integer, Map<Integer, GeneratorType>>)ewplayer.getInv().getExtraData();

                if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getNextItem().getTranslated(ewplayer.getPlayer())))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorsSetupGUI(ewplayer.getPlayer(), pair.getFirst() + 1);
                    return;
                }

                if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getPreviousItem().getTranslated(ewplayer.getPlayer())))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorsSetupGUI(ewplayer.getPlayer(), pair.getFirst() - 1);
                    return;
                }

                Map<Integer, GeneratorType> map = pair.getSecond();
                GeneratorType generatortype;

                if ((generatortype = map.get(clickEvent.getRawSlot())) != null)
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorLevelsGUI(ewplayer.getPlayer(), generatortype, 0);
                    return;
                }

                return;
            }

            if (ewplayer.getInv().getInventoryType() == EwInvType.GENERATOR_LEVELS_SETUP)
            {
                clickEvent.setCancelled(true);
                Pair<Pair<GeneratorType, Integer>, Map<Integer, Integer>> pair = (Pair<Pair<GeneratorType, Integer>, Map<Integer, Integer>>)ewplayer.getInv().getExtraData();

                if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getNextItem().getTranslated(ewplayer.getPlayer())))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorLevelsGUI(ewplayer.getPlayer(), pair.getFirst().getFirst(), pair.getFirst().getSecond() + 1);
                    return;
                }

                if (clickEvent.getCurrentItem().equals(EwPlayerMenu.getPreviousItem().getTranslated(ewplayer.getPlayer())))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorLevelsGUI(ewplayer.getPlayer(), pair.getFirst().getFirst(), pair.getFirst().getSecond() - 1);
                    return;
                }

                Map<Integer, Integer> map = pair.getSecond();
                Integer level;

                if ((level = map.get(clickEvent.getRawSlot())) != null)
                {
                    ewplayer.getPlayer().getInventory().addItem(clickEvent.getCurrentItem());
                    return;
                }

                return;
            }
        }

        @EventHandler
        public void putGeneratorSign(PlayerInteractEvent interactEvent)
        {
            if (interactEvent.getPlayer() == null || !interactEvent.getAction().equals(Action.RIGHT_CLICK_BLOCK) || interactEvent.getItem() == null || !(interactEvent.getClickedBlock().getState() instanceof Sign))
            {
                return;
            }

            EwPlayer ewplayer = PlayerUtils.getEwPlayer(interactEvent.getPlayer());

            if (ewplayer.getSettingArena() == null || !ewplayer.getSettingArena().getStatus().equals(ArenaStatus.SETTING) || !ewplayer.getPlayer().getWorld().equals(ewplayer.getSettingArena().getWorld()))
            {
                ewplayer.setSettingArena(null);
                return;
            }

            ItemStack itemstack = interactEvent.getItem();
            String type = null;
            int level = 0;

            try
            {
                ItemMeta meta = itemstack.getItemMeta();
                type = meta.getPersistentDataContainer().get(new NamespacedKey(EggWars.instance, "GEN_TYPE"), PersistentDataType.STRING);
                level = meta.getPersistentDataContainer().get(new NamespacedKey(EggWars.instance, "GEN_LEVEL"), PersistentDataType.INTEGER);
            }
            catch (Exception ex) {}

            Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();

            if (type != null && generators.containsKey(type) && level <= generators.get(type).getMaxLevel())
            {
                Location clickLoc = interactEvent.getClickedBlock().getLocation();
                Generator generator = new Generator(clickLoc, level, type, ewplayer.getSettingArena());

                if (!generator.equals(ewplayer.getSettingArena().putGenerator(clickLoc, generator)))
                {
                    TranslationUtils.sendMessage("setup.generator.added", interactEvent.getPlayer());
                }

                generator.updateSign();
            }
        }

        public static boolean useCloseMenu(InventoryClickEvent clickEvent)
        {
            if (EwPlayerMenu.getCloseItem().getTranslated((Player)clickEvent.getWhoClicked()).equals(clickEvent.getCurrentItem()))
            {
                clickEvent.setCancelled(true);
                clickEvent.setCurrentItem(null);
                InventoryController.closeInventory((Player)clickEvent.getWhoClicked(), true);
                return true;
            }

            return false;
        }
    }
}
