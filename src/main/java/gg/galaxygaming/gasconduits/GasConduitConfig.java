package gg.galaxygaming.gasconduits;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

@Config(modid = GasConduitsConstants.MOD_ID)
public class GasConduitConfig {
    //TODO: See if these are actually synced from server
    @Comment({
            "Millibuckets per tick extracted by a gas conduit's auto extracting. " +
                    "(synced from server) [range: 1 ~ 1000000000, default: 64]"
    })
    public static int tier1_extractRate = 64;
    @Comment({
            "Millibuckets per tick that can pass through a single connection to a gas conduit. " +
                    "(synced from server) [range: 1 ~ 1000000000, default: 256]"
    })
    public static int tier1_maxIO = 256;
    @Comment({
            "Millibuckets per tick extracted by an advanced gas conduit's auto extracting. " +
                    "(synced from server) [range: 1 ~ 1000000000, default: 512]"
    })
    public static int tier2_extractRate = 512;
    @Comment({
            "Millibuckets per tick that can pass through a single connection to an advanced gas conduit. " +
                    "(synced from server) [range: 1 ~ 1000000000, default: 2048]"
    })
    public static int tier2_maxIO = 2048;
    @Comment({
            "Millibuckets per tick extracted by an ender gas conduit's auto extracting. " +
                    "(synced from server) [range: 1 ~ 1000000000, default: 4096]"
    })
    public static int tier3_extractRate = 4096;
    @Comment({
            "Millibuckets per tick that can pass through a single connection to an ender gas conduit. " +
                    "(synced from server) [range: 1 ~ 1000000000, default: 16384]"
    })
    public static int tier3_maxIO = 16384;
}