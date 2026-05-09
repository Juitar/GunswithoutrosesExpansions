package juitar.gwrexpansions.client.gui;

import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.config.GWREConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

public class GWREConfigScreen {
    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (client, parent) -> GWREConfigScreen.build(parent)));
    }

    public static Screen build(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(text("title"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        addBulletCategory(builder, entries);
        addGunCategory(builder, entries, "item.gwrexpansions.netherite_sniper",
            GWREConfig.SNIPER.netherite, 0, 1.8D, 1.5D, 24, 0.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.cursium_sniper",
            GWREConfig.SNIPER.cursium, 0, 2.0D, 2.0D, 24, 0.0D);
        addGunCategory(builder, entries, "config.gwrexpansions.dragonsteel_snipers",
            GWREConfig.SNIPER.DragonSteel, 0, 1.9D, 1.8D, 24, 0.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.destiny_seven",
            GWREConfig.SNIPER.destiny_seven, 0, 1.5D, 1.5D, 24, 0.0D);

        addGunCategory(builder, entries, "item.gwrexpansions.netherite_shotgun",
            GWREConfig.SHOTGUN.Netherite, 0, 0.6D, 1.0D, 20, 5.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.netherite_monster_shotgun",
            GWREConfig.SHOTGUN.NetheriteMonster, 0, 0.8D, 1.0D, 20, 4.0D);
        addGunCategory(builder, entries, "config.gwrexpansions.dragonsteel_shotguns",
            GWREConfig.SHOTGUN.DragonSteel, 0, 0.75D, 1.0D, 20, 4.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.super_shotgun",
            GWREConfig.SHOTGUN.Supershotgun, 0, 1.5D, 1.0D, 40, 4.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.mirecaller_shotgun",
            GWREConfig.SHOTGUN.Mirecaller, 0, 0.55D, 1.0D, 24, 4.5D);

        addGunCategory(builder, entries, "item.gwrexpansions.netherite_gatling",
            GWREConfig.GATLING.Netherite, 1, 1.0D, 1.0D, 4, 3.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.ignitium_gatling",
            GWREConfig.GATLING.Ignitium, 4, 1.0D, 1.0D, 4, 3.0D);
        addGunCategory(builder, entries, "config.gwrexpansions.dragonsteel_gatlings",
            GWREConfig.GATLING.DragonSteel, 3, 1.0D, 1.0D, 4, 3.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.skullcrusher_pulverizer",
            GWREConfig.GATLING.skull, 0, 1.0D, 1.0D, 6, 6.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.magnetic_gatling",
            GWREConfig.GATLING.Magnetic, 0, 0.9D, 1.0D, 4, 4.0D);

        addGunCategory(builder, entries, "item.gwrexpansions.tidal_pistol",
            GWREConfig.PISTOL.tidal, 0, 1.0D, 1.0D, 18, 2.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.hellforge_revolver",
            GWREConfig.PISTOL.hellforge, 0, 1.0D, 1.2D, 60, 1.0D);
        addGunCategory(builder, entries, "item.gwrexpansions.obsidian_launcher",
            GWREConfig.LAUNCHER.Obisidian, 30, 1.0D, 1.0D, 60, 0.0D);

        addBurstGunCategory(builder, entries, "item.gwrexpansions.voidspike",
            GWREConfig.BURSTGUN.voidBurst, 2, 1.0D, 1.0D, 25, 0.0D, 3, 5);
        addDuskfallCategory(builder, entries);
        addDestinyCategory(builder, entries);
        addClientCategory(builder, entries);

        builder.setSavingRunnable(() -> {
            GWREConfig.save();
            ClientConfig.save();
        });
        return builder.build();
    }

    private static void addBulletCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.bullets"));
        addDouble(category, entries, "phantom_halberd_damage", GWREConfig.BulletConfig.phantomHalberDamage, 10.0D, 0.0D, 100.0D);
        addDouble(category, entries, "phantom_halberd_range", GWREConfig.BulletConfig.phantomHalberdRange, 5.0D, 0.0D, 100.0D);
        addInt(category, entries, "phantom_halberd_delay", GWREConfig.BulletConfig.phantomHalberdDelay, 20, 0, 100);
        addDouble(category, entries, "flamejet_damage", GWREConfig.BulletConfig.flamejetDamage, 7.0D, 0.0D, 100.0D);
        addInt(category, entries, "flamejet_count", GWREConfig.BulletConfig.flamejetCount, 5, 0, 20);
        addDouble(category, entries, "portal_damage", GWREConfig.BulletConfig.portal_damage, 10.0D, 0.0D, 100.0D);
        addDouble(category, entries, "portal_hpdamage", GWREConfig.BulletConfig.portal_hpdamage, 0.0D, 0.0D, 100.0D);
        addDouble(category, entries, "golden_nugget_drop_rate", GWREConfig.BulletConfig.golden_nugget_drop_rate, 0.4D, 0.0D, 1.0D);
        addDouble(category, entries, "golden_apple_drop_rate", GWREConfig.BulletConfig.golden_apple_drop_rate, 0.033D, 0.0D, 1.0D);
    }

    private static void addGunCategory(ConfigBuilder builder, ConfigEntryBuilder entries, String categoryKey,
        GWREConfig.GunConfig config, int defaultBonusDamage, double defaultDamageMultiplier,
        double defaultHeadshotMultiplier, int defaultFireDelay, double defaultInaccuracy) {

        ConfigCategory category = builder.getOrCreateCategory(Component.translatable(categoryKey));
        addGunEntries(category, entries, config, defaultBonusDamage, defaultDamageMultiplier,
            defaultHeadshotMultiplier, defaultFireDelay, defaultInaccuracy);
    }

    private static void addBurstGunCategory(ConfigBuilder builder, ConfigEntryBuilder entries, String categoryKey,
        GWREConfig.BurstgunConfig config, int defaultBonusDamage, double defaultDamageMultiplier,
        double defaultHeadshotMultiplier, int defaultFireDelay, double defaultInaccuracy,
        int defaultBurstSize, int defaultBurstDelay) {

        ConfigCategory category = builder.getOrCreateCategory(Component.translatable(categoryKey));
        addGunEntries(category, entries, config.gunConfig, defaultBonusDamage, defaultDamageMultiplier,
            defaultHeadshotMultiplier, defaultFireDelay, defaultInaccuracy);
        addInt(category, entries, "burst_size", config.burstSize, defaultBurstSize, 1, 10);
        addInt(category, entries, "burst_delay", config.burstDelay, defaultBurstDelay, 1, 100);
    }

    private static void addDuskfallCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        GWREConfig.DuskfallEclipseConfig config = GWREConfig.BURSTGUN.duskfallEclipse;
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("item.gwrexpansions.duskfall_eclipse_blaster"));

        addGunEntries(category, entries, config.gunConfig, 0, 0.65D, 1.0D, 28, 1.5D);
        addInt(category, entries, "burst_size", config.burstSize, 6, 1, 10);
        addInt(category, entries, "burst_delay", config.burstDelay, 3, 1, 100);
        addInt(category, entries, "pierce_count", config.pierceCount, 2, 0, 16);
        addDouble(category, entries, "pierce_damage_multiplier", config.pierceDamageMultiplier, 0.8D, 0.0D, 4.0D);
        addInt(category, entries, "max_spirits", config.maxSpirits, 3, 0, 16);
        addInt(category, entries, "spirit_summon_interval_ticks", config.spiritSummonIntervalTicks, 150, 1, 6000);
        addInt(category, entries, "unequipped_grace_ticks", config.unequippedGraceTicks, 100, 0, 6000);
        addDouble(category, entries, "damage_bonus_per_spirit", config.damageBonusPerSpirit, 0.075D, 0.0D, 4.0D);
        addDouble(category, entries, "damage_reduction_per_spirit", config.damageReductionPerSpirit, 0.05D, 0.0D, 0.95D);
        addDouble(category, entries, "spirit_auto_target_range", config.spiritAutoTargetRange, 18.0D, 1.0D, 128.0D);
        addInt(category, entries, "spirit_attack_cooldown_ticks", config.spiritAttackCooldownTicks, 40, 1, 6000);
        addInt(category, entries, "spirit_warn_ticks", config.spiritWarnTicks, 14, 0, 200);
        addDouble(category, entries, "spirit_attack_damage", config.spiritAttackDamage, 5.0D, 0.0D, 1000.0D);
        addDouble(category, entries, "spirit_max_health", config.spiritMaxHealth, 40.0D, 1.0D, 1000.0D);
        addDouble(category, entries, "spirit_armor", config.spiritArmor, 5.0D, 0.0D, 1000.0D);
        addInt(category, entries, "last_target_memory_ticks", config.lastTargetMemoryTicks, 200, 0, 6000);
    }

    private static void addDestinyCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        GWREConfig.DestinyConfig config = GWREConfig.DESTINY;
        ConfigCategory category = builder.getOrCreateCategory(text("category.destiny"));

        addInt(category, entries, "iron_bust_weight", config.ironBustWeight, 55, 0, 1000);
        addInt(category, entries, "iron_double_weight", config.ironDoubleWeight, 30, 0, 1000);
        addInt(category, entries, "iron_triple_weight", config.ironTripleWeight, 12, 0, 1000);
        addInt(category, entries, "iron_jackpot_weight", config.ironJackpotWeight, 3, 0, 1000);
        addInt(category, entries, "gold_bust_weight", config.goldBustWeight, 35, 0, 1000);
        addInt(category, entries, "gold_double_weight", config.goldDoubleWeight, 38, 0, 1000);
        addInt(category, entries, "gold_triple_weight", config.goldTripleWeight, 20, 0, 1000);
        addInt(category, entries, "gold_jackpot_weight", config.goldJackpotWeight, 7, 0, 1000);
        addInt(category, entries, "diamond_bust_weight", config.diamondBustWeight, 20, 0, 1000);
        addInt(category, entries, "diamond_double_weight", config.diamondDoubleWeight, 40, 0, 1000);
        addInt(category, entries, "diamond_triple_weight", config.diamondTripleWeight, 28, 0, 1000);
        addInt(category, entries, "diamond_jackpot_weight", config.diamondJackpotWeight, 12, 0, 1000);
        addInt(category, entries, "bust_shots", config.bustShots, 1, 1, 64);
        addInt(category, entries, "double_shots", config.doubleShots, 2, 1, 64);
        addInt(category, entries, "triple_shots", config.tripleShots, 3, 1, 64);
        addInt(category, entries, "jackpot_shots", config.jackpotShots, 7, 1, 64);
        addInt(category, entries, "pity_jackpot_weight_per_shot", config.pityJackpotWeightPerShot, 1, 0, 100);
        addInt(category, entries, "pity_max_jackpot_weight", config.pityMaxJackpotWeight, 25, 0, 1000);
        addDouble(category, entries, "obsidian_core_base_damage", config.obsidianCoreBaseDamage, 20.0D, 0.0D, 1000.0D);
    }

    private static void addClientCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.client"));
        category.addEntry(entries.startBooleanToggle(text("coin_counter_enabled"), ClientConfig.INSTANCE.coinCounterEnabled.get())
            .setDefaultValue(true)
            .setSaveConsumer(ClientConfig.INSTANCE.coinCounterEnabled::set)
            .build());
        addInt(category, entries, "coin_counter_offset_x", ClientConfig.INSTANCE.coinCounterOffsetX, 0, -2000, 2000);
        addInt(category, entries, "coin_counter_offset_y", ClientConfig.INSTANCE.coinCounterOffsetY, 8, -2000, 2000);
        addInt(category, entries, "coin_counter_background_alpha", ClientConfig.INSTANCE.coinCounterBackgroundAlpha, 0, 0, 255);
        addInt(category, entries, "coin_counter_scale", ClientConfig.INSTANCE.coinCounterScale, 100, 50, 200);
        category.addEntry(entries.startBooleanToggle(text("coin_counter_show_progress"), ClientConfig.INSTANCE.coinCounterShowProgress.get())
            .setDefaultValue(true)
            .setSaveConsumer(ClientConfig.INSTANCE.coinCounterShowProgress::set)
            .build());
    }

    private static void addGunEntries(ConfigCategory category, ConfigEntryBuilder entries, GWREConfig.GunConfig config,
        int defaultBonusDamage, double defaultDamageMultiplier, double defaultHeadshotMultiplier,
        int defaultFireDelay, double defaultInaccuracy) {

        addInt(category, entries, "bonus_damage", config.bonusDamage, defaultBonusDamage, 0, 100);
        addDouble(category, entries, "damage_multiplier", config.damageMultiplier, defaultDamageMultiplier, 0.0D, 10.0D);
        addDouble(category, entries, "headshot_multiplier", config.headshotMultiplier, defaultHeadshotMultiplier, 1.0D, 10.0D);
        addInt(category, entries, "fire_delay", config.fireDelay, defaultFireDelay, 4, 100);
        addDouble(category, entries, "inaccuracy", config.inaccuracy, defaultInaccuracy, 0.0D, 10.0D);
    }

    private static void addInt(ConfigCategory category, ConfigEntryBuilder entries, String key,
        ForgeConfigSpec.IntValue value, int defaultValue, int min, int max) {

        category.addEntry(entries.startIntField(text(key), value.get())
            .setMin(min)
            .setMax(max)
            .setDefaultValue(defaultValue)
            .setSaveConsumer(value::set)
            .build());
    }

    private static void addDouble(ConfigCategory category, ConfigEntryBuilder entries, String key,
        ForgeConfigSpec.DoubleValue value, double defaultValue, double min, double max) {

        category.addEntry(entries.startDoubleField(text(key), value.get())
            .setMin(min)
            .setMax(max)
            .setDefaultValue(defaultValue)
            .setSaveConsumer(value::set)
            .build());
    }

    private static Component text(String key) {
        return Component.translatable("config.gwrexpansions." + key);
    }
}
