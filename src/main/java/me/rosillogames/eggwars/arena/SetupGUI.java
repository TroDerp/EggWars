package me.rosillogames.eggwars.arena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.enums.ArenaStatus;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.menu.EwMenu;
import me.rosillogames.eggwars.menu.ProfileMenus;
import me.rosillogames.eggwars.menu.SerializingItems;
import me.rosillogames.eggwars.menu.TranslatableMenu;
import me.rosillogames.eggwars.menu.ProfileMenus.MenuSize;
import me.rosillogames.eggwars.objects.ArenaSign;
import me.rosillogames.eggwars.objects.Cage;
import me.rosillogames.eggwars.player.MenuPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableInventory;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.Pair;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class SetupGUI
{
    private static TranslatableMenu selectGenTypeMenu;
    private static Map<GeneratorType, TranslatableMenu> selectGenLvlMenus = new HashMap();
    private final Arena arena;
    private TranslatableMenu arenaMenu;
    private TranslatableMenu basicsMenu;
    private TranslatableMenu teamsMenu;
    private final Map<TeamType, TranslatableMenu> editTeamMenus = new EnumMap(TeamType.class);

    public SetupGUI(Arena arenaIn)
    {
        this.arena = arenaIn;
        this.updateBasicsMenu();
        TranslatableInventory teamsInv = new TranslatableInventory(36, "setup.gui.teams.title");

        for (TeamType teamtype : TeamType.values())
        {
            this.updateTeamInv(teamtype, false);
            int ordinal = teamtype.ordinal();
            int slot = (9 * ((ordinal / 7) + 1)) + ((ordinal % 7) + 1);
            teamsInv.setItem(slot, (player) ->
            {
                Team team = this.arena.getTeams().get(teamtype);
                ItemStack stack = ItemUtils.tryColorizeByTeam(teamtype, new ItemStack(Material.WHITE_WOOL, 1));
                SerializingItems.SETUP_TEAM.setItemReference(stack, teamtype);
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
                TranslatableItem.setName(stack, TranslationUtils.getMessage("setup.gui.teams.team.item_name", player, TeamUtils.translateTeamType(teamtype, player, false)));
                StringBuilder todoLore = new StringBuilder();
                String clickLore;

                if (team == null)
                {
                    todoLore.append(TranslationUtils.getMessage("setup.gui.teams.team.todo.create", player));
                    clickLore = TranslationUtils.getMessage("setup.gui.teams.team.click.create", player);
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

                    clickLore = TranslationUtils.getMessage("setup.gui.teams.team.click.manage", player);
                }

                TranslatableItem.setLore(stack, TranslationUtils.getMessage("setup.gui.teams.team.item_lore", player, todoLore.toString(), clickLore));
                return stack;
            });
        }

        teamsInv.setItem(31, ProfileMenus::getCloseItem);
        this.teamsMenu = new TranslatableMenu(MenuType.SETUP_TEAMS, Listener::setupClick);
        this.teamsMenu.addPage(teamsInv);
    }

    public void closeAllGuis()
    {
        if (this.arenaMenu != null)
        {
            this.arenaMenu.closeForEveryone(false);
        }

        if (this.basicsMenu != null)
        {
            this.basicsMenu.closeForEveryone(false);
        }

        this.teamsMenu.closeForEveryone(false);

        for (TranslatableMenu menu : this.editTeamMenus.values())
        {
            menu.closeForEveryone(false);
        }//What about gen menus? add arena check predicate?
    }

    public void updateBasicsMenu()
    {
        if (this.basicsMenu == null)
        {
            this.basicsMenu = new TranslatableMenu(MenuType.BASIC_SETTINGS, Listener::setupClick);
            this.basicsMenu.addPage(new TranslatableInventory(27, "setup.gui.basic.title"));
        }

        TranslatableInventory tInv = this.basicsMenu.getPage(0);
        tInv.setItem(4, TranslatableItem.translatableNameLore(new ItemStack(Material.WRITABLE_BOOK, 1), "setup.gui.basic.todo.item_lore", "setup.gui.basic.todo.item_name"));
        tInv.setItem(10, getLocationSetting("setup.gui.basic.lobby", this.arena.getLobby(), Material.SANDSTONE));
        tInv.setItem(12, getLocationSetting("setup.gui.basic.center", this.arena.getCenter(), Material.RED_SANDSTONE));
        Bounds bounds = this.arena.getBounds();
        tInv.setItem(14, getLocationSetting("setup.gui.basic.bounds_start", bounds.getStart(), bounds.getStart() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        tInv.setItem(15, getLocationSetting("setup.gui.basic.bounds_end", bounds.getEnd(), bounds.getEnd() != null ? Material.STRUCTURE_VOID : Material.BARRIER));
        tInv.setItem(16, TranslatableItem.fullTranslatable((player) -> new ItemStack(Material.TARGET, 1), (player) -> TranslationUtils.getMessage("setup.gui.basic.bounds_info.item_lore", player, (bounds.getStart() == null && bounds.getEnd() == null ? "" : TranslationUtils.getMessage("setup.gui.basic.bounds_info.remove", player))), (player) -> TranslationUtils.getMessage("setup.gui.basic.bounds_info.item_name", player)));
        tInv.setItem(22, ProfileMenus::getCloseItem);
        this.basicsMenu.sendMenuUpdate(false);
    }

    protected void updateTeamInv(TeamType type, boolean sendUpdate)
    {
        Team team = this.arena.getTeams().get(type);
        TranslatableMenu singleTeamM;

        if (team != null)
        {
            singleTeamM = this.editTeamMenus.get(type);

            if (singleTeamM == null)
            {
                singleTeamM = new TranslatableMenu(MenuType.SETUP_SINGLE_TEAM, Listener::setupClick);
                singleTeamM.addPage(new TranslatableInventory(27, (player) -> TranslationUtils.getMessage("setup.gui.team.title", player, TeamUtils.translateTeamType(team.getType(), player, false))));
                singleTeamM.setExtraData(type);
                this.editTeamMenus.put(type, singleTeamM);
            }

            TranslatableInventory teamInv = singleTeamM.getPage(0);
            teamInv.setItem(10, getLocationSetting("setup.gui.team.villager", team.getVillager(), Material.VILLAGER_SPAWN_EGG));
            teamInv.setItem(12, getMultipleSetting("setup.gui.team.cages", team.getCages(), Material.GLASS, team.getCages().size(), this.arena.getMaxTeamPlayers()));
            teamInv.setItem(14, getLocationSetting("setup.gui.team.respawn", team.getRespawn(), Material.RED_WOOL));
            teamInv.setItem(16, getLocationSetting("setup.gui.team.egg", team.getEgg(), Material.DRAGON_EGG));
            teamInv.setItem(22, ProfileMenus::getCloseItem);

            if (sendUpdate)
            {
                this.teamsMenu.sendMenuUpdate(false);
                singleTeamM.sendMenuUpdate(false);
            }
        }
        else
        {
            singleTeamM = this.editTeamMenus.remove(type);

            if (sendUpdate)
            {
                this.teamsMenu.sendMenuUpdate(false);
                singleTeamM.closeForEveryone(true);
            }
        }
    }

    public ItemStack getItem(Player player)
    {
        ItemStack stack = new ItemStack(Material.ITEM_FRAME);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(TranslationUtils.getMessage("setup.gui.item_name", player, this.arena.getName()));
        meta.setLore(Arrays.asList(TranslationUtils.getMessage("setup.gui.item_lore", player, this.arena.getName()).split("\\n")));
        ItemUtils.arenaId.setTo(meta, this.arena.getId());
        stack.setItemMeta(meta);
        ItemUtils.setOpensMenu(stack, MenuType.ARENA_SETUP);
        return stack;
    }

    public void openArenaGUI(MenuPlayer player)
    {
        if (this.arenaMenu == null)
        {
            this.arenaMenu = new TranslatableMenu(MenuType.ARENA_SETUP, Listener::setupClick);
            TranslatableInventory tInv = new TranslatableInventory(36, "setup.gui.arena.title");
            ItemStack stack = new ItemStack(Material.CRAFTING_TABLE, 1);
            ItemUtils.setOpensMenu(stack, MenuType.BASIC_SETTINGS);
            tInv.setItem(11, TranslatableItem.translatableNameLore(stack, "setup.gui.arena.basic.item_lore", "setup.gui.arena.basic.item_name"));
            stack = new ItemStack(Material.BLACK_BANNER, 1);
            ItemUtils.setOpensMenu(stack, MenuType.SETUP_TEAMS);
            tInv.setItem(13, TranslatableItem.translatableNameLore(stack, "setup.gui.arena.teams.item_lore", "setup.gui.arena.teams.item_name"));
            stack = new ItemStack(Material.OAK_SIGN, 1);
            ItemUtils.setOpensMenu(stack, MenuType.SELECT_GENERATOR);
            tInv.setItem(15, TranslatableItem.translatableNameLore(stack, "setup.gui.arena.generators.item_lore", "setup.gui.arena.generators.item_name"));
            tInv.setItem(31, ProfileMenus::getCloseItem);
            this.arenaMenu.addPage(tInv);
        }

        player.openMenu(this.arenaMenu);
    }

    private void openBasicSetupGUI(MenuPlayer player)
    {
        player.openMenu(this.basicsMenu);
    }

    private void openTeamsSetupGUI(MenuPlayer player)
    {
        player.openMenu(this.teamsMenu);
    }

    private void openSingleTeamSetupGUI(MenuPlayer player, TeamType teamType)
    {
        player.openMenu(this.editTeamMenus.get(teamType));
    }

    private static Function<Player, ItemStack> getLocationSetting(String tKey, Location setting, Material mat)
    {
        return (pl) ->
        {
            ItemStack stack = new ItemStack(mat, 1);
            TranslatableItem.setName(stack, TranslationUtils.getMessage(tKey + ".item_name", pl));
            String settingLore;

            if (setting != null)
            {
                settingLore = TranslationUtils.getMessage("setup.gui.location_desc.set", pl, setting.getBlockX(), setting.getBlockY(), setting.getBlockZ());
            }
            else
            {
                settingLore = TranslationUtils.getMessage("setup.gui.location_desc.unset", pl);
            }

            TranslatableItem.setLore(stack, TranslationUtils.getMessage(tKey + ".item_lore", pl, settingLore));
            return stack;
        };
    }

    /* TODO
     * Remove this in the future, will create new gui of multiple items with single locations
     * to add support for right+click to teleport to cages
     * "setup.gui.cages.title": "{0}&r's cages ({1}/{2})",
     * "setup.gui.cages.cage.item_name": "Cage {0}",
     * "setup.gui.cages.cage.item_lore": "&7Cage settings:\n&f\n&7Location: {0}\n&7Mirror Structure: {1}\n&7Rotate Structure: {2}\n\n&3Click to open settings\n&4Shift click to remove this cage"
     */
    private static Function<Player, ItemStack> getMultipleSetting(String tKey, Collection<Cage> setting, Material mat, int set, int toSet)
    {
        return (pl) ->
        {
            ItemStack stack = new ItemStack(mat, (toSet - set) <= 1 ? 1 : (toSet - set));
            TranslatableItem.setName(stack, TranslationUtils.getMessage(tKey + ".item_name", pl));
            StringBuilder settingLore = new StringBuilder();

            if (setting != null && !setting.isEmpty())
            {
                for (Cage cage : setting)
                {
                    Location loc = cage.getLocation();
                    settingLore.append(TranslationUtils.getMessage("setup.gui.multi_locations.value", pl, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                }
            }
            else
            {
                settingLore.append(TranslationUtils.getMessage("setup.gui.multi_locations.none_set", pl));
            }

            TranslatableItem.setLore(stack, TranslationUtils.getMessage(tKey + ".item_lore", pl, TranslationUtils.getMessage("setup.gui.multi_locations.desc", pl, set, toSet, settingLore.toString())));
            return stack;
        };
    }

    private static void openGeneratorTypes(MenuPlayer ply)
    {
        if (selectGenTypeMenu == null)
        {
            makeGeneratorsTypesGUI();
        }

        ply.openMenu(selectGenTypeMenu);
    }

    public static void makeGeneratorsTypesGUI()
    {
        if (selectGenTypeMenu == null)
        {
            selectGenTypeMenu = new TranslatableMenu(MenuType.SELECT_GENERATOR, Listener::setupClick);
            selectGenTypeMenu.setUsable(true);
            //selectGenTypeMenu.setParent(this.arenaMenu); TODO: not possible, fix later
        }

        selectGenTypeMenu.clearPages();
        List<GeneratorType> generators = new ArrayList(EggWars.getGeneratorManager().getGenerators().values());
        List<ProfileMenus.MenuSize> sizes = ProfileMenus.MenuSize.fromChestSize(generators.size());
        int counter = 0;
        int expected = 0;

        for (int page = 0; page < sizes.size(); ++page)
        {
            MenuSize size = (MenuSize)sizes.get(page);
            TranslatableInventory translatableinv = new TranslatableInventory(size.getSlots(), "setup.gui.generators.title");
            expected += size.getFilledSlots();

            for (int j = 0; j < size.getFilledSlots() && counter < generators.size() && counter <= expected; ++j, ++counter)
            {
                GeneratorType type = generators.get(counter);
                int slot = 9 * (j / 7 + 1) + j % 7 + 1;
                translatableinv.setItem(slot, (pl) ->
                {
                    ItemStack stack = new ItemStack(type.droppedToken().getMaterial(), 1);
                    SerializingItems.OPEN_GEN_TYPE.setItemReference(stack, type);
                    String gType = TranslationUtils.getMessage(type.droppedToken().getTypeName(), pl);
                    TranslatableItem.setName(stack, TranslationUtils.getMessage("setup.gui.generators.type.item_name", pl, gType, type.droppedToken().getColor()));
                    TranslatableItem.setLore(stack, TranslationUtils.getMessage("setup.gui.generators.type.item_lore", pl, gType));
                    return stack;
                });
            }

            if (page < sizes.size() - 1)
            {
                translatableinv.setItem(size.getSlots() - 1, ProfileMenus.getNextItem());
            }

            if (page > 0)
            {
                translatableinv.setItem(size.getSlots() - 9, ProfileMenus.getPreviousItem());
            }

            translatableinv.setItem(size.getSlots() - 5, ProfileMenus::getCloseItem);
            selectGenTypeMenu.addPage(translatableinv);
        }

        selectGenTypeMenu.sendMenuUpdate(true);
    }

    private static void openGeneratorLevels(MenuPlayer ply, GeneratorType type)
    {
        if (!selectGenLvlMenus.containsKey(type))
        {
            TranslatableMenu menu = new TranslatableMenu(MenuType.SELECT_GENERATOR_LEVEL, Listener::setupClick);
            menu.setUsable(true);
            selectGenLvlMenus.put(type, menu);
            makeGeneratorLevelsGUI(type);
        }

        ply.openMenu(selectGenLvlMenus.get(type));
    }

    public static void reloadGeneratorLevelsGUIs()
    {
        Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();

        for (GeneratorType gentype : selectGenLvlMenus.keySet())
        {
            TranslatableMenu menu = selectGenLvlMenus.get(gentype);

            if (!generators.containsKey(gentype.getId()))
            {
                if (menu.getOpeners().size() > 1)
                {
                    menu.closeForEveryone(false);
                }

                selectGenLvlMenus.remove(gentype);
            }
            else if (!generators.get(gentype.getId()).equals(gentype))
            {
                makeGeneratorLevelsGUI(gentype);
            }
        }
    }

    private static void makeGeneratorLevelsGUI(GeneratorType type)
    {//Note: Item-level creation loop works differently than others, since the max level is a level that actually exists!
        TranslatableMenu menu = selectGenLvlMenus.get(type);
        menu.clearPages();
        List<ProfileMenus.MenuSize> sizes = ProfileMenus.MenuSize.fromChestSize(type.getMaxLevel() + 1);
        int counter = 0;
        int expected = 0;

        for (int page = 0; page < sizes.size(); ++page)
        {
            MenuSize size = (MenuSize)sizes.get(page);
            TranslatableInventory translatableinv = new TranslatableInventory(size.getSlots(), "setup.gui.gen_level.title");
            expected += size.getFilledSlots();

            for (int j = 0; j < size.getFilledSlots() && counter <= type.getMaxLevel() && counter <= expected; ++j, ++counter)
            {
                ItemStack stack = new ItemStack(type.droppedToken().getMaterial(), 1);
                SerializingItems.PLACE_GEN.setItemReference(stack, new Pair(type.getId(), counter));
                final int fnl = counter;
                TranslatableItem tItem = new TranslatableItem(stack);
                tItem.setNameTranslatable(pl ->
                {
                    String lvl = fnl == 0 ? TranslationUtils.getMessage("generator.info.broken", pl) : TranslationUtils.getMessage("generator.info.level", pl, fnl);
                    return TranslationUtils.getMessage("setup.gui.gen_level.level.item_name", pl, TranslationUtils.getMessage(type.droppedToken().getTypeName(), pl), lvl, type.droppedToken().getColor());
                });
                tItem.addLoreString("setup.gui.gen_level.level.item_lore", true);
                int slot = 9 * (j / 7 + 1) + j % 7 + 1;
                translatableinv.setItem(slot, tItem);
            }

            if (page < sizes.size() - 1)
            {
                translatableinv.setItem(size.getSlots() - 1, ProfileMenus.getNextItem());
            }

            if (page > 0)
            {
                translatableinv.setItem(size.getSlots() - 9, ProfileMenus.getPreviousItem());
            }

            translatableinv.setItem(size.getSlots() - 5, ProfileMenus::getCloseItem);
            menu.addPage(translatableinv);
        }

        menu.sendMenuUpdate(true);
    }

    public static class Listener implements org.bukkit.event.Listener
    {
        private static void setupClick(InventoryClickEvent event, MenuPlayer ply, EwMenu menu)
        {
            MenuType mType = menu.getMenuType();
            ItemStack currItem = event.getCurrentItem();
            SerializingItems type = SerializingItems.getReferenceType(currItem);

            if (MenuType.SELECT_GENERATOR.equals(mType))
            {
                if (SerializingItems.OPEN_GEN_TYPE.equals(type))
                {
                    GeneratorType genType = SerializingItems.OPEN_GEN_TYPE.getItemReference(currItem);

                    if (genType != null)
                    {
                        SetupGUI.openGeneratorLevels(ply, genType);
                    }

                    return;
                }
            }
            else if (MenuType.SELECT_GENERATOR_LEVEL.equals(mType))
            {
                if (SerializingItems.PLACE_GEN.equals(type))
                {
                    ply.getPlayer().getInventory().addItem(currItem);
                    return;
                }
            }

            Arena arena = EggWars.getArenaManager().getArenaByWorld(ply.getPlayer().getWorld());

            if (!MenuType.isSetupMenu(mType) || arena == null || arena.getStatus() != ArenaStatus.SETTING)
            {
                return;
            }

            if (SerializingItems.OPEN_MENU.equals(type))
            {
                MenuType openType = SerializingItems.OPEN_MENU.getItemReference(currItem);

                if (MenuType.BASIC_SETTINGS.equals(openType))
                {
                    arena.getSetupGUI().openBasicSetupGUI(ply);
                    return;
                }

                if (MenuType.SETUP_TEAMS.equals(openType))
                {
                    arena.getSetupGUI().openTeamsSetupGUI(ply);
                    return;
                }

                if (MenuType.SELECT_GENERATOR.equals(openType))
                {
                    SetupGUI.openGeneratorTypes(ply);
                    return;
                }
            }

            if (MenuType.SETUP_TEAMS.equals(mType))
            {
                if (SerializingItems.SETUP_TEAM.equals(type))
                {
                    TeamType teamtype = SerializingItems.SETUP_TEAM.getItemReference(currItem);

                    if (teamtype != null)
                    {
                        Team team = arena.getTeams().get(teamtype);
                        event.setCurrentItem(null);

                        if (team == null)
                        {
                            arena.addTeam(teamtype);
                            arena.updateSetupTeam(teamtype);
                            TranslationUtils.sendMessage("commands.addTeam.success", ply.getPlayer(), teamtype.id());
                            return;
                        }
                        else if (event.isShiftClick())
                        {
                            arena.removeTeam(teamtype);
                            arena.updateSetupTeam(teamtype);
                            TranslationUtils.sendMessage("commands.removeTeam.success", ply.getPlayer(), teamtype.id());
                            return;
                        }

                        arena.getSetupGUI().openSingleTeamSetupGUI(ply, teamtype);
                    }

                    return;
                }

                return;
            }
            else if (MenuType.BASIC_SETTINGS.equals(mType))
            {
                Player player = ply.getPlayer();
                boolean flag = false;

                if (event.getRawSlot() == 4)
                {
                    arena.sendToDo(player);
                }

                if (event.getRawSlot() == 10)
                {
                    if (tryTpRightClick(event, arena.getLobby()))
                    {
                        return;
                    }

                    arena.setLobby(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setWaitingLobby.success", player, arena.getName());
                    flag = true;
                }

                if (event.getRawSlot() == 12)
                {
                    if (tryTpRightClick(event, arena.getCenter()))
                    {
                        return;
                    }

                    arena.setCenter(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setCenter.success", player, arena.getName());
                    flag = true;
                }

                Bounds bounds = arena.getBounds();

                if (event.getRawSlot() == 14)
                {
                    if (tryTpRightClick(event, bounds.getStart()))
                    {
                        return;
                    }

                    bounds.setStart(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (event.getRawSlot() == 15)
                {
                    if (tryTpRightClick(event, bounds.getEnd()))
                    {
                        return;
                    }

                    bounds.setEnd(player.getLocation().clone());
                    TranslationUtils.sendMessage("commands.setBounds.success", player, arena.getName());
                    flag = true;
                }

                if (event.getRawSlot() == 16 && event.isShiftClick() && (bounds.getStart() != null || bounds.getEnd() != null))
                {
                    bounds.setBounds(null, null);
                    TranslationUtils.sendMessage("commands.setBounds.success.removed", player, arena.getName());
                    flag = true;
                }

                if (flag)
                {
                    event.setCurrentItem(null);
                    arena.getSetupGUI().updateBasicsMenu();
                }

                return;
            }
            else if (MenuType.SETUP_SINGLE_TEAM.equals(mType))
            {
                Player player = ply.getPlayer();
                TeamType teamtype = (TeamType)menu.getExtraData()[0];
                Team team = arena.getTeams().get(teamtype);
                boolean flag = false;

                if (event.getRawSlot() == 10)
                {
                    if (tryTpRightClick(event, team.getVillager()))
                    {
                        return;
                    }

                    team.setVillager(player.getLocation());
                    TranslationUtils.sendMessage("commands.setTeamVillager.success", player, team.getType().id());
                    flag = true;
                }

                if (event.getRawSlot() == 12)
                {
                    if (event.isShiftClick())
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

                if (event.getRawSlot() == 14)
                {
                    if (tryTpRightClick(event, team.getRespawn()))
                    {
                        return;
                    }

                    team.setRespawn(player.getLocation());
                    TranslationUtils.sendMessage("commands.setTeamRespawn.success", player, team.getType().id());
                    flag = true;
                }

                if (event.getRawSlot() == 16)
                {
                    if (tryTpRightClick(event, team.getEgg()))
                    {
                        return;
                    }

                    team.setEgg(player.getLocation());
                    TranslationUtils.sendMessage("commands.setTeamEgg.success", player, team.getType().id());
                    flag = true;
                }

                if (flag)
                {
                    event.setCurrentItem(null);
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

                String arenaId = ItemUtils.arenaId.getFrom(stack.getItemMeta());

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
                if (!event.getPlayer().hasPermission("eggwars.genSign.place") || !SerializingItems.PLACE_GEN.equals(SerializingItems.getReferenceType(stack)))
                {
                    return;
                }

                Pair<String, Integer> pair = SerializingItems.PLACE_GEN.getItemReference(stack);
                Map<String, GeneratorType> generators = EggWars.getGeneratorManager().getGenerators();

                if (pair != null && generators.containsKey(pair.getLeft()) && pair.getRight() <= generators.get(pair.getLeft()).getMaxLevel())
                {
                    event.setCancelled(true);
                    Generator generator = new Generator(event.getClickedBlock().getLocation(), pair.getRight(), pair.getLeft(), arena);
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
                clicker.closeInventory();
                clicker.teleport(Locations.toMiddle(loc));
                return true;
            }

            return false;
        }
    }
}
