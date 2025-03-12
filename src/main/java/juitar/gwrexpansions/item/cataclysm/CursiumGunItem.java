package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.registry.CompatCataclysm;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class CursiumGunItem extends GunItem {
    public CursiumGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability);
    }
    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatCataclysm.tagBaseBullets)) return new ItemStack(CompatCataclysm.cursium_bullet.get());
        else return ammo;
    }
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.cursium_sniper.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.cursium_sniper.desc2") .withStyle(ChatFormatting.GRAY));
    }
}
