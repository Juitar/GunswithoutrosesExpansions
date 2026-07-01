package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class GunThirdPersonPoseClient {
    private static final HumanoidModel.ArmPose GUN_HOLD = HumanoidModel.ArmPose.create(
            "GWRE_GUN_HOLD",
            false,
            GunThirdPersonPoseClient::applyGunHold);

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        HumanoidModel<?> model = event.getRenderer().getModel();

        if (isGun(player.getMainHandItem())) {
            setArmPose(model, player.getMainArm(), GUN_HOLD);
        }

        if (isGun(player.getOffhandItem())) {
            setArmPose(model, player.getMainArm().getOpposite(), GUN_HOLD);
        }
    }

    private static boolean isGun(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof GunItem;
    }

    private static void setArmPose(HumanoidModel<?> model, HumanoidArm arm, HumanoidModel.ArmPose pose) {
        if (arm == HumanoidArm.RIGHT) {
            model.rightArmPose = pose;
        } else {
            model.leftArmPose = pose;
        }
    }

    private static void applyGunHold(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart modelArm = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        float side = arm == HumanoidArm.RIGHT ? -1.0F : 1.0F;

        modelArm.xRot = model.head.xRot - 1.5707964F;
        modelArm.yRot = model.head.yRot + side * 0.12F;
        modelArm.zRot = side * 0.05F;
    }
}
