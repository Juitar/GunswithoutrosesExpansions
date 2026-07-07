package juitar.gwrexpansions.item.vanilla;

import juitar.gwrexpansions.client.render.NetheriteShotgunGeoRenderer;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NetheriteShotgun extends ConfigurableGunItem implements GeoItem {
    private static final String NBT_GECKO_FIRING = "NetheriteShotgunGeckoFiring";
    private static final String NBT_GECKO_ANIMATION_SEQUENCE = "NetheriteShotgunGeckoAnimationSequence";
    private static final String GECKO_CONTROLLER = "controller";
    private static final int FIRE_ANIM_TICKS = 11; // 0.5417s at 20 tps
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FIRE_ANIM = RawAnimation.begin().thenPlay("fire");
    private static final Map<Long, Integer> LAST_SEEN_GECKO_SEQUENCE = new ConcurrentHashMap<>();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public NetheriteShotgun(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                            double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo,
                         lykrast.gunswithoutroses.item.IBullet bulletItem, boolean bulletFree) {
        super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        ensureGeckoId(gun, level);
        setGeckoFiring(gun, FIRE_ANIM_TICKS);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private NetheriteShotgunGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new NetheriteShotgunGeoRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, GECKO_CONTROLLER, 0, state -> {
            ItemStack stack = state.getData(software.bernie.geckolib.constant.DataTickets.ITEMSTACK);
            restartAnimationIfSequenceChanged(state, stack);
            state.setControllerSpeed(1.0F);
            state.setAnimation(isGeckoFiring(stack) ? FIRE_ANIM : IDLE_ANIM);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, slot, isSelected);
        tickGeckoFiring(stack);
        if (!world.isClientSide) {
            ensureGeckoId(stack, world);
        }
    }

    public static boolean isFlashAnimationActive(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        return stack.getOrCreateTag().getInt(NBT_GECKO_FIRING) > 0;
    }

    private static void ensureGeckoId(ItemStack stack, Level level) {
        if (stack.isEmpty() || !(stack.getItem() instanceof NetheriteShotgun) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        GeoItem.getOrAssignId(stack, serverLevel);
    }

    private static void setGeckoFiring(ItemStack stack, int ticks) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_GECKO_FIRING, ticks);
        tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
    }

    private static boolean isGeckoFiring(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return false;
        }
        return stack.getOrCreateTag().getInt(NBT_GECKO_FIRING) > 0;
    }

    private static void tickGeckoFiring(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int timer = tag.getInt(NBT_GECKO_FIRING);
        if (timer > 1) {
            tag.putInt(NBT_GECKO_FIRING, timer - 1);
        } else if (timer == 1) {
            tag.putInt(NBT_GECKO_FIRING, 0);
            tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
        }
    }

    private static void restartAnimationIfSequenceChanged(AnimationState<NetheriteShotgun> state,
                                                          @Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int sequence = tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE);
        long id = GeoItem.getId(stack);
        long key = id != 0L ? id : System.identityHashCode(stack);
        Integer previous = LAST_SEEN_GECKO_SEQUENCE.put(key, sequence);
        if (previous != null && previous != sequence) {
            state.getController().forceAnimationReset();
        }
    }
}
