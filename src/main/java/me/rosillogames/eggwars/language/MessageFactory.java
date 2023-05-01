package me.rosillogames.eggwars.language;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public class MessageFactory
{
    private static final Pattern PATTERN = Pattern.compile("\\{(\\d+)\\}");

    public static String[] factGetMessages(String msg, final boolean prefix, final Object... args)
    {
        final List<Integer> msgParams = new ArrayList<Integer>();
        final Matcher matcher = MessageFactory.PATTERN.matcher(msg);

        while (matcher.find())
        {
            msgParams.add(Integer.valueOf(matcher.group(1)));
        }

        for (final Integer index : msgParams)
        {
            msg = msg.replace("{" + index + "}", args[index].toString());
        }

        final String[] msga = ChatColor.translateAlternateColorCodes('&', msg).split("\\n");

        if (prefix)
        {
            msga[0] = ChatColor.translateAlternateColorCodes('&', "&7") + msga[0];
        }

        return msga;
    }

    public static String factGetMessage(String msg, final Object... args)
    {
        final List<Integer> msgParams = new ArrayList<Integer>();
        final Matcher matcher = MessageFactory.PATTERN.matcher(msg);

        while (matcher.find())
        {
            msgParams.add(Integer.valueOf(matcher.group(1)));
        }

        for (final Integer index : msgParams)
        {
            String arg;

            try
            {
                arg = args[index].toString();
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                arg = "";
            }

            msg = msg.replace("{" + index + "}", arg);
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
