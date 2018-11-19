package gg.galaxygaming.gasconduits.conduit;

import com.enderio.core.api.client.gui.ITabPanel;
import com.enderio.core.common.util.DyeColor;
import crazypants.enderio.base.conduit.*;
import crazypants.enderio.base.machine.modes.RedstoneControlMode;
import crazypants.enderio.conduits.conduit.AbstractConduit;
import gg.galaxygaming.gasconduits.client.GasSettings;
import gg.galaxygaming.gasconduits.common.GasWrapper;
import mekanism.api.gas.*;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map.Entry;

public abstract class AbstractGasConduit extends AbstractConduit implements IGasConduit {

    protected final EnumMap<EnumFacing, RedstoneControlMode> extractionModes = new EnumMap<>(EnumFacing.class);
    protected final EnumMap<EnumFacing, DyeColor> extractionColors = new EnumMap<>(EnumFacing.class);

    public static IGasHandler getExternalGasHandler(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        if (world.getTileEntity(pos) instanceof IConduitBundle) {
            return null;
        }
        return GasWrapper.getGasHandler(world, pos, side);
    }

    public IGasHandler getExternalHandler(EnumFacing direction) {
        return getExternalGasHandler(getBundle().getBundleworld(), getBundle().getLocation().offset(direction), direction.getOpposite());
    }

    @Override
    public boolean canConnectToExternal(@Nonnull EnumFacing direction, boolean ignoreDisabled) {
        IGasHandler h = getExternalHandler(direction);
        return h != null;
    }

    @Override
    @Nonnull
    public Class<? extends IConduit> getBaseConduitType() {
        return IGasConduit.class;
    }

    @Override
    public void setExtractionRedstoneMode(@Nonnull RedstoneControlMode mode, @Nonnull EnumFacing dir) {
        extractionModes.put(dir, mode);
    }

    @Override
    @Nonnull
    public RedstoneControlMode getExtractionRedstoneMode(@Nonnull EnumFacing dir) {
        RedstoneControlMode res = extractionModes.get(dir);
        if (res == null) {
            res = RedstoneControlMode.NEVER;
        }
        return res;
    }

    @Override
    public void setExtractionSignalColor(@Nonnull EnumFacing dir, @Nonnull DyeColor col) {
        extractionColors.put(dir, col);
    }

    @Override
    @Nonnull
    public DyeColor getExtractionSignalColor(@Nonnull EnumFacing dir) {
        DyeColor result = extractionColors.get(dir);
        if (result == null) {
            return DyeColor.RED;
        }
        return result;
    }

    @Override
    public boolean canOutputToDir(@Nonnull EnumFacing dir) {
        if (!canInputToDir(dir)) {
            return false;
        }
        if (conduitConnections.contains(dir)) {
            return true;
        }
        return externalConnections.contains(dir);
    }

    protected boolean autoExtractForDir(@Nonnull EnumFacing dir) {
        if (!canExtractFromDir(dir)) {
            return false;
        }
        RedstoneControlMode mode = getExtractionRedstoneMode(dir);
        return ConduitUtil.isRedstoneControlModeMet(this, mode, getExtractionSignalColor(dir));
    }

    @Override
    public boolean canExtractFromDir(@Nonnull EnumFacing dir) {
        return getConnectionMode(dir).acceptsInput();
    }

    @Override
    public boolean canInputToDir(@Nonnull EnumFacing dir) {
        return getConnectionMode(dir).acceptsOutput() && !autoExtractForDir(dir);
    }

    protected boolean hasExtractableMode() {
        return supportsConnectionMode(ConnectionMode.INPUT) || supportsConnectionMode(ConnectionMode.IN_OUT);
    }

    @Override
    protected void readTypeSettings(@Nonnull EnumFacing dir, @Nonnull NBTTagCompound dataRoot) {
        setExtractionSignalColor(dir, DyeColor.values()[dataRoot.getShort("extractionSignalColor")]);
        setExtractionRedstoneMode(RedstoneControlMode.fromOrdinal(dataRoot.getShort("extractionRedstoneMode")), dir);
    }

    @Override
    protected void writeTypeSettingsToNbt(@Nonnull EnumFacing dir, @Nonnull NBTTagCompound dataRoot) {
        dataRoot.setShort("extractionSignalColor", (short) getExtractionSignalColor(dir).ordinal());
        dataRoot.setShort("extractionRedstoneMode", (short) getExtractionRedstoneMode(dir).ordinal());
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound nbtRoot) {
        super.writeToNBT(nbtRoot);

        for (Entry<EnumFacing, RedstoneControlMode> entry : extractionModes.entrySet()) {
            if (entry.getValue() != null) {
                short ord = (short) entry.getValue().ordinal();
                nbtRoot.setShort("extRM." + entry.getKey().name(), ord);
            }
        }

        for (Entry<EnumFacing, DyeColor> entry : extractionColors.entrySet()) {
            if (entry.getValue() != null) {
                short ord = (short) entry.getValue().ordinal();
                nbtRoot.setShort("extSC." + entry.getKey().name(), ord);
            }
        }

    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbtRoot) {
        super.readFromNBT(nbtRoot);

        for (EnumFacing dir : EnumFacing.VALUES) {
            String key = "extRM." + dir.name();
            if (nbtRoot.hasKey(key)) {
                short ord = nbtRoot.getShort(key);
                if (ord >= 0 && ord < RedstoneControlMode.values().length) {
                    extractionModes.put(dir, RedstoneControlMode.values()[ord]);
                }
            }
            key = "extSC." + dir.name();
            if (nbtRoot.hasKey(key)) {
                short ord = nbtRoot.getShort(key);
                if (ord >= 0 && ord < DyeColor.values().length) {
                    extractionColors.put(dir, DyeColor.values()[ord]);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public ITabPanel createGuiPanel(@Nonnull IGuiExternalConnection gui, @Nonnull IClientConduit con) {
        return new GasSettings(gui, con);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean updateGuiPanel(@Nonnull ITabPanel panel) {
        if (panel instanceof GasSettings) {
            return ((GasSettings) panel).updateConduit(this);
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getGuiPanelTabOrder() {
        return 1;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == Capabilities.TUBE_CONNECTION_CAPABILITY) {
            if (facing != null && containsExternalConnection(facing)) {
                ConnectionMode mode = getConnectionMode(facing);
                return mode.acceptsInput() || mode.acceptsOutput();
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (hasCapability(capability, facing)) {
            return (T) getGasDir(facing);
        }
        return null;
    }

    @Override
    @Nullable
    public IGasHandler getGasDir(@Nullable EnumFacing dir) {
        if (dir != null) {
            return new ConnectionGasSide(dir);
        }
        return null;
    }

    @Override
    public boolean canTubeConnect(EnumFacing facing) {
        ConnectionMode connectionMode = getConnectionMode(facing);
        return connectionMode.acceptsOutput() || connectionMode.acceptsInput();
    }

    /**
     * Inner class for holding the direction of capabilities.
     */
    protected class ConnectionGasSide implements IGasHandler, ITubeConnection {
        protected EnumFacing side;

        public ConnectionGasSide(EnumFacing side) {
            this.side = side;
        }

        @Override
        public int receiveGas(EnumFacing facing, GasStack resource, boolean doFill) {
            if (canReceiveGas(facing, resource.getGas())) {
                return AbstractGasConduit.this.receiveGas(facing, resource, doFill);
            }
            return 0;
        }

        @Override
        public GasStack drawGas(EnumFacing facing, int maxDrain, boolean doDrain) {
            if (canDrawGas(facing, null)) {
                return AbstractGasConduit.this.drawGas(facing, maxDrain, doDrain);
            }
            return null;
        }

        @Override
        public boolean canReceiveGas(EnumFacing facing, Gas gas) {
            if (side.equals(facing) && getConnectionMode(facing).acceptsInput()) {
                return ConduitUtil.isRedstoneControlModeMet(AbstractGasConduit.this, getExtractionRedstoneMode(facing), getExtractionSignalColor(facing));
            }
            return false;
        }

        @Override
        public boolean canDrawGas(EnumFacing facing, Gas gas) {
            if (side.equals(facing) && getConnectionMode(facing).acceptsOutput()) {
                return ConduitUtil.isRedstoneControlModeMet(AbstractGasConduit.this, getExtractionRedstoneMode(facing), getExtractionSignalColor(facing));
            }
            return false;
        }

        @Override
        public GasTankInfo[] getTankInfo() {
            return AbstractGasConduit.this.getTankInfo();
        }

        @Override
        public boolean canTubeConnect(EnumFacing facing) {
            if (!side.equals(facing)) {
                return false;
            }
            ConnectionMode connectionMode = getConnectionMode(facing);
            return connectionMode.acceptsOutput() || connectionMode.acceptsInput();
        }
    }

}
