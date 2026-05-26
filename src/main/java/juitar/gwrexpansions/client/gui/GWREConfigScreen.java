package juitar.gwrexpansions.client.gui;

import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.config.GWREConfig;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.Arrays;
import java.util.List;

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

        addGeneralCategory(builder, entries);
        addBulletCategory(builder, entries);
        addSniperCategory(builder, entries);
        addShotgunCategory(builder, entries);
        addGatlingCategory(builder, entries);
        addPistolCategory(builder, entries);
        addLauncherCategory(builder, entries);
        addBurstGunCategory(builder, entries);
        addDestinyCategory(builder, entries);
        addClientCategory(builder, entries);

        builder.setSavingRunnable(() -> {
            GWREConfig.save();
            ClientConfig.save();
        });
        return builder.build();
    }

    private static void addGeneralCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.general"));
        category.addEntry(entries.startBooleanToggle(text("enable_all_achievements_super_shotgun_reward"),
                GWREConfig.GENERAL.enableAllAchievementsSuperShotgunReward.get())
                .setDefaultValue(true)
                .setSaveConsumer(GWREConfig.GENERAL.enableAllAchievementsSuperShotgunReward::set)
                .setTooltip(text("enable_all_achievements_super_shotgun_reward.tooltip"))
                .build());
    }

    private static void addBulletCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.bullets"));
        SubCategoryBuilder cursium = entries.startSubCategory(text("bullet.cursium")).setExpanded(false);
        addDouble(cursium::add, entries, "phantom_halberd_damage", GWREConfig.BulletConfig.phantomHalberDamage, 10.0D,
                0.0D, 100.0D);
        addDouble(cursium::add, entries, "phantom_halberd_range", GWREConfig.BulletConfig.phantomHalberdRange, 5.0D,
                0.0D, 100.0D);
        addInt(cursium::add, entries, "phantom_halberd_delay", GWREConfig.BulletConfig.phantomHalberdDelay, 20, 0, 100);
        category.addEntry(cursium.build());

        SubCategoryBuilder lavapower = entries.startSubCategory(text("bullet.lavapower")).setExpanded(false);
        addDouble(lavapower::add, entries, "flamejet_damage", GWREConfig.BulletConfig.flamejetDamage, 7.0D, 0.0D,
                100.0D);
        addInt(lavapower::add, entries, "flamejet_count", GWREConfig.BulletConfig.flamejetCount, 5, 0, 20);
        category.addEntry(lavapower.build());

        SubCategoryBuilder tidal = entries.startSubCategory(text("bullet.tidal")).setExpanded(false);
        addDouble(tidal::add, entries, "portal_damage", GWREConfig.BulletConfig.portal_damage, 10.0D, 0.0D, 100.0D);
        addDouble(tidal::add, entries, "portal_hpdamage", GWREConfig.BulletConfig.portal_hpdamage, 0.0D, 0.0D, 100.0D);
        category.addEntry(tidal.build());

        SubCategoryBuilder gold = entries.startSubCategory(text("bullet.gold")).setExpanded(false);
        addDouble(gold::add, entries, "golden_nugget_drop_rate", GWREConfig.BulletConfig.golden_nugget_drop_rate, 0.4D,
                0.0D, 1.0D);
        addDouble(gold::add, entries, "golden_apple_drop_rate", GWREConfig.BulletConfig.golden_apple_drop_rate, 0.033D,
                0.0D, 1.0D);
        category.addEntry(gold.build());
    }

    private static void addSniperCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.snipers"));
        addGunSubCategory(category, entries, "item.gwrexpansions.netherite_sniper",
                GWREConfig.SNIPER.netherite, 0, 1.8D, 1.5D, 24, 0.0D);
        addGunSubCategory(category, entries, "item.gwrexpansions.cursium_sniper",
                GWREConfig.SNIPER.cursium, 0, 2.0D, 2.0D, 24, 0.0D);
        addGunSubCategory(category, entries, "config.gwrexpansions.dragonsteel_snipers",
                GWREConfig.SNIPER.DragonSteel, 0, 1.9D, 1.8D, 24, 0.0D);
        addGunSubCategory(category, entries, "item.gwrexpansions.destiny_seven",
                GWREConfig.SNIPER.destiny_seven, 0, 1.5D, 1.5D, 24, 0.0D);
        addHarbingerRaycasterSubCategory(category, entries);
    }

    private static void addShotgunCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.shotguns"));
        addGunSubCategory(category, entries, "item.gwrexpansions.netherite_shotgun",
                GWREConfig.SHOTGUN.Netherite, 0, 0.6D, 1.0D, 20, 5.0D);
        addGunSubCategory(category, entries, "item.gwrexpansions.netherite_monster_shotgun",
                GWREConfig.SHOTGUN.NetheriteMonster, 0, 0.8D, 1.0D, 20, 4.0D);
        addGunSubCategory(category, entries, "config.gwrexpansions.dragonsteel_shotguns",
                GWREConfig.SHOTGUN.DragonSteel, 0, 0.75D, 1.0D, 20, 4.0D);
        addGunSubCategory(category, entries, "item.gwrexpansions.super_shotgun",
                GWREConfig.SHOTGUN.Supershotgun, 0, 1.5D, 1.0D, 40, 4.0D);
        addRemnantFangshotSubCategory(category, entries);
        addMirecallerSubCategory(category, entries);
    }

    private static void addGatlingCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.gatlings"));
        addGunSubCategory(category, entries, "item.gwrexpansions.netherite_gatling",
                GWREConfig.GATLING.Netherite, 1, 1.0D, 1.0D, 4, 3.0D);
        addGunSubCategory(category, entries, "item.gwrexpansions.ignitium_gatling",
                GWREConfig.GATLING.Ignitium, 4, 1.0D, 1.0D, 4, 3.0D);
        addGunSubCategory(category, entries, "config.gwrexpansions.dragonsteel_gatlings",
                GWREConfig.GATLING.DragonSteel, 3, 1.0D, 1.0D, 4, 3.0D);
        addGunSubCategory(category, entries, "item.gwrexpansions.skullcrusher_pulverizer",
                GWREConfig.GATLING.skull, 0, 1.0D, 1.0D, 6, 6.0D);
        addGunSubCategory(category, entries, "item.gwrexpansions.magnetic_gatling",
                GWREConfig.GATLING.Magnetic, 0, 0.9D, 1.0D, 4, 4.0D);
    }

    private static void addPistolCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.pistols"));
        addTidalPistolSubCategory(category, entries);
        addGunSubCategory(category, entries, "item.gwrexpansions.hellforge_revolver",
                GWREConfig.PISTOL.hellforge, 0, 1.0D, 1.2D, 60, 1.0D);
    }

    private static void addTidalPistolSubCategory(ConfigCategory category, ConfigEntryBuilder entries) {
        GWREConfig.TidalPistolConfig config = GWREConfig.PISTOL.tidal;
        SubCategoryBuilder subCategory = entries.startSubCategory(
                Component.translatable("item.gwrexpansions.tidal_pistol")).setExpanded(false);

        addGunEntries(subCategory::add, entries, config, 0, 1.0D, 1.0D, 18, 2.0D);
        addInt(subCategory::add, entries, "tidal_max_energy", config.maxEnergy, 100, 1, 1000);
        addInt(subCategory::add, entries, "tidal_inventory_regen", config.inventoryRegenPerSecond, 2, 0, 100);
        addInt(subCategory::add, entries, "tidal_held_land_regen", config.heldLandRegenPerSecond, 5, 0, 100);
        addInt(subCategory::add, entries, "tidal_held_water_regen", config.heldWaterRegenPerSecond, 8, 0, 100);
        addInt(subCategory::add, entries, "tidal_hit_energy", config.hitEnergy, 5, 0, 1000);
        addInt(subCategory::add, entries, "tidal_orb_cost", config.orbCost, 20, 0, 1000);
        addInt(subCategory::add, entries, "tidal_mine_cost", config.mineCost, 12, 0, 1000);
        addInt(subCategory::add, entries, "tidal_land_orb_cost", config.landOrbCost, 20, 0, 1000);
        addInt(subCategory::add, entries, "tidal_land_mine_cost", config.landMineCost, 6, 0, 1000);
        addInt(subCategory::add, entries, "tidal_portal_cost", config.portalCost, 50, 0, 1000);
        addInt(subCategory::add, entries, "tidal_rift_cost", config.riftCost, 100, 0, 1000);
        addInt(subCategory::add, entries, "tidal_portal_charge_ticks", config.portalChargeTicks, 12, 1, 72000);
        addInt(subCategory::add, entries, "tidal_rift_charge_ticks", config.riftChargeTicks, 35, 1, 72000);
        addInt(subCategory::add, entries, "tidal_land_orb_cooldown", config.landOrbCooldownTicks, 100, 0, 72000);
        addInt(subCategory::add, entries, "tidal_land_mine_cooldown", config.landMineCooldownTicks, 140, 0, 72000);
        addInt(subCategory::add, entries, "tidal_full_orb_cooldown", config.fullFormOrbCooldownTicks, 8, 0, 72000);
        addInt(subCategory::add, entries, "tidal_full_mine_cooldown", config.fullFormMineCooldownTicks, 12, 0, 72000);
        addDouble(subCategory::add, entries, "tidal_full_orb_chance", config.fullFormOrbChance, 0.45D, 0.0D, 1.0D);
        addDouble(subCategory::add, entries, "tidal_full_mine_chance", config.fullFormMineChance, 0.30D, 0.0D, 1.0D);
        addDouble(subCategory::add, entries, "tidal_land_orb_chance", config.landOrbChance, 0.20D, 0.0D, 1.0D);
        addDouble(subCategory::add, entries, "tidal_land_mine_chance", config.landMineChance, 0.12D, 0.0D, 1.0D);
        addDouble(subCategory::add, entries, "tidal_tentacle_chance", config.tentacleChance, 1.0D, 0.0D, 1.0D);
        addInt(subCategory::add, entries, "tidal_tentacle_cooldown", config.tentacleCooldownTicks, 120, 0, 72000);
        addDouble(subCategory::add, entries, "tidal_orb_speed", config.orbSpeedMultiplier, 4.0D, 0.1D, 10.0D);
        addDouble(subCategory::add, entries, "tidal_land_orb_speed", config.landOrbSpeedMultiplier, 3.2D, 0.1D, 10.0D);
        addDouble(subCategory::add, entries, "tidal_land_skill_damage_multiplier", config.landSkillDamageMultiplier, 0.35D, 0.0D, 100.0D);
        addDouble(subCategory::add, entries, "tidal_water_skill_damage_multiplier", config.waterSkillDamageMultiplier, 0.85D, 0.0D, 100.0D);
        addDouble(subCategory::add, entries, "tidal_portal_damage", config.portalDamage, 8.0D, 0.0D, 1000.0D);
        addDouble(subCategory::add, entries, "tidal_portal_hp_damage", config.portalHpDamage, 0.0D, 0.0D, 100.0D);
        addInt(subCategory::add, entries, "tidal_portal_warmup", config.portalWarmupTicks, 4, 0, 200);
        addInt(subCategory::add, entries, "tidal_rift_duration", config.riftDurationTicks, 240, 1, 72000);
        addDouble(subCategory::add, entries, "tidal_rift_radius", config.riftRadius, 10.0D, 0.0D, 64.0D);
        addDouble(subCategory::add, entries, "tidal_rift_damage", config.riftDamage, 5.0D, 0.0D, 1000.0D);
        addDouble(subCategory::add, entries, "tidal_rift_pull_strength", config.riftPullStrength, 0.14D, 0.0D, 2.0D);
        addInt(subCategory::add, entries, "tidal_rift_damage_interval", config.riftDamageIntervalTicks, 5, 1, 200);
        category.addEntry(subCategory.build());
    }

    private static void addLauncherCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.launchers"));
        addGunSubCategory(category, entries, "item.gwrexpansions.obsidian_launcher",
                GWREConfig.LAUNCHER.Obisidian, 30, 1.0D, 1.0D, 60, 0.0D);
    }

    private static void addBurstGunCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.burstguns"));
        addBurstGunSubCategory(category, entries, "item.gwrexpansions.voidspike",
                GWREConfig.BURSTGUN.voidBurst, 2, 1.0D, 1.0D, 25, 0.0D, 3, 5);
        addDuskfallSubCategory(category, entries);
        addCeraunusSubCategory(category, entries);
    }

    private static void addGunSubCategory(ConfigCategory category, ConfigEntryBuilder entries, String titleKey,
            GWREConfig.GunConfig config, int defaultBonusDamage, double defaultDamageMultiplier,
            double defaultHeadshotMultiplier, int defaultFireDelay, double defaultInaccuracy) {

        SubCategoryBuilder subCategory = entries.startSubCategory(Component.translatable(titleKey)).setExpanded(false);
        addGunEntries(subCategory::add, entries, config, defaultBonusDamage, defaultDamageMultiplier,
                defaultHeadshotMultiplier, defaultFireDelay, defaultInaccuracy);
        category.addEntry(subCategory.build());
    }

    private static void addBurstGunSubCategory(ConfigCategory category, ConfigEntryBuilder entries, String titleKey,
            GWREConfig.BurstgunConfig config, int defaultBonusDamage, double defaultDamageMultiplier,
            double defaultHeadshotMultiplier, int defaultFireDelay, double defaultInaccuracy,
            int defaultBurstSize, int defaultBurstDelay) {

        SubCategoryBuilder subCategory = entries.startSubCategory(Component.translatable(titleKey)).setExpanded(false);
        addGunEntries(subCategory::add, entries, config.gunConfig, defaultBonusDamage, defaultDamageMultiplier,
                defaultHeadshotMultiplier, defaultFireDelay, defaultInaccuracy);
        addInt(subCategory::add, entries, "burst_size", config.burstSize, defaultBurstSize, 1, 10);
        addInt(subCategory::add, entries, "burst_delay", config.burstDelay, defaultBurstDelay, 1, 100);
        category.addEntry(subCategory.build());
    }

    private static void addDuskfallSubCategory(ConfigCategory category, ConfigEntryBuilder entries) {
        GWREConfig.DuskfallEclipseConfig config = GWREConfig.BURSTGUN.duskfallEclipse;
        SubCategoryBuilder subCategory = entries.startSubCategory(
                Component.translatable("item.gwrexpansions.duskfall_eclipse_blaster")).setExpanded(false);

        addGunEntries(subCategory::add, entries, config.gunConfig, 0, 0.65D, 1.0D, 28, 1.5D);
        addInt(subCategory::add, entries, "burst_size", config.burstSize, 6, 1, 10);
        addInt(subCategory::add, entries, "burst_delay", config.burstDelay, 3, 1, 100);
        addInt(subCategory::add, entries, "pierce_count", config.pierceCount, 2, 0, 16);
        addDouble(subCategory::add, entries, "pierce_damage_multiplier", config.pierceDamageMultiplier, 0.8D, 0.0D,
                4.0D);
        addInt(subCategory::add, entries, "max_spirits", config.maxSpirits, 3, 0, 10);
        addInt(subCategory::add, entries, "spirit_summon_interval_ticks", config.spiritSummonIntervalTicks, 150, 1,
                6000);
        addInt(subCategory::add, entries, "unequipped_grace_ticks", config.unequippedGraceTicks, 100, 0, 6000);
        addDouble(subCategory::add, entries, "damage_bonus_per_spirit", config.damageBonusPerSpirit, 0.075D, 0.0D,
                4.0D);
        addDouble(subCategory::add, entries, "damage_reduction_per_spirit", config.damageReductionPerSpirit, 0.05D,
                0.0D, 0.95D);
        addDouble(subCategory::add, entries, "spirit_auto_target_range", config.spiritAutoTargetRange, 18.0D, 1.0D,
                128.0D);
        addInt(subCategory::add, entries, "spirit_attack_cooldown_ticks", config.spiritAttackCooldownTicks, 40, 1,
                6000);
        addInt(subCategory::add, entries, "spirit_warn_ticks", config.spiritWarnTicks, 14, 0, 200);
        addDouble(subCategory::add, entries, "spirit_attack_damage", config.spiritAttackDamage, 10.0D, 0.0D, 1000.0D);
        addDouble(subCategory::add, entries, "spirit_max_health", config.spiritMaxHealth, 20.0D, 1.0D, 1000.0D);
        addDouble(subCategory::add, entries, "spirit_armor", config.spiritArmor, 5.0D, 0.0D, 1000.0D);
        addInt(subCategory::add, entries, "last_target_memory_ticks", config.lastTargetMemoryTicks, 200, 0, 6000);
        category.addEntry(subCategory.build());
    }

    private static void addCeraunusSubCategory(ConfigCategory category, ConfigEntryBuilder entries) {
        GWREConfig.CeraunusBurstConfig config = GWREConfig.BURSTGUN.ceraunusBurst;
        SubCategoryBuilder subCategory = entries.startSubCategory(
                Component.translatable("item.gwrexpansions.ceraunus_burst")).setExpanded(false);

        addGunEntries(subCategory::add, entries, config.gunConfig, 0, 0.72D, 1.0D, 26, 1.35D);
        addInt(subCategory::add, entries, "burst_size", config.burstSize, 3, 1, 10);
        addInt(subCategory::add, entries, "burst_delay", config.burstDelay, 3, 1, 100);
        addDouble(subCategory::add, entries, "ceraunus_base_element_damage_multiplier",
                config.baseElementDamageMultiplier, 0.75D, 0.0D, 10.0D);
        addDouble(subCategory::add, entries, "ceraunus_combo_damage_multiplier", config.comboDamageMultiplier, 1.45D,
                0.0D, 20.0D);
        addDouble(subCategory::add, entries, "ceraunus_combo_radius", config.comboRadius, 6.5D, 1.0D, 32.0D);
        addInt(subCategory::add, entries, "ceraunus_combo_window_ticks", config.comboWindowTicks, 80, 1, 6000);
        addInt(subCategory::add, entries, "ceraunus_combo_display_ticks", config.comboDisplayTicks, 24, 1, 6000);
        addInt(subCategory::add, entries, "ceraunus_combo_delay_ticks", config.comboDelayTicks, 8, 0, 100);
        addInt(subCategory::add, entries, "ceraunus_storm_serpent_max", config.stormSerpentMax, 4, 0, 16);
        addDouble(subCategory::add, entries, "ceraunus_storm_serpent_bite_damage", config.stormSerpentBiteDamage, 12.0D,
                0.0D, 1000.0D);
        addDouble(subCategory::add, entries, "ceraunus_mixed_serpent_bite_damage", config.mixedSerpentBiteDamage, 8.0D,
                0.0D, 1000.0D);
        addDouble(subCategory::add, entries, "ceraunus_storm_serpent_secondary_damage_multiplier",
                config.stormSerpentSecondaryDamageMultiplier, 0.5D, 0.0D, 10.0D);
        addInt(subCategory::add, entries, "ceraunus_lightning_spear_max", config.lightningSpearMax, 4, 0, 32);
        addInt(subCategory::add, entries, "ceraunus_normal_wave_ticks", config.normalWaveTicks, 20, 1, 200);
        addInt(subCategory::add, entries, "ceraunus_pure_water_wave_ticks", config.pureWaterWaveTicks, 30, 1, 200);
        addDouble(subCategory::add, entries, "ceraunus_lightning_bullet_water_damage_multiplier",
                config.lightningBulletWaterDamageMultiplier, 1.3D, 0.0D, 10.0D);
        category.addEntry(subCategory.build());
    }

    private static void addHarbingerRaycasterSubCategory(ConfigCategory category, ConfigEntryBuilder entries) {
        GWREConfig.HarbingerRaycasterConfig config = GWREConfig.SNIPER.harbingerRaycaster;
        SubCategoryBuilder subCategory = entries.startSubCategory(
                Component.translatable("item.gwrexpansions.harbinger_raycaster")).setExpanded(false);

        addGunEntries(subCategory::add, entries, config, 0, 1.6D, 1.75D, 26, 0.0D);
        addInt(subCategory::add, entries, "harbinger_max_overload", config.maxOverload, 6, 1, 100);
        addInt(subCategory::add, entries, "harbinger_overload_mode_duration_ticks", config.overloadModeDurationTicks,
                120, 1, 6000);
        addBoolean(subCategory::add, entries, "harbinger_overload_flight_enabled", config.overloadFlightEnabled, true);
        addDouble(subCategory::add, entries, "harbinger_redstone_damage_bonus", config.redstoneDamageBonus, 1.0D, 0.0D,
                1000.0D);
        addInt(subCategory::add, entries, "harbinger_redstone_pierce", config.redstonePierce, 2, 0, 64);
        addDouble(subCategory::add, entries, "harbinger_redstone_pierce_damage_multiplier",
                config.redstonePierceDamageMultiplier, 1.0D, 0.0D, 10.0D);
        addDouble(subCategory::add, entries, "harbinger_death_laser_damage_multiplier",
                config.deathLaserDamageMultiplier, 0.8D, 0.0D, 100.0D);
        addDouble(subCategory::add, entries, "harbinger_death_laser_hp_damage", config.deathLaserHpDamage, 5.0D, 0.0D,
                100.0D);
        addInt(subCategory::add, entries, "harbinger_death_laser_segments", config.deathLaserSegments, 2, 1, 16);
        addDouble(subCategory::add, entries, "harbinger_death_laser_segment_length", config.deathLaserSegmentLength,
                30.0D, 1.0D, 128.0D);
        addInt(subCategory::add, entries, "harbinger_missiles_per_wave", config.missilesPerWave, 3, 0, 32);
        addInt(subCategory::add, entries, "harbinger_normal_headshot_missiles", config.normalHeadshotMissiles, 1, 0,
                32);
        addInt(subCategory::add, entries, "harbinger_missile_start_delay", config.missileStartDelay, 8, 0, 6000);
        addInt(subCategory::add, entries, "harbinger_missile_interval_ticks", config.missileIntervalTicks, 24, 1, 6000);
        addDouble(subCategory::add, entries, "harbinger_missile_damage", config.missileDamage, 5.0D, 0.0D, 1000.0D);
        addDouble(subCategory::add, entries, "harbinger_missile_target_range", config.missileTargetRange, 18.0D, 1.0D,
                128.0D);
        category.addEntry(subCategory.build());
    }

    private static void addMirecallerSubCategory(ConfigCategory category, ConfigEntryBuilder entries) {
        GWREConfig.MirecallerConfig config = GWREConfig.SHOTGUN.Mirecaller;
        SubCategoryBuilder subCategory = entries.startSubCategory(
                Component.translatable("item.gwrexpansions.mirecaller_shotgun")).setExpanded(false);

        addGunEntries(subCategory::add, entries, config, 0, 0.7D, 1.0D, 24, 4.5D);
        addDouble(subCategory::add, entries, "mirecaller_mine_explosion_power", config.mineExplosionPower, 2.5D, 0.0D,
                16.0D);
        category.addEntry(subCategory.build());
    }

    private static void addRemnantFangshotSubCategory(ConfigCategory category, ConfigEntryBuilder entries) {
        GWREConfig.RemnantFangshotConfig config = GWREConfig.SHOTGUN.RemnantFangshot;
        SubCategoryBuilder subCategory = entries.startSubCategory(
                Component.translatable("item.gwrexpansions.remnant_fangshot")).setExpanded(false);

        addGunEntries(subCategory::add, entries, config, 0, 0.65D, 1.0D, 24, 4.0D);
        addInt(subCategory::add, entries, "remnant_rage_required", config.rageRequired, 3, 1, 20);
        addInt(subCategory::add, entries, "remnant_awakened_ticks", config.awakenedTicks, 200, 1, 6000);
        addInt(subCategory::add, entries, "remnant_blade_amp_ticks", config.bladeAmpTicks, 80, 1, 6000);
        addInt(subCategory::add, entries, "remnant_combo_window_ticks", config.comboWindowTicks, 100, 1, 6000);
        addDouble(subCategory::add, entries, "remnant_base_melee_damage", config.baseMeleeDamage, 9.0D, 0.0D, 1000.0D);
        addDouble(subCategory::add, entries, "remnant_blade_damage_bonus", config.bladeDamageBonus, 3.0D, 0.0D,
                1000.0D);
        addDouble(subCategory::add, entries, "remnant_base_attack_speed_modifier", config.baseAttackSpeedModifier,
                -2.6D, -10.0D, 10.0D);
        addDouble(subCategory::add, entries, "remnant_amped_attack_speed_modifier", config.ampedAttackSpeedModifier,
                -2.4D, -10.0D, 10.0D);
        addDouble(subCategory::add, entries, "remnant_min_full_attack_scale", config.minFullAttackScale, 0.9D, 0.0D,
                1.0D);
        addDouble(subCategory::add, entries, "remnant_cooldown_remaining_multiplier",
                config.cooldownRemainingMultiplier, 0.5D, 0.0D, 1.0D);
        addDouble(subCategory::add, entries, "remnant_power_projectile_damage_multiplier",
                config.powerProjectileDamageMultiplier, 1.2D, 0.0D, 10.0D);
        addDouble(subCategory::add, entries, "remnant_power_stomp_damage_multiplier", config.powerStompDamageMultiplier,
                0.6D, 0.0D, 10.0D);
        addDouble(subCategory::add, entries, "remnant_power_stomp_range", config.powerStompRange, 2.75D, 0.0D, 64.0D);
        addInt(subCategory::add, entries, "remnant_dash_ticks", config.dashTicks, 10, 1, 200);
        addDouble(subCategory::add, entries, "remnant_dash_damage_multiplier", config.dashDamageMultiplier, 2.5D, 0.0D,
                20.0D);
        addDouble(subCategory::add, entries, "remnant_dash_speed", config.dashSpeed, 1.25D, 0.0D, 10.0D);
        addDouble(subCategory::add, entries, "remnant_dash_hit_range", config.dashHitRange, 1.15D, 0.0D, 16.0D);
        addDouble(subCategory::add, entries, "remnant_dash_damage_reduction", config.dashDamageReduction, 0.2D, 0.0D,
                0.95D);
        category.addEntry(subCategory.build());
    }

    private static void addDestinyCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        GWREConfig.DestinyConfig config = GWREConfig.DESTINY;
        ConfigCategory category = builder.getOrCreateCategory(text("category.destiny"));

        SubCategoryBuilder iron = entries.startSubCategory(text("destiny.iron")).setExpanded(false);
        addInt(iron::add, entries, "iron_bust_weight", config.ironBustWeight, 55, 0, 1000);
        addInt(iron::add, entries, "iron_double_weight", config.ironDoubleWeight, 30, 0, 1000);
        addInt(iron::add, entries, "iron_triple_weight", config.ironTripleWeight, 12, 0, 1000);
        addInt(iron::add, entries, "iron_jackpot_weight", config.ironJackpotWeight, 3, 0, 1000);
        category.addEntry(iron.build());

        SubCategoryBuilder gold = entries.startSubCategory(text("destiny.gold")).setExpanded(false);
        addInt(gold::add, entries, "gold_bust_weight", config.goldBustWeight, 35, 0, 1000);
        addInt(gold::add, entries, "gold_double_weight", config.goldDoubleWeight, 38, 0, 1000);
        addInt(gold::add, entries, "gold_triple_weight", config.goldTripleWeight, 20, 0, 1000);
        addInt(gold::add, entries, "gold_jackpot_weight", config.goldJackpotWeight, 7, 0, 1000);
        category.addEntry(gold.build());

        SubCategoryBuilder diamond = entries.startSubCategory(text("destiny.diamond")).setExpanded(false);
        addInt(diamond::add, entries, "diamond_bust_weight", config.diamondBustWeight, 20, 0, 1000);
        addInt(diamond::add, entries, "diamond_double_weight", config.diamondDoubleWeight, 40, 0, 1000);
        addInt(diamond::add, entries, "diamond_triple_weight", config.diamondTripleWeight, 28, 0, 1000);
        addInt(diamond::add, entries, "diamond_jackpot_weight", config.diamondJackpotWeight, 12, 0, 1000);
        category.addEntry(diamond.build());

        SubCategoryBuilder result = entries.startSubCategory(text("destiny.result")).setExpanded(false);
        addInt(result::add, entries, "bust_shots", config.bustShots, 1, 1, 64);
        addInt(result::add, entries, "double_shots", config.doubleShots, 2, 1, 64);
        addInt(result::add, entries, "triple_shots", config.tripleShots, 3, 1, 64);
        addInt(result::add, entries, "jackpot_shots", config.jackpotShots, 7, 1, 64);
        addInt(result::add, entries, "pity_jackpot_weight_per_shot", config.pityJackpotWeightPerShot, 1, 0, 100);
        addInt(result::add, entries, "pity_max_jackpot_weight", config.pityMaxJackpotWeight, 25, 0, 1000);
        addDouble(result::add, entries, "obsidian_core_base_damage", config.obsidianCoreBaseDamage, 20.0D, 0.0D,
                1000.0D);
        category.addEntry(result.build());

        SubCategoryBuilder pools = entries.startSubCategory(text("destiny.bullet_pools")).setExpanded(false);
        addStringList(pools::add, entries, "bust_bullet_pool", config.bustBulletPool,
                Arrays.asList("gunswithoutroses:iron_bullet=70", "gwrexpansions:slime_bullet=30"));
        addStringList(pools::add, entries, "reward_bullet_pool", config.rewardBulletPool,
                Arrays.asList(
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
                        "gwrexpansions:obsidian_core=40"));
        category.addEntry(pools.build());
    }

    private static void addClientCategory(ConfigBuilder builder, ConfigEntryBuilder entries) {
        ConfigCategory category = builder.getOrCreateCategory(text("category.client"));
        category.addEntry(
                entries.startBooleanToggle(text("coin_counter_enabled"), ClientConfig.INSTANCE.coinCounterEnabled.get())
                        .setDefaultValue(true)
                        .setSaveConsumer(ClientConfig.INSTANCE.coinCounterEnabled::set)
                        .build());
        category.addEntry(entries.startEnumSelector(text("coin_counter_position"),
                ClientConfig.CoinCounterPosition.class, ClientConfig.INSTANCE.coinCounterPosition.get())
                .setEnumNameProvider(value -> text("coin_counter_position." + value.name().toLowerCase()))
                .setDefaultValue(ClientConfig.CoinCounterPosition.TOP_CENTER)
                .setSaveConsumer(ClientConfig.INSTANCE.coinCounterPosition::set)
                .build());
        addInt(category::addEntry, entries, "coin_counter_offset_x", ClientConfig.INSTANCE.coinCounterOffsetX, 0, -2000,
                2000);
        addInt(category::addEntry, entries, "coin_counter_offset_y", ClientConfig.INSTANCE.coinCounterOffsetY, 8, -2000,
                2000);
        addInt(category::addEntry, entries, "coin_counter_background_alpha",
                ClientConfig.INSTANCE.coinCounterBackgroundAlpha, 0, 0, 255);
        addInt(category::addEntry, entries, "coin_counter_scale", ClientConfig.INSTANCE.coinCounterScale, 100, 50, 200);
        category.addEntry(entries
                .startBooleanToggle(text("coin_counter_show_progress"),
                        ClientConfig.INSTANCE.coinCounterShowProgress.get())
                .setDefaultValue(true)
                .setSaveConsumer(ClientConfig.INSTANCE.coinCounterShowProgress::set)
                .build());
        addBoolean(category::addEntry, entries, "harbinger_overload_hud_enabled",
                ClientConfig.INSTANCE.harbingerOverloadHudEnabled, true);
        addInt(category::addEntry, entries, "harbinger_overload_hud_offset_x",
                ClientConfig.INSTANCE.harbingerOverloadHudOffsetX, 0, -2000, 2000);
        addInt(category::addEntry, entries, "harbinger_overload_hud_offset_y",
                ClientConfig.INSTANCE.harbingerOverloadHudOffsetY, 32, -2000, 2000);
        addHudClientEntries(category, entries, "tidal_pistol_hud", ClientConfig.INSTANCE.tidalPistolHudEnabled,
                ClientConfig.INSTANCE.tidalPistolHudOffsetX, ClientConfig.INSTANCE.tidalPistolHudOffsetY, 42);
        addHudClientEntries(category, entries, "ceraunus_burst_hud", ClientConfig.INSTANCE.ceraunusBurstHudEnabled,
                ClientConfig.INSTANCE.ceraunusBurstHudOffsetX, ClientConfig.INSTANCE.ceraunusBurstHudOffsetY, 18);
        addHudClientEntries(category, entries, "remnant_fangshot_hud", ClientConfig.INSTANCE.remnantFangshotHudEnabled,
                ClientConfig.INSTANCE.remnantFangshotHudOffsetX, ClientConfig.INSTANCE.remnantFangshotHudOffsetY, 13);
        addHudClientEntries(category, entries, "super_shotgun_hud", ClientConfig.INSTANCE.superShotgunHudEnabled,
                ClientConfig.INSTANCE.superShotgunHudOffsetX, ClientConfig.INSTANCE.superShotgunHudOffsetY, 10);
        addHudClientEntries(category, entries, "skullcrusher_hud", ClientConfig.INSTANCE.skullcrusherHudEnabled,
                ClientConfig.INSTANCE.skullcrusherHudOffsetX, ClientConfig.INSTANCE.skullcrusherHudOffsetY, 10);
        addHudClientEntries(category, entries, "obsidian_launcher_hud", ClientConfig.INSTANCE.obsidianLauncherHudEnabled,
                ClientConfig.INSTANCE.obsidianLauncherHudOffsetX, ClientConfig.INSTANCE.obsidianLauncherHudOffsetY, 10);
    }

    private static void addHudClientEntries(ConfigCategory category, ConfigEntryBuilder entries, String key,
            ForgeConfigSpec.BooleanValue enabled, ForgeConfigSpec.IntValue offsetX, ForgeConfigSpec.IntValue offsetY,
            int defaultY) {

        addBoolean(category::addEntry, entries, key + "_enabled", enabled, true);
        addInt(category::addEntry, entries, key + "_offset_x", offsetX, 0, -2000, 2000);
        addInt(category::addEntry, entries, key + "_offset_y", offsetY, defaultY, -2000, 2000);
    }

    private static void addStringList(EntrySink sink, ConfigEntryBuilder entries, String key,
            ForgeConfigSpec.ConfigValue<List<? extends String>> value, List<String> defaultValue) {

        sink.add(entries.startStrList(text(key), copyStrings(value.get()))
                .setExpanded(false)
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set)
                .setTooltip(text(key + ".tooltip"))
                .build());
    }

    private static void addGunEntries(EntrySink sink, ConfigEntryBuilder entries, GWREConfig.GunConfig config,
            int defaultBonusDamage, double defaultDamageMultiplier, double defaultHeadshotMultiplier,
            int defaultFireDelay, double defaultInaccuracy) {

        addInt(sink, entries, "bonus_damage", config.bonusDamage, defaultBonusDamage, 0, 100);
        addDouble(sink, entries, "damage_multiplier", config.damageMultiplier, defaultDamageMultiplier, 0.0D, 10.0D);
        addDouble(sink, entries, "headshot_multiplier", config.headshotMultiplier, defaultHeadshotMultiplier, 1.0D,
                10.0D);
        addInt(sink, entries, "fire_delay", config.fireDelay, defaultFireDelay, 4, 100);
        addDouble(sink, entries, "inaccuracy", config.inaccuracy, defaultInaccuracy, 0.0D, 10.0D);
    }

    private static void addInt(EntrySink sink, ConfigEntryBuilder entries, String key,
            ForgeConfigSpec.IntValue value, int defaultValue, int min, int max) {

        sink.add(entries.startIntField(text(key), value.get())
                .setMin(min)
                .setMax(max)
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set)
                .build());
    }

    private static void addDouble(EntrySink sink, ConfigEntryBuilder entries, String key,
            ForgeConfigSpec.DoubleValue value, double defaultValue, double min, double max) {

        sink.add(entries.startDoubleField(text(key), value.get())
                .setMin(min)
                .setMax(max)
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set)
                .build());
    }

    private static void addBoolean(EntrySink sink, ConfigEntryBuilder entries, String key,
            ForgeConfigSpec.BooleanValue value, boolean defaultValue) {

        sink.add(entries.startBooleanToggle(text(key), value.get())
                .setDefaultValue(defaultValue)
                .setSaveConsumer(value::set)
                .build());
    }

    private static Component text(String key) {
        return Component.translatable("config.gwrexpansions." + key);
    }

    private static List<String> copyStrings(List<? extends String> values) {
        return values.stream().map(String::valueOf).toList();
    }

    private interface EntrySink {
        void add(AbstractConfigListEntry entry);
    }
}
