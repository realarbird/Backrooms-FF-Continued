package com.sp.entity.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * PORT: delta (1.20.1 -> 26.2). This is a vanilla model (not GeckoLib). 26.2 removed
 * {@code SinglePartEntityModel}; models now extend {@code EntityModel<S extends EntityRenderState>} and
 * pose against a render-state ({@code setupAnim(S)}) instead of {@code setAngles(entity, ...)}. The
 * single billboard "bone" part and its eyes render layer ({@code RenderLayer::getEyes} ->
 * {@code RenderTypes::eyes}) are preserved; rendering goes through the inherited
 * {@code Model.renderToBuffer(...)} (the explicit {@code render(...)} override is no longer needed).
 * The mesh build moved from {@code ModelData}/{@code ModelPartBuilder}/{@code Dilation} to
 * {@code MeshDefinition}/{@code CubeListBuilder}/{@code CubeDeformation}.
 */
public class SmilerModel extends EntityModel<LivingEntityRenderState> {
	public SmilerModel(ModelPart root) {
		super(root.getChild("bone"), RenderTypes::eyes);
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot();
		root.addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(1, 0).addBox(-22.0F, -22.0F, 0.0F, 44.0F, 44.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 2.0F, 0.0F));
		return LayerDefinition.create(meshDefinition, 96, 96);
	}

	@Override
	public void setupAnim(LivingEntityRenderState state) {
	}
}
