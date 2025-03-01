package me.rosillogames.eggwars.menu;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.rosillogames.eggwars.arena.Arena;
import me.rosillogames.eggwars.arena.Team;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.language.TranslationUtils;
import me.rosillogames.eggwars.player.EwPlayer;
import me.rosillogames.eggwars.player.inventory.TranslatableItem;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.reflection.ReflectionUtils;

public class TeamsMenu extends EwMenu
{
    private final Map<TeamType, ItemStack> teamItems = Maps.<TeamType, ItemStack>newEnumMap(TeamType.class);
    private ItemStack randomItem;
    private int size;
    private boolean initialized = false;//dirtySlots ??

    public TeamsMenu()
    {
        super(MenuType.TEAM_SELECTION);
    }

    public void reset(Arena arena)
    {
        this.teamItems.clear();
        this.randomItem = new ItemStack(Material.NETHER_STAR);
        SerializingItems.JOIN_TEAM.setItemReference(this.randomItem, null);
        int i = 0;

        for (Team team : arena.getTeams().values())
        {
            ItemStack item = ItemUtils.tryColorizeByTeam(team.getType(), new ItemStack(Material.WHITE_WOOL, 1));
            SerializingItems.JOIN_TEAM.setItemReference(item, team.getType());
            this.teamItems.put(team.getType(), item);
            i++;
        }

        this.size = (int)Math.ceil((double)(i + 1) / 9.0D) * 9;
        this.initialized = true;
    }

    private void playerChangeTeam(EwPlayer player, @Nullable Team team)
    {
        if (!this.initialized)
        {
            return;
        }

        Team oldTeam = player.getTeam();

        if (team == null && oldTeam != null)
        {
            TranslationUtils.sendMessage("gameplay.teams.random", player.getPlayer());
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);
            oldTeam.removePlayer(player);
            this.updateTeamItem(oldTeam);
        }
        else if (team != null)
        {
            if (oldTeam != null && oldTeam.equals(team))
            {
                TranslationUtils.sendMessage("gameplay.teams.already_in", player.getPlayer());
                return;
            }

            if (!team.canJoin())
            {
                TranslationUtils.sendMessage("gameplay.teams.full", player.getPlayer());
                return;
            }

            String teamName = TeamUtils.translateTeamType(team.getType(), player.getPlayer(), false);
            TranslationUtils.sendMessage("gameplay.teams.joined", player.getPlayer(), teamName);
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 100F, 100F);

            if (oldTeam != null)
            {
                oldTeam.removePlayer(player);
                this.updateTeamItem(oldTeam);
            }

            team.addPlayer(player);
            this.updateTeamItem(team);
        }

        this.sendMenuUpdate(false);
    }

    public void updateTeamItem(Team team)
    {
        int size = team.getPlayers().size();
        this.teamItems.get(team.getType()).setAmount(size <= 1 ? 1 : size);
    }

    @Nullable
    @Override
    public Inventory translateToPlayer(EwPlayer player, boolean reopen)
    {//TODO Reduce repetitions by adding new static/super methods
        if (!this.initialized)
        {
            return null;
        }

        Inventory mcInventory;

        if (player.getMenu() == this && !reopen)
        {
            mcInventory = this.openers.get(player);
        }
        else
        {
            mcInventory = Bukkit.createInventory(null, this.size, TranslationUtils.getMessage("teams.menu_title", player.getPlayer()));
        }

        int i = 0;

        for (TeamType type : this.teamItems.keySet())
        {
            ItemStack stack = this.teamItems.get(type).clone();
            Team team = player.getArena().getTeams().get(type);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(TranslationUtils.getMessage("teams.team.item_name", player.getPlayer(), TeamUtils.translateTeamType(type, player.getPlayer(), false), team.getPlayers().size(), player.getArena().getMaxTeamPlayers()));
            List<String> tLore = Lists.<String>newArrayList();

            for (EwPlayer teampl : team.getPlayers())
            {
                tLore.addAll(Arrays.asList(TranslationUtils.getMessage("teams.team.item_lore_entry", player.getPlayer(), teampl.getPlayer().getDisplayName()).split("\\n")));
            }

            meta.setLore(tLore);
            stack.setItemMeta(meta);
            
            if (team.equals(player.getTeam()))
            {
                ReflectionUtils.setEnchantGlint(stack, true, true);
            }

            mcInventory.setItem(i, stack);
            i++;
        }

        ItemStack randTeam = TranslatableItem.fullyTranslate(this.randomItem, "teams.random.item_name", "teams.random.item_lore", player.getPlayer());

        if (player.getTeam() != null)
        {
            ReflectionUtils.setEnchantGlint(randTeam, false, true);
        }

        mcInventory.setItem(this.size - 1, randTeam);
        return mcInventory;
    }

    @Override
    public void clickInventory(InventoryClickEvent clickEvent, EwPlayer player)
    {//TODO Reduce repetitions by adding new static/super methods
        if (!this.initialized || !player.getArena().getStatus().isLobby())
        {
            return;
        }

        ItemStack currItem = clickEvent.getCurrentItem();
        SerializingItems type = SerializingItems.getReferenceType(currItem);

      //clickEvent.setCurrentItem(null);//When re-opening inventory, it's is clever to set item to null right BEFORE that happens, Important note, use this ONLY and ONLY when we are sure the inventory will FOR SURE, be RE-OPENED.

        if (SerializingItems.JOIN_TEAM.equals(type))
        {
            this.playerChangeTeam(player, player.getArena().getTeams().get(type.getItemReference(currItem)));
        }
    }
}
