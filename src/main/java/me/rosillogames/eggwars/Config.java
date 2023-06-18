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
import org.bukkit.configuration.file.FileConfiguration;
import me.rosillogames.eggwars.dependencies.DependencyUtils;
import me.rosillogames.eggwars.enums.Versions;
import me.rosillogames.eggwars.loaders.ArenaLoader;
import me.rosillogames.eggwars.loaders.GeneratorLoader;
import me.rosillogames.eggwars.loaders.KitLoader;
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
    public boolean skipSoloLobby = true;
    public boolean showKills = true;
    public boolean balanceTeams = false;
    public boolean useBelowBlock = true;
    //Version check for APSS is for an issue with item.setThrower(UUID) not being in early 1.16
    public boolean enableAPSS = true;
    public boolean moveTNTOnIgnite = true;
    public boolean keepInv = false;
    public boolean publicSpectChat = true;
    public int respawnDelay = 4;
    public int finishingTime = 10;
    public int invincibilityTime = 10;
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

        fileConf.addDefault("gameplay.balance_teams", false);
        fileConf.addDefault("gameplay.skip_solo_lobby", true);
        fileConf.addDefault("gameplay.show_kills", true);
        fileConf.addDefault("gameplay.drop_blocks", false);
        fileConf.addDefault("gameplay.keep_inventory", false);
        fileConf.addDefault("gameplay.respawn_delay", 4);
        fileConf.addDefault("gameplay.invincible_time", 10);
        fileConf.addDefault("gameplay.move_tnt_on_ignition", true);
        fileConf.addDefault("gameplay.finishing_time", 10);
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
        fileConf.addDefault("gameplay.breakable_blocks", list);

        fileConf.addDefault("gameplay.points.multiplier", 1.0D);
        fileConf.addDefault("gameplay.points.on_kill", 1);
        fileConf.addDefault("gameplay.points.on_final_kill", 3);
        fileConf.addDefault("gameplay.points.on_win", 50);
        fileConf.addDefault("gameplay.points.on_egg", 6);

        fileConf.addDefault("kits.cooldown_time", 120);

        fileConf.addDefault("generator.fast_items", false);
        fileConf.addDefault("generator.use_below_block", true);
        fileConf.addDefault("generator.enable_apss", true);

        fileConf.addDefault("spectator.can_stay_at_game", true);
        fileConf.addDefault("spectator.can_enter_ingame", false);
        fileConf.addDefault("spectator.public_chat", true);

        fileConf.addDefault("lobby.location", "null");
        fileConf.addDefault("lobby.sign_status.active", true);
        fileConf.addDefault("lobby.sign_status.lobby", "{\"Name\":\"minecraft:lime_stained_glass\"}");
        fileConf.addDefault("lobby.sign_status.starting", "{\"Name\":\"minecraft:yellow_stained_glass\"}");
        fileConf.addDefault("lobby.sign_status.ingame", "{\"Name\":\"minecraft:red_stained_glass\"}");
        fileConf.addDefault("lobby.sign_status.finished", "{\"Name\":\"minecraft:magenta_stained_glass\"}");
        fileConf.addDefault("lobby.sign_status.setting", "{\"Name\":\"minecraft:cyan_stained_glass\"}");

        fileConf.addDefault("inventory.generator_upgrading", "{\"id\":\"minecraft:experience_bottle\"}");
        fileConf.addDefault("inventory.kit_selection.item", "{\"id\":\"minecraft:paper\"}");
        fileConf.addDefault("inventory.kit_selection.slot", 1);
        fileConf.addDefault("inventory.kit_selection.slot_in_cage", 0);
        fileConf.addDefault("inventory.team_selection.item", "{\"id\":\"minecraft:nether_star\"}");
        fileConf.addDefault("inventory.team_selection.slot", 0);
        fileConf.addDefault("inventory.leave.item", "{\"id\":\"minecraft:red_bed\"}");
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
        createDeathMsgSection(map, "CONTACT", "generic");
        createDeathMsgSection(map, "ENTITY_ATTACK", "slain");
        createDeathMsgSection(map, "ENTITY_SWEEP_ATTACK", "slain");
        createDeathMsgSection(map, "PROJECTILE", "shot");
        createDeathMsgSection(map, "SUFFOCATION", "suffocated");
        createDeathMsgSection(map, "FALL", "couldnt_fly", "fell_to_death");
        createDeathMsgSection(map, "FIRE", "burnt_to_crisp", "burned_to_death");
        createDeathMsgSection(map, "FIRE_TICK", "burnt_to_crisp", "burned_to_death");
        createDeathMsgSection(map, "MELTING", "generic");
        createDeathMsgSection(map, "LAVA", "swim_in_lava");
        createDeathMsgSection(map, "DROWNING", "drowned");
        createDeathMsgSection(map, "BLOCK_EXPLOSION", "blown_up");
        createDeathMsgSection(map, "ENTITY_EXPLOSION", "blown_up");
        createDeathMsgSection(map, "VOID", "died_in_void", "thought_void");
        createDeathMsgSection(map, "LIGHTNING", "generic");
        createDeathMsgSection(map, "SUICIDE", "generic");
        createDeathMsgSection(map, "STARVATION", "starved");
        createDeathMsgSection(map, "POISON", "poison");
        createDeathMsgSection(map, "MAGIC", "magic");
        createDeathMsgSection(map, "WITHER", "generic");
        createDeathMsgSection(map, "FALLING_BLOCK", "generic");
        createDeathMsgSection(map, "THORNS", "slain");
        createDeathMsgSection(map, "DRAGON_BREATH", "generic");
        createDeathMsgSection(map, "CUSTOM", "generic");
        createDeathMsgSection(map, "FLY_INTO_WALL", "generic");
        createDeathMsgSection(map, "HOT_FLOOR", "generic");
        createDeathMsgSection(map, "CRAMMING", "generic");
        createDeathMsgSection(map, "DRYOUT", "generic");
        createDeathMsgSection(map, "FREEZE", "generic");
        createDeathMsgSection(map, "SONIC_BOOM", "generic");
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

        EggWars.instance.saveConfig();
        EggWars.instance.reloadConfig();
        EggWars.bungee.loadConfig(fileConf);
        this.pointMultiplier = (float)fileConf.getDouble("gameplay.points.multiplier");
        this.checkUpdates = fileConf.getBoolean("plugin.check_updates");
        this.canSpectStay = fileConf.getBoolean("spectator.can_stay_at_game");
        this.canSpectJoin = fileConf.getBoolean("spectator.can_enter_ingame");
        this.publicSpectChat = fileConf.getBoolean("spectator.public_chat");
        this.respawnDelay = fileConf.getInt("gameplay.respawn_delay");
        this.hidePlayers = fileConf.getBoolean("plugin.hide_players");
        this.alwaysTpToLobby = fileConf.getBoolean("plugin.always_teleport_to_lobby");
        this.moveTNTOnIgnite = fileConf.getBoolean("gameplay.move_tnt_on_ignition");
        this.useBelowBlock = fileConf.getBoolean("generator.use_below_block");
        this.enableAPSS = fileConf.getBoolean("generator.enable_apss") && EggWars.serverVersion.ordinal() >= Versions.V_1_16_R3.ordinal();
        this.vault = fileConf.getBoolean("plugin.vault") && DependencyUtils.vault();
        this.balanceTeams = fileConf.getBoolean("gameplay.balance_teams");
        this.skipSoloLobby = fileConf.getBoolean("gameplay.skip_solo_lobby");
        this.showKills = fileConf.getBoolean("gameplay.show_kills");
        this.keepInv = fileConf.getBoolean("gameplay.keep_inventory");
        this.finishingTime = fileConf.getInt("gameplay.finishing_time");
        this.invincibilityTime = fileConf.getInt("gameplay.invincible_time");
        EggWars.languageManager().loadConfig();
        LobbySigns.loadConfig();
        TeamUtils.loadConfig();
        KitLoader.loadConfig();
        ArenaLoader.loadConfig();
        VoteUtils.loadConfig();
        GeneratorLoader.loadConfig();
        this.breakableBlocks.clear();

        for (String s2 : fileConf.getStringList("gameplay.breakable_blocks"))
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

    private static void createDeathMsgSection(Map map, String cause, String... strings)
    {
        map.put("death_message_keys." + cause, strings);
    }
}
