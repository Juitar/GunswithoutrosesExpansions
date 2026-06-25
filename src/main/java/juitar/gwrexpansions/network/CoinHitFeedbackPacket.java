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
    private final int styleScore;
    private final int heat;
    private final int heatKeepTimer;
    private final String eventType;
    private final int eventValue;

    public CoinHitFeedbackPacket(int hits, int timer) {
        this(hits, timer, 0);
    }

    public CoinHitFeedbackPacket(int hits, int timer, int overheatTimer) {
        this(hits, timer, overheatTimer, hits, 0, 0, "", 0);
    }

    public CoinHitFeedbackPacket(int hits, int timer, int overheatTimer, int styleScore, int heat,
                                 String eventType, int eventValue) {
        this(hits, timer, overheatTimer, styleScore, heat, timer, eventType, eventValue);
    }

    public CoinHitFeedbackPacket(int hits, int timer, int overheatTimer, int styleScore, int heat,
                                 int heatKeepTimer, String eventType, int eventValue) {
        this.hits = hits;
        this.timer = timer;
        this.overheatTimer = overheatTimer;
        this.styleScore = styleScore;
        this.heat = heat;
        this.heatKeepTimer = heatKeepTimer;
        this.eventType = eventType == null ? "" : eventType;
        this.eventValue = eventValue;
    }

    public static void encode(CoinHitFeedbackPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hits);
        buf.writeVarInt(msg.timer);
        buf.writeVarInt(msg.overheatTimer);
        buf.writeVarInt(msg.styleScore);
        buf.writeVarInt(msg.heat);
        buf.writeVarInt(msg.heatKeepTimer);
        buf.writeUtf(msg.eventType);
        buf.writeVarInt(msg.eventValue);
    }

    public static CoinHitFeedbackPacket decode(FriendlyByteBuf buf) {
        return new CoinHitFeedbackPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(CoinHitFeedbackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> CoinHitFeedbackClient.onCoinHit(msg.hits, msg.timer, msg.overheatTimer,
                msg.styleScore, msg.heat, msg.heatKeepTimer, msg.eventType, msg.eventValue)));
        ctx.get().setPacketHandled(true);
    }
}
