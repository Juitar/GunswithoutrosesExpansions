package juitar.gwrexpansions.advancement;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.advancement.BOMD.AvadaKedavraTrigger;
import juitar.gwrexpansions.advancement.BOMD.BadToTheBoneTrigger;
import juitar.gwrexpansions.advancement.BOMD.BrustVoidTrigger;
import juitar.gwrexpansions.advancement.BOMD.HellIsFullTrigger;
import juitar.gwrexpansions.advancement.BOMD.MankindIsDeadTrigger;
import juitar.gwrexpansions.advancement.BOMD.ObsidianCakeTrigger;
import juitar.gwrexpansions.advancement.BOMD.ObsidianWandTrigger;
import juitar.gwrexpansions.advancement.MYF.MYFCriteria;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 自定义进度触发器注册处
 */
public final class GWRECriteria {
	public static final SlimeBulletTripleKillTrigger SLIME_BULLET_TRIPLE_KILL = registerTrigger(new SlimeBulletTripleKillTrigger());
	public static final GoldenAppleFromBulletTrigger GOLDEN_APPLE_FROM_BULLET = registerTrigger(new GoldenAppleFromBulletTrigger());
	public static final ShrapnelHitShooterTrigger SHRAPNEL_HIT_SHOOTER = registerTrigger(new ShrapnelHitShooterTrigger());
	public static final HungerBulletDepleteFoodTrigger HUNGER_BULLET_DEPLETE_FOOD = registerTrigger(new HungerBulletDepleteFoodTrigger());
	public static final ChickenBurnedByFlameJetTrigger CHICKEN_BURNED_BY_FLAME_JET = registerTrigger(new ChickenBurnedByFlameJetTrigger());
	public static final FirstTidalPortalCreatedTrigger FIRST_TIDAL_PORTAL_CREATED = registerTrigger(new FirstTidalPortalCreatedTrigger());
	public static final TidalPortalKilledEndermanTrigger TIDAL_PORTAL_KILLED_ENDERMAN = registerTrigger(new TidalPortalKilledEndermanTrigger());
	public static final HarbingerOverloadProtocolTrigger HARBINGER_OVERLOAD_PROTOCOL = registerTrigger(new HarbingerOverloadProtocolTrigger());
	public static final RemnantSandstormChargeTrigger REMNANT_SANDSTORM_CHARGE = registerTrigger(new RemnantSandstormChargeTrigger());
	public static final CeraunusConductiveRiteTrigger CERAUNUS_CONDUCTIVE_RITE = registerTrigger(new CeraunusConductiveRiteTrigger());
	public static final AllAchievementsCompletedTrigger ALL_ACHIEVEMENTS_COMPLETED = registerTrigger(new AllAchievementsCompletedTrigger());
	
	// BOMD模组成就触发器
	public static final ObsidianWandTrigger OBSIDIAN_WAND = registerTrigger(new ObsidianWandTrigger());
	public static final AvadaKedavraTrigger AVADA_KEDAVRA = registerTrigger(new AvadaKedavraTrigger());
	public static final ObsidianCakeTrigger OBSIDIAN_CAKE = registerTrigger(new ObsidianCakeTrigger());
	public static final BadToTheBoneTrigger BAD_TO_THE_BONE = registerTrigger(new BadToTheBoneTrigger());
	public static final MankindIsDeadTrigger MANKIND_IS_DEAD = registerTrigger(new MankindIsDeadTrigger());
	public static final BloodIsFuelTrigger BLOOD_IS_FUEL = registerTrigger(new BloodIsFuelTrigger());
	public static final HellIsFullTrigger HELL_IS_FULL = registerTrigger(new HellIsFullTrigger());
	public static final BrustVoidTrigger BRUST_VOID = registerTrigger(new BrustVoidTrigger());
	private GWRECriteria() {
	}

	// 仅用于确保类加载（某些环境需要显式调用以触发静态初始化）
	public static void register() {
		// 访问静态字段以强制初始化
		SLIME_BULLET_TRIPLE_KILL.getId();
		GOLDEN_APPLE_FROM_BULLET.getId();
		SHRAPNEL_HIT_SHOOTER.getId();
		HUNGER_BULLET_DEPLETE_FOOD.getId();
		CHICKEN_BURNED_BY_FLAME_JET.getId();
		FIRST_TIDAL_PORTAL_CREATED.getId();
		TIDAL_PORTAL_KILLED_ENDERMAN.getId();
		HARBINGER_OVERLOAD_PROTOCOL.getId();
		REMNANT_SANDSTORM_CHARGE.getId();
		CERAUNUS_CONDUCTIVE_RITE.getId();
		ALL_ACHIEVEMENTS_COMPLETED.getId();
		
		// BOMD模组触发器初始化
		OBSIDIAN_WAND.getId();
		AVADA_KEDAVRA.getId();
		OBSIDIAN_CAKE.getId();
		BAD_TO_THE_BONE.getId();
		MANKIND_IS_DEAD.getId();
		BLOOD_IS_FUEL.getId();
		HELL_IS_FULL.getId();
		BRUST_VOID.getId();
		if (ModList.get().isLoaded(CompatModids.MEETYOURFIGHT)) {
			MYFCriteria.register();
		}
	}

	public static <T extends CriterionTrigger<?>> T registerTrigger(T trigger) {
		for (Method method : CriteriaTriggers.class.getDeclaredMethods()) {
			if (!Modifier.isStatic(method.getModifiers())) {
				continue;
			}

			Class<?>[] parameterTypes = method.getParameterTypes();
			try {
				if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(trigger.getClass())) {
					method.setAccessible(true);
					return (T) method.invoke(null, trigger);
				}

				if (parameterTypes.length == 2) {
					Object[] args = registerArgs(parameterTypes, trigger);
					if (args != null) {
						method.setAccessible(true);
						return (T) method.invoke(null, args);
					}
				}
			} catch (ReflectiveOperationException | RuntimeException e) {
				// Try the next static method. Runtime jars may expose this method under SRG names.
			}
		}
		throw new IllegalStateException("No compatible CriteriaTriggers registration method found for " + trigger.getId());
	}

	private static Object[] registerArgs(Class<?>[] parameterTypes, CriterionTrigger<?> trigger) {
		Object first = argumentFor(parameterTypes[0], trigger);
		Object second = argumentFor(parameterTypes[1], trigger);
		return first != null && second != null ? new Object[] { first, second } : null;
	}

	private static Object argumentFor(Class<?> parameterType, CriterionTrigger<?> trigger) {
		if (parameterType.isAssignableFrom(trigger.getClass())) {
			return trigger;
		}
		if (parameterType == ResourceLocation.class) {
			return trigger.getId();
		}
		if (parameterType == String.class) {
			return trigger.getId().toString();
		}
		return null;
	}
} 
