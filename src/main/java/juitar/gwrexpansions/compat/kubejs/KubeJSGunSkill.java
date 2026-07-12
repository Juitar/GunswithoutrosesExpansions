package juitar.gwrexpansions.compat.kubejs;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.GunSkillItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class KubeJSGunSkill {
    private static final String NBT_SKILL_COOLDOWN = "GWREKubeJSSkillCooldown";
    private final Item item;
    private final Consumer<GunSkillContext> useCallback;
    private final int cooldown;

    public KubeJSGunSkill(Item item, Consumer<GunSkillContext> useCallback, int cooldown) {
        this.item = item;
        this.useCallback = useCallback;
        this.cooldown = Math.max(1, cooldown);
    }

    public boolean canUse(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        return useCallback != null && stack.getItem() == item && getCooldown(stack) <= 0;
    }

    public void use(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (!canUse(player, hand, stack)) return;
        setCooldown(stack, cooldown);
        try {
            useCallback.accept(new GunSkillContext(player, player.serverLevel(), hand, stack, ResourceLocation.tryParse(item.builtInRegistryHolder().key().location().toString())));
        } catch (Throwable error) {
            GWRexpansions.LOG.error("KubeJS gun skill failed for {} used by {}", item.builtInRegistryHolder().key().location(), player.getGameProfile().getName(), error);
        }
    }

    public void tick(ItemStack stack, Level level) {
        if (!level.isClientSide && getCooldown(stack) > 0) setCooldown(stack, getCooldown(stack) - 1);
    }

    private static int getCooldown(ItemStack stack) { return stack.getOrCreateTag().getInt(NBT_SKILL_COOLDOWN); }
    private static void setCooldown(ItemStack stack, int ticks) { stack.getOrCreateTag().putInt(NBT_SKILL_COOLDOWN, Math.max(0, ticks)); }
}
