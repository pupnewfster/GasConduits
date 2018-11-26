package gg.galaxygaming.gasconduits;

import com.enderio.core.common.util.NNList;
import crazypants.enderio.api.addon.IEnderIOAddon;
import crazypants.enderio.base.config.recipes.RecipeFactory;
import crazypants.enderio.base.init.RegisterModObject;
import gg.galaxygaming.gasconduits.common.CommonProxy;
import gg.galaxygaming.gasconduits.common.network.PacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(modid = GasConduitsConstants.MOD_ID, name = GasConduitsConstants.MOD_NAME, version = GasConduitsConstants.VERSION,
        dependencies = GasConduitsConstants.DEPENDENCIES, acceptedMinecraftVersions = GasConduitsConstants.MC_VERSION)
@Mod.EventBusSubscriber(modid = GasConduitsConstants.MOD_ID)
public class GasConduits implements IEnderIOAddon {
    @SidedProxy(serverSide = GasConduitsConstants.PROXY_COMMON, clientSide = GasConduitsConstants.PROXY_CLIENT)
    public static CommonProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.Init(event);
        PacketHandler.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        proxy.serverStart(event);
    }

    @SubscribeEvent
    public static void registerBlocksEarly(@Nonnull RegisterModObject event) {
        GasConduitObject.registerBlocksEarly(event);
    }

    @Override
    @Nonnull
    public NNList<Triple<Integer, RecipeFactory, String>> getRecipeFiles() {
        return new NNList<>(Triple.of(2, null, "conduits-gas"));
    }
}