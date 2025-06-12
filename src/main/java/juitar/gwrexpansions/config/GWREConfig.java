package juitar.gwrexpansions.config;

import juitar.gwrexpansions.item.vanilla.Supershotgun;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
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

    // 霰弹枪配置
    public static class ShotgunConfigs {
        public final GunConfig Netherite;
        public final GunConfig NetheriteMonster;
        public final GunConfig DragonSteel;
        public final GunConfig Supershotgun;

        public ShotgunConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Shotgun");
            Netherite = new GunConfig(builder, "Netherite",0 , 0.6, 1.0, 20, 5.0);
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

        public GatlingConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Gatling");
            Netherite = new GunConfig(builder, "Netherite", 1, 1.0, 1.0, 4, 3.0);
            Ignitium = new GunConfig(builder, "Ignitium", 4, 1.0, 1.0, 4, 3.0);
            DragonSteel = new GunConfig(builder, "DragonSteel", 3, 1.0, 1.0, 4, 3.0);
            skull = new GunConfig(builder, "Skull", 0, 1.0, 1.0, 6, 6.0);
            builder.pop();
        }
    }

    // 手枪配置
    public static class PistolConfigs {
        public final GunConfig tidal;
        public PistolConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Pistol");
            tidal = new GunConfig(builder, "Tidal", 0, 1.0, 1.0, 18, 2.0);
            builder.pop();
        }
    }


    public static final SniperConfigs SNIPER;
    public static final ShotgunConfigs SHOTGUN;
    public static final GatlingConfigs GATLING;
    public static final PistolConfigs PISTOL;
    public static final BulletConfigs BULLET;

    static {
        BUILDER.push("Guns Without Roses Expansions Config");

        SNIPER = new SniperConfigs(BUILDER);
        SHOTGUN = new ShotgunConfigs(BUILDER);
        GATLING = new GatlingConfigs(BUILDER);
        PISTOL = new PistolConfigs(BUILDER);
        BULLET = new BulletConfigs(BUILDER);

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