package juitar.gwrexpansions.network;

import juitar.gwrexpansions.item.GunSkillItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GunSkillPacket {
    public static void encode(GunSkillPacket msg, FriendlyByteBuf buf) {
    }

    public static GunSkillPacket decode(FriendlyByteBuf buf) {
        return new GunSkillPacket();
    }

    public static void handle(GunSkillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            if (tryUseSkill(player, InteractionHand.MAIN_HAND)) {
                return;
            }
            tryUseSkill(player, InteractionHand.OFF_HAND);
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean tryUseSkill(ServerPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof GunSkillItem skillItem && skillItem.canUseGunSkill(player, hand, stack)) {
            skillItem.useGunSkill(player, hand, stack);
            return true;
        }
        return false;
    }
}
