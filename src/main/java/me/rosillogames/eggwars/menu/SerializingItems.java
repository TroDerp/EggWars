package me.rosillogames.eggwars.menu;

import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.google.common.collect.Maps;
import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.arena.GeneratorType;
import me.rosillogames.eggwars.enums.HealthType;
import me.rosillogames.eggwars.enums.ItemType;
import me.rosillogames.eggwars.enums.MenuType;
import me.rosillogames.eggwars.enums.TeamType;
import me.rosillogames.eggwars.objects.Kit;
import me.rosillogames.eggwars.utils.EwNamespace;
import me.rosillogames.eggwars.utils.ItemUtils;
import me.rosillogames.eggwars.utils.Pair;

public class SerializingItems<T /* the object we want to serialize */, D /* serialization result */>
{//TODO: Rename to ItemIdentifiers? I called this like that back when LEAVE_ARENA MenuType was added
    private static final Map<String, SerializingItems> LOOKUP = Maps.<String, SerializingItems>newHashMap();
    public static final SerializingItems<Void, Byte> EMPTY = new SerializingItems("empty");
    public static final SerializingItems<Void, Byte> CLOSE_OR_BACK = new SerializingItems("close_menu");
    public static final SerializingItems<Void, Byte> NEXT_PAGE = new SerializingItems("next_page");
    public static final SerializingItems<Void, Byte> PREVIOUS_PAGE = new SerializingItems("previous_page");
    public static final SerializingItems<Void, Byte> LEAVE_ARENA = new SerializingItems("leave_arena");
    //PropertyItemType.VOTE_ITEM_OR_HEALTH is now OPEN_MENU with type MenuType.ITEM_VOTING and MenuType.HEALTH_VOTING
    public static final SerializingItems<MenuType, String> OPEN_MENU = new SerializingItems<MenuType, String>("open_menu", PersistentDataType.STRING, mt -> mt.toString(), str -> MenuType.parse(str));
    public static final SerializingItems<String, String> SELECT_LANGUAGE = new SerializingItems<String, String>("select_language", PersistentDataType.STRING, str -> str, str -> str);
    public static final SerializingItems<ItemType, String> VOTE_ITEM_TYPE = new SerializingItems<ItemType, String>("vote_item", PersistentDataType.STRING, it -> it.name(), str -> ItemType.valueOf(str));
    public static final SerializingItems<HealthType, String> VOTE_HEALTH_TYPE = new SerializingItems<HealthType, String>("vote_health", PersistentDataType.STRING, ht -> ht.name(), str -> HealthType.valueOf(str));
    public static final SerializingItems<Kit, String> SELECT_KIT = new SerializingItems<Kit, String>("select_kit", PersistentDataType.STRING, kit -> kit.id(), str -> EggWars.getKitManager().getKit(str));
    public static final SerializingItems<TeamType, String> JOIN_TEAM = new SerializingItems<TeamType, String>("join_team", PersistentDataType.STRING, team -> team.id(), str -> 
    {
        try
        {
            return TeamType.byId(str);
        }
        catch (IllegalArgumentException exc)
        {
            return null;//no print because TeamMenu deals with nulls
        }
    });
    public static final SerializingItems<TeamType, String> SETUP_TEAM = new SerializingItems<TeamType, String>("setup_team", JOIN_TEAM.dataType, JOIN_TEAM.serializer, JOIN_TEAM.deserializer);
    public static final SerializingItems<Void, Byte> UPGRADE_GEN = new SerializingItems("upgrade_generator", PersistentDataType.INTEGER, num -> num, num -> num);
    public static final SerializingItems<GeneratorType, String> OPEN_GEN_TYPE = new SerializingItems<GeneratorType, String>("open_generator_type", PersistentDataType.STRING, typ -> typ.getId(), str -> EggWars.getGeneratorManager().getType(str));
    public static final SerializingItems<Pair<String, Integer>, PersistentDataContainer> PLACE_GEN = new SerializingItems<Pair<String, Integer>, PersistentDataContainer>("place_generator", PersistentDataType.TAG_CONTAINER, pair ->
    {
        PersistentDataContainer cont = (new ItemStack(Material.APPLE)).getItemMeta().getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
        ItemUtils.genType.setTo(cont, pair.getLeft());
        ItemUtils.genLevel.setTo(cont, pair.getRight());
        return cont;
    }, cont -> new Pair<String, Integer>(ItemUtils.genType.getFrom(cont), ItemUtils.genLevel.getFrom(cont)));
    /* two ints are the slot and page themselves */
    public static final SerializingItems<String, String> OPEN_CATEGORY = new SerializingItems<String, String>("open_category", PersistentDataType.STRING, str -> str, str -> str);
    /* integer is the slot itself */
    public static final SerializingItems<String, String> BUY_OFFER = new SerializingItems<String, String>("buy_offer", PersistentDataType.STRING, str -> str, str -> str);
    public static final SerializingItems<Void, Byte> CLASSIC_SHOP = new SerializingItems<Void, Byte>("toggle_classic_shop");
    public static EwNamespace<String> typeKey;
    public static NamespacedKey valueKey;
    private final String name;
    private final PersistentDataType<?, D> dataType;
    @Nullable
    private final Function<D, T> deserializer;
    @Nullable
    private final Function<T, D> serializer;

    private SerializingItems(String serName)
    {
        this.name = serName;
        this.dataType = null;
        this.deserializer = null;
        this.serializer = null;
        LOOKUP.put(serName, this);
    }

    private SerializingItems(String serName, PersistentDataType<?, D> data, Function<T, D> ser, Function<D, T> deser)
    {
        this.name = serName;
        this.dataType = data;
        this.deserializer = deser;
        this.serializer = ser;
        LOOKUP.put(serName, this);
    }

    /** Stores the item type and an the optional serialized object that is supposed to have **/
    public void setItemReference(ItemStack stack, @Nullable T object)
    {
        ItemMeta meta = stack.getItemMeta();
        typeKey.setTo(meta, this.name);

        if (object != null && this.serializer != null)
        {
            meta.getPersistentDataContainer().set(valueKey, this.dataType, this.serializer.apply(object));
        }
        else if (object != null)
        {
            throw new IllegalStateException("Can't add non-null reference to item if the type can't hold any values!");
        }

        stack.setItemMeta(meta);
    }

    /** Tries to retrieve the optionally stored object by its stored serialized value **/
    @Nullable
    public T getItemReference(ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();

        if (!this.equals(getReferenceType(stack)))
        {
            throw new IllegalStateException("Trying to get value from incorrect reference type. Perhaps we are doing this in the wrong order?");
        }
        else if (this.deserializer != null)
        {
            return this.deserializer.apply(meta.getPersistentDataContainer().get(valueKey, this.dataType));
        }

        return null;
    }

    @Nullable
    public static SerializingItems getReferenceType(ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();

        if (meta != null && typeKey.isInHolder(meta))
        {
            return LOOKUP.get(typeKey.getFrom(meta));
        }

        return null;
    }

    @Override
    public boolean equals(Object otr)
    {
        if (otr == null || this.getClass() != otr.getClass())
        {
            return false;
        }

        return this.name.equals(((SerializingItems)otr).name);
    }
}
