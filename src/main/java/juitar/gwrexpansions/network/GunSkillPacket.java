package juitar.gwrexpansions.network;

import juitar.gwrexpansions.item.GunSkillItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GunSkillPacket {
    private final boolean pressed;

    public GunSkillPacket(boolean pressed) {
        this.pressed = pressed;
    }

    public static void encode(GunSkillPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.pressed);
    }

    public static GunSkillPacket decode(FriendlyByteBuf buf) {
        return new GunSkillPacket(buf.readBoolean());
    }

    public static void handle(GunSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            if (trySkill(player, InteractionHand.MAIN_HAND, msg.pressed)) {
                return;
            }
            trySkill(player, InteractionHand.OFF_HAND, msg.pressed);
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean trySkill(ServerPlayer player, InteractionHand hand, boolean pressed) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof GunSkillItem skillItem && skillItem.canUseGunSkill(player, hand, stack)) {
            if (pressed) {
                skillItem.useGunSkill(player, hand, stack);
            } else {
                skillItem.releaseGunSkill(player, hand, stack);
            }
            return true;
        }
        return false;
    }
}
