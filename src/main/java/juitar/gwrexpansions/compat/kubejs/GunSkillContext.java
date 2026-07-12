package juitar.gwrexpansions.compat.kubejs;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record GunSkillContext(ServerPlayer player, ServerLevel level, InteractionHand hand, ItemStack item, ResourceLocation itemId) {
    public boolean isMainHand() {
        return hand == InteractionHand.MAIN_HAND;
    }

    public void message(String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
