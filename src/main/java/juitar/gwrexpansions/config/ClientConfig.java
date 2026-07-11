package juitar.gwrexpansions.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 客户端配置
 */
public class ClientConfig {

    /**
     * 硬币计数器位置枚举
     */
    public enum CoinCounterPosition {
        TOP_CENTER("top_center", "屏幕上方居中"),
        TOP_LEFT("top_left", "屏幕左上角"),
        TOP_RIGHT("top_right", "屏幕右上角"),
        BOTTOM_CENTER("bottom_center", "屏幕下方居中"),
        BOTTOM_LEFT("bottom_left", "屏幕左下角"),
        BOTTOM_RIGHT("bottom_right", "屏幕右下角"),
        CUSTOM("custom", "自定义位置");

        private final String configValue;
        private final String displayName;

        CoinCounterPosition(String configValue, String displayName) {
            this.configValue = configValue;
            this.displayName = displayName;
        }

        public String getConfigValue() {
            return configValue;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static CoinCounterPosition fromConfigValue(String value) {
            for (CoinCounterPosition pos : values()) {
                if (pos.configValue.equals(value)) {
                    return pos;
                }
            }
            return TOP_CENTER; // 默认值
        }
    }

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig INSTANCE;

    // 硬币计数器UI配置
    public final ForgeConfigSpec.BooleanValue coinCounterEnabled;
    public final ForgeConfigSpec.EnumValue<CoinCounterPosition> coinCounterPosition;
    public final ForgeConfigSpec.DoubleValue coinCounterOffsetX;
    public final ForgeConfigSpec.DoubleValue coinCounterOffsetY;
    public final ForgeConfigSpec.IntValue coinCounterBackgroundAlpha;
    public final ForgeConfigSpec.IntValue coinCounterScale;
    public final ForgeConfigSpec.BooleanValue coinCounterShowProgress;
    public final ForgeConfigSpec.BooleanValue harbingerOverloadHudEnabled;
    public final ForgeConfigSpec.DoubleValue harbingerOverloadHudOffsetX;
    public final ForgeConfigSpec.DoubleValue harbingerOverloadHudOffsetY;
    public final ForgeConfigSpec.BooleanValue tidalPistolHudEnabled;
    public final ForgeConfigSpec.DoubleValue tidalPistolHudOffsetX;
    public final ForgeConfigSpec.DoubleValue tidalPistolHudOffsetY;
    public final ForgeConfigSpec.BooleanValue ceraunusBurstHudEnabled;
    public final ForgeConfigSpec.DoubleValue ceraunusBurstHudOffsetX;
    public final ForgeConfigSpec.DoubleValue ceraunusBurstHudOffsetY;
    public final ForgeConfigSpec.BooleanValue remnantFangshotHudEnabled;
    public final ForgeConfigSpec.DoubleValue remnantFangshotHudOffsetX;
    public final ForgeConfigSpec.DoubleValue remnantFangshotHudOffsetY;
    public final ForgeConfigSpec.BooleanValue cursiumSniperHudEnabled;
    public final ForgeConfigSpec.DoubleValue cursiumSniperHudOffsetX;
    public final ForgeConfigSpec.DoubleValue cursiumSniperHudOffsetY;
    public final ForgeConfigSpec.BooleanValue superShotgunHudEnabled;
    public final ForgeConfigSpec.DoubleValue superShotgunHudOffsetX;
    public final ForgeConfigSpec.DoubleValue superShotgunHudOffsetY;
    public final ForgeConfigSpec.DoubleValue superShotgunHudScale;
    public final ForgeConfigSpec.BooleanValue skullcrusherHudEnabled;
    public final ForgeConfigSpec.DoubleValue skullcrusherHudOffsetX;
    public final ForgeConfigSpec.DoubleValue skullcrusherHudOffsetY;
    public final ForgeConfigSpec.DoubleValue skullcrusherHudCenterYAdjust;
    public final ForgeConfigSpec.IntValue skullcrusherHudLeftArcXAdjust;
    public final ForgeConfigSpec.IntValue skullcrusherHudRightArcXAdjust;
    public final ForgeConfigSpec.DoubleValue skullcrusherHudArcScale;
    public final ForgeConfigSpec.IntValue skullcrusherHudBaseGap;
    public final ForgeConfigSpec.DoubleValue skullcrusherHudSpreadGapMultiplier;
    public final ForgeConfigSpec.BooleanValue skullcrusherIdleSoundEnabled;
    public final ForgeConfigSpec.BooleanValue obsidianLauncherHudEnabled;
    public final ForgeConfigSpec.DoubleValue obsidianLauncherHudOffsetX;
    public final ForgeConfigSpec.DoubleValue obsidianLauncherHudOffsetY;
    public final ForgeConfigSpec.BooleanValue hellforgeChainHudEnabled;
    public final ForgeConfigSpec.DoubleValue hellforgeChainHudOffsetX;
    public final ForgeConfigSpec.DoubleValue hellforgeChainHudOffsetY;
    public final ForgeConfigSpec.IntValue hellforgeChainHudScale;
    public final ForgeConfigSpec.BooleanValue hellforgeCoinHitShockEnabled;
    public final ForgeConfigSpec.IntValue hellforgeCoinHitShockStrength;
    public final ForgeConfigSpec.BooleanValue hellforgeCoinHitFovPunchEnabled;
    public final ForgeConfigSpec.BooleanValue hellforgeCoinHitHudFlashEnabled;
    public final ForgeConfigSpec.BooleanValue hellforgeOverheatVoiceEnabled;
    public final ForgeConfigSpec.BooleanValue hellforgeOverheatMusicEnabled;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        INSTANCE = new ClientConfig(builder);
        CLIENT_SPEC = builder.build();
    }

    public ClientConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("硬币计数器UI设置 / Coin Counter UI Settings")
                .push("coin_counter");

        coinCounterEnabled = builder
                .comment("是否启用硬币计数器UI / Enable coin counter UI")
                .define("enabled", true);

        coinCounterPosition = builder
                .comment("硬币计数器位置 / Coin counter position",
                        "选项 / Options:",
                        "- TOP_CENTER: 屏幕上方居中",
                        "- TOP_LEFT: 屏幕左上角",
                        "- TOP_RIGHT: 屏幕右上角",
                        "- BOTTOM_CENTER: 屏幕下方居中",
                        "- BOTTOM_LEFT: 屏幕左下角",
                        "- BOTTOM_RIGHT: 屏幕右下角",
                        "- CUSTOM: 自定义位置（使用offset_x和offset_y）")
                .defineEnum("position", CoinCounterPosition.TOP_CENTER);

        coinCounterOffsetX = builder
                .comment("硬币计数器X轴偏移 (仅在position为custom时生效) / X offset (only works when position is custom)")
                .defineInRange("offset_x", 0.0D, -2000.0D, 2000.0D);

        coinCounterOffsetY = builder
                .comment("硬币计数器Y轴偏移 (仅在position为custom时生效) / Y offset (only works when position is custom)")
                .defineInRange("offset_y", 8.0D, -2000.0D, 2000.0D);

        coinCounterBackgroundAlpha = builder
                .comment("硬币计数器背景透明度 (0-255, 0为完全透明) / Background alpha (0-255, 0 is fully transparent)")
                .defineInRange("background_alpha", 0, 0, 255);

        coinCounterScale = builder
                .comment("硬币计数器缩放百分比 / Scale percentage")
                .defineInRange("scale", 100, 50, 200);

        coinCounterShowProgress = builder
                .comment("是否显示连击进度 / Show combo progress")
                .define("show_progress", true);

        builder.pop();

        builder.comment("先兆裁光过载HUD设置 / Harbinger Raycaster Overload HUD Settings")
                .push("harbinger_overload_hud");

        harbingerOverloadHudEnabled = builder
                .comment("是否启用先兆裁光过载电池HUD / Enable Harbinger Raycaster overload battery HUD")
                .define("enabled", true);

        harbingerOverloadHudOffsetX = builder
                .comment("先兆裁光过载HUD X轴偏移 / X offset for Harbinger overload HUD")
                .defineInRange("offset_x", 0.0D, -2000.0D, 2000.0D);

        harbingerOverloadHudOffsetY = builder
                .comment("先兆裁光过载HUD Y轴偏移 / Y offset for Harbinger overload HUD")
                .defineInRange("offset_y", 32.0D, -2000.0D, 2000.0D);

        builder.pop();

        builder.comment("武器HUD设置 / Weapon HUD Settings")
                .push("weapon_huds");

        tidalPistolHudEnabled = defineHudEnabled(builder, "tidal_pistol", "潮汐手枪 / Tidal Pistol");
        tidalPistolHudOffsetX = defineHudOffsetX(builder, "tidal_pistol");
        tidalPistolHudOffsetY = defineHudOffsetY(builder, "tidal_pistol", 42);

        ceraunusBurstHudEnabled = defineHudEnabled(builder, "ceraunus_burst", "雷霆刻律 / Ceraunus Burst");
        ceraunusBurstHudOffsetX = defineHudOffsetX(builder, "ceraunus_burst");
        ceraunusBurstHudOffsetY = defineHudOffsetY(builder, "ceraunus_burst", 18);

        remnantFangshotHudEnabled = defineHudEnabled(builder, "remnant_fangshot", "遗迹牙铳 / Remnant Fangshot");
        remnantFangshotHudOffsetX = defineHudOffsetX(builder, "remnant_fangshot");
        remnantFangshotHudOffsetY = defineHudOffsetY(builder, "remnant_fangshot", 13);

        cursiumSniperHudEnabled = defineHudEnabled(builder, "cursium_sniper", "咒骸弩炮 / Cursium Ballista");
        cursiumSniperHudOffsetX = defineHudOffsetX(builder, "cursium_sniper");
        cursiumSniperHudOffsetY = defineHudOffsetY(builder, "cursium_sniper", 23);

        superShotgunHudEnabled = defineHudEnabled(builder, "super_shotgun", "超级霰弹枪 / Super Shotgun");
        superShotgunHudOffsetX = defineHudOffsetX(builder, "super_shotgun");
        superShotgunHudOffsetY = defineHudOffsetY(builder, "super_shotgun", 10);
        superShotgunHudScale = builder.comment("超级霰弹枪肉钩 HUD 缩放 / Super Shotgun meat hook HUD scale")
                .defineInRange("super_shotgun_hud_scale", 1.0D, 0.5D, 2.0D);

        skullcrusherHudEnabled = defineHudEnabled(builder, "skullcrusher", "骷髅粉碎者 / Skullcrusher");
        skullcrusherHudOffsetX = defineHudOffsetX(builder, "skullcrusher");
        skullcrusherHudOffsetY = builder.comment("skullcrusher HUD Y轴偏移 / HUD Y offset")
                .defineInRange("skullcrusher_offset_y", 10.0D, -2000.0D, 2000.0D);
        skullcrusherHudCenterYAdjust = builder.comment("骷髅粉碎者HUD中心Y微调 / Skullcrusher HUD center Y fine adjustment")
                .defineInRange("skullcrusher_center_y_adjust", -13.0D, -200.0D, 200.0D);
        skullcrusherHudLeftArcXAdjust = builder.comment("骷髅粉碎者HUD左弧X微调 / Skullcrusher HUD left arc X fine adjustment")
                .defineInRange("skullcrusher_left_arc_x_adjust", 0, -200, 200);
        skullcrusherHudRightArcXAdjust = builder.comment("骷髅粉碎者HUD右弧X微调 / Skullcrusher HUD right arc X fine adjustment")
                .defineInRange("skullcrusher_right_arc_x_adjust", 0, -200, 200);
        skullcrusherHudArcScale = builder.comment("骷髅粉碎者HUD弧线缩放 / Skullcrusher HUD arc scale")
                .defineInRange("skullcrusher_arc_scale", 0.5D, 0.1D, 2.0D);
        skullcrusherHudBaseGap = builder.comment("骷髅粉碎者HUD准星到弧线基础距离 / Skullcrusher HUD base gap from crosshair to arc")
                .defineInRange("skullcrusher_base_gap", 4, -100, 200);
        skullcrusherHudSpreadGapMultiplier = builder
                .comment("骷髅粉碎者HUD扩散距离倍率 / Skullcrusher HUD spread gap multiplier")
                .defineInRange("skullcrusher_spread_gap_multiplier", 0.9D, 0.0D, 10.0D);
        skullcrusherIdleSoundEnabled = builder.comment("是否启用骷髅粉碎者手持待机音效 / Enable Skullcrusher held idle sound")
                .define("skullcrusher_idle_sound_enabled", true);

        obsidianLauncherHudEnabled = defineHudEnabled(builder, "obsidian_launcher", "黑曜石发射器 / Obsidian Launcher");
        obsidianLauncherHudOffsetX = defineHudOffsetX(builder, "obsidian_launcher");
        obsidianLauncherHudOffsetY = defineHudOffsetY(builder, "obsidian_launcher", 10);

        hellforgeChainHudEnabled = defineHudEnabled(builder, "hellforge_chain", "Hellforge Coin Chain");
        hellforgeChainHudOffsetX = defineHudOffsetX(builder, "hellforge_chain");
        hellforgeChainHudOffsetY = defineHudOffsetY(builder, "hellforge_chain", 30);
        hellforgeChainHudScale = builder.comment("Hellforge coin chain HUD scale percentage")
                .defineInRange("hellforge_chain_scale", 100, 50, 200);
        hellforgeCoinHitShockEnabled = builder.comment("Enable Hellforge coin-hit SHOCK camera kick")
                .define("hellforge_coin_hit_shock_enabled", true);
        hellforgeCoinHitShockStrength = builder.comment("Hellforge coin-hit SHOCK strength percentage")
                .defineInRange("hellforge_coin_hit_shock_strength", 100, 0, 300);
        hellforgeCoinHitFovPunchEnabled = builder.comment("Enable Hellforge coin-hit FOV punch")
                .define("hellforge_coin_hit_fov_punch_enabled", true);
        hellforgeCoinHitHudFlashEnabled = builder.comment("Enable Hellforge coin-hit HUD flash")
                .define("hellforge_coin_hit_hud_flash_enabled", true);
        hellforgeOverheatVoiceEnabled = builder.comment("Enable Hellforge overheat voice callout")
                .define("hellforge_overheat_voice_enabled", true);
        hellforgeOverheatMusicEnabled = builder.comment("Enable Hellforge overheat heating music")
                .define("hellforge_overheat_music_enabled", true);

        builder.pop();
    }

    private static ForgeConfigSpec.BooleanValue defineHudEnabled(ForgeConfigSpec.Builder builder, String key, String label) {
        return builder.comment("是否启用" + label + " HUD / Enable " + label + " HUD")
                .define(key + "_enabled", true);
    }

    private static ForgeConfigSpec.DoubleValue defineHudOffsetX(ForgeConfigSpec.Builder builder, String key) {
        return builder.comment(key + " HUD X轴偏移 / HUD X offset")
                .defineInRange(key + "_offset_x", 0.0D, -2000.0D, 2000.0D);
    }

    private static ForgeConfigSpec.DoubleValue defineHudOffsetY(ForgeConfigSpec.Builder builder, String key, double defaultValue) {
        return builder.comment(key + " HUD Y轴偏移 / HUD Y offset")
                .defineInRange(key + "_offset_y", defaultValue, -2000.0D, 2000.0D);
    }

    /**
     * 注册客户端配置
     */
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "gwrexpansions-client.toml");
    }

    public static void save() {
        CLIENT_SPEC.save();
    }

    public static boolean getBoolean(ForgeConfigSpec.BooleanValue value, boolean fallback) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return fallback;
        }
    }

    public static int getInt(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return fallback;
        }
    }

    public static double getDouble(ForgeConfigSpec.DoubleValue value, double fallback) {
        try {
            return value.get();
        } catch (IllegalStateException e) {
            return fallback;
        }
    }

    /**
     * 获取硬币计数器位置
     */
    public static Position getCoinCounterPosition(int screenWidth, int screenHeight) {
        CoinCounterPosition position;
        int scale;

        try {
            position = INSTANCE.coinCounterPosition.get();
            scale = INSTANCE.coinCounterScale.get();
        } catch (IllegalStateException e) {
            // 配置尚未加载，使用默认值
            position = CoinCounterPosition.TOP_CENTER;
            scale = 100;
        }

        // 计算缩放后的硬币大小
        int iconSize = (12 * scale) / 100;
        int spacing = (16 * scale) / 100;
        int maxCoins = 4;
        int totalWidth = (maxCoins - 1) * spacing + iconSize;

        double x, y;

        switch (position) {
            case TOP_LEFT:
                x = 10;
                y = 10;
                break;
            case TOP_RIGHT:
                x = screenWidth - totalWidth - 10;
                y = 10;
                break;
            case TOP_CENTER:
                x = (screenWidth - totalWidth) / 2;
                y = 10;
                break;
            case BOTTOM_LEFT:
                x = 10;
                y = screenHeight - iconSize - 10;
                break;
            case BOTTOM_RIGHT:
                x = screenWidth - totalWidth - 10;
                y = screenHeight - iconSize - 10;
                break;
            case BOTTOM_CENTER:
                x = (screenWidth - totalWidth) / 2;
                y = screenHeight - iconSize - 10;
                break;
            case CUSTOM:
                try {
                    x = INSTANCE.coinCounterOffsetX.get();
                    y = INSTANCE.coinCounterOffsetY.get();
                } catch (IllegalStateException e) {
                    // 配置尚未加载，使用默认值
                    x = 0.0D;
                    y = 8.0D;
                }
                break;
            default:
                // 默认为TOP_CENTER
                x = (screenWidth - totalWidth) / 2;
                y = 10;
                break;
        }

        return new Position(x, y);
    }

    /**
     * 位置类
     */
    public static class Position {
        public final double x;
        public final double y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
