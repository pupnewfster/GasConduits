package gg.galaxygaming.gasconduits.common.network;

import crazypants.enderio.conduits.network.AbstractConduitPacket;
import gg.galaxygaming.gasconduits.common.conduit.IGasConduit;
import gg.galaxygaming.gasconduits.common.conduit.ender.EnderGasConduit;
import gg.galaxygaming.gasconduits.common.filter.GasFilter;
import gg.galaxygaming.gasconduits.common.filter.IGasFilter;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketGasFilter extends AbstractConduitPacket<IGasConduit> {

    private EnumFacing dir;
    private boolean isInput;
    private IGasFilter filter;

    public PacketGasFilter() {
    }

    public PacketGasFilter(EnderGasConduit eConduit, EnumFacing dir, IGasFilter filter, boolean isInput) {
        super(eConduit);
        this.dir = dir;
        this.filter = filter;
        this.isInput = isInput;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        if (dir != null) {
            buf.writeShort(dir.ordinal());
        } else {
            buf.writeShort(-1);
        }
        buf.writeBoolean(isInput);
        NBTTagCompound tag = new NBTTagCompound();
        filter.writeToNBT(tag);
        ByteBufUtils.writeTag(buf, tag);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        short ord = buf.readShort();
        dir = ord < 0 ? null : EnumFacing.values()[ord];
        isInput = buf.readBoolean();
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        filter = new GasFilter();
        if (tag != null) {
            filter.readFromNBT(tag);
        }
    }

    public static class Handler implements IMessageHandler<PacketGasFilter, IMessage> {

        @Override
        public IMessage onMessage(PacketGasFilter message, MessageContext ctx) {
            IGasConduit conduit = message.getConduit(ctx);
            if (conduit instanceof EnderGasConduit) {
                ((EnderGasConduit) conduit).setFilter(message.dir, message.filter, message.isInput);
                World world = message.getWorld(ctx);
                IBlockState bs = world.getBlockState(message.getPos());
                world.notifyBlockUpdate(message.getPos(), bs, bs, 3);
            }
            return null;
        }
    }
}