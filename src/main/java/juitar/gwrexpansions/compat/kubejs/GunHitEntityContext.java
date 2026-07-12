package juitar.gwrexpansions.compat.kubejs;

import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record GunHitEntityContext(ServerPlayer player, ServerLevel level, ItemStack item, ResourceLocation itemId,
                                  BulletEntity projectile, LivingEntity target) {
    public void message(String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
