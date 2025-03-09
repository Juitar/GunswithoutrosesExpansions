package juitar.gwrexpansions.item.iceandfire;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class SilverBulletItem extends BulletItem {
    public SilverBulletItem(Properties properties,int damage) {
        super(properties,damage);
    }
    @Override
    public void onLivingEntityHit(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, Level world,boolean headshot){
        if (target.getMobType() == MobType.UNDEAD) {
            if(headshot)
                target.hurt(shooter.level().damageSources().magic(), (float) (bullet.getDamage()*2.0F));
            else
                target.hurt(shooter.level().damageSources().magic(), (float) (bullet.getDamage()+4.0F));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.silver_bullet.desc").withStyle(ChatFormatting.GRAY));
    }
}
