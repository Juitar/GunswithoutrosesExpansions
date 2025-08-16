package juitar.gwrexpansions.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Aimed效果 - 标记被Hellforge击中的目标
 * 用于追踪子弹的目标识别
 */
public class AimedEffect extends MobEffect {
    
    public AimedEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000); // 红色效果
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 这个效果主要用于标记，不需要持续的tick效果
        // 可以在这里添加粒子效果等视觉反馈
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 不需要每tick都执行效果
        return false;
    }
}
