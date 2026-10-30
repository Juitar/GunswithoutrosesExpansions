package juitar.gwrexpansions.client;

import juitar.gwrexpansions.item.cataclysm.IgnitiumGatlingItem;
import juitar.gwrexpansions.network.IgnitiumStatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public final class IgnitiumClientState {
    private record Key(int playerId, long stackId) {}
    private record Pending(IgnitiumStatePacket packet, long expiresAt) {}
    private static final Map<Key, Pending> PENDING = new HashMap<>();
    private static ClientLevel packetLevel;
    private IgnitiumClientState() {}

    public static void apply(IgnitiumStatePacket msg) {
        var level = Minecraft.getInstance().level;
        if (level == null || msg.state() == null) return;
        useLevel(level);
        Key key = new Key(msg.playerId(), msg.stackId());
        if (tryApply(level, msg)) PENDING.remove(key);
        else PENDING.put(key, new Pending(msg, level.getGameTime() + 40));
    }

    private static boolean tryApply(ClientLevel level, IgnitiumStatePacket msg) {
        if (!(level.getEntity(msg.playerId()) instanceof Player player)) return false;
        // A hand is only a location hint: the gun may have moved since this packet was
        // sent. Never initialize an unknown ID from a state packet meant for another gun.
        if (IgnitiumGatlingItem.applySnapshotIfMatches(player.getItemInHand(msg.hand()), msg.stackId(), msg.state())) return true;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (IgnitiumGatlingItem.applySnapshotIfMatches(stack, msg.stackId(), msg.state())) return true;
        }
        return false;
    }

    private static void useLevel(ClientLevel level) {
        if (packetLevel != level) {
            PENDING.clear();
            packetLevel = level;
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        var level = Minecraft.getInstance().level;
        useLevel(level);
        if (level != null) PENDING.values().removeIf(pending ->
                level.getGameTime() >= pending.expiresAt() || tryApply(level, pending.packet()));
    }
}
