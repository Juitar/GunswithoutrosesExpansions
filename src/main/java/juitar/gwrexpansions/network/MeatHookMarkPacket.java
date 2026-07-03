package juitar.gwrexpansions.network;

import juitar.gwrexpansions.client.MeatHookMarkClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MeatHookMarkPacket {
    private final int entityId;
    private final int durationTicks;
    private final boolean marked;

    public MeatHookMarkPacket(int entityId, int durationTicks, boolean marked) {
        this.entityId = entityId;
        this.durationTicks = durationTicks;
        this.marked = marked;
    }

    public static void encode(MeatHookMarkPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.durationTicks);
        buf.writeBoolean(msg.marked);
    }

    public static MeatHookMarkPacket decode(FriendlyByteBuf buf) {
        return new MeatHookMarkPacket(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(MeatHookMarkPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (msg.marked) {
                MeatHookMarkClient.markEntity(msg.entityId, msg.durationTicks);
            } else {
                MeatHookMarkClient.clearEntity(msg.entityId);
            }
        }));
        ctx.get().setPacketHandled(true);
    }
}
