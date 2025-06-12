package juitar.gwrexpansions.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MeatHookModel extends Model {
    // 模型主体部分
    private final ModelPart root;
    public MeatHookModel() {
        super(RenderType::entityCutoutNoCull);
        // 创建一个简单的肉钩模型
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        // 创建肉钩的主体部分
        PartDefinition hookPart = partdefinition.addOrReplaceChild("hook", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F), // 主体方块
                PartPose.offset(0.0F, 0.0F, 0.0F));
        // 添加钩子部分
        hookPart.addOrReplaceChild("spike1", CubeListBuilder.create()
                .texOffs(0, 4)
                .addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 2.0F), // 前钩
                PartPose.offset(0.0F, 0.0F, -1.0F));
        hookPart.addOrReplaceChild("spike2", CubeListBuilder.create()
                .texOffs(6, 0)
                .addBox(-0.5F, -0.5F, 0.5F, 1.0F, 1.0F, 2.0F), // 后钩
                PartPose.offset(0.0F, 0.0F, 1.0F));
                
        LayerDefinition layerdefinition = LayerDefinition.create(meshdefinition, 16, 16);
        this.root = layerdefinition.bakeRoot();
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // 渲染主体部分
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
} 