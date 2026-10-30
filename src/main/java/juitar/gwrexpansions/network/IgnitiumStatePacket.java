package juitar.gwrexpansions.network;

import juitar.gwrexpansions.client.IgnitiumClientState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Authoritative heat, mode and animation snapshot for the equipped weapon. */
public record IgnitiumStatePacket(int playerId, InteractionHand hand, long stackId, CompoundTag state) {
    public static void encode(IgnitiumStatePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.playerId);
        buf.writeEnum(msg.hand);
        buf.writeLong(msg.stackId);
        buf.writeNbt(msg.state);
    }

    public static IgnitiumStatePacket decode(FriendlyByteBuf buf) {
        return new IgnitiumStatePacket(buf.readVarInt(), buf.readEnum(InteractionHand.class), buf.readLong(), buf.readNbt());
    }

    public static void handle(IgnitiumStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> IgnitiumClientState.apply(msg)));
        ctx.get().setPacketHandled(true);
    }
}
