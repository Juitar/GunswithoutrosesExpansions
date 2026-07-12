package juitar.gwrexpansions.compat.kubejs;

import juitar.gwrexpansions.item.GunSkillItem;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.item.ShotgunItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.List;

public class KubeJSShotgunItem extends ShotgunItem implements GunSkillItem, KubeJSGunEventSource {
    private final ProjectileConversion conversion;
    private final int pierceCount;
    private final List<AmmoConversion> ammoConversions;
    private final KubeJSGunSkill skill;
    private final KubeJSGunEvents callbacks;

    public KubeJSShotgunItem(Properties properties, KubeJSShotgunItemBuilder builder) {
        super(properties, builder.bonusDamage, builder.damageMultiplier, builder.fireDelay, builder.inaccuracy, builder.enchantability, builder.projectiles);
        chanceFreeShot(builder.chanceFreeShot);
        headshotMult(builder.headshotMultiplier);
        knockback(builder.knockback);
        projectileSpeed(builder.projectileSpeed);
        conversion = builder.projectileConversion;
        pierceCount = builder.pierceCount;
        ammoConversions = List.copyOf(builder.ammoConversions);
        skill = new KubeJSGunSkill(this, builder.skillUse, builder.skillCooldown);
        callbacks = new KubeJSGunEvents(builder);
    }

    @Override protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bullet, boolean bulletFree) { GunProjectileSupport.shoot(this, level, player, gun, ammo, bullet, bulletFree, conversion, pierceCount, ammoConversions, callbacks); }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) { super.inventoryTick(stack, level, entity, slot, selected); skill.tick(stack, level); }
    @Override public boolean canUseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) { return skill.canUse(player, hand, stack); }
    @Override public void useGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) { skill.use(player, hand, stack); }
    @Override public KubeJSGunEvents callbacks() { return callbacks; }
}
