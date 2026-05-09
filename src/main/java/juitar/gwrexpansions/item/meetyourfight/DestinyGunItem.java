package juitar.gwrexpansions.item.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.ObsidianCoreEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.meetyourfight.registry.MYFSounds;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class DestinyGunItem extends ConfigurableGunItem {
    private static final String PITY_TAG = "DestinyPityShots";
    private static final ResourceLocation IRON_TICKET = new ResourceLocation("gunswithoutroses", "iron_bullet");
    private static final ResourceLocation GOLD_TICKET = new ResourceLocation("gwrexpansions", "golden_bullet");
    private static final ResourceLocation DIAMOND_TICKET = new ResourceLocation("gwrexpansions", "diamond_bullet");
    private static final ResourceLocation OBSIDIAN_CORE = new ResourceLocation("gwrexpansions", "obsidian_core");
    private static final ResourceLocation OBSIDIAN_CORE_ENTITY = new ResourceLocation("gwrexpansions", "obsidian_core_entity");

    public DestinyGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        Ticket ticket = getTicket(ammo);
        if (ticket == null) {
            super.shoot(world, player, gun, ammo, bulletItem, bulletFree);
            return;
        }

        int pityShots = gun.getOrCreateTag().getInt(PITY_TAG);
        Outcome outcome = rollOutcome(world.getRandom(), ticket, pityShots);
        List<? extends String> bulletPool = outcome == Outcome.BUST ? GWREConfig.DESTINY.bustBulletPool.get() : GWREConfig.DESTINY.rewardBulletPool.get();
        int shots = outcome.getShots();

        player.displayClientMessage(Component.translatable(outcome.messageKey), true);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), outcome.getSound(), SoundSource.PLAYERS, 1.0F, 1.0F);

        if (outcome == Outcome.JACKPOT) {
            gun.getOrCreateTag().putInt(PITY_TAG, 0);
        } else {
            gun.getOrCreateTag().putInt(PITY_TAG, pityShots + 1);
        }

        float extraInaccuracy = shots != 1 ? 3.0F : 1.0F;
        for (int i = 0; i < shots; ++i) {
            WeightedProjectile projectile = rollProjectile(world.getRandom(), bulletPool, ammo);
            if (projectile.kind == ProjectileKind.OBSIDIAN_CORE) {
                if (shootObsidianCore(world, player, gun)) {
                    continue;
                }
                projectile = fallbackBullet(ammo);
            }
            shootBullet(world, player, gun, projectile.asAmmoStack(), bulletItem, bulletFree, extraInaccuracy);
        }
    }

    private static Ticket getTicket(ItemStack ammo) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(ammo.getItem());
        if (IRON_TICKET.equals(id)) return Ticket.IRON;
        if (GOLD_TICKET.equals(id)) return Ticket.GOLD;
        if (DIAMOND_TICKET.equals(id)) return Ticket.DIAMOND;
        return null;
    }

    private static Outcome rollOutcome(RandomSource random, Ticket ticket, int pityShots) {
        GWREConfig.DestinyConfig config = GWREConfig.DESTINY;
        int bust = ticket.bustWeight();
        int doubled = ticket.doubleWeight();
        int triple = ticket.tripleWeight();
        int jackpot = ticket.jackpotWeight();
        int pityBonus = Math.min(config.pityMaxJackpotWeight.get(), pityShots * config.pityJackpotWeightPerShot.get());
        bust = Math.max(0, bust - pityBonus);
        jackpot += pityBonus;

        int total = bust + doubled + triple + jackpot;
        if (total <= 0) return Outcome.BUST;

        int roll = random.nextInt(total);
        if (roll < bust) return Outcome.BUST;
        roll -= bust;
        if (roll < doubled) return Outcome.DOUBLE;
        roll -= doubled;
        if (roll < triple) return Outcome.TRIPLE;
        return Outcome.JACKPOT;
    }

    private void shootBullet(Level world, Player player, ItemStack gun, ItemStack firedAmmo, IBullet fallbackBullet, boolean bulletFree, float extraInaccuracy) {
        IBullet firedBullet = firedAmmo.getItem() instanceof IBullet ? (IBullet) firedAmmo.getItem() : fallbackBullet;
        BulletEntity shot = firedBullet.createProjectile(world, firedAmmo, player);
        shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float)this.getProjectileSpeed(gun, player), (float)this.getInaccuracy(gun, player) + extraInaccuracy);
        shot.setDamage(Math.max(0.0D, shot.getDamage() + this.getBonusDamage(gun, player)) * this.getDamageMultiplier(gun, player));
        shot.setKnockbackStrength(shot.getKnockbackStrength() + this.getKnockbackBonus(gun, player));
        shot.setHeadshotMultiplier(this.getHeadshotMultiplier(gun, player));
        this.affectBulletEntity(player, gun, shot, bulletFree);
        world.addFreshEntity(shot);
    }

    private boolean shootObsidianCore(Level world, Player player, ItemStack gun) {
        if (GWREEntities.OBSIDIAN_CORE == null || !GWREEntities.OBSIDIAN_CORE.isPresent()) {
            return false;
        }

        ObsidianCoreEntity.SpellType[] spellTypes = ObsidianCoreEntity.SpellType.values();
        ObsidianCoreEntity.SpellType spellType = spellTypes[world.getRandom().nextInt(spellTypes.length)];
        ObsidianCoreEntity core = new ObsidianCoreEntity(GWREEntities.OBSIDIAN_CORE.get(), world, player, spellType);
        double baseDamage = GWREConfig.DESTINY.obsidianCoreBaseDamage.get();
        core.setBaseDamage(Math.max(1.0D, (baseDamage + this.getBonusDamage(gun, player)) * this.getDamageMultiplier(gun, player)));
        core.setAOERadiusMultiplier(1.0F);
        core.setMaxRange(30.0F);

        Vec3 look = player.getLookAngle();
        core.setPos(
            player.getX() + look.x * 0.5D,
            player.getEyeY() - 0.1D + look.y * 0.5D,
            player.getZ() + look.z * 0.5D
        );
        core.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float)this.getProjectileSpeed(gun, player), (float)this.getInaccuracy(gun, player));
        world.addFreshEntity(core);
        return true;
    }

    private static WeightedProjectile rollProjectile(RandomSource random, List<? extends String> entries, ItemStack fallbackAmmo) {
        int totalWeight = 0;
        for (String entry : entries) {
            WeightedProjectile projectile = parseProjectile(entry);
            if (projectile != null) totalWeight += projectile.weight;
        }

        if (totalWeight <= 0) return fallbackBullet(fallbackAmmo);

        int roll = random.nextInt(totalWeight);
        for (String entry : entries) {
            WeightedProjectile projectile = parseProjectile(entry);
            if (projectile == null) continue;
            if (roll < projectile.weight) return projectile;
            roll -= projectile.weight;
        }
        return fallbackBullet(fallbackAmmo);
    }

    @Nullable
    private static WeightedProjectile parseProjectile(String entry) {
        String[] parts = entry.split("=", 2);
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        if (id == null) return null;

        int weight = 1;
        if (parts.length == 2) {
            try {
                weight = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (weight <= 0) return null;

        if ((OBSIDIAN_CORE.equals(id) || OBSIDIAN_CORE_ENTITY.equals(id)) && GWREEntities.OBSIDIAN_CORE != null && GWREEntities.OBSIDIAN_CORE.isPresent()) {
            return WeightedProjectile.obsidianCore(weight);
        }

        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (!(item instanceof IBullet)) return null;
        return WeightedProjectile.bullet(item, weight);
    }

    private static WeightedProjectile fallbackBullet(ItemStack fallbackAmmo) {
        Item item = ForgeRegistries.ITEMS.getValue(IRON_TICKET);
        if (item instanceof IBullet) return WeightedProjectile.bullet(item, 0);
        return WeightedProjectile.bullet(fallbackAmmo.getItem(), 0);
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.destiny_seven.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.destiny_seven.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.destiny_seven.desc3").withStyle(ChatFormatting.GRAY));
    }

    private enum Ticket {
        IRON {
            @Override int bustWeight() { return GWREConfig.DESTINY.ironBustWeight.get(); }
            @Override int doubleWeight() { return GWREConfig.DESTINY.ironDoubleWeight.get(); }
            @Override int tripleWeight() { return GWREConfig.DESTINY.ironTripleWeight.get(); }
            @Override int jackpotWeight() { return GWREConfig.DESTINY.ironJackpotWeight.get(); }
        },
        GOLD {
            @Override int bustWeight() { return GWREConfig.DESTINY.goldBustWeight.get(); }
            @Override int doubleWeight() { return GWREConfig.DESTINY.goldDoubleWeight.get(); }
            @Override int tripleWeight() { return GWREConfig.DESTINY.goldTripleWeight.get(); }
            @Override int jackpotWeight() { return GWREConfig.DESTINY.goldJackpotWeight.get(); }
        },
        DIAMOND {
            @Override int bustWeight() { return GWREConfig.DESTINY.diamondBustWeight.get(); }
            @Override int doubleWeight() { return GWREConfig.DESTINY.diamondDoubleWeight.get(); }
            @Override int tripleWeight() { return GWREConfig.DESTINY.diamondTripleWeight.get(); }
            @Override int jackpotWeight() { return GWREConfig.DESTINY.diamondJackpotWeight.get(); }
        };

        abstract int bustWeight();
        abstract int doubleWeight();
        abstract int tripleWeight();
        abstract int jackpotWeight();
    }

    private enum Outcome {
        BUST("msg.gwrexpansions.bust") {
            @Override int getShots() { return GWREConfig.DESTINY.bustShots.get(); }
            @Override SoundEvent getSound() { return MYFSounds.dameFortunaCardWrong.get(); }
        },
        DOUBLE("msg.gwrexpansions.double") {
            @Override int getShots() { return GWREConfig.DESTINY.doubleShots.get(); }
            @Override SoundEvent getSound() { return MYFSounds.dameFortunaClap.get(); }
        },
        TRIPLE("msg.gwrexpansions.triple") {
            @Override int getShots() { return GWREConfig.DESTINY.tripleShots.get(); }
            @Override SoundEvent getSound() { return MYFSounds.dameFortunaChipsStart.get(); }
        },
        JACKPOT("msg.gwrexpansions.jackpot") {
            @Override int getShots() { return GWREConfig.DESTINY.jackpotShots.get(); }
            @Override SoundEvent getSound() { return MYFSounds.dameFortunaCardRight.get(); }
        };

        private final String messageKey;

        Outcome(String messageKey) {
            this.messageKey = messageKey;
        }

        abstract int getShots();
        abstract SoundEvent getSound();
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
