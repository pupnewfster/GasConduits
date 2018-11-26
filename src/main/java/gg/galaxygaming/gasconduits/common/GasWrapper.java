package gg.galaxygaming.gasconduits.common;

import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class GasWrapper {
    public static @Nullable
    IGasHandler getGasHandler(IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (world == null || pos == null) {
            return null;
        }
        return getGasHandler(world.getTileEntity(pos), side);
    }

    public static @Nullable
    IGasHandler getGasHandler(TileEntity tile, EnumFacing facing) {
        if (tile instanceof IGasHandler) {
            return (IGasHandler) tile;
        } else if (tile != null && tile.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, facing)) {
            return tile.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, facing);
        }
        return null;
    }
}