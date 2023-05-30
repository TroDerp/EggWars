package me.rosillogames.eggwars.language;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TranslationUtils
{
    public static final String UNIT_AND_OTHER = "misc.unit_and_other";
    public static final String UNIT_WITH_OTHER = "misc.unit_with_other";

    /** WARNING! use full option only when we are sure it's going to fit at the message **/
    public static String translateTime(Player player, int seconds, boolean full)
    {
        return translateTime(LanguageUtils.getPlayerLanguage(player), seconds, full);
    }

    public static String translateTime(Language lang, int seconds, boolean full)
    {
        ArrayList<String> units = new ArrayList();
        int remaining = seconds;
        int years = (((remaining / 60) / 60) / 24) / 365;

        if (years > 0)
        {
            remaining -= ((((years * 365) * 24) * 60) * 60);
            units.add(years > 1 ? TranslationUtils.getMessage("misc.years", lang, years) : TranslationUtils.getMessage("misc.one_year", lang));
        }

        int days = ((remaining / 60) / 60) / 24;

        if (days > 0)
        {
            remaining -= (((days * 24) * 60) * 60);
            units.add(days > 1 ? TranslationUtils.getMessage("misc.days", lang, days) : TranslationUtils.getMessage("misc.one_day", lang));
        }

        if (full || units.size() <= 1)
        {
            int hours = (remaining / 60) / 60;

            if (hours > 0)
            {
                remaining -= ((hours * 60) * 60);
                units.add(hours > 1 ? TranslationUtils.getMessage("misc.hours", lang, hours) : TranslationUtils.getMessage("misc.one_hour", lang));
            }

            if (full || units.size() <= 1)
            {
                int minutes = remaining / 60;

                if (minutes > 0)
                {
                    remaining -= (minutes * 60);
                    units.add(minutes > 1 ? TranslationUtils.getMessage("misc.minutes", lang, minutes) : TranslationUtils.getMessage("misc.one_minute", lang));
                }

                if ((full || units.size() <= 1) && remaining > 0)
                {
                    units.add(remaining != 1 ? TranslationUtils.getMessage("misc.seconds", lang, remaining) : TranslationUtils.getMessage("misc.one_second", lang));
                }
            }
        }

        //This is used to merge time strings depending of the amount of units, for example; "3 hours, 20 minutes and 5 seconds"
        if (units.size() > 1)
        {
            String merged = "";

            for (int i = 1; i < units.size(); ++i)
            {
                if (i == 1)
                {
                    merged = TranslationUtils.getMessage(units.size() == 2 ? UNIT_AND_OTHER : UNIT_WITH_OTHER, lang, units.get(0), units.get(1));
                    continue;
                }

                merged = TranslationUtils.getMessage(i == (units.size() - 1) ? UNIT_AND_OTHER : UNIT_WITH_OTHER, lang, merged, units.get(i));
            }

            return merged;
        }
        else if (units.size() == 1)
        {
            return units.get(0);
        }

        return TranslationUtils.getMessage("misc.seconds", lang, 0);
    }

    public static void sendMessage(String msg, CommandSender sender)
    {
        sendMessagePrefix(msg, sender, true, new Object[0]);
    }

    public static void sendMessage(String msg, CommandSender sender, Object... args)
    {
        sendMessagePrefix(msg, sender, true, args);
    }

    public static void sendMessagePrefix(String msg, CommandSender sender, boolean prefix, Object... args)
    {
        Language lang = (sender != null && sender instanceof Player) ? LanguageUtils.getPlayerLanguage((Player)sender) : LanguageManager.getDefaultLanguage();
        sender.sendMessage(lang != null ? MessageFactory.factGetMessages(lang.getOrDefault(msg).getString(), prefix, args) : new String[] {msg});
    }

    public static boolean hasMessage(String msg, Player player)
    {
        return (player != null ? LanguageUtils.getPlayerLanguage(player) : LanguageManager.getDefaultLanguage()).has(msg);
    }

    public static String getMessage(String msg)
    {
        return getMessage(msg, LanguageManager.getDefaultLanguage());
    }

    public static String getMessage(String msg, Object[] args)
    {
        return getMessage(msg, LanguageManager.getDefaultLanguage(), args);
    }

    public static String getMessage(String msg, Player player)
    {
        return getMessage(msg, (player != null ? LanguageUtils.getPlayerLanguage(player) : LanguageManager.getDefaultLanguage()));
    }

    public static String getMessage(String msg, Player player, Object... args)
    {
        return getMessage(msg, (player != null ? LanguageUtils.getPlayerLanguage(player) : LanguageManager.getDefaultLanguage()), args);
    }

    public static String getMessage(String msg, Language lang, Object... args)
    {
        return lang != null ? MessageFactory.factGetMessage(lang.getOrDefault(msg).getString(), args) : msg;
    }
}
