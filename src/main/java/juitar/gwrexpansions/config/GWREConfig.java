package juitar.gwrexpansions.config;

import juitar.gwrexpansions.item.vanilla.Supershotgun;
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

    public static class BurstgunConfig {
        // 包含基础枪支配置
        public final GunConfig gunConfig;
        public final ForgeConfigSpec.IntValue burstSize;
        public final ForgeConfigSpec.IntValue burstDelay;

        public BurstgunConfig(ForgeConfigSpec.Builder builder, String name, int defaultDamage,
                double defaultMultiplier, double defaultHeadshot,
                int defaultDelay, double defaultInaccuracy, int defualtburstSize, int defualtburstDelay) {
            // 创建基础枪支配置
            gunConfig = new GunConfig(builder, name, defaultDamage, defaultMultiplier, defaultHeadshot, defaultDelay,
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

    // 狙击枪配置
    public static class SniperConfigs {
        public final GunConfig netherite;
        public final GunConfig cursium;
        public final GunConfig DragonSteel;
        public final GunConfig destiny_seven;

        public SniperConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Sniper");
            netherite = new GunConfig(builder, "Netherite", 0, 1.8, 1.5, 24, 0.0);
            cursium = new GunConfig(builder, "Cursium", 0, 2.0, 2.0, 24, 0.0);
            DragonSteel = new GunConfig(builder, "DragonSteel", 0, 1.9, 1.8, 24, 0.0);
            destiny_seven = new GunConfig(builder, "DestinySeven", 0, 1.5, 1.5, 24, 0.0);
            builder.pop();
        }
    }

    // 爆发枪配置
    public static class BurstgunConfigs {
        public final BurstgunConfig voidBurst;

        public BurstgunConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Burstgun");
            voidBurst = new BurstgunConfig(builder, "Void", 2, 1.0, 1.0, 25, 0.0, 3, 5);
            builder.pop();
        }
    }

    // 霰弹枪配置
    public static class ShotgunConfigs {
        public final GunConfig Netherite;
        public final GunConfig NetheriteMonster;
        public final GunConfig DragonSteel;
        public final GunConfig Supershotgun;

        public ShotgunConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Shotgun");
            Netherite = new GunConfig(builder, "Netherite", 0, 0.6, 1.0, 20, 5.0);
            NetheriteMonster = new GunConfig(builder, "NetheriteMonster", 0, 0.8, 1.0, 20, 4.0);
            DragonSteel = new GunConfig(builder, "DragonSteel", 0, 0.75, 1.0, 20, 4);
            Supershotgun = new GunConfig(builder, "Supershotgun", 0, 1.5, 1.0, 40, 4);
            builder.pop();
        }
    }

    // 加特林配置
    public static class GatlingConfigs {
        public final GunConfig Netherite;
        public final GunConfig Ignitium;
        public final GunConfig DragonSteel;
        public final GunConfig skull;
        public final GunConfig Magnetic;

        public GatlingConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Gatling");
            Netherite = new GunConfig(builder, "Netherite", 1, 1.0, 1.0, 4, 3.0);
            Ignitium = new GunConfig(builder, "Ignitium", 4, 1.0, 1.0, 4, 3.0);
            DragonSteel = new GunConfig(builder, "DragonSteel", 3, 1.0, 1.0, 4, 3.0);
            skull = new GunConfig(builder, "Skull", 0, 1.0, 1.0, 6, 6.0);
            Magnetic = new GunConfig(builder, "Magnetic", 0, 0.9, 1.0, 4, 4.0);
            builder.pop();
        }
    }

    // 手枪配置
    public static class PistolConfigs {
        public final GunConfig tidal;
        public final GunConfig hellforge;

        public PistolConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Pistol");
            tidal = new GunConfig(builder, "Tidal", 0, 1.0, 1.0, 18, 2.0);
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
            pityJackpotWeightPerShot = builder.comment("Jackpot weight added per non-jackpot shot stored on the gun.")
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
                            "gwrexpansions:slime_bullet=30"), value -> value instanceof String);
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
                            "gwrexpansions:obsidian_core=40"), value -> value instanceof String);
            builder.pop();
        }

        private static ForgeConfigSpec.IntValue defineWeight(ForgeConfigSpec.Builder builder, String name,
                int defaultValue) {
            return builder.defineInRange(name, defaultValue, 0, 1000);
        }
    }

    public static final LauncherConfigs LAUNCHER;
    public static final BurstgunConfigs BURSTGUN;
    public static final SniperConfigs SNIPER;
    public static final ShotgunConfigs SHOTGUN;
    public static final GatlingConfigs GATLING;
    public static final PistolConfigs PISTOL;
    public static final BulletConfigs BULLET;
    public static final DestinyConfig DESTINY;

    static {
        BUILDER.push("Guns Without Roses Expansions Config");
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

    public static void updateCachedValue(String path, double value) {
        configCache.put(path, value);
    }

    public static double getCachedValue(String path) {
        return configCache.getOrDefault(path, 0.0);
    }
}
