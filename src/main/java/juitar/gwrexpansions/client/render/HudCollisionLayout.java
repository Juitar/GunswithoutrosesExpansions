package juitar.gwrexpansions.client.render;

import net.minecraftforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.List;

final class HudCollisionLayout {
    private static final int PADDING = 3;
    private static final int STEP = 8;
    private static final List<Bounds> USED = new ArrayList<>();
    private static RenderGuiEvent currentEvent;

    private HudCollisionLayout() {
    }

    static Bounds claim(RenderGuiEvent event, int preferredX, int preferredY, int width, int height,
                        int screenWidth, int screenHeight) {
        if (currentEvent != event) {
            currentEvent = event;
            USED.clear();
        }

        int clampedX = clamp(preferredX, 0, Math.max(0, screenWidth - width));
        int clampedY = clamp(preferredY, 0, Math.max(0, screenHeight - height));
        Bounds preferred = new Bounds(clampedX, clampedY, width, height);
        if (!intersectsAny(preferred)) {
            USED.add(preferred);
            return preferred;
        }

        int maxAttempts = Math.max(12, screenHeight / STEP);
        for (int i = 1; i <= maxAttempts; i++) {
            Bounds down = new Bounds(clampedX, clamp(clampedY + i * STEP, 0, Math.max(0, screenHeight - height)),
                    width, height);
            if (!intersectsAny(down)) {
                USED.add(down);
                return down;
            }
        }

        USED.add(preferred);
        return preferred;
    }

    private static boolean intersectsAny(Bounds bounds) {
        for (Bounds used : USED) {
            if (bounds.intersects(used, PADDING)) {
                return true;
            }
        }
        return false;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static final class Bounds {
        final int x;
        final int y;
        final int width;
        final int height;

        private Bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private boolean intersects(Bounds other, int padding) {
            return this.x < other.x + other.width + padding
                    && this.x + this.width + padding > other.x
                    && this.y < other.y + other.height + padding
                    && this.y + this.height + padding > other.y;
        }
    }
}
