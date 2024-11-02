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
            if (index >= 0 && index < args.length)
            {
                msg = msg.replace("{" + index + "}", args[index].toString());
            }
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
    }/*
    Brown team test
    @NotNull
    public static String translateAlternateColorCodes(char altColorChar, @NotNull String textToTranslate)
    {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();
        List<Character> charList = new ArrayList();
        boolean dontAddNext = false;

        for (int i = 0; i < b.length; i++)
        {
            if (dontAddNext)
            {
                dontAddNext = false;
                continue;
            }

            if (i < (b.length - 1) && b[i] == altColorChar && "0123456789AaBbCcDdEeFfGgKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1)
            {
                b[i] = '§';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
                charList.add(b[i]);

                if (b[i + 1] == 'g')
                {
                    charList.addAll(Arrays.asList('x', '§', '7', '§', '2', '§', '3', '§', 'd', '§', '0', '§', 'd'));
                    dontAddNext = true;
                }
            }
            else
            {
                charList.add(b[i]);
            }
        }

        char[] newCharArr = new char[charList.size()];

        for (int i = 0; i < newCharArr.length; i++)
        {
            newCharArr[i] = charList.get(i).charValue();
        }

        return new String(newCharArr);
    }*/
}
