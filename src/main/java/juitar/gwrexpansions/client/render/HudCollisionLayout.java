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

    static Bounds claim(RenderGuiEvent event, double preferredX, double preferredY, int width, int height,
                        int screenWidth, int screenHeight) {
        if (currentEvent != event) {
            currentEvent = event;
            USED.clear();
        }

        int floorX = (int)Math.floor(preferredX);
        int floorY = (int)Math.floor(preferredY);
        double fracX = preferredX - floorX;
        double fracY = preferredY - floorY;
        int clampedX = clamp(floorX, 0, Math.max(0, screenWidth - width));
        int clampedY = clamp(floorY, 0, Math.max(0, screenHeight - height));
        Bounds preferred = new Bounds(clampedX, clampedY, width, height, fracX, fracY);
        if (!intersectsAny(preferred)) {
            USED.add(preferred);
            return preferred;
        }

        int maxAttempts = Math.max(12, screenHeight / STEP);
        for (int i = 1; i <= maxAttempts; i++) {
            Bounds down = new Bounds(clampedX, clamp(clampedY + i * STEP, 0, Math.max(0, screenHeight - height)),
                    width, height, fracX, fracY);
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
        final double fracX;
        final double fracY;

        private Bounds(int x, int y, int width, int height, double fracX, double fracY) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.fracX = fracX;
            this.fracY = fracY;
        }

        private boolean intersects(Bounds other, int padding) {
            return this.x < other.x + other.width + padding
                    && this.x + this.width + padding > other.x
                    && this.y < other.y + other.height + padding
                    && this.y + this.height + padding > other.y;
        }
    }
}
