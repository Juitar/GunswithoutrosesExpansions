package juitar.gwrexpansions.compat.kubejs;

import juitar.gwrexpansions.GWRexpansions;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public final class KubeJSGunEvents {
    private static final String SOURCE_GUN_TAG = "GWREKubeJSSourceGun";
    private final Consumer<GunFireContext> fire;
    private final Consumer<GunHitEntityContext> hitEntity;
    private final Consumer<GunHitEntityContext> headshot;

    KubeJSGunEvents(AbstractKubeJSGunBuilder builder) {
        fire = builder.fire;
        hitEntity = builder.hitEntity;
        headshot = builder.headshot;
    }

    void onFire(Item gun, Level level, Player player, ItemStack stack) {
        if (fire == null || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(gun);
        try {
            fire.accept(new GunFireContext(serverPlayer, serverLevel, stack, itemId));
        } catch (Throwable error) {
            GWRexpansions.LOG.error("KubeJS onFire callback failed for {} used by {}", itemId, serverPlayer.getGameProfile().getName(), error);
        }
    }

    void markProjectile(Item gun, BulletEntity projectile) {
        projectile.getPersistentData().putString(SOURCE_GUN_TAG, BuiltInRegistries.ITEM.getKey(gun).toString());
    }

    public static void onBulletHit(BulletEntity projectile, LivingEntity target, Entity shooter, Level level, boolean isHeadshot) {
        if (!(level instanceof ServerLevel serverLevel) || !(shooter instanceof ServerPlayer player)) return;
        CompoundTag data = projectile.getPersistentData();
        if (!data.contains(SOURCE_GUN_TAG)) return;
        ResourceLocation itemId = ResourceLocation.tryParse(data.getString(SOURCE_GUN_TAG));
        if (itemId == null) return;
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (!(item instanceof KubeJSGunEventSource source)) return;
        source.callbacks().onHit(player, serverLevel, findGunStack(player, item), itemId, projectile, target, isHeadshot);
    }

    private void onHit(ServerPlayer player, ServerLevel level, ItemStack stack, ResourceLocation itemId,
                       BulletEntity projectile, LivingEntity target, boolean isHeadshot) {
        GunHitEntityContext context = new GunHitEntityContext(player, level, stack, itemId, projectile, target);
        runCallback(hitEntity, context, "onHitEntity", itemId, player);
        if (isHeadshot) runCallback(headshot, context, "onHeadshot", itemId, player);
    }

    private static void runCallback(Consumer<GunHitEntityContext> callback, GunHitEntityContext context,
                                    String name, ResourceLocation itemId, ServerPlayer player) {
        if (callback == null) return;
        try {
            callback.accept(context);
        } catch (Throwable error) {
            GWRexpansions.LOG.error("KubeJS {} callback failed for {} used by {}", name, itemId, player.getGameProfile().getName(), error);
        }
    }

    private static ItemStack findGunStack(ServerPlayer player, Item item) {
        if (player.getMainHandItem().is(item)) return player.getMainHandItem();
        if (player.getOffhandItem().is(item)) return player.getOffhandItem();
        for (ItemStack stack : player.getInventory().items) if (stack.is(item)) return stack;
        return ItemStack.EMPTY;
    }
}
