package juitar.gwrexpansions.item.meetyourfight;

import juitar.gwrexpansions.advancement.MYF.DestinyAllInTrigger;
import juitar.gwrexpansions.client.render.DestinySevenGeoRenderer;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.ObsidianCoreEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWRESounds;
import lykrast.meetyourfight.registry.MYFSounds;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DestinyGunItem extends ConfigurableGunItem implements GeoItem {
    private static final String PITY_TAG = "DestinyPityShots";
    private static final String NBT_GECKO_ANIMATION = "DestinyGeckoAnimation";
    private static final String NBT_GECKO_ANIMATION_TIMER = "DestinyGeckoAnimationTimer";
    private static final String NBT_GECKO_ANIMATION_SEQUENCE = "DestinyGeckoAnimationSequence";
    private static final String GECKO_CONTROLLER = "controller";
    private static final String GECKO_ANIM_FIRE = "fire";
    private static final String GECKO_ANIM_FIRE_BAD = "fire-bad";
    private static final String GECKO_ANIM_FIRE_DOUBLE = "fire-double";
    private static final String GECKO_ANIM_FIRE_TRIPLE = "fire-triple";
    private static final String GECKO_ANIM_FIRE_JACKPOT = "fire-jackpot";
    private static final float FIRE_ANIMATION_SPEED = 1.0F;
    private static final int FIRE_ANIMATION_BASE_TICKS = 8;
    private static final int GECKO_FIRE_TICKS = scaledFireTicks(FIRE_ANIMATION_BASE_TICKS);
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FIRE_ANIM = RawAnimation.begin().thenPlay("fire");
    private static final RawAnimation FIRE_BAD_ANIM = RawAnimation.begin().thenPlay("fire-bad");
    private static final RawAnimation FIRE_DOUBLE_ANIM = RawAnimation.begin().thenPlay("fire-double");
    private static final RawAnimation FIRE_TRIPLE_ANIM = RawAnimation.begin().thenPlay("fire-triple");
    private static final RawAnimation FIRE_JACKPOT_ANIM = RawAnimation.begin().thenPlay("fire-jackpot");
    private static final Map<Long, Integer> LAST_SEEN_GECKO_SEQUENCE = new ConcurrentHashMap<>();
    private static final ResourceLocation IRON_TICKET = new ResourceLocation("gunswithoutroses", "iron_bullet");
    private static final ResourceLocation GOLD_TICKET = new ResourceLocation("gwrexpansions", "golden_bullet");
    private static final ResourceLocation DIAMOND_TICKET = new ResourceLocation("gwrexpansions", "diamond_bullet");
    private static final ResourceLocation OBSIDIAN_CORE = new ResourceLocation("gwrexpansions", "obsidian_core");
    private static final ResourceLocation OBSIDIAN_CORE_ENTITY = new ResourceLocation("gwrexpansions",
            "obsidian_core_entity");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DestinyGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
            double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        ItemStack ammo = player.getProjectile(gun);
        boolean canAttemptFire = !ammo.isEmpty() || player.getAbilities().instabuild;
        if (!world.isClientSide && canAttemptFire && !player.getCooldowns().isOnCooldown(this)) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), GWRESounds.destiny_pull.get(),
                    SoundSource.PLAYERS, 1.5F, 1.0F);
        }

        return super.use(world, player, hand);
    }

    @Override
    protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem,
            boolean bulletFree) {
        Ticket ticket = getTicket(ammo);
        if (ticket == null) {
            ensureGeckoId(gun, world);
            setGeckoAnimation(gun, GECKO_ANIM_FIRE, GECKO_FIRE_TICKS);
            super.shoot(world, player, gun, ammo, bulletItem, bulletFree);
            return;
        }

        int pityShots = gun.getOrCreateTag().getInt(PITY_TAG);
        Outcome outcome = rollOutcome(world.getRandom(), ticket, pityShots, player.getLuck());
        UUID jackpotGroup = applyOutcome(world, player, gun, outcome, pityShots);
        ensureGeckoId(gun, world);
        setGeckoAnimation(gun, outcome.animationKey(), GECKO_FIRE_TICKS);
        executeShot(world, player, gun, ammo, bulletItem, bulletFree, outcome, jackpotGroup);
    }

    @Nullable
    private static UUID applyOutcome(Level world, Player player, ItemStack gun, Outcome outcome, int pityShots) {
        CompoundTag tag = gun.getOrCreateTag();
        UUID jackpotGroup = null;

        if (outcome == Outcome.JACKPOT) {
            tag.putInt(PITY_TAG, 0);
            if (player instanceof ServerPlayer serverPlayer) {
                jackpotGroup = DestinyAllInTrigger.onJackpot(serverPlayer);
            }
        } else {
            tag.putInt(PITY_TAG, pityShots + 1);
        }

        player.displayClientMessage(Component.translatable(outcome.messageKey), true);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), outcome.getSound(), SoundSource.PLAYERS,
                1.0F, 1.0F);
        return jackpotGroup;
    }

    private void executeShot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem,
            boolean bulletFree, Outcome outcome, @Nullable UUID jackpotGroup) {
        List<? extends String> bulletPool = outcome == Outcome.BUST ? GWREConfig.DESTINY.bustBulletPool.get()
                : GWREConfig.DESTINY.rewardBulletPool.get();
        int shots = outcome.getShots();

        float extraInaccuracy = shots != 1 ? 3.0F : 1.0F;
        for (int i = 0; i < shots; ++i) {
            WeightedProjectile projectile = rollProjectile(world.getRandom(), bulletPool, ammo);
            if (projectile.kind == ProjectileKind.OBSIDIAN_CORE) {
                if (shootObsidianCore(world, player, gun, jackpotGroup)) {
                    continue;
                }
                projectile = fallbackBullet(ammo);
            }
            shootBullet(world, player, gun, projectile.asAmmoStack(), bulletItem, bulletFree, extraInaccuracy,
                    jackpotGroup);
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DestinySevenGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new DestinySevenGeoRenderer();
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
            state.setControllerSpeed(getGeckoAnimationSpeed(stack));
            state.setAnimation(getGeckoAnimation(stack));
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
        tickGeckoAnimation(stack);
        if (!world.isClientSide) {
            ensureGeckoId(stack, world);
        }
    }

    private static Ticket getTicket(ItemStack ammo) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(ammo.getItem());
        if (IRON_TICKET.equals(id))
            return Ticket.IRON;
        if (GOLD_TICKET.equals(id))
            return Ticket.GOLD;
        if (DIAMOND_TICKET.equals(id))
            return Ticket.DIAMOND;
        return null;
    }

    private static Outcome rollOutcome(RandomSource random, Ticket ticket, int pityShots, float playerLuck) {
        GWREConfig.DestinyConfig config = GWREConfig.DESTINY;
        int bust = ticket.bustWeight();
        int doubled = ticket.doubleWeight();
        int triple = ticket.tripleWeight();
        int jackpot = ticket.jackpotWeight();
        int pityBonus = Math.min(config.pityMaxJackpotWeight.get(), pityShots * config.pityJackpotWeightPerShot.get());
        bust = Math.max(0, bust - pityBonus);
        jackpot += pityBonus;

        int luckBonus = Math.min(config.luckMaxBonusWeight.get(),
                Math.max(0, (int) Math.floor(playerLuck * config.luckWeightPerPoint.get())));
        if (luckBonus > 0) {
            int doubleBonus = luckBonus;
            int tripleBonus = luckBonus * 2;
            int jackpotBonus = luckBonus * 3;
            int winningBonus = doubleBonus + tripleBonus + jackpotBonus;

            bust = Math.max(0, bust - winningBonus);
            doubled += doubleBonus;
            triple += tripleBonus;
            jackpot += jackpotBonus;
        }

        int total = bust + doubled + triple + jackpot;
        if (total <= 0)
            return Outcome.BUST;

        int roll = random.nextInt(total);
        if (roll < bust)
            return Outcome.BUST;
        roll -= bust;
        if (roll < doubled)
            return Outcome.DOUBLE;
        roll -= doubled;
        if (roll < triple)
            return Outcome.TRIPLE;
        return Outcome.JACKPOT;
    }

    private void shootBullet(Level world, Player player, ItemStack gun, ItemStack firedAmmo, IBullet fallbackBullet,
            boolean bulletFree, float extraInaccuracy, @Nullable UUID jackpotGroup) {
        IBullet firedBullet = firedAmmo.getItem() instanceof IBullet ? (IBullet) firedAmmo.getItem() : fallbackBullet;
        BulletEntity shot = firedBullet.createProjectile(world, firedAmmo, player);
        shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                (float) this.getProjectileSpeed(gun, player),
                (float) this.getInaccuracy(gun, player) + extraInaccuracy);
        shot.setDamage(Math.max(0.0D, shot.getDamage() + this.getBonusDamage(gun, player))
                * this.getDamageMultiplier(gun, player));
        shot.setKnockbackStrength(shot.getKnockbackStrength() + this.getKnockbackBonus(gun, player));
        shot.setHeadshotMultiplier(this.getHeadshotMultiplier(gun, player));
        this.affectBulletEntity(player, gun, shot, bulletFree);
        if (jackpotGroup != null) {
            shot.getPersistentData().putUUID(DestinyAllInTrigger.JACKPOT_GROUP_TAG, jackpotGroup);
        }
        world.addFreshEntity(shot);
    }

    private boolean shootObsidianCore(Level world, Player player, ItemStack gun, @Nullable UUID jackpotGroup) {
        if (GWREEntities.OBSIDIAN_CORE == null || !GWREEntities.OBSIDIAN_CORE.isPresent()) {
            return false;
        }

        ObsidianCoreEntity.SpellType[] spellTypes = ObsidianCoreEntity.SpellType.values();
        ObsidianCoreEntity.SpellType spellType = spellTypes[world.getRandom().nextInt(spellTypes.length)];
        ObsidianCoreEntity core = new ObsidianCoreEntity(GWREEntities.OBSIDIAN_CORE.get(), world, player, spellType);
        double baseDamage = GWREConfig.DESTINY.obsidianCoreBaseDamage.get();
        core.setBaseDamage(Math.max(1.0D,
                (baseDamage + this.getBonusDamage(gun, player)) * this.getDamageMultiplier(gun, player)));
        core.setAOERadiusMultiplier(1.0F);
        core.setMaxRange(30.0F);

        Vec3 look = player.getLookAngle();
        core.setPos(
                player.getX() + look.x * 0.5D,
                player.getEyeY() - 0.1D + look.y * 0.5D,
                player.getZ() + look.z * 0.5D);
        core.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                (float) this.getProjectileSpeed(gun, player), (float) this.getInaccuracy(gun, player));
        if (jackpotGroup != null) {
            core.getPersistentData().putUUID(DestinyAllInTrigger.JACKPOT_GROUP_TAG, jackpotGroup);
        }
        world.addFreshEntity(core);
        return true;
    }

    private static WeightedProjectile rollProjectile(RandomSource random, List<? extends String> entries,
            ItemStack fallbackAmmo) {
        int totalWeight = 0;
        for (String entry : entries) {
            WeightedProjectile projectile = parseProjectile(entry);
            if (projectile != null)
                totalWeight += projectile.weight;
        }

        if (totalWeight <= 0)
            return fallbackBullet(fallbackAmmo);

        int roll = random.nextInt(totalWeight);
        for (String entry : entries) {
            WeightedProjectile projectile = parseProjectile(entry);
            if (projectile == null)
                continue;
            if (roll < projectile.weight)
                return projectile;
            roll -= projectile.weight;
        }
        return fallbackBullet(fallbackAmmo);
    }

    @Nullable
    private static WeightedProjectile parseProjectile(String entry) {
        String[] parts = entry.split("=", 2);
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        if (id == null)
            return null;

        int weight = 1;
        if (parts.length == 2) {
            try {
                weight = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (weight <= 0)
            return null;

        if ((OBSIDIAN_CORE.equals(id) || OBSIDIAN_CORE_ENTITY.equals(id)) && GWREEntities.OBSIDIAN_CORE != null
                && GWREEntities.OBSIDIAN_CORE.isPresent()) {
            return WeightedProjectile.obsidianCore(weight);
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (!(item instanceof IBullet))
            return null;
        return WeightedProjectile.bullet(item, weight);
    }

    private static WeightedProjectile fallbackBullet(ItemStack fallbackAmmo) {
        Item item = ForgeRegistries.ITEMS.getValue(IRON_TICKET);
        if (item instanceof IBullet)
            return WeightedProjectile.bullet(item, 0);
        return WeightedProjectile.bullet(fallbackAmmo.getItem(), 0);
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.destiny_seven.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.destiny_seven.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.destiny_seven.desc3").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.destiny_seven.desc4").withStyle(ChatFormatting.GRAY));
    }

    private static void ensureGeckoId(ItemStack stack, Level level) {
        if (stack.isEmpty() || !(stack.getItem() instanceof DestinyGunItem)
                || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        GeoItem.getOrAssignId(stack, serverLevel);
    }

    private static void setGeckoAnimation(ItemStack stack, String animation, int ticks) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(NBT_GECKO_ANIMATION, animation);
        tag.putInt(NBT_GECKO_ANIMATION_TIMER, ticks);
        tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
    }

    private static void tickGeckoAnimation(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int timer = tag.getInt(NBT_GECKO_ANIMATION_TIMER);
        if (timer > 1) {
            tag.putInt(NBT_GECKO_ANIMATION_TIMER, timer - 1);
        } else if (timer == 1) {
            tag.putInt(NBT_GECKO_ANIMATION_TIMER, 0);
            tag.remove(NBT_GECKO_ANIMATION);
            tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
        }
    }

    private static RawAnimation getGeckoAnimation(@Nullable ItemStack stack) {
        if (stack != null && stack.hasTag() && stack.getOrCreateTag().getInt(NBT_GECKO_ANIMATION_TIMER) > 0) {
            String animation = stack.getOrCreateTag().getString(NBT_GECKO_ANIMATION);
            if (GECKO_ANIM_FIRE_BAD.equals(animation)) {
                return FIRE_BAD_ANIM;
            }
            if (GECKO_ANIM_FIRE_DOUBLE.equals(animation)) {
                return FIRE_DOUBLE_ANIM;
            }
            if (GECKO_ANIM_FIRE_TRIPLE.equals(animation)) {
                return FIRE_TRIPLE_ANIM;
            }
            if (GECKO_ANIM_FIRE_JACKPOT.equals(animation)) {
                return FIRE_JACKPOT_ANIM;
            }
            if (GECKO_ANIM_FIRE.equals(animation)) {
                return FIRE_ANIM;
            }
        }

        return IDLE_ANIM;
    }

    private static float getGeckoAnimationSpeed(@Nullable ItemStack stack) {
        if (stack != null && stack.hasTag() && stack.getOrCreateTag().getInt(NBT_GECKO_ANIMATION_TIMER) > 0) {
            return FIRE_ANIMATION_SPEED;
        }

        return 1.0F;
    }

    private static int scaledFireTicks(int baseTicks) {
        return Math.max(1, Math.round(baseTicks / Math.max(0.01F, FIRE_ANIMATION_SPEED)));
    }

    private static void restartAnimationIfSequenceChanged(AnimationState<DestinyGunItem> state,
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

    private enum Ticket {
        IRON {
            @Override
            int bustWeight() {
                return GWREConfig.DESTINY.ironBustWeight.get();
            }

            @Override
            int doubleWeight() {
                return GWREConfig.DESTINY.ironDoubleWeight.get();
            }

            @Override
            int tripleWeight() {
                return GWREConfig.DESTINY.ironTripleWeight.get();
            }

            @Override
            int jackpotWeight() {
                return GWREConfig.DESTINY.ironJackpotWeight.get();
            }
        },
        GOLD {
            @Override
            int bustWeight() {
                return GWREConfig.DESTINY.goldBustWeight.get();
            }

            @Override
            int doubleWeight() {
                return GWREConfig.DESTINY.goldDoubleWeight.get();
            }

            @Override
            int tripleWeight() {
                return GWREConfig.DESTINY.goldTripleWeight.get();
            }

            @Override
            int jackpotWeight() {
                return GWREConfig.DESTINY.goldJackpotWeight.get();
            }
        },
        DIAMOND {
            @Override
            int bustWeight() {
                return GWREConfig.DESTINY.diamondBustWeight.get();
            }

            @Override
            int doubleWeight() {
                return GWREConfig.DESTINY.diamondDoubleWeight.get();
            }

            @Override
            int tripleWeight() {
                return GWREConfig.DESTINY.diamondTripleWeight.get();
            }

            @Override
            int jackpotWeight() {
                return GWREConfig.DESTINY.diamondJackpotWeight.get();
            }
        };

        abstract int bustWeight();

        abstract int doubleWeight();

        abstract int tripleWeight();

        abstract int jackpotWeight();
    }

    private enum Outcome {
        BUST("msg.gwrexpansions.bust") {
            @Override
            int getShots() {
                return GWREConfig.DESTINY.bustShots.get();
            }

            @Override
            SoundEvent getSound() {
                return MYFSounds.dameFortunaCardWrong.get();
            }

            @Override
            String animationKey() {
                return GECKO_ANIM_FIRE_BAD;
            }
        },
        DOUBLE("msg.gwrexpansions.double") {
            @Override
            int getShots() {
                return GWREConfig.DESTINY.doubleShots.get();
            }

            @Override
            SoundEvent getSound() {
                return MYFSounds.dameFortunaClap.get();
            }

            @Override
            String animationKey() {
                return GECKO_ANIM_FIRE_DOUBLE;
            }
        },
        TRIPLE("msg.gwrexpansions.triple") {
            @Override
            int getShots() {
                return GWREConfig.DESTINY.tripleShots.get();
            }

            @Override
            SoundEvent getSound() {
                return MYFSounds.dameFortunaCardRight.get();
            }

            @Override
            String animationKey() {
                return GECKO_ANIM_FIRE_TRIPLE;
            }
        },
        JACKPOT("msg.gwrexpansions.jackpot") {
            @Override
            int getShots() {
                return GWREConfig.DESTINY.jackpotShots.get();
            }

            @Override
            SoundEvent getSound() {
                return MYFSounds.dameFortunaChipsStart.get();
            }

            @Override
            String animationKey() {
                return GECKO_ANIM_FIRE_JACKPOT;
            }
        };

        private final String messageKey;

        Outcome(String messageKey) {
            this.messageKey = messageKey;
        }

        abstract int getShots();

        abstract SoundEvent getSound();

        abstract String animationKey();
    }

    private enum ProjectileKind {
        BULLET,
        OBSIDIAN_CORE
    }

    private static class WeightedProjectile {
        private final ProjectileKind kind;
        private final Item item;
        private final int weight;

        private WeightedProjectile(ProjectileKind kind, Item item, int weight) {
            this.kind = kind;
            this.item = item;
            this.weight = weight;
        }

        private static WeightedProjectile bullet(Item item, int weight) {
            return new WeightedProjectile(ProjectileKind.BULLET, item, weight);
        }

        private static WeightedProjectile obsidianCore(int weight) {
            return new WeightedProjectile(ProjectileKind.OBSIDIAN_CORE, null, weight);
        }

        private ItemStack asAmmoStack() {
            return new ItemStack(this.item);
        }
    }
}
