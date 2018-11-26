package gg.galaxygaming.gasconduits.common.utils;

import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class GasWrapper {
    @Nullable
    public static IGasHandler getGasHandler(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return world == null || pos == null ? null : getGasHandler(world.getTileEntity(pos), side);
    }

    @Nullable
    public static IGasHandler getGasHandler(TileEntity tile, EnumFacing facing) {
        if (tile instanceof IGasHandler) {
            return (IGasHandler) tile;
        } else if (tile != null && tile.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, facing)) {
            return tile.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, facing);
        }
        return null;
    }
}