package juitar.gwrexpansions.network;

import juitar.gwrexpansions.client.CoinHitFeedbackClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CoinHitFeedbackPacket {
    private final int hits;
    private final int timer;
    private final int overheatTimer;

    public CoinHitFeedbackPacket(int hits, int timer) {
        this(hits, timer, 0);
    }

    public CoinHitFeedbackPacket(int hits, int timer, int overheatTimer) {
        this.hits = hits;
        this.timer = timer;
        this.overheatTimer = overheatTimer;
    }

    public static void encode(CoinHitFeedbackPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hits);
        buf.writeVarInt(msg.timer);
        buf.writeVarInt(msg.overheatTimer);
    }

    public static CoinHitFeedbackPacket decode(FriendlyByteBuf buf) {
        return new CoinHitFeedbackPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(CoinHitFeedbackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> CoinHitFeedbackClient.onCoinHit(msg.hits, msg.timer, msg.overheatTimer)));
        ctx.get().setPacketHandled(true);
    }
}