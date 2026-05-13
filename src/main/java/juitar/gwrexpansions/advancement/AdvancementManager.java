package juitar.gwrexpansions.advancement;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.registry.VanillaItem;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * 成就管理器
 * 处理模组检测和条件成就
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class AdvancementManager {
    
    private static final Map<String, Boolean> modLoadedCache = new HashMap<>();
    private static final ResourceLocation RIP_AND_TEAR = new ResourceLocation(GWRexpansions.MODID, "rip_and_tear");
    
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
        
        // 检查Meet Your Fight模组成就（如果模组加载）
        if (isModLoaded(CompatModids.MEETYOURFIGHT)) {
            if (!hasCompletedAdvancement(player, "gwrexpansions:mirecaller_shotgun")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:mirecaller_mine_burst")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:duskfall_eclipse_blaster")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:dusk_rose_stand_attack")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:destiny_seven")) return false;
            if (!hasCompletedAdvancement(player, "gwrexpansions:destiny_all_in")) return false;
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

    public static void grantAllAchievementsReward(ServerPlayer player) {
        if (!GWREConfig.GENERAL.enableAllAchievementsSuperShotgunReward.get()) {
            return;
        }

        ItemStack stack = new ItemStack(VanillaItem.super_shotgun.get());
        stack.setHoverName(Component.literal("SUPER SHOTGUN").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        CompoundTag display = stack.getOrCreateTagElement("display");
        ListTag lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.literal("Reward Weapon").withStyle(ChatFormatting.YELLOW).withStyle(style -> style.withItalic(false)))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(
                Component.literal("Proof of completing every achievement").withStyle(ChatFormatting.GRAY).withStyle(style -> style.withItalic(false)))));
        display.put("Lore", lore);

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
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
                GWRECriteria.ALL_ACHIEVEMENTS_COMPLETED.trigger(player);
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancementEarned(net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && RIP_AND_TEAR.equals(event.getAdvancement().getId())) {
            grantAllAchievementsReward(player);
        }
    }
} 
 
