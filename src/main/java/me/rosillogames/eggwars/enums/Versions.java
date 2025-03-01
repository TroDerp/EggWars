package me.rosillogames.eggwars.enums;

import me.rosillogames.eggwars.EggWars;
import me.rosillogames.eggwars.utils.NumericUtils;

public enum Versions
{
    OTHER(false, -1, "other"),
    V_1_16_R1(false, 5, "v1_16_R1", "1.16.1"),
    V_1_16_R2(false, 6, "v1_16_R2", "1.16.2", "1.16.3"),
    V_1_16_R3(false, 6, "v1_16_R3", "1.16.4", "1.16.5"),
    V_1_17(false, 7, "v1_17_R1", "1.17", "1.17.1"),
    V_1_18_R1(false, 7, "v1_18_R1", "1.18", "1.18.1"),
    V_1_18_R2(false, 7, "v1_18_R2", "1.18.2"),
    V_1_19_R1(false, 7, "v1_19_R1", "1.19", "1.19.1", "1.19.2"),
    V_1_19_R2(false, 7, "v1_19_R2", "1.19.3"),
    V_1_19_R3(false, 8, "v1_19_R3", "1.19.4"),
    V_1_20_R1(false, 8, "v1_20_R1", "1.20.1"), //1.20 released as 1.20.1
    V_1_20_R2(false, 9, "v1_20_R2", "1.20.2"),
    V_1_20_R3(false, 9, "v1_20_R3", "1.20.4"), //1.20.3 released as 1.20.4
    V_1_20_R4(true, 10, "v1_20_R4", "1.20.5", "1.20.6"),
    V_1_21_R1(true, 11, "v1_21_R1", "1.21", "1.21.1"),
    V_1_21_R2(true, 12, "v1_21_R2", "1.21.2", "1.21.3"),
    V_1_21_R3(true, 13, "v1_21_R3", "1.21.4");

    public static final String SUPPORTED_TEXT = "1.16.1 - 1.21.4";
    private final String packageId;
    private final int formatVersion;
    private final String[] versionNames;
    private final boolean fixPaper;

    private Versions(boolean flag, int formatIn, String packageIdIn, String... versionIdsIn)
    {
        this.fixPaper = flag;
        this.formatVersion = formatIn;
        this.packageId = packageIdIn;
        this.versionNames = versionIdsIn;
    }

    public int getFormatVersion()
    {
        return this.formatVersion;
    }

    public boolean isFormatCompatible(String[] formats)
    {
        for (int i = 0; i < formats.length; i++)
        {
            if ((NumericUtils.isInteger(formats[i]) && Integer.parseInt(formats[i]) == this.getFormatVersion()))
            {
                return true;
            }
            else
            {
                for (int j = 0; j < this.versionNames.length; j++)
                {
                    if (this.versionNames[j].equalsIgnoreCase(formats[i]))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static String getCompatibleList(String[] formats)
    {
        if (formats.length == 0)
        {
            return "unknown";
        }

        StringBuilder builder = new StringBuilder();
        boolean nextOnLoop = false;

        for (Versions version : values())
        {
            if (version.isFormatCompatible(formats))
            {
                for (int i = 0; i < version.versionNames.length; ++i)
                {
                    if (nextOnLoop)
                    {
                        builder.append(", ");
                    }

                    builder.append(version.versionNames[i] + " (" + version.getFormatVersion() + ")");
                    nextOnLoop = true;
                }
            }
        }

        return builder.toString();
    }

    public static Versions get(String spigotWay, String paperWay)
    {
        for (Versions version : values())
        {
            if (spigotWay.equalsIgnoreCase(version.packageId))
            {
                return version;
            }

            for (String versionId : version.versionNames)
            {
                if (paperWay.equalsIgnoreCase(versionId))
                {
                    if (version.fixPaper)
                    {//don't use condition as older versions don't have any versionIds
                        EggWars.fixPaperOBC = true;
                    }

                    return version;
                }
            }
        }

        return OTHER;
    }
}
