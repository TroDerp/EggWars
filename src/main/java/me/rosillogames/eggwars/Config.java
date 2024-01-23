package me.rosillogames.eggwars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.rosillogames.eggwars.dependencies.DependencyUtils;
import me.rosillogames.eggwars.enums.ModeOption;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.managers.ArenaManager;
import me.rosillogames.eggwars.managers.GeneratorManager;
import me.rosillogames.eggwars.managers.KitManager;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.LobbySigns;
import me.rosillogames.eggwars.utils.Locations;
import me.rosillogames.eggwars.utils.TeamUtils;
import me.rosillogames.eggwars.utils.VoteUtils;

public class Config
{
    public boolean checkUpdates = true;
    public boolean hidePlayers = true;
    public boolean alwaysTpToLobby = true;
    public boolean vault = false;
    public ModeOption skipsLobby = ModeOption.SOLO;
    public boolean showKills = true;
    public boolean balanceTeams = false;
    public boolean shareTeamEC = false;
    public boolean useBelowBlock = true;
    //Version check for APSS is for an issue with item.setThrower(UUID) not being in early 1.16
    public boolean enableAPSS = true;
    public boolean keepInv = false;
    public boolean dropInv = false;
    public boolean publicSpectChat = true;
    public boolean bestAssistIsKiller = false;
    public ModeOption enableAssists = ModeOption.TEAM;
    public int dmgForgetTime = 20;
    public int respawnDelay = 4;
    public int finishingTime = 10;
    public int invincibleTime = 10;
    @Nullable
    public Location lobby = null;
    public Set<Material> breakableBlocks = new HashSet();
    public float pointMultiplier = 1.0F;
    public boolean canSpectJoin = false;
    public boolean canSpectStay = true;

    @SuppressWarnings("deprecation")
    public void loadConfig()
    {
        EggWars.instance.reloadConfig();
        FileConfiguration fileConf = EggWars.instance.getConfig();
        boolean converted = false;
        boolean converted2 = false;
        boolean converted3 = false;
        boolean converted4 = false;

        //TODO: remove these in next version
        if (fileConf.isConfigurationSection("gameplay"))
        {
            ConfigurationSection cs = fileConf.getConfigurationSection("gameplay");
            fileConf.addDefault("game", cs);
            fileConf.set("gameplay", null);
            convertOldToNewKey(fileConf, "game.keep_inventory", "game.player.keep_inventory");
            convertOldToNewKey(fileConf, "game.respawn_delay", "game.player.respawn_delay");
            convertOldToNewKey(fileConf, "game.invincible_time", "game.player.invincible_time");
            convertOldToNewKey(fileConf, "game.move_tnt_on_ignition", "game.tnt.move_when_ignited");
            converted = true;
        }

        if (fileConf.contains("lobby.sign_status.lobby"))
        {
            convertOldToNewKey(fileConf, "lobby.sign_status.lobby", "lobby.sign_status.waiting");
            converted2 = true;
        }

        if (fileConf.contains("lobby.sign_status.ingame"))
        {
            convertOldToNewKey(fileConf, "lobby.sign_status.ingame", "lobby.sign_status.in_game");
            converted3 = true;
        }

        if (fileConf.contains("lobby.sign_status.finished"))
        {
            convertOldToNewKey(fileConf, "lobby.sign_status.finished", "lobby.sign_status.finishing");
            converted4 = true;
        }

        fileConf.options().header("###########################################\n#               - EggWars -               #\n#         - By gaelitoelquesito -         #\n#      - Remastered by RosilloGames -     #\n###########################################\n");

        fileConf.addDefault("plugin.version", EggWars.EGGWARS_VERSION);
        fileConf.addDefault("plugin.check_updates", true);
        fileConf.addDefault("plugin.language", "en_us");
        fileConf.addDefault("plugin.ignore_client_language", false);
        fileConf.addDefault("plugin.vault", false);
        fileConf.addDefault("plugin.hide_players", true);
        fileConf.addDefault("plugin.always_teleport_to_lobby", true);
        //legacy option, it regenerates the arena's world when the game finishes
        fileConf.addDefault("plugin.regenerate_worlds", false);

        EggWars.bungee.addConfigDefaults(fileConf);

        fileConf.addDefault("database.auto_mode", true);
        fileConf.addDefault("database.url", "jdbc:mysql://{IP}:{PORT}/{DATABASENAME}?useSSL=false");
        fileConf.addDefault("database.username", "bukkit");
        fileConf.addDefault("database.password", "walrus");

        fileConf.addDefault("game.balance_teams", false);
        String skipSoloLobby = "game.skip_solo_lobby";

        if (fileConf.contains(skipSoloLobby))
        {
            fileConf.addDefault("game.skip_lobby", (fileConf.getBoolean(skipSoloLobby) ? ModeOption.SOLO : ModeOption.NONE).toString());
            fileConf.addDefault(skipSoloLobby, null);
        }
        else
        {
            fileConf.addDefault("game.skip_lobby", ModeOption.SOLO.toString());
        }

        fileConf.addDefault("game.skip_solo_lobby", true);
        fileConf.addDefault("game.show_kills", true);
        fileConf.addDefault("game.drop_blocks", false);
        fileConf.addDefault("game.share_team_ender_chest", false);
        fileConf.addDefault("game.finishing_time", 10);
        fileConf.addDefault("game.player.drop_inventory", false);

        if (!converted)
        {
            fileConf.addDefault("game.player.keep_inventory", false);
            fileConf.addDefault("game.player.respawn_delay", 4);
            fileConf.addDefault("game.player.invincible_time", 10);
        }

        fileConf.addDefault("game.player.allow_starving", false);
        fileConf.addDefault("game.assists.enable", ModeOption.TEAM.toString());
        fileConf.addDefault("game.assists.grant_kill_to_best", false);
        fileConf.addDefault("game.assists.forget_time", 20);
        fileConf.addDefault("game.tnt.auto_ignite", true);

        if (!converted)
        {
            fileConf.addDefault("game.tnt.move_when_ignited", true);
        }

        fileConf.addDefault("game.tnt.strenght", 3.0D);
        fileConf.addDefault("game.tnt.fuse_ticks", 80);
        List<String> list = new ArrayList();
        list.add("minecraft:fern");
        list.add("minecraft:grass");
        list.add("minecraft:large_fern");
        list.add("minecraft:tall_grass");
        list.add("minecraft:dead_bush");
        list.add("minecraft:crimson_roots");
        list.add("minecraft:warped_roots");
        list.add("minecraft:nether_sprouts");
        list.add("minecraft:dandelion");
        list.add("minecraft:poppy");
        list.add("minecraft:blue_orchid");
        list.add("minecraft:allium");
        list.add("minecraft:azure_bluet");
        list.add("minecraft:red_tulip");
        list.add("minecraft:orange_tulip");
        list.add("minecraft:white_tulip");
        list.add("minecraft:pink_tulip");
        list.add("minecraft:oxeye_daisy");
        list.add("minecraft:cornflower");
        list.add("minecraft:lily_of_the_valley");
        list.add("minecraft:wither_rose");
        list.add("minecraft:sunflower");
        list.add("minecraft:lilac");
        list.add("minecraft:rose_bush");
        list.add("minecraft:peony");
        list.add("minecraft:snow");
        fileConf.addDefault("game.breakable_blocks", list);

        fileConf.addDefault("game.points.multiplier", 1.0D);
        fileConf.addDefault("game.points.on_kill", 1);
        fileConf.addDefault("game.points.on_final_kill", 3);
        fileConf.addDefault("game.points.on_win", 50);
        fileConf.addDefault("game.points.on_egg", 6);

        fileConf.addDefault("kits.cooldown_time", 120);

        fileConf.addDefault("generator.fast_items", false);
        fileConf.addDefault("generator.use_below_block", true);
        fileConf.addDefault("generator.enable_apss", true);

        fileConf.addDefault("spectator.can_stay_at_game", true);
        fileConf.addDefault("spectator.can_enter_ingame", false);
        fileConf.addDefault("spectator.public_chat", true);

        fileConf.addDefault("lobby.location", "null");
        fileConf.addDefault("lobby.sign_status.active", true);

        if (!converted2)
        {
            fileConf.addDefault("lobby.sign_status.waiting", "{\"Name\":\"minecraft:lime_stained_glass\"}");
        }

        fileConf.addDefault("lobby.sign_status.starting", "{\"Name\":\"minecraft:yellow_stained_glass\"}");

        if (!converted3)
        {
            fileConf.addDefault("lobby.sign_status.in_game", "{\"Name\":\"minecraft:red_stained_glass\"}");
        }

        fileConf.addDefault("lobby.sign_status.setting", "{\"Name\":\"minecraft:cyan_stained_glass\"}");

        if (!converted4)
        {
            fileConf.addDefault("lobby.sign_status.finishing", "{\"Name\":\"minecraft:magenta_stained_glass\"}");
        }

        fileConf.addDefault("inventory.generator_upgrading", "{\"id\":\"minecraft:experience_bottle\"}");
        fileConf.addDefault("inventory.kit_selection.item", "{\"id\":\"minecraft:paper\"}");
        fileConf.addDefault("inventory.kit_selection.slot", 1);
        fileConf.addDefault("inventory.kit_selection.slot_in_cage", 0);
        fileConf.addDefault("inventory.team_selection.item", "{\"id\":\"minecraft:nether_star\"}");
        fileConf.addDefault("inventory.team_selection.slot", 0);
        fileConf.addDefault("inventory.leave.item", "{\"id\":\"minecraft:red_dye\"}");
        fileConf.addDefault("inventory.leave.slot", 8);
        fileConf.addDefault("inventory.voting.item", "{\"id\":\"minecraft:end_crystal\"}");
        fileConf.addDefault("inventory.voting.slot", 2);
        fileConf.addDefault("inventory.voting.slot_in_cage", 1);
        fileConf.addDefault("inventory.voting_items", "{\"id\":\"minecraft:villager_spawn_egg\"}");
        fileConf.addDefault("inventory.voting_hardcore_items", "{\"id\":\"minecraft:wooden_sword\"}");
        fileConf.addDefault("inventory.voting_normal_items", "{\"id\":\"minecraft:stone_sword\"}");
        fileConf.addDefault("inventory.voting_overpowered_items", "{\"id\":\"minecraft:diamond_sword\"}");
        fileConf.addDefault("inventory.voting_health", "{\"id\":\"minecraft:poppy\"}");
        fileConf.addDefault("inventory.voting_half_health", "{\"id\":\"minecraft:bowl\"}");
        fileConf.addDefault("inventory.voting_normal_health", "{\"id\":\"minecraft:glass_bottle\"}");
        fileConf.addDefault("inventory.voting_double_health", "{\"id\":\"minecraft:potion\"}");
        fileConf.addDefault("inventory.voting_triple_health", "{\"id\":\"minecraft:experience_bottle\"}");
        Map<String, Object> map = new HashMap();
        addMsgsToMap(map, "CONTACT", "generic");
        addMsgsToMap(map, "ENTITY_ATTACK", "slain");
        addMsgsToMap(map, "ENTITY_SWEEP_ATTACK", "slain");
        addMsgsToMap(map, "PROJECTILE", "shot");
        addMsgsToMap(map, "SUFFOCATION", "suffocated");
        addMsgsToMap(map, "FALL", "couldnt_fly", "fell_to_death");
        addMsgsToMap(map, "FIRE", "burnt_to_crisp", "burned_to_death");
        addMsgsToMap(map, "FIRE_TICK", "burnt_to_crisp", "burned_to_death");
        addMsgsToMap(map, "MELTING", "generic");
        addMsgsToMap(map, "LAVA", "swim_in_lava");
        addMsgsToMap(map, "DROWNING", "drowned");
        addMsgsToMap(map, "BLOCK_EXPLOSION", "blown_up");
        addMsgsToMap(map, "ENTITY_EXPLOSION", "blown_up");
        addMsgsToMap(map, "VOID", "died_in_void", "thought_void");
        addMsgsToMap(map, "LIGHTNING", "generic");
        addMsgsToMap(map, "SUICIDE", "generic");
        addMsgsToMap(map, "STARVATION", "starved");
        addMsgsToMap(map, "POISON", "poison");
        addMsgsToMap(map, "MAGIC", "magic");
        addMsgsToMap(map, "WITHER", "generic");
        addMsgsToMap(map, "FALLING_BLOCK", "generic");
        addMsgsToMap(map, "THORNS", "slain");
        addMsgsToMap(map, "DRAGON_BREATH", "generic");
        addMsgsToMap(map, "CUSTOM", "generic");
        addMsgsToMap(map, "FLY_INTO_WALL", "generic");
        addMsgsToMap(map, "HOT_FLOOR", "generic");
        addMsgsToMap(map, "CRAMMING", "generic");
        addMsgsToMap(map, "DRYOUT", "generic");
        addMsgsToMap(map, "FREEZE", "generic");
        addMsgsToMap(map, "SONIC_BOOM", "generic");
        fileConf.addDefaults((Map<String, Object>)map);
        fileConf.options().copyDefaults(true);
        String s1 = fileConf.getString("plugin.version");
        s1 = ((s1 == null || s1.isEmpty()) ? "unknown" : s1);

        if (!s1.equals(EggWars.EGGWARS_VERSION))
        {
            //EggWars.instance.getLogger().log(Level.WARNING, "Opening config from EggWars version " + s1 + "! (this version: " + EggWars.EGGWARS_VERSION + ") This may cause some problems on your configuration. I recommend to remove /langs/ and /custom/ folders to update them.");
            fileConf.set("plugin.version", EggWars.EGGWARS_VERSION);

            if (fileConf.contains("database.useSSL") && !fileConf.getString("database.url").contains("useSSL="))
            {
                fileConf.set("database.url", fileConf.getString("database.url") + "?useSSL=" + fileConf.getBoolean("database.useSSL"));
                fileConf.set("database.useSSL", null);
            }
        }

        if (fileConf.isConfigurationSection("gameplay"))
        {
            ConfigurationSection cs = fileConf.getConfigurationSection("gameplay");
            fileConf.set("game", cs);
            fileConf.set("gameplay", null);
        }

        EggWars.instance.saveConfig();
        EggWars.instance.reloadConfig();
        EggWars.bungee.loadConfig(fileConf);
        this.pointMultiplier = (float)fileConf.getDouble("game.points.multiplier");
        this.checkUpdates = fileConf.getBoolean("plugin.check_updates");
        this.canSpectStay = fileConf.getBoolean("spectator.can_stay_at_game");
        this.canSpectJoin = fileConf.getBoolean("spectator.can_enter_ingame");
        this.publicSpectChat = fileConf.getBoolean("spectator.public_chat");
        this.bestAssistIsKiller = fileConf.getBoolean("game.assists.grant_kill_to_best");
        this.enableAssists = ModeOption.getOrDefault(fileConf.getString("game.assists.enable"), ModeOption.TEAM);
        this.dmgForgetTime = fileConf.getInt("game.assists.forget_time");
        this.respawnDelay = fileConf.getInt("game.player.respawn_delay");
        this.hidePlayers = fileConf.getBoolean("plugin.hide_players");
        this.alwaysTpToLobby = fileConf.getBoolean("plugin.always_teleport_to_lobby");
        this.useBelowBlock = fileConf.getBoolean("generator.use_below_block");
        this.enableAPSS = fileConf.getBoolean("generator.enable_apss") && EggWars.serverVersion.ordinal() >= Versions.V_1_16_R3.ordinal();
        this.vault = fileConf.getBoolean("plugin.vault") && DependencyUtils.vault();
        this.balanceTeams = fileConf.getBoolean("game.balance_teams");
        this.shareTeamEC = fileConf.getBoolean("game.share_team_ender_chest");
        this.skipsLobby = ModeOption.getOrDefault(fileConf.getString("game.skip_lobby"), ModeOption.SOLO);
        this.showKills = fileConf.getBoolean("game.show_kills");
        this.dropInv = fileConf.getBoolean("game.player.drop_inventory");
        this.keepInv = fileConf.getBoolean("game.player.keep_inventory");
        this.finishingTime = fileConf.getInt("game.finishing_time");
        this.invincibleTime = fileConf.getInt("game.player.invincible_time");
        EggWars.languageManager().loadConfig();
        LobbySigns.loadConfig();
        TeamUtils.loadConfig();
        KitManager.loadConfig();
        ArenaManager.loadConfig();
        VoteUtils.loadConfig();
        GeneratorManager.loadConfig();

        this.breakableBlocks.clear();

        for (String s2 : fileConf.getStringList("game.breakable_blocks"))
        {
            BlockData blockdata = ItemUtils.getBlockData(String.format("{\"Name\":\"%s\"}", s2), null);

            if (blockdata != null)
            {
                this.breakableBlocks.add(blockdata.getMaterial());
            }
        }

        try
        {
            this.lobby = Locations.fromStringWithWorld(fileConf.getString("lobby.location"));
        }
        catch (Exception exception)
        {
            this.lobby = null;
        }
    }

    public void setMainLobby(Location location)
    {
        this.lobby = location;
        //Keep LocationSerializer.toStringNew ??
        EggWars.instance.getConfig().set("lobby.location", Locations.toStringWithWorld(location, true));
        EggWars.instance.saveConfig();
    }

    private static void addMsgsToMap(Map map, String key, String... values)
    {
        map.put("death_message_keys." + key, values);
    }

    private static void convertOldToNewKey(FileConfiguration conf, String oldKey, String newKey)
    {
        conf.addDefault(newKey, conf.get(oldKey));
        conf.addDefault(oldKey, null);
    }
}
