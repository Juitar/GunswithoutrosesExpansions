package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Portal_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import juitar.gwrexpansions.config.GWREConfig;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRDamage;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.util.Objects;


public class TidalBulletEntity extends BulletEntity {
    private double BURN_MULTI = 1.0;
    private  double AQUATIC_MULTI = 1.5;
    private static final double PORTAL_DAMAGE = GWREConfig.BulletConfig.portal_damage.get();
    private static final double PORTAL_HPDAMAGE = GWREConfig.BulletConfig.portal_hpdamage.get();
    public TidalBulletEntity(EntityType<? extends BulletEntity> type, Level world) {
        super(type, world);
    }
    public TidalBulletEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }
    @Override
    protected double waterInertia() {
        return 1.2; // 在水中加速
    }
    @Override
    protected void onHitEntity(EntityHitResult raytrace) {
        if (!this.level().isClientSide) {
            Entity target = raytrace.getEntity();
            Entity shooter = this.getOwner();
            Item item = this.getItemRaw().getItem();
            IBullet bullet = item instanceof IBullet ? (IBullet)item :  GWRItems.ironBullet.get();
            if (this.isOnFire()) {
                target.setSecondsOnFire(5);
            }
            int lastHurtResistant = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (target instanceof LivingEntity Livingtarget) {
                MobEffectInstance Effect = Livingtarget.getEffect(ModEffect.EFFECTABYSSAL_CURSE.get());
                int Effectlevel = Effect != null ? Effect.getAmplifier() + 1 : 0;
                BURN_MULTI += Effectlevel * 0.1;
            }
            boolean headshot = this.hasHeadshot(target);
            boolean WaterCreature = target.getTags().contains("aquatic");
            float hitdamage = (float)bullet.modifyDamage(this.damage * (headshot ? this.headshotMult : (double)1.0F) * (WaterCreature ? AQUATIC_MULTI : (double)1.0F) * BURN_MULTI, this, target, shooter, this.level(), headshot);
            boolean damaged = shooter == null ? target.hurt(GWRDamage.gunDamage(this.level().registryAccess(), this), hitdamage) : target.hurt(GWRDamage.gunDamage(this.level().registryAccess(), this, shooter), hitdamage);
            if (damaged && target instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity)target;
                if (this.knockbackStrength > (double)0.0F) {
                    double actualKnockback = this.knockbackStrength;
                    Vec3 vec = this.getDeltaMovement().multiply(1.0F, 0.0F, 1.0F).normalize().scale(actualKnockback * 0.6);
                    if (vec.lengthSqr() > (double)0.0F) {
                        livingTarget.push(vec.x, 0.1, vec.z);
                    }
                }
                if (shooter instanceof LivingEntity) {
                    this.doEnchantDamageEffects((LivingEntity)shooter, target);
                }
                bullet.onLivingEntityHit(this, livingTarget, shooter, this.level(), headshot);
            } else if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }
            // 在伤害计算后添加传送门生成逻辑
            if (damaged && target instanceof LivingEntity Livingtarget && shooter instanceof LivingEntity) {
                MobEffectInstance currentEffect = Livingtarget.getEffect(ModEffect.EFFECTABYSSAL_CURSE.get());
                int newAmplifier = Math.min(4, currentEffect != null ? currentEffect.getAmplifier() + 1 : 0);
                // 添加新效果，重置持续时间
                if(newAmplifier < 4)
                Livingtarget.addEffect(new MobEffectInstance(ModEffect.EFFECTABYSSAL_CURSE.get(), 200, newAmplifier));
                else {
                    Livingtarget.removeEffect(ModEffect.EFFECTABYSSAL_CURSE.get());
                    spawnPortal((LivingEntity) shooter, this.level(), (LivingEntity) target);
                }
            }
        }
    }

    private void spawnPortal(LivingEntity shooter, Level world, LivingEntity target) {
        if (!ModList.get().isLoaded("cataclysm")) {
            return;
        }
        
        BlockPos targetPos = target.blockPosition();
        BlockPos portalPos = null;
        
        // 检查目标脚下是否有方块
        if (!world.getBlockState(targetPos.below()).isAir()) {
            portalPos = targetPos.below();
        } else {
            // 向下投影寻找最近的方块
            for (int i = 1; i <= 10; i++) {
                BlockPos checkPos = targetPos.below(i);
                if (!world.getBlockState(checkPos).isAir() || !world.getFluidState(checkPos).isEmpty()) {
                    portalPos = checkPos;
                    break;
                }
            }
        }
        
        if (portalPos != null) {
            // 创建深渊传送门实体
            Abyss_Blast_Portal_Entity portal = new Abyss_Blast_Portal_Entity(
                world,
                portalPos.getX() + 0.5,
                portalPos.getY() + 1.0,
                portalPos.getZ() + 0.5,
                0.0F,    // yaw
                0,      // warmup delay
                (float)PORTAL_DAMAGE,    // damage
                (float)PORTAL_HPDAMAGE,   // hp damage
                shooter  // caster
            );
            world.addFreshEntity(portal);
            world.playSound(null, portalPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
}
