package juitar.gwrexpansions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * 狱锻之轮硬币计数器UI覆盖层
 */
@Mod.EventBusSubscriber(modid = "gwrexpansions", value = Dist.CLIENT)
public class CoinCounterOverlay {
    
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!ModList.get().isLoaded(CompatModids.BOMD)) {
            return;
        }

        // 检查配置是否启用硬币计数器
        boolean enabled;
        try {
            enabled = ClientConfig.INSTANCE.coinCounterEnabled.get();
        } catch (IllegalStateException e) {
            // 配置尚未加载，使用默认值（启用）
            enabled = true;
        }

        if (!enabled) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.options.hideGui) {
            return;
        }

        // 检查玩家是否持有狱锻之轮
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack hellforgStack = null;
        if (mainHand.getItem() instanceof Hellforge) {
            hellforgStack = mainHand;
        } else if (offHand.getItem() instanceof Hellforge) {
            hellforgStack = offHand;
        }

        if (hellforgStack == null) {
            return;
        }

        // 获取硬币数据
        CompoundTag tag = hellforgStack.getOrCreateTag();
        int coins = tag.getInt(Hellforge.NBT_COINS);
        int rechargeTimer = tag.getInt(Hellforge.NBT_COIN_RECHARGE_TIMER);
        int maxCoins = Hellforge.getMaxCoins(hellforgStack);

        // 渲染硬币阵列
        renderCoinArray(guiGraphics, screenWidth, screenHeight, coins, maxCoins, rechargeTimer);
    }
    

    
    /**
     * 渲染硬币阵列（显示每个硬币的状态）
     */
    private static void renderCoinArray(GuiGraphics guiGraphics, int screenWidth, int screenHeight,
                                      int coins, int maxCoins, int rechargeTimer) {
        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 从配置获取参数
        int scale;
        int backgroundAlpha;

        try {
            scale = ClientConfig.INSTANCE.coinCounterScale.get();
            backgroundAlpha = ClientConfig.INSTANCE.coinCounterBackgroundAlpha.get();
        } catch (IllegalStateException e) {
            // 配置尚未加载，使用默认值
            scale = 100;
            backgroundAlpha = 0;
        }

        // 计算缩放后的大小（需要在获取位置之前计算）
        int iconSize = (12 * scale) / 100;
        int spacing = (16 * scale) / 100;
        int totalWidth = (maxCoins - 1) * spacing + iconSize;

        // 获取配置的位置（传入已计算的尺寸信息）
        ClientConfig.Position position = getConfiguredPosition(screenWidth, screenHeight, scale, totalWidth, iconSize);
        int startX = (int)Math.floor(position.x);
        int y = (int)Math.floor(position.y);
        double fracX = position.x - startX;
        double fracY = position.y - y;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(fracX, fracY, 0.0F);

        // 渲染背景（如果透明度大于0）
        if (backgroundAlpha > 0) {
            int bgColor = (backgroundAlpha << 24) | 0x000000; // 黑色背景，使用配置的透明度
            int bgPadding = (4 * scale) / 100;
            int bgWidth = totalWidth + bgPadding * 2;
            int bgHeight = iconSize + bgPadding * 2;

            guiGraphics.fill(startX - bgPadding, y - bgPadding,
                           startX + bgWidth - bgPadding, y + bgHeight - bgPadding, bgColor);
        }

        // 渲染4个硬币图标
        float rechargeProgress = Math.min(1.0F, Math.max(0.0F, rechargeTimer / (float) Hellforge.getCoinRechargeTicks()));
        for (int i = 0; i < maxCoins; i++) {
            int iconX = startX + i * spacing;
            int iconY = y;

            if (i < coins) {
                // 有硬币：金色实心圆
                renderCoin(guiGraphics, iconX, iconY, iconSize, 0xFFFFD700, 0xFFFFA500);
            } else if (i == coins && rechargeProgress > 0.0F) {
                renderRechargeClock(guiGraphics, iconX, iconY, iconSize, rechargeProgress);
            } else {
                // 无硬币：灰色边框圆
                renderCoinOutline(guiGraphics, iconX, iconY, iconSize, 0xFF666666);
            }
        }

        guiGraphics.pose().popPose();
        RenderSystem.disableBlend();
    }

    /**
     * 获取配置的位置（避免重复计算）
     */
    private static ClientConfig.Position getConfiguredPosition(int screenWidth, int screenHeight, int scale, int totalWidth, int iconSize) {
        ClientConfig.CoinCounterPosition position;

        try {
            position = ClientConfig.INSTANCE.coinCounterPosition.get();
        } catch (IllegalStateException e) {
            // 配置尚未加载，使用默认值
            position = ClientConfig.CoinCounterPosition.TOP_CENTER;
        }

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
                    x = ClientConfig.INSTANCE.coinCounterOffsetX.get();
                    y = ClientConfig.INSTANCE.coinCounterOffsetY.get();
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

        return new ClientConfig.Position(x, y);
    }

    /**
     * 渲染实心硬币
     */
    private static void renderCoin(GuiGraphics guiGraphics, int x, int y, int size, int outerColor, int innerColor) {
        // 外圈（圆形）
        guiGraphics.fill(x + 2, y + 1, x + size - 2, y + size - 1, outerColor);
        guiGraphics.fill(x + 1, y + 2, x + size - 1, y + size - 2, outerColor);

        // 内圈（稍小一点，营造立体感）
        guiGraphics.fill(x + 3, y + 2, x + size - 3, y + size - 2, innerColor);
        guiGraphics.fill(x + 2, y + 3, x + size - 2, y + size - 3, innerColor);
    }

    /**
     * 渲染硬币边框
     */
    private static void renderCoinOutline(GuiGraphics guiGraphics, int x, int y, int size, int color) {
        // 圆形边框（更细的线条）
        guiGraphics.fill(x + 2, y, x + size - 2, y + 1, color); // 上边
        guiGraphics.fill(x + 2, y + size - 1, x + size - 2, y + size, color); // 下边
        guiGraphics.fill(x, y + 2, x + 1, y + size - 2, color); // 左边
        guiGraphics.fill(x + size - 1, y + 2, x + size, y + size - 2, color); // 右边

        // 圆角像素
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 2, color); // 左上
        guiGraphics.fill(x + size - 2, y + 1, x + size - 1, y + 2, color); // 右上
        guiGraphics.fill(x + 1, y + size - 2, x + 2, y + size - 1, color); // 左下
        guiGraphics.fill(x + size - 2, y + size - 2, x + size - 1, y + size - 1, color); // 右下
    }

    private static void renderRechargeClock(GuiGraphics guiGraphics, int x, int y, int size, float progress) {
        renderCoinOutline(guiGraphics, x, y, size, 0xFF777777);

        int filledRows = Math.max(0, Math.min(size, Math.round(size * progress)));
        int fillTop = y + size - filledRows;
        int outerColor = 0xFFFFD700;
        int innerColor = 0xFFFFA500;

        fillMaskedCoin(guiGraphics, x, y, size, fillTop, outerColor, innerColor);
    }

    private static void fillMaskedCoin(GuiGraphics guiGraphics, int x, int y, int size, int fillTop, int outerColor, int innerColor) {
        fillMaskedRect(guiGraphics, x + 2, y + 1, x + size - 2, y + size - 1, fillTop, outerColor);
        fillMaskedRect(guiGraphics, x + 1, y + 2, x + size - 1, y + size - 2, fillTop, outerColor);
        fillMaskedRect(guiGraphics, x + 3, y + 2, x + size - 3, y + size - 2, fillTop, innerColor);
        fillMaskedRect(guiGraphics, x + 2, y + 3, x + size - 2, y + size - 3, fillTop, innerColor);
    }

    private static void fillMaskedRect(GuiGraphics guiGraphics, int minX, int minY, int maxX, int maxY, int fillTop, int color) {
        int clippedMinY = Math.max(minY, fillTop);
        if (clippedMinY < maxY) {
            guiGraphics.fill(minX, clippedMinY, maxX, maxY, color);
        }
    }

}
