package gg.galaxygaming.gasconduits.common.utils;

import mekanism.api.gas.*;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GasUtil {
    public static boolean areGassesTheSame(@Nullable Gas gas, @Nullable Gas gas2) {
        if (gas == null) {
            return gas2 == null;
        }
        if (gas2 == null) {
            return false;
        }
        return gas == gas2 || gas.getName().equals(gas2.getName());
    }

    public static GasStack getGasTypeFromItem(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof IGasItem) {
            return ((IGasItem) stack.getItem()).getGas(stack);
        }
        return null;
    }

    @Nullable
    public static IGasItem getGasHandler(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof IGasItem) {
            return (IGasItem) stack.getItem();
        }
        return null;
    }

    public static GasStack getGasStack(IGasHandler tank) {
        if (tank == null) {
            return null;
        }
        GasTankInfo[] tankInfo = tank.getTankInfo();
        int stored = 0;
        Gas type = null;
        for (GasTankInfo info : tankInfo) {
            stored += info.getStored();
            if (info.getGas() != null) {
                type = info.getGas().getGas();
            }
        }

        if (type == null || stored <= 0) {
            return null;
        }
        return new GasStack(type, stored);
    }
}