package juitar.gwrexpansions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GWREConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec SPEC;
    private static Map<String, Double> configCache = new ConcurrentHashMap<>();

    // 子弹配置
    public static class BulletConfig {
        public final ForgeConfigSpec.DoubleValue phantomHalberDamage;
        public final ForgeConfigSpec.DoubleValue phantomHalberdRange;
        public final ForgeConfigSpec.DoubleValue flamejetDamage;
        public final ForgeConfigSpec.IntValue flamejetCount;

        public BulletConfig(ForgeConfigSpec.Builder bulider, String name, boolean hasPhantomHalber, boolean hasFlamejet) {
            bulider.push(name);
            if(hasPhantomHalber){
                phantomHalberDamage = bulider
                    .comment("Phantom Halber Damage")
                    .defineInRange("phantomHalberDamage", 10.0, 0.0, 100.0);
                phantomHalberdRange = bulider
                    .comment("Phantom Halberd Range")
                    .defineInRange("phantomHalberdRange", 5.0, 0.0, 100.0);
            } else {
                phantomHalberDamage = null;
                phantomHalberdRange = null;
            }
            
            if(hasFlamejet){
                flamejetDamage = bulider
                    .comment("Flamejet Damage")
                    .defineInRange("flamejetDamage", 7.0, 0.0, 100.0);
                flamejetCount = bulider
                    .comment("Flamejet Count")
                    .defineInRange("flamejetCount", 5, 0, 20);
            } else {
                flamejetDamage = null;
                flamejetCount = null;
            }
            bulider.pop();
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

        public SniperConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Sniper");
            netherite = new GunConfig(builder, "Netherite", 0, 1.8, 1.5, 24, 0.0);
            cursium = new GunConfig(builder, "Cursium", 0, 2.0, 2.0, 24, 0.0);
            DragonSteel = new GunConfig(builder, "DragonSteel", 0, 1.9, 1.8, 24, 0.0);
            builder.pop();
        }
    }

    // 霰弹枪配置
    public static class ShotgunConfigs {
        public final GunConfig netherite;
        public final GunConfig netheriteMonster;
        public final GunConfig DragonSteel;

        public ShotgunConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Shotgun");
            netherite = new GunConfig(builder, "Netherite",0 , 0.6, 1.0, 20, 5.0);
            netheriteMonster = new GunConfig(builder, "NetheriteMonster", 0, 0.8, 1.0, 20, 4.0);
            DragonSteel = new GunConfig(builder, "DragonSteel", 0, 0.75, 1.0, 20, 4);
            builder.pop();
        }
    }

    // 加特林配置
    public static class GatlingConfigs {
        public final GunConfig netherite;
        public final GunConfig ignitium;
        public final GunConfig DragonSteel;

        public GatlingConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Gatling");
            netherite = new GunConfig(builder, "Netherite", 1, 1.0, 1.0, 4, 3.0);
            ignitium = new GunConfig(builder, "Ignitium", 4, 1.0, 1.0, 4, 3.0);
            DragonSteel = new GunConfig(builder, "DragonSteel", 3, 1.0, 1.0, 4, 3.0);
            builder.pop();
        }
    }

    // 手枪配置
    public static class PistolConfigs {
        // TODO: 手枪配置
        public PistolConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Pistol");

            builder.pop();
        }
    }
    // 子弹配置
    public static class BulletConfigs {
        public final BulletConfig lavapower;
        public final BulletConfig cursium;


        public BulletConfigs(ForgeConfigSpec.Builder builder) {
            builder.push("Bullets");
            lavapower = new BulletConfig(builder, "Lavapower", false,true);
            cursium = new BulletConfig(builder, "Cursium", true,false);
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
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    public static void updateCachedValue(String path, double value) {
        configCache.put(path, value);
    }

    public static double getCachedValue(String path) {
        return configCache.getOrDefault(path, 0.0);
    }
}