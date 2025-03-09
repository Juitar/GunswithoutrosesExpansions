package juitar.gwrexpansions.item.iceandfire;

import juitar.gwrexpansions.registry.CompatIceandfire;
import lykrast.gunswithoutroses.item.GatlingItem;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class FireDragonGatlingItem extends GatlingItem {
    public FireDragonGatlingItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability){
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability);
    }
    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatIceandfire.tagBaseBullets)) return new ItemStack(CompatIceandfire.dragonsteel_fire_bullet.get());
        else return ammo;
    }
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.fire_dragon_gun.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gunswithoutroses.gatling.hold").withStyle(ChatFormatting.GRAY));
    }
}
