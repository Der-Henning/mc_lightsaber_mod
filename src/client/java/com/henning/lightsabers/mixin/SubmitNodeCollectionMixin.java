package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlow;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin {

    @Shadow
    @Final
    private List<SubmitNodeStorage.ItemSubmit> itemSubmits;

    @Inject(method = "submitItem", at = @At("RETURN"))
    private void lightsabers$markLightsaberSubmit(
        PoseStack poseStack,
        ItemDisplayContext displayContext,
        int lightCoords,
        int overlayCoords,
        int outlineColor,
        int[] tintLayers,
        List<BakedQuad> quads,
        ItemStackRenderState.FoilType foilType,
        CallbackInfo ci
    ) {
        if (!itemSubmits.isEmpty()) {
            LightsaberGlow.markCurrentTargetSubmit(
                itemSubmits.get(itemSubmits.size() - 1)
            );
        }
    }
}
