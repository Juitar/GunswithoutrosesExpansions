package juitar.gwrexpansions.network;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import juitar.gwrexpansions.entity.vanilla.MeatHookEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkEvent;

public class MeatHookInputPacket {
    private static final float INPUT_CLAMP = 12.0F;

    private final float deltaYaw;
    private final float deltaPitch;

    public MeatHookInputPacket(float deltaYaw, float deltaPitch) {
        this.deltaYaw = Mth.clamp(deltaYaw, -INPUT_CLAMP, INPUT_CLAMP);
        this.deltaPitch = Mth.clamp(deltaPitch, -INPUT_CLAMP, INPUT_CLAMP);
    }

    public static void encode(MeatHookInputPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.deltaYaw);
        buf.writeFloat(msg.deltaPitch);
    }

    public static MeatHookInputPacket decode(FriendlyByteBuf buf) {
        return new MeatHookInputPacket(buf.readFloat(), buf.readFloat());
    }

    public static void handle(MeatHookInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            MeatHookEntity hook = findPullingHook(player);
            if (hook != null) {
                hook.addOrbitInput(msg.deltaYaw, msg.deltaPitch);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static MeatHookEntity findPullingHook(ServerPlayer player) {
        List<MeatHookEntity> hooks = player.level().getEntitiesOfClass(MeatHookEntity.class,
                player.getBoundingBox().inflate(64.0D),
                hook -> hook.isPulling() && hook.getHookOwnerId() == player.getId());

        return hooks.stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }
}
