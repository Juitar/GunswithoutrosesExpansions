package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public final class MeatHookMarkClient {
    private static final Map<Integer, Long> MARKED_ENTITIES = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean> ORIGINAL_GLOWING_STATE = new HashMap<>();

    private MeatHookMarkClient() {
    }

    public static void markEntity(int entityId, int durationTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        MARKED_ENTITIES.put(entityId, mc.level.getGameTime() + Math.max(1, durationTicks));
    }

    public static void clearEntity(int entityId) {
        MARKED_ENTITIES.remove(entityId);
        restoreOriginalGlow(entityId);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            clearAllTrackedGlow();
            MARKED_ENTITIES.clear();
            return;
        }

        long gameTime = mc.level.getGameTime();
        Iterator<Map.Entry<Integer, Long>> iterator = MARKED_ENTITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            Entity entity = mc.level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity living) || !living.isAlive() || entry.getValue() < gameTime) {
                iterator.remove();
                restoreOriginalGlow(entry.getKey());
                continue;
            }

            ORIGINAL_GLOWING_STATE.putIfAbsent(entry.getKey(), living.isCurrentlyGlowing());
            living.setGlowingTag(true);
        }

        Iterator<Integer> glowingIterator = ORIGINAL_GLOWING_STATE.keySet().iterator();
        while (glowingIterator.hasNext()) {
            int entityId = glowingIterator.next();
            if (MARKED_ENTITIES.containsKey(entityId)) {
                continue;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity != null) {
                entity.setGlowingTag(ORIGINAL_GLOWING_STATE.get(entityId));
            }
            glowingIterator.remove();
        }
    }

    private static void restoreOriginalGlow(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        Boolean original = ORIGINAL_GLOWING_STATE.remove(entityId);
        if (mc.level == null || original == null) {
            return;
        }

        Entity entity = mc.level.getEntity(entityId);
        if (entity != null) {
            entity.setGlowingTag(original);
        }
    }

    private static void clearAllTrackedGlow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (Map.Entry<Integer, Boolean> entry : ORIGINAL_GLOWING_STATE.entrySet()) {
                Entity entity = mc.level.getEntity(entry.getKey());
                if (entity != null) {
                    entity.setGlowingTag(entry.getValue());
                }
            }
        }
        ORIGINAL_GLOWING_STATE.clear();
    }
}
