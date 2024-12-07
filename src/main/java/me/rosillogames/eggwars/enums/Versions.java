package me.rosillogames.eggwars.enums;

import me.rosillogames.eggwars.EggWars;

public enum Versions
{
    OTHER(false, "other"),
    V_1_16_R1(true, "v1_16_R1"),//1.16.1
    V_1_16_R2(true, "v1_16_R2"),//1.16.2 - 1.16.3
    V_1_16_R3(true, "v1_16_R3"),//1.16.4 - 1.16.5
    V_1_17(true, "v1_17_R1"),
    V_1_18_R1(true, "v1_18_R1"),
    V_1_18_R2(true, "v1_18_R2"),
    V_1_19_R1(true, "v1_19_R1"), //1.19 - 1.19.2
    V_1_19_R2(true, "v1_19_R2"), //1.19.3
    V_1_19_R3(true, "v1_19_R3"), //1.19.4
    V_1_20_R1(true, "v1_20_R1"), //1.20.1
    V_1_20_R2(true, "v1_20_R2"), //1.20.2
    V_1_20_R3(true, "v1_20_R3"), //1.20.4
    V_1_20_R4(true, "v1_20_R4", "1.20.5", "1.20.6"), //1.20.5 - 1.20.6
    V_1_21_R1(true, "v1_21_R1", "1.21", "1.21.1"), //1.21 - 1.21.1
    V_1_21_R2(true, "v1_21_R2", "1.21.2", "1.21.3"), //1.21.2 - 1.21.3
    V_1_21_R3(true, "v1_21_R3", "1.21.4"); //1.21.2 - 1.21.3

    public static final String SUPPORTED_TEXT = "1.16.1 - 1.21.3";
    private final String packageId;
    private final String[] versionIds;
    private final boolean allowed;

    private Versions(boolean flag, String packageIdIn, String... versionIdsIn)
    {
        this.allowed = flag;
        this.packageId = packageIdIn;
        this.versionIds = new String[versionIdsIn.length];

        for (int i = 0; i < versionIdsIn.length; ++i)
        {
            this.versionIds[i] = versionIdsIn[i] + "-R0.1-SNAPSHOT";
        }
    }

    public boolean isAllowedVersion()
    {
        return this.allowed;
    }

    public static Versions get(String oldWay, String newWay)
    {
        for (Versions version : values())
        {
            if (oldWay.equalsIgnoreCase(version.packageId))
            {
                return version;
            }

            for (String versionId : version.versionIds)
            {
                if (newWay.equalsIgnoreCase(versionId))
                {
                    //if (version.ordinal() >= Versions.V_1_20_R4.ordinal())
                    //{//don't use condition as older versions don't have any versionIds
                        EggWars.fixPaperOBC = true;
                    //}

                    return version;
                }
            }
        }

        return OTHER;
    }
}
