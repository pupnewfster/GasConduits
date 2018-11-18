package gg.galaxygaming.gasconduits.network;

import com.enderio.core.common.util.DyeColor;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.filter.IFilter;
import crazypants.enderio.base.filter.capability.CapabilityFilterHolder;
import crazypants.enderio.base.filter.capability.IFilterHolder;
import crazypants.enderio.conduits.network.PacketConduitFilter;
import gg.galaxygaming.gasconduits.conduit.EnderGasConduit;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;

public class PacketEnderGasConduit extends PacketConduitFilter<EnderGasConduit> {

    private DyeColor colIn;
    private DyeColor colOut;
    private int priority;
    private boolean roundRobin;
    private boolean selfFeed;

    public PacketEnderGasConduit() {
    }

    public PacketEnderGasConduit(@Nonnull EnderGasConduit con, @Nonnull EnumFacing dir) {
        super(con, dir);
        colIn = con.getInputColor(dir);
        colOut = con.getOutputColor(dir);
        priority = con.getOutputPriority(dir);
        roundRobin = con.isRoundRobinEnabled(dir);
        selfFeed = con.isSelfFeedEnabled(dir);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeShort(colIn.ordinal());
        buf.writeShort(colOut.ordinal());
        buf.writeInt(priority);
        buf.writeBoolean(roundRobin);
        buf.writeBoolean(selfFeed);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        colIn = DyeColor.values()[buf.readShort()];
        colOut = DyeColor.values()[buf.readShort()];
        priority = buf.readInt();
        roundRobin = buf.readBoolean();
        selfFeed = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketEnderGasConduit, IMessage> {

        @Override
        public IMessage onMessage(PacketEnderGasConduit message, MessageContext ctx) {
            EnderGasConduit conduit = message.getConduit(ctx);
            if (conduit != null) {
                conduit.setInputColor(message.dir, message.colIn);
                conduit.setOutputColor(message.dir, message.colOut);
                conduit.setOutputPriority(message.dir, message.priority);
                conduit.setRoundRobinEnabled(message.dir, message.roundRobin);
                conduit.setSelfFeedEnabled(message.dir, message.selfFeed);
                applyFilter(message.dir, conduit, message.inputFilter, true);
                applyFilter(message.dir, conduit, message.outputFilter, false);

                IBlockState bs = message.getWorld(ctx).getBlockState(message.getPos());
                message.getWorld(ctx).notifyBlockUpdate(message.getPos(), bs, bs, 3);
            }
            return null;
        }

        private void applyFilter(EnumFacing dir, IConduit conduit, IFilter filter, boolean isInput) {
            if (conduit.hasInternalCapability(CapabilityFilterHolder.FILTER_HOLDER_CAPABILITY, dir)) {
                IFilterHolder<IFilter> filterHolder = conduit.getInternalCapability(CapabilityFilterHolder.FILTER_HOLDER_CAPABILITY, dir);
                if (filterHolder == null) {
                    return;
                }
                if (isInput) {
                    filterHolder.setFilter(filterHolder.getInputFilterIndex(), dir.ordinal(), filter);
                } else {
                    filterHolder.setFilter(filterHolder.getOutputFilterIndex(), dir.ordinal(), filter);
                }
            }
        }

    }
}