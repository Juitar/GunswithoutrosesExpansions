package juitar.gwrexpansions.item.iceandfire;

import com.github.alexthe666.iceandfire.entity.EntityFireDragon;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import com.github.alexthe666.iceandfire.event.ServerEvents;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class LightningDragonSteelBulletItem extends BulletItem {
    public LightningDragonSteelBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }
    @Override
    public void onLivingEntityHit(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, Level world){
        boolean flag = true;

        if (!shooter.level().isClientSide ) {
            LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(target.level());
            lightningboltentity.getTags().add(ServerEvents.BOLT_DONT_DESTROY_LOOT);
            lightningboltentity.getTags().add(shooter.getStringUUID());
            lightningboltentity.moveTo(target.position());
            if (!target.level().isClientSide) {
                target.level().addFreshEntity(lightningboltentity);
            }
        }
        target.knockback(1F, bullet.getX() - target.getX(), bullet.getZ() - target.getZ());
        if(target instanceof EntityIceDragon||target instanceof EntityFireDragon){
            target.hurt(shooter.level().damageSources().drown(),2.0F);
        }
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("dragon_sword_lightning.hurt2").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.firedragon_bonus",2).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.icedragon_bonus",2).withStyle(ChatFormatting.GREEN));
    }
}
