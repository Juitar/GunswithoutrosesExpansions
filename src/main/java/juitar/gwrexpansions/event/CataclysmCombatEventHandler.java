package juitar.gwrexpansions.event;

import com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity;
import com.github.L_Ender.cataclysm.entity.util.TidalTentacleUtil;
import com.github.L_Ender.cataclysm.init.ModEntities;
import com.github.L_Ender.cataclysm.init.ModSounds;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.cataclysm.TidalGunItem;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CataclysmCombatEventHandler {
    private CataclysmCombatEventHandler() {
    }

    public static final class CommonEvents {
        private CommonEvents() {
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onPlayerAttack(LivingHurtEvent event) {
            if (event.getEntity().level().isClientSide || event.isCanceled() || event.getAmount() <= 0.0F) {
                return;
            }

            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof Player player) {
                TidalGunItem.rememberPortalPriorityTarget(player, event.getEntity());
            }
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onPlayerHurt(LivingHurtEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.level().isClientSide
                    || event.isCanceled() || event.getAmount() <= 0.0F) {
                return;
            }

            ItemStack stack = TidalGunItem.findHeldTidalPistol(player);
            if (stack.isEmpty() || TidalGunItem.hasTentacleCooldown(player)) {
                return;
            }

            LivingEntity attacker = getLivingAttacker(event);
            if (attacker == null || attacker == player || attacker.isAlliedTo(player) || player.isAlliedTo(attacker)) {
                return;
            }

            GWREConfig.TidalPistolConfig config = TidalGunItem.tidalConfig();
            if (config.tentacleChance.get() <= 0.0D || player.getRandom().nextDouble() >= config.tentacleChance.get()) {
                return;
            }

            Level level = player.level();
            if (!TidalTentacleUtil.canLaunchTentacles(level, player)) {
                return;
            }

            int count = player.isInWaterOrBubble() ? 2 : 1;
            TidalTentacleUtil.retractFarTentacles(level, player);
            boolean spawned = false;
            for (int i = 0; i < count; i++) {
                spawned |= spawnTentacle(level, player, attacker);
            }

            if (spawned) {
                TidalGunItem.setTentacleCooldown(player, config.tentacleCooldownTicks.get());
                level.playSound(null, player.blockPosition(), ModSounds.TIDAL_TENTACLE.get(), SoundSource.PLAYERS,
                        count > 1 ? 0.85F : 0.65F, count > 1 ? 1.05F : 1.2F);
            }
        }

        private static LivingEntity getLivingAttacker(LivingHurtEvent event) {
            Entity source = event.getSource().getEntity();
            if (source instanceof LivingEntity living) {
                return living;
            }

            Entity direct = event.getSource().getDirectEntity();
            return direct instanceof LivingEntity living ? living : null;
        }

        private static boolean spawnTentacle(Level level, Player player, LivingEntity target) {
            Tidal_Tentacle_Entity tentacle = ModEntities.TIDAL_TENTACLE.get().create(level);
            if (tentacle == null) {
                return false;
            }

            tentacle.copyPosition(player);
            level.addFreshEntity(tentacle);
            tentacle.setCreatorEntityUUID(player.getUUID());
            tentacle.setFromEntityID(player.getId());
            tentacle.setToEntityID(target.getId());
            tentacle.copyPosition(player);
            tentacle.setProgress(0.0F);
            TidalTentacleUtil.setLastTentacle(player, tentacle);
            return true;
        }
    }
}
