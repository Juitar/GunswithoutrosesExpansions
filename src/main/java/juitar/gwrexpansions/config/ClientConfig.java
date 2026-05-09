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
    public final ForgeConfigSpec.IntValue coinCounterOffsetX;
    public final ForgeConfigSpec.IntValue coinCounterOffsetY;
    public final ForgeConfigSpec.IntValue coinCounterBackgroundAlpha;
    public final ForgeConfigSpec.IntValue coinCounterScale;
    public final ForgeConfigSpec.BooleanValue coinCounterShowProgress;
    
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
            .defineInRange("offset_x", 0, -2000, 2000);
        
        coinCounterOffsetY = builder
            .comment("硬币计数器Y轴偏移 (仅在position为custom时生效) / Y offset (only works when position is custom)")
            .defineInRange("offset_y", 8, -2000, 2000);
        
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
        
        int x, y;

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
                    x = 0;
                    y = 8;
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
        public final int x;
        public final int y;
        
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
