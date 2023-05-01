package me.rosillogames.eggwars.utils;

public class NumericUtils
{
    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    public static boolean isDouble(String s)
    {
        try
        {
            Double.parseDouble(s);
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    public static boolean isFloat(String s)
    {
        try
        {
            Float.parseFloat(s);
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    public static boolean isShort(String s)
    {
        try
        {
            Short.parseShort(s);
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    public static boolean isLong(String s)
    {
        try
        {
            Long.parseLong(s);
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }

    public static boolean isByte(String s)
    {
        try
        {
            Byte.parseByte(s);
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }
}
