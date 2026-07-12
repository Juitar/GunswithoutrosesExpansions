package juitar.gwrexpansions.compat.kubejs;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record GunFireContext(ServerPlayer player, ServerLevel level, ItemStack item, ResourceLocation itemId) {
    public void message(String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
