package juitar.gwrexpansions.entity.BOMD;

import com.cerbon.bosses_of_mass_destruction.entity.custom.void_blossom.Spikes;
import com.cerbon.bosses_of_mass_destruction.entity.custom.void_blossom.VoidBlossomClientSpikeHandler;
import com.cerbon.bosses_of_mass_destruction.entity.custom.void_blossom.hitbox.HitboxId;
import com.cerbon.bosses_of_mass_destruction.entity.custom.void_blossom.hitbox.NetworkedHitboxManager;
import com.cerbon.bosses_of_mass_destruction.packet.custom.SpikeS2CPacket;
import com.cerbon.bosses_of_mass_destruction.particle.BMDParticles;
import com.cerbon.bosses_of_mass_destruction.sound.BMDSounds;
import com.cerbon.cerbons_api.api.general.event.EventScheduler;
import com.cerbon.cerbons_api.api.general.event.EventSeries;
import com.cerbon.cerbons_api.api.general.event.TimedEvent;
import com.cerbon.cerbons_api.api.network.Dispatcher;
import com.cerbon.cerbons_api.api.static_utilities.SoundUtils;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

/**
 * 花苞弹实体 - 击中实体后向四处分散孢子子弹
 */
public class BudBulletEntity extends BulletEntity {
    public BudBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public BudBulletEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    @Override
    public void tick() {
        super.tick();
    }

    /**
     * 运行事件调度器 - 供外部调用
     */
    public void runEventScheduler() {
        if (!this.level().isClientSide) {
            // 由于EventScheduler没有tick方法，我们需要手动处理事件
            // 这里我们使用一个简单的计数器来模拟延迟
            // 实际上，EventScheduler的事件应该由BOMD模组的事件系统来处理
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity shooter = this.getOwner();

            // 先处理正常的子弹伤害
            super.onHitEntity(result);

            // 然后生成孢子子弹
            spawnSpores(target.getX(), target.getY() + 0.5, target.getZ(), shooter);


            // 播放花苞爆炸音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GRASS_BREAK, SoundSource.NEUTRAL, 1.0F, 0.8F);

            // 生成绿色粒子效果
            for (int i = 0; i < 15; i++) {
                double d0 = target.getRandomX(0.8D);
                double d1 = target.getRandomY();
                double d2 = target.getRandomZ(0.8D);
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!this.level().isClientSide) {
            Entity shooter = this.getOwner();
            spawnSpores(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, shooter);
            // 播放花苞爆炸音效
            this.level().playSound(null, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    SoundEvents.GRASS_BREAK, SoundSource.NEUTRAL, 1.0F, 0.8F);

        }
    }

    /**
     * 生成孢子子弹向四处分散
     */
    private void spawnSpores(double x, double y, double z, Entity shooter) {
        // 生成8个孢子子弹，围成一圈
        int sporeCount = 8; // 固定为8个
        
        for (int i = 0; i < sporeCount; i++) {
            SporeEntity spore = new SporeEntity(GWREEntities.SPORE.get(), this.level(), (LivingEntity) shooter);

            // 设置基本属性
            spore.setDamage(2); // 孢子伤害较低
            spore.setPos(x, y, z);

            // 计算均匀分布的角度（每个孢子间隔45度，形成一个完整的圆）
            float yRot = i * 45.0F; // 360度/8 = 45度
            float xRot = -10.0F; // 保持水平发射
            
            // 设置较慢的初始速度
            float speed = 0.2F;
            spore.shootFromRotation(spore, xRot, yRot, 0.0F, speed, 0.0F); // 移除散射，使发射更精确

            this.level().addFreshEntity(spore);
        }
    }
}
