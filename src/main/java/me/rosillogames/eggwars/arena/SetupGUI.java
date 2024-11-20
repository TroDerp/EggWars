package me.rosillogames.eggwars.arena;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.objects.Cage;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.EwPlayerMenu;
import me.rosillogames.eggwars.player.inventory.InventoryController;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.PlayerUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class SetupGUI
{
    //private static TranslatableInventory generatorsInv;
    //private static Map<String, TranslatableInventory> generatorLevelInvs = new HashMap();
    private final Arena arena;
    private TranslatableInventory basicSetupInv;
    private TranslatableInventory teamsSetupInv;
    private final Map<Integer, TeamType> teamsSlots = new HashMap();
    private final Map<TeamType, TranslatableInventory> teamInvs = new EnumMap(TeamType.class);

    public SetupGUI(Arena arenaIn)
    {
        this.arena = arenaIn;
        this.updateBasicSetupInv();
        TranslatableInventory teamsInv = new TranslatableInventory(36, "setup.gui.teams.title");

        for (TeamType teamtype : TeamType.values())
        {
            TranslatableItem tItem = new TranslatableItem((player) ->
            {
                Team team = this.arena.getTeams().get(teamtype);
                ItemStack stack = ItemUtils.tryColorizeByTeam(teamtype, new ItemStack(Material.WHITE_WOOL, 1));
                int to_conf = 1;//don't change this! negative items (INCLUDING zero) don't work anymore

                if (team != null)
                {
                    if (team.getVillager() == null)
                    {
                        to_conf++;
                    }

                    if (team.getCages() == null || team.getCages().size() < this.arena.getMaxTeamPlayers())
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
                        ReflectionUtils.setEnchantGlint(stack, true, true);
                    }
                }

                stack.setAmount(to_conf);
                return stack;
            });

            tItem.setName((player) -> TranslationUtils.getMessage("setup.gui.teams.team.item_name", player, TeamUtils.translateTeamType(teamtype, player, false)));
            tItem.addLoreTranslatable((player) ->
            {
                Team team = this.arena.getTeams().get(teamtype);
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

                    if (team.getCages() == null || team.getCages().size() < this.arena.getMaxTeamPlayers())
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
            this.updateTeamInv(teamtype, false);
            int ordinal = teamtype.ordinal();
            int slot = (9 * ((ordinal / 7) + 1)) + ((ordinal % 7) + 1);
            this.teamsSlots.put(slot, teamtype);
            teamsInv.setItem(slot, tItem);
        }

        teamsInv.setItem(31, EwPlayerMenu.getCloseItem());
        this.teamsSetupInv = teamsInv;
    }

    public void updateBasicSetupInv()
    {
        if (this.basicSetupInv == null)
        {
            this.basicSetupInv = new TranslatableInventory(27, "setup.gui.basic.title");
        }

        TranslatableInventory tInv = this.basicSetupInv;
        tInv.setItem(4, TranslatableItem.translatableNameLore(new ItemStack(Material.WRITABLE_BOOK, 1), "setup.gui.basic.todo.item_lore", "setup.gui.basic.todo.item_name"));
        tInv.setItem(10, getLocationSetting("setup.gui.basic.lobby", this.arena.getLobby(), Material.SANDSTONE));
        tInv.setItem(12, getLocationSetting("setup.gui.basic.center", this.arena.getCenter(), Material.RED_SANDSTONE));
        Bounds bounds = this.arena.getBounds();
        tInv.setItem(14, getLocationSetting("setup.gui.basic.bounds_start", bounds.getStart(), bounds.getStart() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        tInv.setItem(15, getLocationSetting("setup.gui.basic.bounds_end", bounds.getEnd(), bounds.getEnd() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        tInv.setItem(16, TranslatableItem.fullTranslatable((player) -> new ItemStack(Material.TARGET, 1), (player) -> TranslationUtils.getMessage("setup.gui.basic.bounds_info.item_lore", player, (bounds.getStart() == null && bounds.getEnd() == null ? "" : TranslationUtils.getMessage("setup.gui.basic.bounds_info.remove", player))), (player) -> TranslationUtils.getMessage("setup.gui.basic.bounds_info.item_name", player)));
        tInv.setItem(22, EwPlayerMenu.getCloseItem());
        InventoryController.updateInventories((p) -> this.arena.equals(((Arena)p.getInv().getExtraData()[0])), tInv, MenuType.BASIC_SETTINGS);
    }

    protected void updateTeamInv(TeamType type, boolean sendUpdate)
    {
        Predicate<EwPlayer> invPredicate = (p) -> this.arena.equals(((Arena)p.getInv().getExtraData()[0])) && type.equals(((TeamType)p.getInv().getExtraData()[1]));
        Team team = this.arena.getTeams().get(type);

        if (team != null)
        {
            TranslatableInventory teamInv = this.teamInvs.get(type);

            if (teamInv == null)
            {
                teamInv = new TranslatableInventory(27, (player) -> TranslationUtils.getMessage("setup.gui.team.title", player, TeamUtils.translateTeamType(team.getType(), player, false)));
            }

            teamInv.setItem(10, getLocationSetting("setup.gui.team.villager", team.getVillager(), Material.VILLAGER_SPAWN_EGG));
            teamInv.setItem(12, getMultipleSetting("setup.gui.team.cages", team.getCages(), Material.GLASS, team.getCages().size(), this.arena.getMaxTeamPlayers()));
            teamInv.setItem(14, getLocationSetting("setup.gui.team.respawn", team.getRespawn(), Material.RED_WOOL));
            teamInv.setItem(16, getLocationSetting("setup.gui.team.egg", team.getEgg(), Material.DRAGON_EGG));
            teamInv.setItem(22, EwPlayerMenu.getCloseItem());
            this.teamInvs.put(type, teamInv);

            if (sendUpdate)
            {
                InventoryController.updateInventories(invPredicate, teamInv, MenuType.SETUP_SINGLE_TEAM);
            }
        }
        else
        {
            this.teamInvs.remove(type);

            if (sendUpdate)
            {
                InventoryController.closeInventories(invPredicate, MenuType.SETUP_SINGLE_TEAM, 1);
            }
        }
    }

    public ItemStack getItem(Player player)
    {
        ItemStack stack = new ItemStack(Material.ITEM_FRAME);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(TranslationUtils.getMessage("setup.gui.item_name", player, this.arena.getName()));
        meta.setLore(Arrays.asList(TranslationUtils.getMessage("setup.gui.item_lore", player, this.arena.getName()).split("\\n")));
        meta.getPersistentDataContainer().set(ItemUtils.arenaId, PersistentDataType.STRING, this.arena.getId());
        stack.setItemMeta(meta);
        ItemUtils.setOpensMenu(stack, MenuType.ARENA_SETUP);
        return stack;
    }

    public void openArenaGUI(Player player)
    {
        TranslatableInventory tInv = new TranslatableInventory(36, "setup.gui.arena.title");
        tInv.setItem(11, TranslatableItem.translatableNameLore(new ItemStack(Material.CRAFTING_TABLE, 1), "setup.gui.arena.basic.item_lore", "setup.gui.arena.basic.item_name"));
        tInv.setItem(13, TranslatableItem.translatableNameLore(new ItemStack(Material.BLACK_BANNER, 1), "setup.gui.arena.teams.item_lore", "setup.gui.arena.teams.item_name"));
        tInv.setItem(15, TranslatableItem.translatableNameLore(new ItemStack(Material.OAK_SIGN, 1), "setup.gui.arena.generators.item_lore", "setup.gui.arena.generators.item_name"));

        for (TranslatableItem item : tInv.getContents().values())
        {
            if (this.arena.getStatus() != ArenaStatus.SETTING)
            {
                item.addLoreString("commands.error.arena_needs_edit_mode", true);
            }
            else if (this.arena != EggWars.getArenaManager().getArenaByWorld(player.getWorld()))
            {
                item.addLoreString("commands.error.not_in_arena_world", true);
            }
        }

        tInv.setItem(31, EwPlayerMenu.getCloseItem());
        InventoryController.openInventory(player, tInv, MenuType.ARENA_SETUP).setExtraData(this.arena);
    }

    private void openBasicSetupGUI(Player player)
    {
        InventoryController.openInventory(player, this.basicSetupInv, MenuType.BASIC_SETTINGS).setExtraData(this.arena);
    }

    private void openTeamsSetupGUI(Player player)
    {
        InventoryController.openInventory(player, this.teamsSetupInv, MenuType.SETUP_TEAMS).setExtraData(this.arena);
    }

    private void openSingleTeamSetupGUI(Player player, TeamType teamType)
    {
        InventoryController.openInventory(player, this.teamInvs.get(teamType), MenuType.SETUP_SINGLE_TEAM).setExtraData(this.arena, teamType);
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
     * "setup.gui.cages.title": "{0}&r's cages ({1}/{2})",
     * "setup.gui.cages.cage.item_name": "Cage {0}",
     * "setup.gui.cages.cage.item_lore": "&7Cage settings:\n&f\n&7Location: {0}\n&7Mirror Structure: {1}\n&7Rotate Structure: {2}\n\n&3Click to open settings\n&4Shift click to remove this cage"
     */
    private static TranslatableItem getMultipleSetting(String tKey, Collection<Cage> setting, Material mat, int set, int toSet)
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
                for (Cage cage : setting)
                {
                    Location loc = cage.getLocation();
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
                GeneratorType type = entry.getValue();
                tInv.setItem(slot, TranslatableItem.translatableNameLore(new ItemStack(type.droppedToken().getMaterial(), 1), (player) -> TranslationUtils.getMessage("setup.gui.generators.type.item_lore", player), (player) -> TranslationUtils.getMessage("setup.gui.generators.type.item_name", player, TranslationUtils.getMessage(type.droppedToken().getTypeName(), player), type.droppedToken().getColor())));
                typeMap.put(slot, type);
            }

            counter++;
        }

        InventoryController.openInventory(player1, tInv, MenuType.SELECT_GENERATOR).setExtraData(page, typeMap);
    }

    private static void openGeneratorLevelsGUI(Player player, GeneratorType type, int page)
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
                tItem.setName(pl ->
                {
                    String lvl = fnl == 0 ? TranslationUtils.getMessage("generator.info.broken", pl) : TranslationUtils.getMessage("generator.info.level", pl, fnl);
                    return TranslationUtils.getMessage("setup.gui.gen_level.level.item_name", pl, TranslationUtils.getMessage(type.droppedToken().getTypeName(), pl), lvl, type.droppedToken().getColor());
                });
                tItem.addLoreString("setup.gui.gen_level.level.item_lore", true);
                tInv.setItem(slot, tItem);
                typeMap.put(slot, i);
            }

            counter++;
        }

        InventoryController.openInventory(player, tInv, MenuType.SELECT_GENERATOR_LEVEL).setExtraData(page, type, typeMap);
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

            if (ewplayer.getInv().getInventoryType() == MenuType.SELECT_GENERATOR)
            {
                clickEvent.setCancelled(true);
                int page = (Integer)ewplayer.getInv().getExtraData()[0];

                if (EwPlayerMenu.getNextItem().equalsItem(clickEvent.getCurrentItem(), ewplayer.getPlayer()))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorsSetupGUI(ewplayer.getPlayer(), page + 1);
                    return;
                }

                if (EwPlayerMenu.getPreviousItem().equalsItem(clickEvent.getCurrentItem(), ewplayer.getPlayer()))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorsSetupGUI(ewplayer.getPlayer(), page - 1);
                    return;
                }

                Map<Integer, GeneratorType> map = (Map<Integer, GeneratorType>)ewplayer.getInv().getExtraData()[1];
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
                int page = (Integer)ewplayer.getInv().getExtraData()[0];
                GeneratorType type = (GeneratorType)ewplayer.getInv().getExtraData()[1];

                if (EwPlayerMenu.getNextItem().equalsItem(clickEvent.getCurrentItem(), ewplayer.getPlayer()))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorLevelsGUI(ewplayer.getPlayer(), type, page + 1);
                    return;
                }

                if (EwPlayerMenu.getPreviousItem().equalsItem(clickEvent.getCurrentItem(), ewplayer.getPlayer()))
                {
                    clickEvent.setCurrentItem(null);
                    SetupGUI.openGeneratorLevelsGUI(ewplayer.getPlayer(), type, page - 1);
                    return;
                }

                Map<Integer, Integer> map = (Map<Integer, Integer>)ewplayer.getInv().getExtraData()[2];

                if (map.get(clickEvent.getRawSlot()) != null)
                {
                    ewplayer.getPlayer().getInventory().addItem(clickEvent.getCurrentItem());
                    return;
                }

                return;
            }

            MenuType menu = ewplayer.getInv().getInventoryType();

            if (!MenuType.isSetupMenu(menu))
            {
                return;
            }

            clickEvent.setCancelled(true);
            Arena arena = (Arena)ewplayer.getInv().getExtraData()[0];

            if (menu == MenuType.ARENA_SETUP)
            {
                if (arena.getStatus() != ArenaStatus.SETTING || arena != EggWars.getArenaManager().getArenaByWorld(ewplayer.getPlayer().getWorld()))
                {
                    return;
                }

                if (clickEvent.getRawSlot() == 11)
                {
                    clickEvent.setCurrentItem(null);
                    arena.getSetupGUI().openBasicSetupGUI(ewplayer.getPlayer());
                    return;
                }

                if (clickEvent.getRawSlot() == 13)
                {
                    clickEvent.setCurrentItem(null);
                    arena.getSetupGUI().openTeamsSetupGUI(ewplayer.getPlayer());
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

            if (menu == MenuType.BASIC_SETTINGS)
            {
                Player player = ewplayer.getPlayer();
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

                Bounds bounds = arena.getBounds();

                if (clickEvent.getRawSlot() == 14)
                {
                    if (tryTpRightClick(clickEvent, bounds.getStart()))
                    {
                        return;
                    }

                    bounds.setStart(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 15)
                {
                    if (tryTpRightClick(clickEvent, bounds.getEnd()))
                    {
                        return;
                    }

                    bounds.setEnd(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (clickEvent.getRawSlot() == 16 && clickEvent.isShiftClick() && (bounds.getStart() != null || bounds.getEnd() != null))
                {
                    bounds.setBounds(null, null);
                    TranslationUtils.sendMessage("commands.setBounds.success.removed", player, arena.getName());
                    flag = true;
                }

                if (flag)
                {
                    clickEvent.setCurrentItem(null);
                    arena.getSetupGUI().updateBasicSetupInv();
                }

                return;
            }

            if (menu == MenuType.SETUP_TEAMS)
            {
                Map<Integer, TeamType> map = arena.getSetupGUI().teamsSlots;
                TeamType teamtype;

                if ((teamtype = map.get(clickEvent.getRawSlot())) != null)
                {
                    Team team = arena.getTeams().get(teamtype);
                    clickEvent.setCurrentItem(null);

                    if (team == null)
                    {
                        arena.addTeam(teamtype);
                        arena.updateSetupTeam(teamtype);
                        TranslationUtils.sendMessage("commands.addTeam.success", ewplayer.getPlayer(), teamtype.id());
                        return;
                    }
                    else if (clickEvent.isShiftClick())
                    {
                        arena.removeTeam(teamtype);
                        arena.updateSetupTeam(teamtype);
                        TranslationUtils.sendMessage("commands.removeTeam.success", ewplayer.getPlayer(), teamtype.id());
                        return;
                    }

                    arena.getSetupGUI().openSingleTeamSetupGUI(ewplayer.getPlayer(), teamtype);
                    return;
                }

                return;
            }

            Player player = ewplayer.getPlayer();

            if (menu == MenuType.SETUP_SINGLE_TEAM)
            {
                TeamType teamtype = (TeamType)ewplayer.getInv().getExtraData()[1];
                Team team = arena.getTeams().get(teamtype);
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
                    arena.updateSetupTeam(teamtype);
                }

                return;
            }
        }

        @EventHandler
        public void signClick(PlayerInteractEvent event)
        {
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getItem() == null || !(event.getClickedBlock().getState() instanceof Sign))
            {
                return;
            }

            Arena arena = EggWars.getArenaManager().getArenaByWorld(event.getPlayer().getWorld());
            ItemStack stack = event.getItem();

            if (arena == null)
            {
                if (!event.getPlayer().hasPermission("eggwars.arenaSign.place"))
                {
                    return;
                }

                String arenaId = ItemUtils.getPersistentData(stack, ItemUtils.arenaId, PersistentDataType.STRING);

                if ((arena = EggWars.getArenaManager().getArenaById(arenaId)) == null)
                {
                    return;
                }

                if (EggWars.bungee.isEnabled())
                {
                    TranslationUtils.sendMessage("commands.error.bungee_mode", event.getPlayer());
                    return;
                }

                event.setCancelled(true);

                if (LobbySigns.isValidBlockSign(event.getClickedBlock()))
                {
                    EggWars.signs.add(new ArenaSign(arena, event.getClickedBlock().getLocation()));
                    TranslationUtils.sendMessage("setup.sign.arena.added", event.getPlayer());
                    EggWars.saveSigns();
                }

                return;
            }
            else if (arena.getStatus().equals(ArenaStatus.SETTING))
            {
                if (!event.getPlayer().hasPermission("eggwars.genSign.place"))
                {
                    return;
                }

                String type = ItemUtils.getPersistentData(stack, ItemUtils.genType, PersistentDataType.STRING);
                Integer level = ItemUtils.getPersistentData(stack, ItemUtils.genLevel, PersistentDataType.INTEGER);
                Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();

                if (type != null && level != null && generators.containsKey(type) && level <= generators.get(type).getMaxLevel())
                {
                    event.setCancelled(true);
                    Generator generator = new Generator(event.getClickedBlock().getLocation(), level, type, arena);
                    Generator prevGen = arena.putGenerator(generator);

                    if (!generator.equals(prevGen))
                    {
                        TranslationUtils.sendMessage(prevGen != null ? "setup.generator.changed" : "setup.generator.added", event.getPlayer());
                    }

                    generator.reloadCache();
                }
            }
        }

        private static boolean tryTpRightClick(InventoryClickEvent click, Location loc)
        {
            if (click.isRightClick() && loc != null)
            {
                click.setCurrentItem(null);
                Player clicker = (Player)click.getWhoClicked();
                InventoryController.closeInventory(clicker, 0);
                clicker.teleport(Locations.toMiddle(loc));
                return true;
            }

            return false;
        }

        public static boolean useCloseMenu(InventoryClickEvent clickEvent)
        {
            if (EwPlayerMenu.getCloseItem().equalsItem(clickEvent.getCurrentItem(), (Player)clickEvent.getWhoClicked()))
            {
                clickEvent.setCancelled(true);
                clickEvent.setCurrentItem(null);
                InventoryController.closeInventory((Player)clickEvent.getWhoClicked(), 1);
                return true;
            }

            return false;
        }
    }
}
