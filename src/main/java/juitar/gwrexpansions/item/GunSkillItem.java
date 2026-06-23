package juitar.gwrexpansions.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface GunSkillItem {
    boolean canUseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack);

    void useGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack);

    default void releaseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
    }
}
