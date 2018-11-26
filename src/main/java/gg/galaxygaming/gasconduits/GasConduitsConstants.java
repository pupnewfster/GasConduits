package gg.galaxygaming.gasconduits;

public class GasConduitsConstants {
    public static final String MOD_ID = "gasconduits";
    public static final String MOD_NAME = "GasConduits";
    public static final String VERSION = "1.12.2-1.1.0";
    public static final String MC_VERSION = "1.12.2,";

    //Proxy Constants
    public static final String PROXY_COMMON = "gg.galaxygaming.gasconduits.common.CommonProxy";
    public static final String PROXY_CLIENT = "gg.galaxygaming.gasconduits.client.ClientProxy";
    public static final String DEPENDENCIES = "required-after:enderioconduits@[5.0.38,);required-after:mekanism;" +
            "before:enderioconduitsappliedenergistics;before:enderioconduitsopencomputers;before:enderioconduitsrefinedstorage;";

    public static final int GAS_VOLUME = 1000;
    public static final int GAS_MAX_EXTRACTED_SCALER = 2;
}