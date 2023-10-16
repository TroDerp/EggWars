package me.rosillogames.eggwars.arena;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
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
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;

public class SetupGUI
{
    public static void openArenaGUI(Player player1, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(36, "setup.gui.arena.title");
        tInv.setItem(11, TranslatableItem.translatableNameLore(new ItemStack(Material.CRAFTING_TABLE, 1), "setup.gui.arena.basic.item_lore", "setup.gui.arena.basic.item_name"));
        tInv.setItem(13, TranslatableItem.translatableNameLore(new ItemStack(Material.BLACK_BANNER, 1), "setup.gui.arena.teams.item_lore", "setup.gui.arena.teams.item_name"));
        tInv.setItem(15, TranslatableItem.translatableNameLore(new ItemStack(Material.OAK_SIGN, 1), "setup.gui.arena.generators.item_lore", "setup.gui.arena.generators.item_name"));
        tInv.setItem(31, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player1, tInv, MenuType.ARENA_SETUP);
    }

    public static TranslatableItem getSetupGUIItem()
    {
        ItemStack stack = new ItemStack(Material.ITEM_FRAME);
        ItemUtils.setOpensMenu(stack, MenuType.ARENA_SETUP);
        return TranslatableItem.translatableNameLore(stack, "setup.gui.item_lore", "setup.gui.item_name");
    }

    private static void openBasicSetupGUI(Player player1, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(27, "setup.gui.basic.title");
        tInv.setItem(4, TranslatableItem.translatableNameLore(new ItemStack(Material.WRITABLE_BOOK, 1), "setup.gui.basic.todo.item_lore", "setup.gui.basic.todo.item_name"));
        tInv.setItem(10, getLocationSetting("setup.gui.basic.lobby", arena.getLobby(), Material.SANDSTONE));
        tInv.setItem(12, getLocationSetting("setup.gui.basic.center", arena.getCenter(), Material.RED_SANDSTONE));

        if (arena.getBounds() == null)
        {
            arena.setBounds(new Bounds(null, null));
        }

        Bounds bounds = arena.getBounds();
        tInv.setItem(14, getLocationSetting("setup.gui.basic.bounds_start", bounds.getStart(), bounds.getStart() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        tInv.setItem(15, getLocationSetting("setup.gui.basic.bounds_end", bounds.getEnd(), bounds.getEnd() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        tInv.setItem(16, TranslatableItem.fullTranslatable((player) -> new ItemStack(Material.TARGET, 1), (player) -> TranslationUtils.getMessage("setup.gui.basic.bounds_info.item_lore", player, (bounds.getStart() == null && bounds.getEnd() == null ? "" : TranslationUtils.getMessage("setup.gui.basic.bounds_info.remove", player))), (player) -> TranslationUtils.getMessage("setup.gui.basic.bounds_info.item_name", player)));
        tInv.setItem(22, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player1, tInv, MenuType.BASIC_SETTINGS);
    }

    private static void openTeamsSetupGUI(Player player1, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(36, "setup.gui.teams.title");
        Map<Integer, TeamType> map = new HashMap();
        int i = 0;

        for (TeamType teamtype : TeamType.values())
        {
            Team team = arena.getTeams().get(teamtype);
            TranslatableItem tItem = new TranslatableItem((player) ->
            {
                ItemStack stack = ItemUtils.tryColorizeByTeam(teamtype, new ItemStack(Material.WHITE_WOOL, 1));
                int to_conf = 1;//don't change this! negative items (INCLUDING zero) don't work anymore

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

            tItem.setName((player) -> TranslationUtils.getMessage("setup.gui.teams.team.item_name", player, TeamUtils.translateTeamType(teamtype, player, false)));
            tItem.addLoreTranslatable((player) ->
            {
                StringBuilder todoLore = new StringBuilder();
                String clickLore;

                if (team == null)
                {
                    todoLore.append(TranslationUtils.getMessage("setup.gui.teams.team.todo.create", player));
                    clickLore = TranslationUtils.getMessage("setup.gui.teams.team.click.uncreated", player);
                }
                else
                {
                    boolean done = true;

                    if (team.getVillager() == null)
                    {
                        done = false;
                        todoLore.append(TranslationUtils.getMessage("setup.gui.teams.team.todo.villager", player));
                    }

                    if (team.getCages() == null || team.getCages().size() < arena.getMaxTeamPlayers())
                    {
                        done = false;
                        todoLore.append(TranslationUtils.getMessage("setup.gui.teams.team.todo.cages", player));
                    }

                    if (team.getRespawn() == null)
                    {
                        done = false;
                        todoLore.append(TranslationUtils.getMessage("setup.gui.teams.team.todo.respawn", player));
                    }

                    if (team.getEgg() == null)
                    {
                        done = false;
                        todoLore.append(TranslationUtils.getMessage("setup.gui.teams.team.todo.egg", player));
                    }

                    if (done)
                    {
                        todoLore.append(TranslationUtils.getMessage("setup.gui.teams.team.todo.done", player));
                    }

                    clickLore = TranslationUtils.getMessage("setup.gui.teams.team.click.created", player);
                }

                return TranslationUtils.getMessage("setup.gui.teams.team.item_lore", player, todoLore.toString(), clickLore);
            });

            int slot = (9 * ((i / 7) + 1)) + ((i % 7) + 1);
            map.put(slot, teamtype);
            tInv.setItem(slot, tItem);
            i++;
        }

        tInv.setItem(31, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player1, tInv, MenuType.TEAMS_SETUP).setExtraData(map);
    }

    private static void openSingleTeamSetupGUI(Player player, Team team, Arena arena)
    {
        TranslatableInventory tInv = new TranslatableInventory(27, (player1) -> TeamUtils.translateTeamType(team.getType(), player1, false) + "'s settings");
        tInv.setItem(10, getLocationSetting("setup.gui.team.villager", team.getVillager(), Material.VILLAGER_SPAWN_EGG));
        tInv.setItem(12, getMultipleSetting("setup.gui.team.cages", team.getCages(), Material.GLASS, team.getCages().size(), arena.getMaxTeamPlayers()));
        tInv.setItem(14, getLocationSetting("setup.gui.team.respawn", team.getRespawn(), Material.RED_WOOL));
        tInv.setItem(16, getLocationSetting("setup.gui.team.egg", team.getEgg(), Material.DRAGON_EGG));
        tInv.setItem(22, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player, tInv, MenuType.SINGLE_TEAM_SETUP).setExtraData(team);
    }

    private static TranslatableItem getLocationSetting(String tKey, Location setting, Material mat)
    {
        TranslatableItem settingItem = new TranslatableItem(new ItemStack(mat, 1));
        settingItem.setName((player) -> TranslationUtils.getMessage(tKey + ".item_name", player));
        settingItem.addLoreTranslatable((player) ->
        {
            String settingLore;

            if (setting != null)
            {
                settingLore = TranslationUtils.getMessage("setup.gui.location_desc.set", player, setting.getBlockX(), setting.getBlockY(), setting.getBlockZ());
            }
            else
            {
                settingLore = TranslationUtils.getMessage("setup.gui.location_desc.unset", player);
            }

            return TranslationUtils.getMessage(tKey + ".item_lore", player, settingLore);
        });

        return settingItem;
    }

    /* TODO
     * Remove this in the future, will create new gui of multiple items with single locations
     * to add support for right+click to teleport to cages
     */
    private static TranslatableItem getMultipleSetting(String tKey, Collection<Location> setting, Material mat, int set, int toSet)
    {
        TranslatableItem settingItem = new TranslatableItem((player) ->
        {
            ItemStack stack = new ItemStack(mat, (toSet - set) <= 0 ? 1 : (toSet - set));
            return stack;
        });

        settingItem.setName((player) -> TranslationUtils.getMessage(tKey + ".item_name", player));
        settingItem.addLoreTranslatable((player) ->
        {
            StringBuilder settingLore = new StringBuilder();

            if (setting != null && !setting.isEmpty())
            {
                for (Location loc : setting)
                {
                    settingLore.append(TranslationUtils.getMessage("setup.gui.multi_locations.value", player, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                }
            }
            else
            {
                settingLore.append(TranslationUtils.getMessage("setup.gui.multi_locations.none_set", player));
            }

            return TranslationUtils.getMessage(tKey + ".item_lore", player, TranslationUtils.getMessage("setup.gui.multi_locations.desc", player, set, toSet, settingLore.toString()));
        });
        return settingItem;
    }

    private static void openGeneratorsSetupGUI(Player player1, int page)
    {
        Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();
        List<EwPlayerMenu.MenuSize> sizes = EwPlayerMenu.MenuSize.fromChestSize(generators.size());
        TranslatableInventory tInv = new TranslatableInventory(sizes.get(page).getSlots(), "setup.gui.generators.title");

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
                tInv.setItem(slot, TranslatableItem.translatableNameLore(new ItemStack(entry.getValue().droppedToken().getMaterial(), 1), (player) -> TranslationUtils.getMessage("setup.gui.generators.type.item_lore", player), (player) -> TranslationUtils.getMessage("setup.gui.generators.type.item_name", player, entry.getValue().droppedToken().getFormattedName(player))));
                typeMap.put(slot, entry.getValue());
            }

            counter++;
        }

        InventoryController.openInventory(player1, tInv, MenuType.SELECT_GENERATOR).setExtraData(new Pair(page, typeMap));
    }

    private static void openGeneratorLevelsGUI(Player player1, GeneratorType type, int page)
    {
        List<EwPlayerMenu.MenuSize> sizes = EwPlayerMenu.MenuSize.fromChestSize(type.getMaxLevel());
        TranslatableInventory tInv = new TranslatableInventory(sizes.get(page).getSlots(), "setup.gui.gen_level.title");

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
                meta.getPersistentDataContainer().set(ItemUtils.genType, PersistentDataType.STRING, type.getId());
                meta.getPersistentDataContainer().set(ItemUtils.genLevel, PersistentDataType.INTEGER, i);
                itemstack.setItemMeta(meta);
                final int fnl = i;
                TranslatableItem tItem = new TranslatableItem(itemstack);
                tItem.setName((player) -> TranslationUtils.getMessage("setup.gui.gen_level.level.item_name", player, type.droppedToken().getFormattedName(player), fnl));
                tItem.addLoreString("setup.gui.gen_level.level.item_lore", true);
                tInv.setItem(slot, tItem);
                typeMap.put(slot, i);
            }

            counter++;
        }

        InventoryController.openInventory(player1, tInv, MenuType.SELECT_GENERATOR_LEVEL).setExtraData(new Pair(new Pair(type, page), typeMap));
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

            if (ewplayer.getInv().getInventoryType() == MenuType.ARENA_SETUP)
            {
                clickEvent.setCancelled(true);

                if (clickEvent.getRawSlot() == 11)
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openBasicSetupGUI(ewplayer.getPlayer(), ewplayer.getSettingArena());
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

            if (ewplayer.getInv().getInventoryType() == MenuType.BASIC_SETTINGS)
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
                    if (tryTpRightClick(clickEvent, arena.getLobby()))
                    {
                        return;
                    }

                    arena.setLobby(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setWaitingLobby.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 12)
                {
                    if (tryTpRightClick(clickEvent, arena.getCenter()))
                    {
                        return;
                    }

                    arena.setCenter(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setCenter.success", player, arena.getName());
                    flag = true;
                }

                Bounds builder = arena.getBounds();

                if (clickEvent.getRawSlot() == 14)
                {
                    if (tryTpRightClick(clickEvent, builder.getStart()))
                    {
                        return;
                    }

                    builder.setStart(player.getLocation().clone());
                    arena.setBounds(builder);
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 15)
                {
                    if (tryTpRightClick(clickEvent, builder.getEnd()))
                    {
                        return;
                    }

                    builder.setEnd(player.getLocation().clone());
                    arena.setBounds(builder);
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 16 && clickEvent.isShiftClick() && (builder.getStart() != null || builder.getEnd() != null))
                {
                    arena.setBounds(null);
                    TranslationUtils.sendMessage("commands.setBounds.success.removed", player, arena.getName());
                    flag = true;
                }

                if (flag)
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openBasicSetupGUI(ewplayer.getPlayer(), arena);
                }

                return;
            }

            if (ewplayer.getInv().getInventoryType() == MenuType.TEAMS_SETUP)
            {
                clickEvent.setCancelled(true);

                Map<Integer, TeamType> map = (Map<Integer, TeamType>)ewplayer.getInv().getExtraData();
                TeamType teamtype;

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

            if (ewplayer.getInv().getInventoryType() == MenuType.SINGLE_TEAM_SETUP)
            {
                clickEvent.setCancelled(true);
                Team team = (Team)ewplayer.getInv().getExtraData();
                Player player = ewplayer.getPlayer();
                boolean flag = false;

                if (clickEvent.getRawSlot() == 10)
                {
                    if (tryTpRightClick(clickEvent, team.getVillager()))
                    {
                        return;
                    }

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
                    if (tryTpRightClick(clickEvent, team.getRespawn()))
                    {
                        return;
                    }

                    team.setRespawn(player.getLocation());
                    TranslationUtils.sendMessage("commands.setTeamRespawn.success", player, team.getType().id());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 16)
                {
                    if (tryTpRightClick(clickEvent, team.getEgg()))
                    {
                        return;
                    }

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

            if (ewplayer.getInv().getInventoryType() == MenuType.SELECT_GENERATOR)
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

            if (ewplayer.getInv().getInventoryType() == MenuType.SELECT_GENERATOR_LEVEL)
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

                if (map.get(clickEvent.getRawSlot()) != null)
                {
                    ewplayer.getPlayer().getInventory().addItem(clickEvent.getCurrentItem());
                    return;
                }

                return;
            }
        }

        @EventHandler
        public void putGeneratorSign(PlayerInteractEvent event)
        {
            if (event.getPlayer() == null || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getItem() == null || !(event.getClickedBlock().getState() instanceof Sign))
            {
                return;
            }

            EwPlayer ewplayer = PlayerUtils.getEwPlayer(event.getPlayer());
            Arena arena = ewplayer.getSettingArena();

            if (arena == null || !arena.getStatus().equals(ArenaStatus.SETTING) || !ewplayer.getPlayer().getWorld().equals(arena.getWorld()))
            {
                ewplayer.setSettingArena(null);
                return;
            }

            ItemStack itemstack = event.getItem();
            String type = null;
            int level = 0;

            try
            {
                ItemMeta meta = itemstack.getItemMeta();
                type = meta.getPersistentDataContainer().get(ItemUtils.genType, PersistentDataType.STRING);
                level = meta.getPersistentDataContainer().get(ItemUtils.genLevel, PersistentDataType.INTEGER);
            }
            catch (Exception ex)
            {
            }

            Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();

            if (type != null && generators.containsKey(type) && level <= generators.get(type).getMaxLevel())
            {
                event.setCancelled(true);
                Generator generator = new Generator(event.getClickedBlock().getLocation(), level, type, arena);

                if (!generator.equals(arena.putGenerator(generator)))
                {
                    TranslationUtils.sendMessage("setup.generator.added", event.getPlayer());
                }

                generator.updateSign();
            }
        }

        private static boolean tryTpRightClick(InventoryClickEvent click, Location loc)
        {
            if (click.isRightClick() && loc != null)
            {
                click.setCurrentItem(null);
                Player clicker = (Player)click.getWhoClicked();
                InventoryController.closeInventory(clicker, true);
                loc.setWorld(clicker.getWorld());
                clicker.teleport(Locations.toMiddle(loc));
                return true;
            }

            return false;
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
