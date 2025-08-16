package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.vanilla.SlimeBulletEntity;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 史莱姆弹丸四杀成就触发器
 * 检测一颗史莱姆弹丸是否击杀了四个或以上目标（允许重复）
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class SlimeBulletTripleKillTrigger extends SimpleCriterionTrigger<SlimeBulletTripleKillTrigger.TriggerInstance> {

	private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "slime_bullet_triple_kill");

	// 存储每个史莱姆弹丸的击杀计数（允许重复实体）
	private static final Map<UUID, Integer> bulletKillCounts = new HashMap<>();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate entityPredicate, DeserializationContext deserializationContext) {
		return new TriggerInstance(entityPredicate);
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, instance -> instance.matches(player));
	}

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		LivingEntity killed = event.getEntity();
		Entity direct = event.getSource().getDirectEntity(); // 直接伤害来源（弹丸）

		// 检查是否是史莱姆弹丸造成的死亡
		if (direct instanceof SlimeBulletEntity slimeBullet) {
			Entity shooter = slimeBullet.getOwner();

			// 射手必须是玩家
			if (shooter instanceof ServerPlayer shooterPlayer) {
				UUID bulletId = slimeBullet.getUUID();

				// 增加击杀计数（允许重复实体）
				int newCount = bulletKillCounts.getOrDefault(bulletId, 0) + 1;
				bulletKillCounts.put(bulletId, newCount);

				// 达到4次则触发
				if (newCount >= 4) {
					GWRECriteria.SLIME_BULLET_TRIPLE_KILL.trigger(shooterPlayer);
					bulletKillCounts.remove(bulletId);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {

		public TriggerInstance(ContextAwarePredicate entityPredicate) {
			super(ID, entityPredicate);
		}

		public boolean matches(ServerPlayer player) {
			return true;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			return super.serializeToJson(serializationContext);
		}
	}
} 