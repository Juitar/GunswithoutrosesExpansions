package juitar.gwrexpansions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GWREConfig {
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        private static final ForgeConfigSpec SPEC;
        private static final Map<String, Double> configCache = new ConcurrentHashMap<>();

        // 子弹配置
        public static class BulletConfig {
                public static ForgeConfigSpec.DoubleValue phantomHalberDamage = null;
                public static ForgeConfigSpec.DoubleValue phantomHalberdRange = null;
                public static ForgeConfigSpec.IntValue phantomHalberdDelay = null;
                public static ForgeConfigSpec.DoubleValue flamejetDamage = null;
                public static ForgeConfigSpec.IntValue flamejetCount = null;
                public static ForgeConfigSpec.DoubleValue portal_damage = null;
                public static ForgeConfigSpec.DoubleValue portal_hpdamage = null;
                public static ForgeConfigSpec.DoubleValue golden_nugget_drop_rate = null;
                public static ForgeConfigSpec.DoubleValue golden_apple_drop_rate = null;

                public BulletConfig(ForgeConfigSpec.Builder bulider, String name, int index) {
                        bulider.push(name);
                        switch (index) {
                                case 1:
                                        phantomHalberDamage = bulider
                                                        .comment("Phantom Halber Damage")
                                                        .defineInRange("phantomHalberDamage", 10.0, 0.0, 100.0);
                                        phantomHalberdRange = bulider
                                                        .comment("Phantom Halberd Range")
                                                        .defineInRange("phantomHalberdRange", 5.0, 0.0, 100.0);
                                        phantomHalberdDelay = bulider
                                                        .comment("Phantom Halberd Delay")
                                                        .defineInRange("phantomHalberdDelay", 20, 0, 100);
                                        break;
                                case 2:
                                        flamejetDamage = bulider
                                                        .comment("Flamejet Damage")
                                                        .defineInRange("flamejetDamage", 7.0, 0.0, 100.0);
                                        flamejetCount = bulider
                                                        .comment("Flamejet Count")
                                                        .defineInRange("flamejetCount", 5, 0, 20);
                                        break;
                                case 3:
                                        portal_damage = bulider
                                                        .comment("Portal damage")
                                                        .defineInRange("Portal damage", 10.0, 0.0, 100.0);
                                        portal_hpdamage = bulider
                                                        .comment("Portal hpdamage")
                                                        .defineInRange("Portal hpdamage", 0.0, 0.0, 100.0);
                                case 4:
                                        golden_nugget_drop_rate = bulider
                                                        .comment("Golden Nugget Drop Rate")
                                                        .defineInRange("golden_nugget_drop_rate", 0.4, 0.0, 1.0);
                                        golden_apple_drop_rate = bulider
                                                        .comment("Golden Apple Drop Rate")
                                                        .defineInRange("golden_apple_drop_rate", 0.033, 0.0, 1.0);
                                        break;
                                default:
                                        break;
                        }
                        bulider.pop();
                }
        }

        // 子弹配置
        public static class BulletConfigs {
                public final BulletConfig lavapower;
                public final BulletConfig cursium;
                public final BulletConfig tidal;
                public final BulletConfig gold;

                public BulletConfigs(ForgeConfigSpec.Builder builder) {
                        builder.push("Bullets");
                        cursium = new BulletConfig(builder, "Cursium", 1);
                        lavapower = new BulletConfig(builder, "Lavapower", 2);
                        tidal = new BulletConfig(builder, "Tidal", 3);
                        gold = new BulletConfig(builder, "Gold", 4);
                        builder.pop();
                }
        }

        public static class GunConfig {
                public final ForgeConfigSpec.IntValue bonusDamage;
                public final ForgeConfigSpec.DoubleValue damageMultiplier;
                public final ForgeConfigSpec.DoubleValue headshotMultiplier;
                public final ForgeConfigSpec.IntValue fireDelay;
                public final ForgeConfigSpec.DoubleValue inaccuracy;

                public GunConfig(ForgeConfigSpec.Builder builder, String name, int defaultDamage,
                                double defaultMultiplier, double defaultHeadshot,
                                int defaultDelay, double defaultInaccuracy) {
                        builder.push(name);
                        bonusDamage = builder
                                        .comment("Bonus Damage")
                                        .defineInRange("bonusDamage", defaultDamage, 0, 100);
                        damageMultiplier = builder
                                        .comment("Damage Multiplier")
                                        .defineInRange("damageMultiplier", defaultMultiplier, 0.0, 10.0);
                        headshotMultiplier = builder
                                        .comment("Headshot Multiplier")
                                        .defineInRange("headshotMultiplier", defaultHeadshot, 1.0, 10.0);
                        fireDelay = builder
                                        .comment("fire Delay")
                                        .defineInRange("fireDelay", defaultDelay, 4, 100);
                        inaccuracy = builder
                                        .comment("inaccuracy")
                                        .defineInRange("inaccuracy", defaultInaccuracy, 0.0, 10.0);
                        builder.pop();
                }
        }

        public static class GeneralConfig {
                public final ForgeConfigSpec.BooleanValue enableAllAchievementsSuperShotgunReward;

                public GeneralConfig(ForgeConfigSpec.Builder builder) {
                        builder.push("General");
                        enableAllAchievementsSuperShotgunReward = builder
                                        .comment("Whether completing the RIP AND TEAR all-achievements challenge grants the Super Shotgun reward.")
                                        .define("enableAllAchievementsSuperShotgunReward", true);
                        builder.pop();
                }
        }

        public static class CursiumSniperConfig extends GunConfig {
                public final ForgeConfigSpec.IntValue maxRage;

                public CursiumSniperConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "Cursium", 0, 2.0, 2.0, 24, 0.0);
                        builder.push("Cursium");
                        builder.push("Mechanics");
                        maxRage = builder.comment("Maximum rage stored by Cursium Sniper headshots.")
                                        .defineInRange("maxRage", 5, 1, 100);
                        builder.pop();
                        builder.pop();
                }
        }

        public static class IgnitiumGatlingConfig extends GunConfig {
                public final ForgeConfigSpec.IntValue blueFireBonusDamage;
                public final ForgeConfigSpec.IntValue blueFireDelay;
                public final ForgeConfigSpec.IntValue blueFireDurationTicks;
                public final ForgeConfigSpec.DoubleValue blueFireHealingBonus;

                public IgnitiumGatlingConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "Ignitium", 3, 1.0, 1.0, 6, 3.0);
                        builder.push("Ignitium");
                        builder.push("Blue_Fire");
                        blueFireBonusDamage = builder
                                        .comment("Bonus damage used while Ignitium Gatling blue fire is active.")
                                        .defineInRange("blueFireBonusDamage", 6, 0, 100);
                        blueFireDelay = builder.comment("Fire delay used while Ignitium Gatling blue fire is active.")
                                        .defineInRange("blueFireDelay", 3, 1, 100);
                        blueFireDurationTicks = builder
                                        .comment("Blue fire duration in ticks after crossing to half health or below.")
                                        .defineInRange("blueFireDurationTicks", 100, 1, 72000);
                        blueFireHealingBonus = builder.comment(
                                        "Flat healing added to each Ignitium Bullet hit while blue fire is active.")
                                        .defineInRange("blueFireHealingBonus", 1.0, 0.0, 100.0);
                        builder.pop();
                        builder.pop();
                }
        }

        public static class TidalPistolConfig extends GunConfig {
                public final ForgeConfigSpec.IntValue maxEnergy;
                public final ForgeConfigSpec.IntValue inventoryRegenPerSecond;
                public final ForgeConfigSpec.IntValue heldLandRegenPerSecond;
                public final ForgeConfigSpec.IntValue heldWaterRegenPerSecond;
                public final ForgeConfigSpec.IntValue hitEnergy;
                public final ForgeConfigSpec.IntValue orbCost;
                public final ForgeConfigSpec.IntValue mineCost;
                public final ForgeConfigSpec.IntValue landOrbCost;
                public final ForgeConfigSpec.IntValue landMineCost;
                public final ForgeConfigSpec.IntValue portalCost;
                public final ForgeConfigSpec.IntValue riftCost;
                public final ForgeConfigSpec.IntValue portalChargeTicks;
                public final ForgeConfigSpec.IntValue riftChargeTicks;
                public final ForgeConfigSpec.IntValue landOrbCooldownTicks;
                public final ForgeConfigSpec.IntValue landMineCooldownTicks;
                public final ForgeConfigSpec.IntValue fullFormOrbCooldownTicks;
                public final ForgeConfigSpec.IntValue fullFormMineCooldownTicks;
                public final ForgeConfigSpec.DoubleValue fullFormOrbChance;
                public final ForgeConfigSpec.DoubleValue fullFormMineChance;
                public final ForgeConfigSpec.DoubleValue landOrbChance;
                public final ForgeConfigSpec.DoubleValue landMineChance;
                public final ForgeConfigSpec.DoubleValue tentacleChance;
                public final ForgeConfigSpec.IntValue tentacleCooldownTicks;
                public final ForgeConfigSpec.DoubleValue orbSpeedMultiplier;
                public final ForgeConfigSpec.DoubleValue landOrbSpeedMultiplier;
                public final ForgeConfigSpec.DoubleValue landSkillDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue waterSkillDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue portalDamage;
                public final ForgeConfigSpec.DoubleValue portalHpDamage;
                public final ForgeConfigSpec.IntValue portalWarmupTicks;
                public final ForgeConfigSpec.IntValue riftDurationTicks;
                public final ForgeConfigSpec.DoubleValue riftRadius;
                public final ForgeConfigSpec.DoubleValue riftDamage;
                public final ForgeConfigSpec.DoubleValue riftPullStrength;
                public final ForgeConfigSpec.IntValue riftDamageIntervalTicks;

                public TidalPistolConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "Tidal", 0, 1.0, 1.0, 18, 2.0);

                        builder.push("Tidal_Mechanics");
                        maxEnergy = builder.comment("Maximum tidal energy stored on the Tidal Pistol.")
                                        .defineInRange("maxEnergy", 100, 1, 1000);
                        inventoryRegenPerSecond = builder.comment(
                                        "Tidal energy regenerated per second while the Tidal Pistol is in a player's inventory.")
                                        .defineInRange("inventoryRegenPerSecond", 2, 0, 100);
                        heldLandRegenPerSecond = builder.comment(
                                        "Tidal energy regenerated per second while the Tidal Pistol is held outside water.")
                                        .defineInRange("heldLandRegenPerSecond", 5, 0, 100);
                        heldWaterRegenPerSecond = builder.comment(
                                        "Tidal energy regenerated per second while the Tidal Pistol is held in water.")
                                        .defineInRange("heldWaterRegenPerSecond", 8, 0, 100);
                        hitEnergy = builder.comment("Tidal energy gained when a Tidal Bullet damages an entity.")
                                        .defineInRange("hitEnergy", 5, 0, 1000);
                        orbCost = builder.comment("Tidal energy cost for the entity-hit Abyss Orb follow-up.")
                                        .defineInRange("orbCost", 20, 0, 1000);
                        mineCost = builder.comment("Tidal energy cost for the block-hit Abyss Mine follow-up.")
                                        .defineInRange("mineCost", 12, 0, 1000);
                        landOrbCost = builder.comment("Tidal energy cost for the weakened land Abyss Orb echo.")
                                        .defineInRange("landOrbCost", 20, 0, 1000);
                        landMineCost = builder.comment("Tidal energy cost for the weakened land Abyss Mine echo.")
                                        .defineInRange("landMineCost", 6, 0, 1000);
                        portalCost = builder.comment("Tidal energy cost for the charged Abyss Blast Portal.")
                                        .defineInRange("portalCost", 50, 0, 1000);
                        riftCost = builder.comment("Tidal energy cost for the full-charge safe tidal rift.")
                                        .defineInRange("riftCost", 100, 0, 1000);
                        portalChargeTicks = builder.comment("Minimum held ticks for the charged portal release.")
                                        .defineInRange("portalChargeTicks", 12, 1, 72000);
                        riftChargeTicks = builder.comment("Minimum held ticks for the full rift release.")
                                        .defineInRange("riftChargeTicks", 35, 1, 72000);
                        landOrbCooldownTicks = builder.comment("Cooldown in ticks for the weakened land orb follow-up.")
                                        .defineInRange("landOrbCooldownTicks", 100, 0, 72000);
                        landMineCooldownTicks = builder
                                        .comment("Cooldown in ticks for the weakened land mine follow-up.")
                                        .defineInRange("landMineCooldownTicks", 140, 0, 72000);
                        fullFormOrbCooldownTicks = builder.comment("Cooldown in ticks for the water/rain orb echo.")
                                        .defineInRange("fullFormOrbCooldownTicks", 8, 0, 72000);
                        fullFormMineCooldownTicks = builder.comment("Cooldown in ticks for the water/rain mine echo.")
                                        .defineInRange("fullFormMineCooldownTicks", 12, 0, 72000);
                        fullFormOrbChance = builder.comment(
                                        "Chance for an entity hit to trigger a water/rain tracking Abyss Orb echo.")
                                        .defineInRange("fullFormOrbChance", 0.45, 0.0, 1.0);
                        fullFormMineChance = builder
                                        .comment("Chance for a block hit to trigger a water/rain Abyss Mine echo.")
                                        .defineInRange("fullFormMineChance", 0.30, 0.0, 1.0);
                        landOrbChance = builder.comment(
                                        "Chance for an entity hit to trigger a weakened land tracking Abyss Orb echo.")
                                        .defineInRange("landOrbChance", 0.20, 0.0, 1.0);
                        landMineChance = builder
                                        .comment("Chance for a block hit to trigger a weakened land Abyss Mine echo.")
                                        .defineInRange("landMineChance", 0.12, 0.0, 1.0);
                        tentacleChance = builder.comment(
                                        "Chance for the Tidal Pistol to launch Tidal Tentacles when its holder is hurt.")
                                        .defineInRange("tentacleChance", 1.0, 0.0, 1.0);
                        tentacleCooldownTicks = builder.comment("Cooldown in ticks for the Tidal Tentacle assist.")
                                        .defineInRange("tentacleCooldownTicks", 120, 0, 72000);
                        orbSpeedMultiplier = builder
                                        .comment("Multiplier applied to the sustained Abyss Orb tracking speed.")
                                        .defineInRange("orbSpeedMultiplier", 4.0, 0.1, 10.0);
                        landOrbSpeedMultiplier = builder.comment(
                                        "Multiplier applied to the sustained weakened land Abyss Orb tracking speed.")
                                        .defineInRange("landOrbSpeedMultiplier", 3.2, 0.1, 10.0);
                        landSkillDamageMultiplier = builder
                                        .comment("Damage multiplier for weakened land follow-up skills.")
                                        .defineInRange("landSkillDamageMultiplier", 0.35, 0.0, 100.0);
                        waterSkillDamageMultiplier = builder
                                        .comment("Damage multiplier for water/rain follow-up skills.")
                                        .defineInRange("waterSkillDamageMultiplier", 0.85, 0.0, 100.0);
                        portalDamage = builder.comment("Damage for the controlled Abyss Blast Portal.")
                                        .defineInRange("portalDamage", 8.0, 0.0, 1000.0);
                        portalHpDamage = builder
                                        .comment("Percent max-health damage for the controlled Abyss Blast Portal.")
                                        .defineInRange("portalHpDamage", 0.0, 0.0, 100.0);
                        portalWarmupTicks = builder
                                        .comment("Warmup delay in ticks for the controlled Abyss Blast Portal.")
                                        .defineInRange("portalWarmupTicks", 4, 0, 200);
                        riftDurationTicks = builder.comment("Lifetime in ticks for the safe tidal rift.")
                                        .defineInRange("riftDurationTicks", 240, 1, 72000);
                        riftRadius = builder.comment("Radius for the safe tidal rift pull and damage.")
                                        .defineInRange("riftRadius", 10.0, 0.0, 64.0);
                        riftDamage = builder.comment("Damage dealt by each safe tidal rift pulse.")
                                        .defineInRange("riftDamage", 5.0, 0.0, 1000.0);
                        riftPullStrength = builder.comment("Pull strength applied by the safe tidal rift each tick.")
                                        .defineInRange("riftPullStrength", 0.14, 0.0, 2.0);
                        riftDamageIntervalTicks = builder.comment("Ticks between safe tidal rift damage pulses.")
                                        .defineInRange("riftDamageIntervalTicks", 5, 1, 200);
                        builder.pop();
                }
        }

        public static class MirecallerConfig extends GunConfig {
                public final ForgeConfigSpec.DoubleValue mineExplosionPower;

                public MirecallerConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "Mirecaller", 0, 0.7, 1.0, 24, 4.5);
                        builder.push("Mirecaller_Mechanics");
                        mineExplosionPower = builder
                                        .comment("Explosion power for mines thrown by the Mirecaller Shotgun. Vanilla MYF Swampjaw mines default to 2.5.")
                                        .defineInRange("mineExplosionPower", 2.5, 0.0, 16.0);
                        builder.pop();
                }
        }

        public static class RemnantFangshotConfig extends GunConfig {
                public final ForgeConfigSpec.IntValue rageRequired;
                public final ForgeConfigSpec.IntValue awakenedTicks;
                public final ForgeConfigSpec.IntValue bladeAmpTicks;
                public final ForgeConfigSpec.IntValue comboWindowTicks;
                public final ForgeConfigSpec.DoubleValue baseMeleeDamage;
                public final ForgeConfigSpec.DoubleValue bladeDamageBonus;
                public final ForgeConfigSpec.DoubleValue baseAttackSpeedModifier;
                public final ForgeConfigSpec.DoubleValue ampedAttackSpeedModifier;
                public final ForgeConfigSpec.DoubleValue minFullAttackScale;
                public final ForgeConfigSpec.DoubleValue cooldownRemainingMultiplier;
                public final ForgeConfigSpec.DoubleValue powerProjectileDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue powerStompDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue powerStompRange;
                public final ForgeConfigSpec.IntValue dashTicks;
                public final ForgeConfigSpec.DoubleValue dashDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue dashSpeed;
                public final ForgeConfigSpec.DoubleValue dashHitRange;
                public final ForgeConfigSpec.DoubleValue dashDamageReduction;

                public RemnantFangshotConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "RemnantFangshot", 0, 0.65, 1.0, 24, 4.0);

                        builder.push("RemnantFangshot_Mechanics");
                        rageRequired = builder.comment("Gunblade cycles required to awaken Remnant Fangshot.")
                                        .defineInRange("rageRequired", 3, 1, 20);
                        awakenedTicks = builder.comment("Awakened duration in ticks. 200 ticks = 10 seconds.")
                                        .defineInRange("awakenedTicks", 200, 1, 6000);
                        bladeAmpTicks = builder.comment("Ticks the blade remains empowered after a projectile hit.")
                                        .defineInRange("bladeAmpTicks", 80, 1, 6000);
                        comboWindowTicks = builder
                                        .comment("Ticks allowed between the shot hit and the charged melee hit.")
                                        .defineInRange("comboWindowTicks", 100, 1, 6000);
                        baseMeleeDamage = builder.comment("Base melee damage shown by the gunblade.")
                                        .defineInRange("baseMeleeDamage", 9.0, 0.0, 1000.0);
                        bladeDamageBonus = builder.comment("Melee damage added while the blade is empowered.")
                                        .defineInRange("bladeDamageBonus", 3.0, 0.0, 1000.0);
                        baseAttackSpeedModifier = builder
                                        .comment("Attack speed attribute modifier when the blade is not empowered.")
                                        .defineInRange("baseAttackSpeedModifier", -2.6, -10.0, 10.0);
                        ampedAttackSpeedModifier = builder
                                        .comment("Attack speed attribute modifier while the blade is empowered.")
                                        .defineInRange("ampedAttackSpeedModifier", -2.4, -10.0, 10.0);
                        minFullAttackScale = builder
                                        .comment("Minimum attack strength scale required for gunblade combo hits.")
                                        .defineInRange("minFullAttackScale", 0.9, 0.0, 1.0);
                        cooldownRemainingMultiplier = builder.comment(
                                        "Remaining firing cooldown multiplier after a correct charged melee hit. 0.5 cuts remaining cooldown in half.")
                                        .defineInRange("cooldownRemainingMultiplier", 0.5, 0.0, 1.0);
                        powerProjectileDamageMultiplier = builder
                                        .comment("Projectile damage multiplier while awakened. 1.2 is +20%.")
                                        .defineInRange("powerProjectileDamageMultiplier", 1.2, 0.0, 10.0);
                        powerStompDamageMultiplier = builder
                                        .comment("Awakened stomp damage as a multiplier of current melee damage.")
                                        .defineInRange("powerStompDamageMultiplier", 0.6, 0.0, 10.0);
                        powerStompRange = builder.comment("Awakened stomp radius.")
                                        .defineInRange("powerStompRange", 2.75, 0.0, 64.0);
                        dashTicks = builder.comment("Remnant Charge duration in ticks.")
                                        .defineInRange("dashTicks", 10, 1, 200);
                        dashDamageMultiplier = builder
                                        .comment("Remnant Charge damage as a multiplier of current melee damage.")
                                        .defineInRange("dashDamageMultiplier", 2.5, 0.0, 20.0);
                        dashSpeed = builder.comment("Forward speed applied during Remnant Charge.")
                                        .defineInRange("dashSpeed", 1.25, 0.0, 10.0);
                        dashHitRange = builder.comment("Hit radius around the charging sandstorm.")
                                        .defineInRange("dashHitRange", 1.15, 0.0, 16.0);
                        dashDamageReduction = builder
                                        .comment("Incoming damage reduction during Remnant Charge. 0.2 is 20%.")
                                        .defineInRange("dashDamageReduction", 0.2, 0.0, 0.95);
                        builder.pop();
                }
        }

        public static class BurstgunConfig {
                // 包含基础枪支配置
                public final GunConfig gunConfig;
                public final ForgeConfigSpec.IntValue burstSize;
                public final ForgeConfigSpec.IntValue burstDelay;

                public BurstgunConfig(ForgeConfigSpec.Builder builder, String name, int defaultDamage,
                                double defaultMultiplier, double defaultHeadshot,
                                int defaultDelay, double defaultInaccuracy, int defualtburstSize,
                                int defualtburstDelay) {
                        // 创建基础枪支配置
                        gunConfig = new GunConfig(builder, name, defaultDamage, defaultMultiplier, defaultHeadshot,
                                        defaultDelay,
                                        defaultInaccuracy);

                        // 在同一个分组中添加burst特有的配置
                        builder.push(name + "_Burst");
                        burstSize = builder
                                        .comment("Burst Size")
                                        .defineInRange("burstSize", defualtburstSize, 1, 10);
                        burstDelay = builder
                                        .comment("Burst Delay")
                                        .defineInRange("burstDelay", defualtburstDelay, 1, 100);
                        builder.pop();
                }
        }

        public static class DuskfallEclipseConfig extends BurstgunConfig {
                public final ForgeConfigSpec.IntValue pierceCount;
                public final ForgeConfigSpec.DoubleValue pierceDamageMultiplier;
                public final ForgeConfigSpec.IntValue maxSpirits;
                public final ForgeConfigSpec.IntValue spiritSummonIntervalTicks;
                public final ForgeConfigSpec.IntValue unequippedGraceTicks;
                public final ForgeConfigSpec.DoubleValue damageBonusPerSpirit;
                public final ForgeConfigSpec.DoubleValue damageReductionPerSpirit;
                public final ForgeConfigSpec.DoubleValue spiritAutoTargetRange;
                public final ForgeConfigSpec.IntValue spiritAttackCooldownTicks;
                public final ForgeConfigSpec.IntValue spiritWarnTicks;
                public final ForgeConfigSpec.DoubleValue spiritAttackDamage;
                public final ForgeConfigSpec.DoubleValue spiritMaxHealth;
                public final ForgeConfigSpec.DoubleValue spiritArmor;
                public final ForgeConfigSpec.IntValue lastTargetMemoryTicks;

                public DuskfallEclipseConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "DuskfallEclipse", 0, 0.65, 1.0, 28, 1.5, 6, 3);

                        builder.push("DuskfallEclipse_Mechanics");
                        pierceCount = builder.comment("Number of additional entities each shot can pierce.")
                                        .defineInRange("pierceCount", 2, 0, 16);
                        pierceDamageMultiplier = builder.comment("Damage multiplier applied after each pierce.")
                                        .defineInRange("pierceDamageMultiplier", 0.8, 0.0, 4.0);
                        maxSpirits = builder.comment("Maximum friendly Dusk Rose Spirits a player can maintain.")
                                        .defineInRange("maxSpirits", 3, 0, 10);
                        spiritSummonIntervalTicks = builder
                                        .comment("Ticks between spirit summons while holding the weapon.")
                                        .defineInRange("spiritSummonIntervalTicks", 150, 1, 6000);
                        unequippedGraceTicks = builder
                                        .comment("Ticks spirits persist after the player stops holding the weapon.")
                                        .defineInRange("unequippedGraceTicks", 100, 0, 6000);
                        damageBonusPerSpirit = builder
                                        .comment("Outgoing damage bonus per active spirit. 0.08 is 8%.")
                                        .defineInRange("damageBonusPerSpirit", 0.08, 0.0, 4.0);
                        damageReductionPerSpirit = builder
                                        .comment("Incoming damage reduction per active spirit. 0.05 is 5%.")
                                        .defineInRange("damageReductionPerSpirit", 0.05, 0.0, 0.95);
                        spiritAutoTargetRange = builder.comment("Range used by spirits to find targets.")
                                        .defineInRange("spiritAutoTargetRange", 18.0, 1.0, 128.0);
                        spiritAttackCooldownTicks = builder.comment("Ticks between spirit attacks.")
                                        .defineInRange("spiritAttackCooldownTicks", 40, 1, 6000);
                        spiritWarnTicks = builder.comment("Warning ticks before a spirit attack lands.")
                                        .defineInRange("spiritWarnTicks", 14, 0, 200);
                        spiritAttackDamage = builder.comment("Damage dealt by each spirit assist attack.")
                                        .defineInRange("spiritAttackDamage", 10.0, 0.0, 1000.0);
                        spiritMaxHealth = builder.comment("Maximum health for player-owned Dusk Rose Spirits.")
                                        .defineInRange("spiritMaxHealth", 20.0, 1.0, 1000.0);
                        spiritArmor = builder.comment("Armor for player-owned Dusk Rose Spirits.")
                                        .defineInRange("spiritArmor", 5.0, 0.0, 1000.0);
                        lastTargetMemoryTicks = builder
                                        .comment("Ticks spirits remember the last target hit by this weapon.")
                                        .defineInRange("lastTargetMemoryTicks", 200, 0, 6000);
                        builder.pop();
                }
        }

        public static class CeraunusBurstConfig extends BurstgunConfig {
                public final ForgeConfigSpec.DoubleValue baseElementDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue comboDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue comboRadius;
                public final ForgeConfigSpec.IntValue comboWindowTicks;
                public final ForgeConfigSpec.IntValue comboDisplayTicks;
                public final ForgeConfigSpec.IntValue comboDelayTicks;
                public final ForgeConfigSpec.IntValue stormSerpentMax;
                public final ForgeConfigSpec.DoubleValue stormSerpentBiteDamage;
                public final ForgeConfigSpec.DoubleValue mixedSerpentBiteDamage;
                public final ForgeConfigSpec.DoubleValue stormSerpentSecondaryDamageMultiplier;
                public final ForgeConfigSpec.IntValue lightningSpearMax;
                public final ForgeConfigSpec.IntValue normalWaveTicks;
                public final ForgeConfigSpec.IntValue pureWaterWaveTicks;
                public final ForgeConfigSpec.DoubleValue lightningBulletWaterDamageMultiplier;

                public CeraunusBurstConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "CeraunusBurst", 0, 0.72, 1.0, 26, 1.35, 3, 3);

                        builder.push("CeraunusBurst_Mechanics");
                        baseElementDamageMultiplier = builder
                                        .comment("Damage multiplier for iron/water, golden/storm, and diamond/lightning element shots.")
                                        .defineInRange("baseElementDamageMultiplier", 0.75, 0.0, 10.0);
                        comboDamageMultiplier = builder
                                        .comment("Damage multiplier used by the three-shot Ceraunus combo.")
                                        .defineInRange("comboDamageMultiplier", 1.45, 0.0, 20.0);
                        comboRadius = builder
                                        .comment("Radius used by Ceraunus combos.")
                                        .defineInRange("comboRadius", 6.5, 1.0, 32.0);
                        comboWindowTicks = builder
                                        .comment("Ticks allowed between Ceraunus element shots before the current combo resets.")
                                        .defineInRange("comboWindowTicks", 80, 1, 6000);
                        comboDisplayTicks = builder
                                        .comment("Ticks the completed combo remains visible on the Ceraunus HUD.")
                                        .defineInRange("comboDisplayTicks", 24, 1, 6000);
                        comboDelayTicks = builder
                                        .comment("Ticks before a completed Ceraunus combo triggers.")
                                        .defineInRange("comboDelayTicks", 8, 0, 100);
                        stormSerpentMax = builder
                                        .comment("Maximum Storm Serpents a single Ceraunus combo may summon.")
                                        .defineInRange("stormSerpentMax", 4, 0, 16);
                        stormSerpentBiteDamage = builder
                                        .comment("Bite damage for full storm Ceraunus serpents.")
                                        .defineInRange("stormSerpentBiteDamage", 12.0, 0.0, 1000.0);
                        mixedSerpentBiteDamage = builder
                                        .comment("Bite damage for mixed-element Ceraunus serpents.")
                                        .defineInRange("mixedSerpentBiteDamage", 8.0, 0.0, 1000.0);
                        stormSerpentSecondaryDamageMultiplier = builder
                                        .comment("Secondary water/lightning damage multiplier based on serpent bite damage.")
                                        .defineInRange("stormSerpentSecondaryDamageMultiplier", 0.5, 0.0, 10.0);
                        lightningSpearMax = builder
                                        .comment("Maximum lightning spears for the full lightning Ceraunus combo.")
                                        .defineInRange("lightningSpearMax", 4, 0, 32);
                        normalWaveTicks = builder
                                        .comment("Lifetime in ticks for normal Ceraunus wave attacks.")
                                        .defineInRange("normalWaveTicks", 20, 1, 200);
                        pureWaterWaveTicks = builder
                                        .comment("Lifetime in ticks for the full water Ceraunus wave attack.")
                                        .defineInRange("pureWaterWaveTicks", 30, 1, 200);
                        lightningBulletWaterDamageMultiplier = builder
                                        .comment("Damage multiplier for Ceraunus lightning bullets when the bullet or target is in water.")
                                        .defineInRange("lightningBulletWaterDamageMultiplier", 1.3, 0.0, 10.0);
                        builder.pop();
                }
        }

        public static class HarbingerRaycasterConfig extends GunConfig {
                public final ForgeConfigSpec.IntValue maxOverload;
                public final ForgeConfigSpec.IntValue overloadModeDurationTicks;
                public final ForgeConfigSpec.BooleanValue overloadFlightEnabled;
                public final ForgeConfigSpec.DoubleValue redstoneDamageBonus;
                public final ForgeConfigSpec.IntValue redstonePierce;
                public final ForgeConfigSpec.DoubleValue redstonePierceDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue deathLaserDamageMultiplier;
                public final ForgeConfigSpec.DoubleValue deathLaserHpDamage;
                public final ForgeConfigSpec.IntValue deathLaserSegments;
                public final ForgeConfigSpec.DoubleValue deathLaserSegmentLength;
                public final ForgeConfigSpec.IntValue missilesPerWave;
                public final ForgeConfigSpec.IntValue normalHeadshotMissiles;
                public final ForgeConfigSpec.IntValue missileStartDelay;
                public final ForgeConfigSpec.IntValue missileIntervalTicks;
                public final ForgeConfigSpec.DoubleValue missileDamage;
                public final ForgeConfigSpec.DoubleValue missileTargetRange;

                public HarbingerRaycasterConfig(ForgeConfigSpec.Builder builder) {
                        super(builder, "HarbingerRaycaster", 0, 1.6, 1.75, 26, 0.0);

                        builder.push("HarbingerRaycaster_Mechanics");
                        maxOverload = builder.comment("Redstone shots required to fully overload Harbinger Raycaster.")
                                        .defineInRange("maxOverload", 6, 1, 100);
                        overloadModeDurationTicks = builder
                                        .comment("Overload mode duration in ticks. 120 ticks = 6 seconds.")
                                        .defineInRange("overloadModeDurationTicks", 120, 1, 6000);
                        overloadFlightEnabled = builder
                                        .comment("Whether overload mode temporarily allows the player to fly.")
                                        .define("overloadFlightEnabled", true);
                        redstoneDamageBonus = builder
                                        .comment("Flat damage added to redstone bullets fired by Harbinger Raycaster.")
                                        .defineInRange("redstoneDamageBonus", 1.0, 0.0, 1000.0);
                        redstonePierce = builder.comment(
                                        "Additional entities a redstone bullet can pierce when fired by Harbinger Raycaster.")
                                        .defineInRange("redstonePierce", 2, 0, 64);
                        redstonePierceDamageMultiplier = builder
                                        .comment("Damage multiplier applied after each redstone bullet pierce.")
                                        .defineInRange("redstonePierceDamageMultiplier", 1.0, 0.0, 10.0);
                        deathLaserDamageMultiplier = builder.comment(
                                        "Damage multiplier applied to the empowered redstone shot damage for the death laser.")
                                        .defineInRange("deathLaserDamageMultiplier", 0.45, 0.0, 100.0);
                        deathLaserHpDamage = builder
                                        .comment("Death laser bonus damage as percent of target max health.")
                                        .defineInRange("deathLaserHpDamage", 5.0, 0.0, 100.0);
                        deathLaserSegments = builder.comment(
                                        "Number of 30-block Cataclysm death laser segments to chain. Total range is segments * segment length.")
                                        .defineInRange("deathLaserSegments", 2, 1, 16);
                        deathLaserSegmentLength = builder.comment("Length of each chained death laser segment.")
                                        .defineInRange("deathLaserSegmentLength", 30.0, 1.0, 128.0);
                        missilesPerWave = builder.comment("Homing missiles fired per overload wave.")
                                        .defineInRange("missilesPerWave", 3, 0, 32);
                        normalHeadshotMissiles = builder.comment(
                                        "Extra homing missiles fired near the player when a non-overload Harbinger redstone bullet headshots.")
                                        .defineInRange("normalHeadshotMissiles", 1, 0, 32);
                        missileStartDelay = builder.comment("Delay in ticks before the first overload missile wave.")
                                        .defineInRange("missileStartDelay", 8, 0, 6000);
                        missileIntervalTicks = builder
                                        .comment("Ticks between homing missile waves while overload mode is active.")
                                        .defineInRange("missileIntervalTicks", 24, 1, 6000);
                        missileDamage = builder.comment("Damage dealt by each overload homing missile.")
                                        .defineInRange("missileDamage", 5.0, 0.0, 1000.0);
                        missileTargetRange = builder
                                        .comment("Range used by overload homing missiles to find nearby enemies.")
                                        .defineInRange("missileTargetRange", 18.0, 1.0, 128.0);
                        builder.pop();
                }
        }

        // 狙击枪配置
        public static class SniperConfigs {
                public final GunConfig netherite;
                public final CursiumSniperConfig cursium;
                public final GunConfig DragonSteel;
                public final GunConfig destiny_seven;
                public final HarbingerRaycasterConfig harbingerRaycaster;

                public SniperConfigs(ForgeConfigSpec.Builder builder) {
                        builder.push("Sniper");
                        netherite = new GunConfig(builder, "Netherite", 0, 1.8, 1.5, 24, 0.0);
                        cursium = new CursiumSniperConfig(builder);
                        DragonSteel = new GunConfig(builder, "DragonSteel", 0, 1.9, 1.8, 24, 0.0);
                        destiny_seven = new GunConfig(builder, "DestinySeven", 0, 1.5, 1.5, 24, 0.0);
                        harbingerRaycaster = new HarbingerRaycasterConfig(builder);
                        builder.pop();
                }
        }

        // 爆发枪配置
        public static class BurstgunConfigs {
                public final BurstgunConfig voidBurst;
                public final DuskfallEclipseConfig duskfallEclipse;
                public final CeraunusBurstConfig ceraunusBurst;

                public BurstgunConfigs(ForgeConfigSpec.Builder builder) {
                        builder.push("Burstgun");
                        voidBurst = new BurstgunConfig(builder, "Void", 2, 1.0, 1.0, 25, 0.0, 3, 5);
                        duskfallEclipse = new DuskfallEclipseConfig(builder);
                        ceraunusBurst = new CeraunusBurstConfig(builder);
                        builder.pop();
                }
        }

        // 霰弹枪配置
        public static class ShotgunConfigs {
                public final GunConfig Netherite;
                public final GunConfig NetheriteMonster;
                public final GunConfig DragonSteel;
                public final GunConfig Supershotgun;
                public final RemnantFangshotConfig RemnantFangshot;
                public final MirecallerConfig Mirecaller;

                public ShotgunConfigs(ForgeConfigSpec.Builder builder) {
                        builder.push("Shotgun");
                        Netherite = new GunConfig(builder, "Netherite", 0, 0.6, 1.0, 20, 5.0);
                        NetheriteMonster = new GunConfig(builder, "NetheriteMonster", 0, 0.8, 1.0, 20, 4.0);
                        DragonSteel = new GunConfig(builder, "DragonSteel", 0, 0.75, 1.0, 20, 4);
                        Supershotgun = new GunConfig(builder, "Supershotgun", 0, 1.5, 1.0, 40, 4);
                        RemnantFangshot = new RemnantFangshotConfig(builder);
                        Mirecaller = new MirecallerConfig(builder);
                        builder.pop();
                }
        }

        // 加特林配置
        public static class GatlingConfigs {
                public final GunConfig Netherite;
                public final IgnitiumGatlingConfig Ignitium;
                public final GunConfig DragonSteel;
                public final GunConfig skull;
                public final GunConfig Magnetic;

                public GatlingConfigs(ForgeConfigSpec.Builder builder) {
                        builder.push("Gatling");
                        Netherite = new GunConfig(builder, "Netherite", 1, 1.0, 1.0, 4, 3.0);
                        Ignitium = new IgnitiumGatlingConfig(builder);
                        DragonSteel = new GunConfig(builder, "DragonSteel", 3, 1.0, 1.0, 4, 3.0);
                        skull = new GunConfig(builder, "Skull", 0, 1.0, 1.0, 6, 6.0);
                        Magnetic = new GunConfig(builder, "Magnetic", 0, 0.9, 1.0, 4, 4.0);
                        builder.pop();
                }
        }

        // 手枪配置
        public static class PistolConfigs {
                public final TidalPistolConfig tidal;
                public final GunConfig hellforge;

                public PistolConfigs(ForgeConfigSpec.Builder builder) {
                        builder.push("Pistol");
                        tidal = new TidalPistolConfig(builder);
                        hellforge = new GunConfig(builder, "Hellforge", 0, 1.0, 1.2, 60, 1.0);
                        builder.pop();
                }
        }

        public static class LauncherConfigs {
                public final GunConfig Obisidian;

                public LauncherConfigs(ForgeConfigSpec.Builder builder) {
                        builder.push("Launcher");
                        Obisidian = new GunConfig(builder, "Obsidian", 30, 1.0, 1.0, 60, 0.0);
                        builder.pop();
                }
        }

        public static class DestinyConfig {
                public final ForgeConfigSpec.IntValue ironBustWeight;
                public final ForgeConfigSpec.IntValue ironDoubleWeight;
                public final ForgeConfigSpec.IntValue ironTripleWeight;
                public final ForgeConfigSpec.IntValue ironJackpotWeight;
                public final ForgeConfigSpec.IntValue goldBustWeight;
                public final ForgeConfigSpec.IntValue goldDoubleWeight;
                public final ForgeConfigSpec.IntValue goldTripleWeight;
                public final ForgeConfigSpec.IntValue goldJackpotWeight;
                public final ForgeConfigSpec.IntValue diamondBustWeight;
                public final ForgeConfigSpec.IntValue diamondDoubleWeight;
                public final ForgeConfigSpec.IntValue diamondTripleWeight;
                public final ForgeConfigSpec.IntValue diamondJackpotWeight;
                public final ForgeConfigSpec.IntValue bustShots;
                public final ForgeConfigSpec.IntValue doubleShots;
                public final ForgeConfigSpec.IntValue tripleShots;
                public final ForgeConfigSpec.IntValue jackpotShots;
                public final ForgeConfigSpec.IntValue pityJackpotWeightPerShot;
                public final ForgeConfigSpec.IntValue pityMaxJackpotWeight;
                public final ForgeConfigSpec.DoubleValue obsidianCoreBaseDamage;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> bustBulletPool;
                public final ForgeConfigSpec.ConfigValue<List<? extends String>> rewardBulletPool;

                public DestinyConfig(ForgeConfigSpec.Builder builder) {
                        builder.push("DestinySeven");
                        builder.comment("Weights for iron bullet lottery tickets.");
                        ironBustWeight = defineWeight(builder, "ironBustWeight", 55);
                        ironDoubleWeight = defineWeight(builder, "ironDoubleWeight", 30);
                        ironTripleWeight = defineWeight(builder, "ironTripleWeight", 12);
                        ironJackpotWeight = defineWeight(builder, "ironJackpotWeight", 3);

                        builder.comment("Weights for golden bullet lottery tickets.");
                        goldBustWeight = defineWeight(builder, "goldBustWeight", 35);
                        goldDoubleWeight = defineWeight(builder, "goldDoubleWeight", 38);
                        goldTripleWeight = defineWeight(builder, "goldTripleWeight", 20);
                        goldJackpotWeight = defineWeight(builder, "goldJackpotWeight", 7);

                        builder.comment("Weights for diamond bullet lottery tickets.");
                        diamondBustWeight = defineWeight(builder, "diamondBustWeight", 20);
                        diamondDoubleWeight = defineWeight(builder, "diamondDoubleWeight", 40);
                        diamondTripleWeight = defineWeight(builder, "diamondTripleWeight", 28);
                        diamondJackpotWeight = defineWeight(builder, "diamondJackpotWeight", 12);

                        bustShots = builder.comment("Projectile count when Destiny Seven rolls Bust.")
                                        .defineInRange("bustShots", 1, 1, 64);
                        doubleShots = builder.comment("Projectile count when Destiny Seven rolls Double.")
                                        .defineInRange("doubleShots", 2, 1, 64);
                        tripleShots = builder.comment("Projectile count when Destiny Seven rolls Triple.")
                                        .defineInRange("tripleShots", 3, 1, 64);
                        jackpotShots = builder.comment("Projectile count when Destiny Seven rolls Jackpot.")
                                        .defineInRange("jackpotShots", 7, 1, 64);
                        pityJackpotWeightPerShot = builder
                                        .comment("Jackpot weight added per non-jackpot shot stored on the gun.")
                                        .defineInRange("pityJackpotWeightPerShot", 1, 0, 100);
                        pityMaxJackpotWeight = builder.comment("Maximum extra jackpot weight from stored pity.")
                                        .defineInRange("pityMaxJackpotWeight", 25, 0, 1000);
                        obsidianCoreBaseDamage = builder.comment(
                                        "Base damage used when Destiny Seven rolls a rare Obsidian Core entity. Gun bonus damage and damage multiplier still apply.")
                                        .defineInRange("obsidianCoreBaseDamage", 20.0, 0.0, 1000.0);

                        bustBulletPool = builder
                                        .comment(
                                                        "Weighted bullet pool for Bust. Format: modid:item=weight. Missing optional mod bullets are skipped.")
                                        .defineList("bustBulletPool", Arrays.asList(
                                                        "gunswithoutroses:iron_bullet=70",
                                                        "gwrexpansions:slime_bullet=30"),
                                                        value -> value instanceof String);
                        rewardBulletPool = builder
                                        .comment(
                                                        "Weighted bullet/entity pool for Double/Triple/Jackpot. Format: modid:item=weight. Missing optional mod bullets/entities are skipped. gwrexpansions:obsidian_core is a rare Obsidian Core entity when BOMD is loaded.")
                                        .defineList("rewardBulletPool", Arrays.asList(
                                                        "gunswithoutroses:iron_bullet=200",
                                                        "gwrexpansions:slime_bullet=180",
                                                        "gwrexpansions:golden_bullet=150",
                                                        "gwrexpansions:diamond_bullet=120",
                                                        "gwrexpansions:silver_bullet=120",
                                                        "gwrexpansions:dragonsteel_fire_bullet=80",
                                                        "gwrexpansions:dragonsteel_ice_bullet=80",
                                                        "gwrexpansions:dragonsteel_lightning_bullet=80",
                                                        "gwrexpansions:netherite_bullet=80",
                                                        "gwrexpansions:lavapower_bullet=80",
                                                        "gwrexpansions:cursium_bullet=80",
                                                        "gwrexpansions:ignitium_bullet=80",
                                                        "gwrexpansions:tidal_bullet=80",
                                                        "gwrexpansions:obsidian_core=40"),
                                                        value -> value instanceof String);
                        builder.pop();
                }

                private static ForgeConfigSpec.IntValue defineWeight(ForgeConfigSpec.Builder builder, String name,
                                int defaultValue) {
                        return builder.defineInRange(name, defaultValue, 0, 1000);
                }
        }

        public static final LauncherConfigs LAUNCHER;
        public static final GeneralConfig GENERAL;
        public static final BurstgunConfigs BURSTGUN;
        public static final SniperConfigs SNIPER;
        public static final ShotgunConfigs SHOTGUN;
        public static final GatlingConfigs GATLING;
        public static final PistolConfigs PISTOL;
        public static final BulletConfigs BULLET;
        public static final DestinyConfig DESTINY;

        static {
                BUILDER.push("Guns Without Roses Expansions Config");
                GENERAL = new GeneralConfig(BUILDER);
                SNIPER = new SniperConfigs(BUILDER);
                SHOTGUN = new ShotgunConfigs(BUILDER);
                GATLING = new GatlingConfigs(BUILDER);
                PISTOL = new PistolConfigs(BUILDER);
                BULLET = new BulletConfigs(BUILDER);
                LAUNCHER = new LauncherConfigs(BUILDER);
                BURSTGUN = new BurstgunConfigs(BUILDER);
                DESTINY = new DestinyConfig(BUILDER);
                BUILDER.pop();
                SPEC = BUILDER.build();
        }

        public static void register() {
                ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
        }

        public static void save() {
                SPEC.save();
        }

        public static void updateCachedValue(String path, double value) {
                configCache.put(path, value);
        }

        public static double getCachedValue(String path) {
                return configCache.getOrDefault(path, 0.0);
        }
}
