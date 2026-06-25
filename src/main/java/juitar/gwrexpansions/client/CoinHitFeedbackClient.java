package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class CoinHitFeedbackClient {
    private static final int ACTION_FEED_SIZE = 2;
    private static final int ACTION_LIFETIME_TICKS = 62;
    private static final int ACTION_PENDING_TICKS = 20;
    private static int feedbackTicks;
    private static int currentHits;
    private static int chainTimer;
    private static int styleScore;
    private static int styleHeat;
    private static int heatKeepTimer;
    private static int flashTicks;
    private static int clearSuppressTicks;
    private static int overheatTimer;
    private static int fovPunchTicks;
    private static int fovPunchDuration;
    private static float fovPunchStrength;
    private static float recoilDirection;
    private static final ActionFeedEntry[] actionFeed = new ActionFeedEntry[ACTION_FEED_SIZE];
    private static ActionFeedEntry pendingAction;
    private static int pendingActionTicks;

    public static void onCoinHit(int hits, int timer, int newOverheatTimer) {
        onCoinHit(hits, timer, newOverheatTimer, hits, 0, timer, "", 0);
    }

    public static void onCoinHit(int hits, int timer, int newOverheatTimer, int newStyleScore, int newHeat,
                                 String eventType, int eventValue) {
        onCoinHit(hits, timer, newOverheatTimer, newStyleScore, newHeat, timer, eventType, eventValue);
    }

    public static void onCoinHit(int hits, int timer, int newOverheatTimer, int newStyleScore, int newHeat,
                                 int newHeatKeepTimer, String eventType, int eventValue) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        overheatTimer = Math.max(overheatTimer, newOverheatTimer);
        styleScore = Math.max(0, newStyleScore);
        styleHeat = Math.max(0, newHeat);
        heatKeepTimer = Math.max(heatKeepTimer, newHeatKeepTimer);
        addAction(eventType, eventValue, overheatTimer > 0);

        if (hits <= 0 || timer <= 0) {
            currentHits = 0;
            chainTimer = 0;
            feedbackTicks = 0;
            flashTicks = 0;
            fovPunchTicks = 0;
            clearSuppressTicks = 20;
            return;
        }

        clearSuppressTicks = 0;
        currentHits = hits;
        chainTimer = timer;
        flashTicks = ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitHudFlashEnabled, true) ? Math.min(10, 4 + hits) : 0;

        if (ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitShockEnabled, true)) {
            feedbackTicks = Math.min(9, 4 + Math.max(0, hits / 2));
            recoilDirection = player.getRandom().nextBoolean() ? 1.0F : -1.0F;
        } else {
            feedbackTicks = 0;
        }

        if (ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitFovPunchEnabled, true)) {
            float scale = getShockScale();
            fovPunchDuration = Math.min(9, 5 + hits / 2);
            fovPunchTicks = fovPunchDuration;
            fovPunchStrength = Math.min(0.07F, (0.018F + hits * 0.0045F) * scale);
        } else {
            fovPunchDuration = 0;
            fovPunchTicks = 0;
            fovPunchStrength = 0.0F;
        }

        float pitch = Math.min(1.8F, 0.95F + hits * 0.12F);
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
            GWRESounds.HELLFORGE_REVOLVER_COIN_HIT.get(), SoundSource.PLAYERS, 1.0F, pitch, false);
    }

    private static void addAction(String eventType, int eventValue, boolean showNow) {
        String text = formatAction(eventType, eventValue);
        if (text.isEmpty()) {
            return;
        }

        ActionFeedEntry entry = new ActionFeedEntry(text, getActionColor(eventType), ACTION_LIFETIME_TICKS);
        if (!showNow) {
            pendingAction = entry;
            pendingActionTicks = ACTION_PENDING_TICKS;
            return;
        }

        pushAction(entry);
    }

    private static void pushAction(ActionFeedEntry entry) {
        actionFeed[1] = actionFeed[0] == null ? null : actionFeed[0].asScrolled();
        actionFeed[0] = entry;
    }

    private static String formatAction(String eventType, int eventValue) {
        if (eventType == null || eventType.isEmpty()) {
            return "";
        }
        return switch (eventType) {
            case "COIN_HIT" -> "COIN HIT";
            case "COIN_CHAIN" -> "COIN CHAIN x" + Math.max(2, eventValue);
            case "HEADSHOT" -> "HEADSHOT";
            case "HEADSHOT_KILL" -> "HEADSHOT KILL";
            case "ONE_SHOT" -> "ONE SHOT";
            case "RICOSHOT_KILL" -> "RICOSHOT KILL";
            case "DOUBLE_KILL" -> "DOUBLE KILL";
            case "TRIPLE_KILL" -> "TRIPLE KILL";
            case "OVERHEAT" -> "OVERHEAT";
            case "MAX_OVERHEAT" -> "MAX OVERHEAT";
            default -> "";
        };
    }

    private static int getActionColor(String eventType) {
        if (eventType == null) {
            return 0xFFFFFFFF;
        }
        return switch (eventType) {
            case "COIN_HIT", "COIN_CHAIN" -> 0xFFFFD66B;
            case "HEADSHOT", "HEADSHOT_KILL", "ONE_SHOT" -> 0xFFFFF2E8;
            case "RICOSHOT_KILL" -> 0xFF6BE4FF;
            case "DOUBLE_KILL", "TRIPLE_KILL" -> 0xFFFF7A35;
            case "OVERHEAT", "MAX_OVERHEAT" -> 0xFFFF4A2E;
            default -> 0xFFFFFFFF;
        };
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        if (feedbackTicks > 0 && ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitShockEnabled, true)) {
            float progress = feedbackTicks / (float)Math.max(1, Math.min(9, 4 + Math.max(0, currentHits / 2)));
            float strength = Math.min(2.2F, 0.34F + currentHits * 0.12F) * progress * progress * getShockScale();
            float yaw = recoilDirection * strength * 0.55F + (player.getRandom().nextFloat() - 0.5F) * strength * 0.35F;
            float pitch = -strength * 0.22F + (player.getRandom().nextFloat() - 0.5F) * strength * 0.18F;
            player.turn(yaw, pitch);
            feedbackTicks--;
        } else if (feedbackTicks > 0) {
            feedbackTicks = 0;
        }

        if (chainTimer > 0) {
            chainTimer--;
        }
        if (flashTicks > 0) {
            flashTicks--;
        }
        if (fovPunchTicks > 0) {
            fovPunchTicks--;
        }
        if (clearSuppressTicks > 0) {
            clearSuppressTicks--;
        }
        if (overheatTimer > 0) {
            overheatTimer--;
            if (pendingAction != null && pendingActionTicks > 0) {
                pushAction(pendingAction);
                pendingAction = null;
                pendingActionTicks = 0;
            }
        }
        if (heatKeepTimer > 0) {
            heatKeepTimer--;
        }
        if (pendingActionTicks > 0) {
            pendingActionTicks--;
            if (pendingActionTicks <= 0) {
                pendingAction = null;
            }
        }
        tickActionFeed();
        if (chainTimer <= 0) {
            currentHits = 0;
        }
    }

    private static void tickActionFeed() {
        for (int i = 0; i < actionFeed.length; i++) {
            ActionFeedEntry entry = actionFeed[i];
            if (entry == null) {
                continue;
            }
            entry.tick();
            if (entry.age <= 0) {
                actionFeed[i] = null;
            }
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (fovPunchTicks <= 0 || fovPunchDuration <= 0
                || !ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitFovPunchEnabled, true)) {
            return;
        }
        float progress = fovPunchTicks / (float)fovPunchDuration;
        float punch = fovPunchStrength * progress * progress;
        event.setFOV(event.getFOV() * (1.0D + punch));
    }

    private static float getShockScale() {
        return Math.max(0.0F, ClientConfig.getInt(ClientConfig.INSTANCE.hellforgeCoinHitShockStrength, 100) / 100.0F);
    }

    public static int getCurrentHits() {
        return currentHits;
    }

    public static int getChainTimer() {
        return chainTimer;
    }

    public static int getStyleScore() {
        return styleScore;
    }

    public static int getStyleHeat() {
        return styleHeat;
    }

    public static int getHeatKeepTimer() {
        return heatKeepTimer;
    }

    public static int getFlashTicks() {
        return flashTicks;
    }

    public static int getOverheatTimer() {
        return overheatTimer;
    }

    public static boolean isClearSuppressed() {
        return clearSuppressTicks > 0;
    }

    public static ActionFeedEntry getActionFeedEntry(int index) {
        return index >= 0 && index < actionFeed.length ? actionFeed[index] : null;
    }

    public static class ActionFeedEntry {
        private final String text;
        private final int color;
        private int age;
        private int popTicks;
        private int scrollTicks;

        private ActionFeedEntry(String text, int color, int age) {
            this.text = text;
            this.color = color;
            this.age = age;
            this.popTicks = 12;
            this.scrollTicks = 0;
        }

        private ActionFeedEntry asScrolled() {
            this.scrollTicks = 8;
            this.popTicks = 0;
            return this;
        }

        private void tick() {
            age--;
            if (popTicks > 0) {
                popTicks--;
            }
            if (scrollTicks > 0) {
                scrollTicks--;
            }
        }

        public String getText() {
            return text;
        }

        public int getColor() {
            int alpha = Math.max(0, Math.min(255, age < 12 ? age * 21 : 255));
            return (alpha << 24) | (color & 0x00FFFFFF);
        }

        public float getScale() {
            if (popTicks <= 0) {
                return 1.0F;
            }
            float progress = popTicks / 12.0F;
            return 1.0F + (float)Math.sin(progress * Math.PI) * 0.28F;
        }

        public int getSlideX() {
            if (popTicks > 0) {
                return Math.round((popTicks / 12.0F) * 22.0F);
            }
            return 0;
        }

        public int getScrollY() {
            if (scrollTicks <= 0) {
                return 0;
            }
            return Math.round((scrollTicks / 8.0F) * -14.0F);
        }

        public int getShakeX(int index) {
            long time = System.currentTimeMillis();
            return Math.round((float)Math.sin((time + index * 71L) / 34.0D) * 1.6F);
        }

        public int getShakeY(int index) {
            long time = System.currentTimeMillis();
            return Math.round((float)Math.sin((time + index * 43L) / 57.0D) * 1.1F);
        }

        public int getFlashAlpha() {
            if (popTicks <= 0) {
                return 0;
            }
            return Math.min(155, 40 + popTicks * 9);
        }
    }
}
