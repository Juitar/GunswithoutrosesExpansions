package juitar.gwrexpansions.item.minecraft;

import java.util.List;
import javax.annotation.Nullable;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import juitar.gwrexpansions.entity.minecraft.SlimeBulletEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SlimeBulletItem extends BulletItem {
    protected int maxBounces;

    public SlimeBulletItem(Properties properties, int damage, int maxBounces) {
        super(properties, damage);
        this.maxBounces = maxBounces;
    }

    @Override
    public SlimeBulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        SlimeBulletEntity entity = new SlimeBulletEntity(world, shooter);
        entity.setItem(stack);
        entity.setDamage(damage);
        entity.setMaxBounces(maxBounces);
        return entity;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.slime_bullet.desc", maxBounces).withStyle(ChatFormatting.GRAY));
    }
}

