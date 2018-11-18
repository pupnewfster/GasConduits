package gg.galaxygaming.gasconduits;

import net.minecraftforge.common.config.Config;

@Config(modid = GasConduitsConstants.MOD_ID)
public class GasConduitConfig {
    //TODO: Add comments describing configs
    public static int tier1_extractRate = 64;
    public static int tier1_maxIO = 256;
    public static int tier2_extractRate = 256;
    public static int tier2_maxIO = 1024;
    public static int tier3_extractRate = 1024;
    public static int tier3_maxIO = 4096;
}