package juitar.gwrexpansions.item.iceandfire;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import juitar.gwrexpansions.registry.CompatIceandfire;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class LightningDragonGatlingItem extends ConfigurableGatlingItem {
    public LightningDragonGatlingItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier){
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }
    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatIceandfire.tagBaseBullets)) return new ItemStack(CompatIceandfire.dragonsteel_lightning_bullet.get());
        else return ammo;
    }
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.lightning_dragon_gun.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gunswithoutroses.gatling.hold").withStyle(ChatFormatting.GRAY));
    }
}
