package gg.galaxygaming.gasconduits.common.network;

import crazypants.enderio.conduits.network.AbstractConduitPacket;
import gg.galaxygaming.gasconduits.common.conduit.IGasConduit;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;

public class PacketConduitGasLevel extends AbstractConduitPacket<IGasConduit> {
    public NBTTagCompound tc;

    public PacketConduitGasLevel() {
    }

    public PacketConduitGasLevel(@Nonnull IGasConduit conduit) {
        super(conduit);
        tc = new NBTTagCompound();
        conduit.writeToNBT(tc);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeTag(buf, tc);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        tc = ByteBufUtils.readTag(buf);
    }

    public static class Handler implements IMessageHandler<PacketConduitGasLevel, IMessage> {
        @Override
        public IMessage onMessage(PacketConduitGasLevel message, MessageContext ctx) {
            final NBTTagCompound nbt = message.tc;
            final IGasConduit conduit = message.getConduit(ctx);
            if (nbt != null && conduit != null) {
                conduit.readFromNBT(nbt);
            }
            return null;
        }
    }
}