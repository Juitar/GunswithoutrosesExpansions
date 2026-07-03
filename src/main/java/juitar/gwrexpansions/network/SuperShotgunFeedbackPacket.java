package juitar.gwrexpansions.network;

import juitar.gwrexpansions.client.SuperShotgunFeedbackClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SuperShotgunFeedbackPacket {
    public static void encode(SuperShotgunFeedbackPacket msg, FriendlyByteBuf buf) {
    }

    public static SuperShotgunFeedbackPacket decode(FriendlyByteBuf buf) {
        return new SuperShotgunFeedbackPacket();
    }

    public static void handle(SuperShotgunFeedbackPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> SuperShotgunFeedbackClient::onShot));
        ctx.get().setPacketHandled(true);
    }
}
