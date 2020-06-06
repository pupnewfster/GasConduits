package gg.galaxygaming.gasconduits;

import com.enderio.core.common.mixin.SimpleMixinLoader;
import com.enderio.core.common.util.NNList;
import crazypants.enderio.api.addon.IEnderIOAddon;
import crazypants.enderio.base.config.ConfigHandlerEIO;
import crazypants.enderio.base.config.recipes.RecipeFactory;
import crazypants.enderio.base.init.RegisterModObject;
import gg.galaxygaming.gasconduits.common.conduit.GasConduitObject;
import gg.galaxygaming.gasconduits.common.config.Config;
import gg.galaxygaming.gasconduits.common.network.PacketHandler;
import info.loenwind.autoconfig.ConfigHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;

@Mod(modid = GasConduitsConstants.MOD_ID, name = GasConduitsConstants.MOD_NAME, version = GasConduitsConstants.VERSION,
      dependencies = GasConduitsConstants.DEPENDENCIES, acceptedMinecraftVersions = GasConduitsConstants.MC_VERSION)
@Mod.EventBusSubscriber(modid = GasConduitsConstants.MOD_ID)
public class GasConduits implements IEnderIOAddon {

    public static Logger logger;

    private static ConfigHandler configHandler;

    public GasConduits() {
        SimpleMixinLoader.loadMixinSources(this);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        configHandler = new ConfigHandlerEIO(event, Config.F);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PacketHandler.init(event);
    }

    @SubscribeEvent
    public static void registerBlocksEarly(@Nonnull RegisterModObject event) {
        GasConduitObject.registerBlocksEarly(event);
    }

    @Override
    @Nullable
    public Configuration getConfiguration() {
        return Config.F.getConfig();
    }

    @Override
    @Nonnull
    public NNList<Triple<Integer, RecipeFactory, String>> getRecipeFiles() {
        return new NNList<>(Triple.of(2, null, "conduits-gas"));
    }
}