package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
import java.util.List;

public class LavapowerBulletItem extends BulletItem {

    public LavapowerBulletItem(Properties properties, int damage) {
        super(properties,damage);
    }

    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter){
        LavapowerBulletEntity bullet = new LavapowerBulletEntity(world, shooter);
        bullet.setOwner(shooter);
        bullet.setItem(stack);
        bullet.setDamage(damage);
        return bullet;
    }



    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.lavapower_bullet.desc").withStyle(ChatFormatting.GRAY));
    }
}
