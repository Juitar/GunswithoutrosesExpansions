package juitar.gwrexpansions.item.iceandfire;

import juitar.gwrexpansions.entity.iceandfire.IceDragonSteelBulletEntity;
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

public class IceDragonSteelBulletItem extends BulletItem {
    public IceDragonSteelBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }
    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        IceDragonSteelBulletEntity bullet = new IceDragonSteelBulletEntity(world, shooter);
        bullet.setItem(stack);
        ItemStack mainHand = shooter.getMainHandItem();
        ItemStack offHand = shooter.getOffhandItem();
        if (mainHand.getItem() instanceof IceDragonGunItem || offHand.getItem() instanceof IceDragonGunItem || mainHand.getItem() instanceof IceDragonGatlingItem || offHand.getItem() instanceof IceDragonGatlingItem) {
            bullet.setDamage(damage + 3);
        }else{
            bullet.setDamage(damage);
        }
        return bullet;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("dragon_sword_ice.hurt2").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.firedragon_bonus",4).withStyle(ChatFormatting.GREEN));
    }
}
