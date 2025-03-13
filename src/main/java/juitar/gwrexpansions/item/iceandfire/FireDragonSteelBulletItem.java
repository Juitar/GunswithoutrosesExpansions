package juitar.gwrexpansions.item.iceandfire;
import juitar.gwrexpansions.entity.iceandfire.FireDragonSteelBulletEntity;
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
import java.util.function.Supplier;

public class FireDragonSteelBulletItem extends BulletItem {
    public FireDragonSteelBulletItem(Properties properties,  int damage) {
        super(properties, damage);
    }
    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        FireDragonSteelBulletEntity bullet = new FireDragonSteelBulletEntity(world, shooter);
        bullet.setItem(stack);
        ItemStack mainHand = shooter.getMainHandItem();
        ItemStack offHand = shooter.getOffhandItem();
        if (mainHand.getItem() instanceof FireDragonGunItem || offHand.getItem() instanceof FireDragonGunItem || mainHand.getItem() instanceof FireDragonGatlingItem || offHand.getItem() instanceof FireDragonGatlingItem) {
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
        tooltip.add(Component.translatable("dragon_sword_fire.hurt2").withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.icedragon_bonus",4).withStyle(ChatFormatting.GREEN));
    }
}
