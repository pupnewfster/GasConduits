package gg.galaxygaming.gasconduits.common.filter;

import com.enderio.core.client.gui.widget.GhostSlot;
import com.enderio.core.common.network.NetworkUtil;
import com.enderio.core.common.util.NNList;
import crazypants.enderio.base.integration.jei.IHaveGhostTargets;
import crazypants.enderio.util.NbtValue;
import gg.galaxygaming.gasconduits.common.utils.GasUtil;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GasFilter implements IGasFilter {
    private final GasStack[] gasses = new GasStack[5];
    private boolean isBlacklist;

    @Override
    public boolean isEmpty() {
        for (GasStack f : gasses) {
            if (f != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        return gasses.length;
    }

    @Override
    public GasStack getGasStackAt(int index) {
        return index < 0 || index >= gasses.length ? null : gasses[index];
    }

    @Override
    public boolean setGas(int index, @Nullable GasStack gas) {
        if (index < 0 || index >= gasses.length) {
            return false;
        }
        if (gas == null || gas.getGas() == null) {
            gasses[index] = null;
        } else {
            gasses[index] = gas;
        }
        return true;
    }

    @Override
    public boolean setGas(int index, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return setGas(index, (GasStack) null);
        }
        if (index < 0 || index >= gasses.length) {
            return false;
        }
        GasStack f = GasUtil.getGasTypeFromItem(stack);
        if (f == null || f.getGas() == null) {
            return setGas(index, (GasStack) null);
        }
        return setGas(index, f);
    }

    @Override
    public boolean isBlacklist() {
        return isBlacklist;
    }

    @Override
    public void setBlacklist(boolean isBlacklist) {
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean isDefault() {
        return !isBlacklist && isEmpty();
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound nbtRoot) {
        NbtValue.FILTER_BLACKLIST.setBoolean(nbtRoot, isBlacklist);

        NBTTagList gasList = new NBTTagList();

        int index = 0;
        for (GasStack g : gasses) {
            NBTTagCompound fRoot = new NBTTagCompound();
            if (g != null) {
                fRoot.setInteger("index", index);
                g.write(fRoot);
                gasList.appendTag(fRoot);
            }
            index++;
        }
        nbtRoot.setTag("gasses", gasList);

    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbtRoot) {

        isBlacklist = NbtValue.FILTER_BLACKLIST.getBoolean(nbtRoot);
        clear();

        NBTTagList tagList = nbtRoot.getTagList("gasses", nbtRoot.getId());
        for (int i = 0; i < tagList.tagCount(); i++) {
            gasses[i] = GasStack.readFromNBT(tagList.getCompoundTagAt(i));
        }
    }

    private void clear() {
        for (int i = 0; i < gasses.length; i++) {
            gasses[i] = null;
        }
    }

    @Override
    public boolean matchesFilter(GasStack drained) {
        if (drained == null || drained.getGas() == null) {
            return false;
        }
        if (isEmpty()) {
            return true;
        }
        for (GasStack f : gasses) {
            if (f != null && f.isGasEqual(drained)) {
                return !isBlacklist;
            }
        }
        return isBlacklist;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
        setGas(slot, stack);
    }

    @Override
    public void writeToByteBuf(@Nonnull ByteBuf buf) {
        NBTTagCompound root = new NBTTagCompound();
        writeToNBT(root);
        NetworkUtil.writeNBTTagCompound(root, buf);
    }

    @Override
    public void readFromByteBuf(@Nonnull ByteBuf buf) {
        NBTTagCompound tag = NetworkUtil.readNBTTagCompound(buf);
        readFromNBT(tag);
    }

    public void createGhostSlots(@Nonnull NNList<GhostSlot> slots, int xOffset, int yOffset, @Nullable Runnable cb) {
        int index = 0;
        int numRows = 1;
        int rowSpacing = 2;
        int numCols = 5;
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                int x = xOffset + col * 18;
                int y = yOffset + row * 18 + rowSpacing * row;
                slots.add(new GasFilterGhostSlot(index, x, y, cb));
                index++;
            }
        }
    }

    @Override
    public int getSlotCount() {
        return gasses.length;
    }

    class GasFilterGhostSlot extends GhostSlot implements IHaveGhostTargets.ICustomGhostSlot {
        private final Runnable cb;

        GasFilterGhostSlot(int slot, int x, int y, Runnable cb) {
            this.setX(x);
            this.setY(y);
            this.setSlot(slot);
            this.cb = cb;
        }

        @Override
        public void putStack(@Nonnull ItemStack stack, int realSize) {
            setGas(getSlot(), stack);
            cb.run();
        }

        @Override
        public @Nonnull
        ItemStack getStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public void putIngredient(Object ingredient) {
            GasStack stack = null;
            if (ingredient instanceof Gas) {
                stack = new GasStack((Gas) ingredient, 0);
            } else if (ingredient instanceof GasStack) {
                stack = (GasStack) ingredient;
            }
            setGas(getSlot(), stack);
        }

        @Override
        public boolean isType(Object ingredient) {
            return ingredient instanceof GasStack || ingredient instanceof Gas;
        }
    }

}