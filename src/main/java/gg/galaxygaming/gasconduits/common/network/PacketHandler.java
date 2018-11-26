package gg.galaxygaming.gasconduits.common.network;

import com.enderio.core.common.network.ThreadedNetworkWrapper;
import gg.galaxygaming.gasconduits.GasConduitsConstants;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class PacketHandler {
    @Nonnull
    public static final ThreadedNetworkWrapper INSTANCE = new ThreadedNetworkWrapper(GasConduitsConstants.MOD_ID);
    public static int ID;

    public static void init(FMLInitializationEvent event) {
        INSTANCE.registerMessage(PacketConduitGasLevel.Handler.class, PacketConduitGasLevel.class, ID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketGasFilter.Handler.class, PacketGasFilter.class, ID++, Side.SERVER);
        INSTANCE.registerMessage(PacketEnderGasConduit.Handler.class, PacketEnderGasConduit.class, ID++, Side.SERVER);
    }
}