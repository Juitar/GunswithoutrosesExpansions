package juitar.gwrexpansions.advancement;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * 成就管理器
 * 处理模组检测和条件成就
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class AdvancementManager {
    
    private static final Map<String, Boolean> modLoadedCache = new HashMap<>();
    
    /**
     * 检查模组是否加载
     */
    public static boolean isModLoaded(String modId) {
        return modLoadedCache.computeIfAbsent(modId, ModList.get()::isLoaded);
    }
    
    /**
     * 检查玩家是否完成所有成就
     */
    public static boolean hasCompletedAllAchievements(ServerPlayer player) {
        // 检查原版成就
        if (!hasCompletedAdvancement(player, "gwrexpansions:bounce_master")) return false;
        if (!hasCompletedAdvancement(player, "gwrexpansions:gold_digger")) return false;
        if (!hasCompletedAdvancement(player, "gwrexpansions:unlimited_bullet_works")) return false;
        if (!hasCompletedAdvancement(player, "gwrexpansions:stakeholders_shrapnel")) return false;
        if (!hasCompletedAdvancement(player, "gwrexpansions:netherite_arsenal_happy_meal")) return false;
        if (!hasCompletedAdvancement(player, "gwrexpansions:dont_starve")) return false;
        
        // 检查Cataclysm模组成就（如果模组加载）
        if (isModLoaded(CompatModids.CATACLYSM)) {
            if (!hasCompletedAdvancement(player, "gwrexpansions:bfg_600c")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:ghost_sniper")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:die_insect")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:leviathans_phallus")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:steves_lava_chicken")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:cenobites_lament_round")) return false;
        }
        
        // 检查ICE AND FIRE模组成就（如果模组加载）
        if (isModLoaded(CompatModids.ICEANDFIRE)) {
            if (!hasCompletedAdvancement(player, "gwrexpansions:dragons_bane")) return false;
        }
        
        // 检查BOMD模组成就（如果模组加载）
        if (isModLoaded(CompatModids.BOMD)) {
            if (!hasCompletedAdvancement(player, "gwrexpansions:obsidian_wand")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:avada_kedavra")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:obsidian_cake")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:bad_to_the_bone")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:mankind_is_dead")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:blood_is_fuel")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:hell_is_full")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:brust_void")) return false;
        }
        
        return true;
    }
    
    /**
     * 检查玩家是否完成特定成就
     */
    private static boolean hasCompletedAdvancement(ServerPlayer player, String advancementId) {
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(new ResourceLocation(advancementId));
        if (advancement == null) return false;
        
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        return progress.isDone();
    }
    
    /**
     * 玩家登录时检查成就完成情况
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 检查是否完成所有成就
            if (hasCompletedAllAchievements(player)) {
                // 触发RIP AND TEAR成就
                // 这里需要实现一个特殊的触发器来检查所有成就完成情况
                GWRexpansions.LOG.info("Player {} has completed all achievements!", player.getName().getString());
            }
        }
    }
} 
 