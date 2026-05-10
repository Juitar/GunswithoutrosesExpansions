package juitar.gwrexpansions.event;

import juitar.gwrexpansions.entity.meetyourfight.DuskRoseSpiritEntity;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

final class MYFModEventHandler {
    private MYFModEventHandler() {
    }

    static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        if (GWREEntities.DUSK_ROSE_SPIRIT != null && GWREEntities.DUSK_ROSE_SPIRIT.isPresent()) {
            event.put(GWREEntities.DUSK_ROSE_SPIRIT.get(), DuskRoseSpiritEntity.createDuskAttributes().build());
        }
    }
}
