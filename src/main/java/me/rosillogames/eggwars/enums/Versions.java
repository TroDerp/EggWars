package me.rosillogames.eggwars.enums;

public enum Versions
{
    OTHER(false, "other"),
    V_1_16_R1(true, "v1_16_R1"),
    V_1_16_R2(true, "v1_16_R2"),
    V_1_16_R3(true, "v1_16_R3"),
    V_1_17(true, "v1_17"),
    V_1_18_R1(true, "v1_18_R1"),
    V_1_18_R2(true, "v1_18_R2"),
    V_1_19_R1(true, "v1_19_R1"), //1.19 - 1.19.2
    V_1_19_R2(true, "v1_19_R2"), //1.19.3
    V_1_19_R3(true, "v1_19_R3"); //1.19.4

    public static final String SUPPORTED_TEXT = "1.16.1 - 1.19.4";
    private final String[] packageIds;
    private final boolean allowed;

    private Versions(boolean flag, String... packageIdsIn)
    {
        this.allowed = flag;
        this.packageIds = packageIdsIn;
    }

    public boolean isAllowedVersion()
    {
        return this.allowed;
    }

    public static Versions get(String s)
    {
        for (Versions version : values())
        {
            for (String packageid : version.packageIds)
            {
                if (s.contains(packageid))
                {
                    return version;
                }
            }
        }

        return OTHER;
    }
}
