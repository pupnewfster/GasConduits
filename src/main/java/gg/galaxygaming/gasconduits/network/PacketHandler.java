package gg.galaxygaming.gasconduits.network;

import com.enderio.core.common.network.ThreadedNetworkWrapper;
import gg.galaxygaming.gasconduits.GasConduitsConstants;
import gg.galaxygaming.gasconduits.conduit.PacketConduitGasLevel;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class PacketHandler {
    public static final @Nonnull
    ThreadedNetworkWrapper INSTANCE = new ThreadedNetworkWrapper(GasConduitsConstants.MOD_ID);
    public static int ID;

    public static void init(FMLInitializationEvent event) {
        INSTANCE.registerMessage(PacketConduitGasLevel.Handler.class, PacketConduitGasLevel.class, ID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketGasFilter.Handler.class, PacketGasFilter.class, ID++, Side.SERVER);
        INSTANCE.registerMessage(PacketEnderGasConduit.Handler.class, PacketEnderGasConduit.class, ID++, Side.SERVER);
    }
}