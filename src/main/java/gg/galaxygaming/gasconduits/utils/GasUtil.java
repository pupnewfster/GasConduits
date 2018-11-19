package gg.galaxygaming.gasconduits.utils;

import com.enderio.core.common.vecmath.Vector4f;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
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
            IGasItem item = (IGasItem) stack.getItem();
            return item.getGas(stack);
        } else {
            return null;
        }
    }

    @Nullable
    public static IGasItem getGasHandler(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof IGasItem) {
            return (IGasItem) stack.getItem();
        } else {
            return null;
        }
    }

    public static Vector4f getColor(int color, float filledRatio) {
        return new Vector4f((color >> 16 & 0xFF) / 255d, (color >> 8 & 0xFF) / 255d, (color & 0xFF) / 255d, filledRatio);
    }
}