package juitar.gwrexpansions.item.cataclysm;


import juitar.gwrexpansions.entity.cataclysm.IgnitiumBulletEntity;
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

public class IgnitiumBulletItem extends BulletItem {
    public IgnitiumBulletItem(Properties properties,  int damage) {
        super(properties,damage);
    }
    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        IgnitiumBulletEntity bullet = new IgnitiumBulletEntity(world, shooter);
        // 检查发射时的武器是否是IgnitiumGatlingItem
        bullet.setOwner(shooter);
        bullet.setItem(stack);
        bullet.setDamage(damage);
        if (!(shooter.getMainHandItem().getItem() instanceof IgnitiumGatlingItem||shooter.getOffhandItem().getItem() instanceof IgnitiumGatlingItem)) return bullet;
        // 给子弹添加Ignitium属性
        bullet.setHealing(true);

        return bullet;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_bullet.desc").withStyle(ChatFormatting.GRAY));
    }
}
