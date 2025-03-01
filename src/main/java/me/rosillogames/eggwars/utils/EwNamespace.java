package me.rosillogames.eggwars.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import me.rosillogames.eggwars.EggWars;

public class EwNamespace<T>
{
    //private static final EwNamespace<Long> MOST_BITS_KEY = new EwNamespace("UUID_MOST_BITS", PersistentDataType.LONG);
    //private static final EwNamespace<Long> LEAST_BITS_KEY = new EwNamespace("UUID_LEAST_BITS", PersistentDataType.LONG);
    private final NamespacedKey key;
    private final PersistentDataType<?, T> valueType;

    public EwNamespace(String keyName, PersistentDataType<?, T> type)
    {
        this.key = new NamespacedKey(EggWars.instance, keyName);
        this.valueType = type;
    }

    public NamespacedKey getKey(Player player)
    {
        return this.key;
    }

    public boolean isContainedBy(PersistentDataContainer container)
    {
        return container.has(this.key, this.valueType);
    }

    public boolean isInHolder(PersistentDataHolder holder)
    {
        return this.isContainedBy(holder.getPersistentDataContainer());
    }

    public T getFrom(PersistentDataContainer container)
    {
        return container.get(this.key, this.valueType);
    }

    public T getFrom(PersistentDataHolder holder)
    {
        return this.getFrom(holder.getPersistentDataContainer());
    }

    public void setTo(PersistentDataContainer container, T value)
    {
        container.set(this.key, this.valueType, value);
    }

    public void setTo(PersistentDataHolder holder, T value)
    {
        this.setTo(holder.getPersistentDataContainer(), value);
    }

    /* Sample by spigot, transformed to use sub-containers. Should new PersistentDataTypes
     * be used for eggwars?
    public class UUIDTagType implements PersistentDataType<PersistentDataContainer, UUID>
    {
        @Override
        public Class<PersistentDataContainer> getPrimitiveType()
        {
            return PersistentDataContainer.class;
        }

        @Override
        public Class<UUID> getComplexType()
        {
            return UUID.class;
        }

        @Override
        public PersistentDataContainer toPrimitive(UUID complex, PersistentDataAdapterContext context)
        {
            PersistentDataContainer foo = context.newPersistentDataContainer();
            MOST_BITS_KEY.setTo(() -> foo, complex.getMostSignificantBits());
            LEAST_BITS_KEY.setTo(() -> foo, complex.getLeastSignificantBits());
            return foo;
        }

        @Override
        public UUID fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context)
        {
            long firstLong = MOST_BITS_KEY.getFrom(() -> primitive);
            long secondLong = LEAST_BITS_KEY.getFrom(() -> primitive);
            return new UUID(firstLong, secondLong);
        }
    }*/
}
