package juitar.gwrexpansions.advancement.BOMD;


import juitar.gwrexpansions.advancement.*;
import juitar.gwrexpansions.registry.CompatBOMD;
import juitar.gwrexpansions.CompatModids;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

/**
 * BOMD模组成就事件处理器
 * 处理BOMD模组相关的成就触发
 */
@Mod.EventBusSubscriber
public class BOMDAchievementEventHandler {

    /**
     * 处理物品获得事件，触发相关成就
     */
    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        if (!ModList.get().isLoaded(CompatModids.BOMD)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack pickedUpItem = event.getStack();
        if (pickedUpItem.isEmpty()) {
            return;
        }

        // 检查获得的物品并触发相应成就
        if (pickedUpItem.is(CompatBOMD.obsidian_launcher.get())) {
            ObsidianWandTrigger.onObsidianWandObtained(player);
        } else if (pickedUpItem.is(CompatBOMD.skullcrusher_pulverizer.get())) {
            BadToTheBoneTrigger.onSkullCrusherObtained(player);
        } else if (pickedUpItem.is(CompatBOMD.hellforge.get())) {
            MankindIsDeadTrigger.onHellforgeRevolverObtained(player);
        } else if (pickedUpItem.is(CompatBOMD.voidspike.get())) {
            BrustVoidTrigger.onVoidPiercerObtained(player);
        }
    }

    /**
     * 处理物品栏变化事件，确保在物品栏变化时也能触发成就
     */
    @SubscribeEvent
    public static void onInventoryChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!ModList.get().isLoaded(CompatModids.BOMD)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 检查玩家物品栏中的BOMD物品
        checkPlayerInventoryForBOMDItems(player);
    }

    /**
     * 处理玩家登录事件，检查已有物品
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModList.get().isLoaded(CompatModids.BOMD)) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 检查玩家物品栏中的BOMD物品
        checkPlayerInventoryForBOMDItems(player);
    }

    /**
     * 检查玩家物品栏中的BOMD物品并触发相应成就
     */
    private static void checkPlayerInventoryForBOMDItems(ServerPlayer player) {
        // 检查主手和副手
        checkItemStackForAchievement(player, player.getMainHandItem());
        checkItemStackForAchievement(player, player.getOffhandItem());

        // 检查物品栏
        for (ItemStack stack : player.getInventory().items) {
            checkItemStackForAchievement(player, stack);
        }
    }

    /**
     * 检查单个物品栈并触发相应成就
     */
    private static void checkItemStackForAchievement(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (stack.is(CompatBOMD.obsidian_launcher.get())) {
            ObsidianWandTrigger.onObsidianWandObtained(player);
        } else if (stack.is(CompatBOMD.skullcrusher_pulverizer.get())) {
            BadToTheBoneTrigger.onSkullCrusherObtained(player);
        } else if (stack.is(CompatBOMD.hellforge.get())) {
            MankindIsDeadTrigger.onHellforgeRevolverObtained(player);
        } else if (stack.is(CompatBOMD.voidspike.get())) {
            BrustVoidTrigger.onVoidPiercerObtained(player);
        }
    }
} 