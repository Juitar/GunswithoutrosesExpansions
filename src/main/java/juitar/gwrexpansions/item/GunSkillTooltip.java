package juitar.gwrexpansions.item;

import juitar.gwrexpansions.client.GunSkillKeyHandler;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class GunSkillTooltip {
    private GunSkillTooltip() {
    }

    public static Component keyName() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return ClientOnly.keyName();
        }
        return Component.translatable("key.gwrexpansions.gun_skill");
    }

    private static final class ClientOnly {
        private static Component keyName() {
            return GunSkillKeyHandler.GUN_SKILL.getTranslatedKeyMessage();
        }
    }
}
