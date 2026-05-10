package juitar.gwrexpansions.advancement.MYF;

import juitar.gwrexpansions.event.MYFCombatEventHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.common.MinecraftForge;

public final class MYFCriteria {
    public static final MirecallerMineBurstTrigger MIRECALLER_MINE_BURST = CriteriaTriggers.register(new MirecallerMineBurstTrigger());
    public static final DuskRoseStandAttackTrigger DUSK_ROSE_STAND_ATTACK = CriteriaTriggers.register(new DuskRoseStandAttackTrigger());
    public static final DestinyAllInTrigger DESTINY_ALL_IN = CriteriaTriggers.register(new DestinyAllInTrigger());
    private static boolean eventsRegistered;

    private MYFCriteria() {
    }

    public static void register() {
        MIRECALLER_MINE_BURST.getId();
        DUSK_ROSE_STAND_ATTACK.getId();
        DESTINY_ALL_IN.getId();

        if (!eventsRegistered) {
            MinecraftForge.EVENT_BUS.register(MirecallerMineBurstTrigger.class);
            MinecraftForge.EVENT_BUS.register(DuskRoseStandAttackTrigger.class);
            MinecraftForge.EVENT_BUS.register(DestinyAllInTrigger.class);
            MinecraftForge.EVENT_BUS.register(MYFCombatEventHandler.class);
            eventsRegistered = true;
        }
    }
}
