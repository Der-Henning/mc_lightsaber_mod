package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFeatureRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "getFoilRenderType", at = @At("HEAD"), cancellable = true)
    private static void lightsabers$boostGlintRenderType(
        RenderType layer,
        boolean solid,
        CallbackInfoReturnable<RenderType> cir
    ) {
        if (!LightsaberGlow.isRenderingLightsaberSubmit()) {
            return;
        }

        if (
            Minecraft.useShaderTransparency() &&
            layer.outputTarget() == OutputTarget.ITEM_ENTITY_TARGET
        ) {
            cir.setReturnValue(LightsaberGlow.getGlintTranslucent());
            return;
        }

        cir.setReturnValue(
            solid ? LightsaberGlow.getGlint() : LightsaberGlow.getEntityGlint()
        );
    }

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void lightsabers$beginLightsaberItemRender(
        MultiBufferSource.BufferSource bufferSource,
        OutlineBufferSource outlineBufferSource,
        SubmitNodeStorage.ItemSubmit itemSubmit,
        CallbackInfo ci
    ) {
        LightsaberGlow.beginRenderingSubmit(itemSubmit);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void lightsabers$endLightsaberItemRender(
        MultiBufferSource.BufferSource bufferSource,
        OutlineBufferSource outlineBufferSource,
        SubmitNodeStorage.ItemSubmit itemSubmit,
        CallbackInfo ci
    ) {
        LightsaberGlow.endRenderingSubmit(itemSubmit);
    }
}
