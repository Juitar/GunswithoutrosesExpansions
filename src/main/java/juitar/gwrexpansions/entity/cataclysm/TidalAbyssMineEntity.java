package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Mine_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TidalAbyssMineEntity extends Abyss_Mine_Entity {
    private static final String DAMAGE_TAG = "TidalMineDamage";

    private float tidalDamage;

    public TidalAbyssMineEntity(EntityType<? extends TidalAbyssMineEntity> type, Level level) {
        super(type, level);
    }

    public TidalAbyssMineEntity(Level level, double x, double y, double z, float yaw, int warmup,
            LivingEntity caster, float damage) {
        this(GWREEntities.TIDAL_ABYSS_MINE.get(), level);
        configure(x, y, z, yaw, warmup, caster, damage);
    }

    public void configure(double x, double y, double z, float yaw, int warmup, LivingEntity caster, float damage) {
        CompoundTag warmupTag = new CompoundTag();
        warmupTag.putInt("Warmup", warmup);
        super.readAdditionalSaveData(warmupTag);
        setCaster(caster);
        setYRot(yaw);
        setPos(x, y, z);
        this.tidalDamage = Math.max(0.0F, damage);
    }

    @Override
    protected void explode(LivingEntity target) {
        LivingEntity caster = getCaster();
        if (!target.isAlive()) {
            return;
        }
        if (caster != null && (target == caster || caster.isAlliedTo(target) || target.isAlliedTo(caster))) {
            return;
        }

        level().explode(caster == null ? this : caster, getX(), getY(0.0625D), getZ(), 0.0F,
                Level.ExplosionInteraction.NONE);
        if (tidalDamage > 0.0F) {
            DamageSource source = caster == null
                    ? level().damageSources().magic()
                    : level().damageSources().indirectMagic(this, caster);
            target.hurt(source, tidalDamage);
        }

        MobEffect abyssalFear = ModEffect.EFFECTABYSSAL_FEAR.get();
        target.addEffect(new MobEffectInstance(abyssalFear, 200, 0));
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.tidalDamage = tag.getFloat(DAMAGE_TAG);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(DAMAGE_TAG, this.tidalDamage);
    }
}
