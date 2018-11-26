package gg.galaxygaming.gasconduits.conduit;

import crazypants.enderio.base.conduit.IClientConduit;
import crazypants.enderio.base.conduit.IExtractor;
import crazypants.enderio.base.conduit.IServerConduit;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IGasConduit extends IGasHandler, IExtractor, IServerConduit, IClientConduit, ITubeConnection {
    boolean canOutputToDir(@Nonnull EnumFacing dir);

    boolean canExtractFromDir(@Nonnull EnumFacing dir);

    boolean canInputToDir(@Nonnull EnumFacing dir);

    /**
     * Used to get the capability of the conduit for the given direction
     *
     * @param from side for the capability
     * @return returns the connection with reference to the relevant side
     */
    IGasHandler getGasDir(@Nullable EnumFacing from);
}