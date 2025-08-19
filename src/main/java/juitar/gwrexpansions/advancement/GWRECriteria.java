package juitar.gwrexpansions.advancement;

import juitar.gwrexpansions.advancement.BOMD.AvadaKedavraTrigger;
import juitar.gwrexpansions.advancement.BOMD.BadToTheBoneTrigger;
import juitar.gwrexpansions.advancement.BOMD.BrustVoidTrigger;
import juitar.gwrexpansions.advancement.BOMD.HellIsFullTrigger;
import juitar.gwrexpansions.advancement.BOMD.MankindIsDeadTrigger;
import juitar.gwrexpansions.advancement.BOMD.ObsidianCakeTrigger;
import juitar.gwrexpansions.advancement.BOMD.ObsidianWandTrigger;
import net.minecraft.advancements.CriteriaTriggers;

/**
 * 自定义进度触发器注册处
 */
public final class GWRECriteria {
	public static final SlimeBulletTripleKillTrigger SLIME_BULLET_TRIPLE_KILL = CriteriaTriggers.register(new SlimeBulletTripleKillTrigger());
	public static final GoldenAppleFromBulletTrigger GOLDEN_APPLE_FROM_BULLET = CriteriaTriggers.register(new GoldenAppleFromBulletTrigger());
	public static final ShrapnelHitShooterTrigger SHRAPNEL_HIT_SHOOTER = CriteriaTriggers.register(new ShrapnelHitShooterTrigger());
	public static final HungerBulletDepleteFoodTrigger HUNGER_BULLET_DEPLETE_FOOD = CriteriaTriggers.register(new HungerBulletDepleteFoodTrigger());
	public static final ChickenBurnedByFlameJetTrigger CHICKEN_BURNED_BY_FLAME_JET = CriteriaTriggers.register(new ChickenBurnedByFlameJetTrigger());
	public static final FirstTidalPortalCreatedTrigger FIRST_TIDAL_PORTAL_CREATED = CriteriaTriggers.register(new FirstTidalPortalCreatedTrigger());
	public static final AllAchievementsCompletedTrigger ALL_ACHIEVEMENTS_COMPLETED = CriteriaTriggers.register(new AllAchievementsCompletedTrigger());
	
	// BOMD模组成就触发器
	public static final ObsidianWandTrigger OBSIDIAN_WAND = CriteriaTriggers.register(new ObsidianWandTrigger());
	public static final AvadaKedavraTrigger AVADA_KEDAVRA = CriteriaTriggers.register(new AvadaKedavraTrigger());
	public static final ObsidianCakeTrigger OBSIDIAN_CAKE = CriteriaTriggers.register(new ObsidianCakeTrigger());
	public static final BadToTheBoneTrigger BAD_TO_THE_BONE = CriteriaTriggers.register(new BadToTheBoneTrigger());
	public static final MankindIsDeadTrigger MANKIND_IS_DEAD = CriteriaTriggers.register(new MankindIsDeadTrigger());
	public static final BloodIsFuelTrigger BLOOD_IS_FUEL = CriteriaTriggers.register(new BloodIsFuelTrigger());
	public static final HellIsFullTrigger HELL_IS_FULL = CriteriaTriggers.register(new HellIsFullTrigger());
	public static final BrustVoidTrigger BRUST_VOID = CriteriaTriggers.register(new BrustVoidTrigger());

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
	}
} 