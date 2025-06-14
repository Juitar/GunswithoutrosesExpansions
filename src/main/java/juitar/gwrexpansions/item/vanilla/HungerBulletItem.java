package juitar.gwrexpansions.item.vanilla;

import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class HungerBulletItem extends BulletItem {

        public HungerBulletItem(Properties properties, int damage) {
            super(properties, damage);
        }

        @Override
        public void consume(ItemStack stack, Player player) {
            if (player.getFoodData().getFoodLevel() <= 0) player.hurt(player.damageSources().starve(), 1);
            player.getFoodData().addExhaustion(3);
        }

        @Override
        public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
            ItemStack fake = new ItemStack(VanillaItem.hunger_bullet.get());
            fake.getOrCreateTag().putBoolean("shot", true);
            return super.createProjectile(world, fake, shooter);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
            super.appendHoverText(stack, worldIn, tooltip, flagIn);
            tooltip.add(Component.translatable("tooltip.gwrexpansions.hungerbulletitem").withStyle(ChatFormatting.GRAY));
        }

        public static boolean isShot(ItemStack stack) {
            return !stack.isEmpty() && stack.getOrCreateTag().contains("shot") && stack.getOrCreateTag().getBoolean("shot");
        }
    }

