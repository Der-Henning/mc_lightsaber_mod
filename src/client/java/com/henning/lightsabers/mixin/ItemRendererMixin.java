package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlow;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "getItemGlintConsumer", at = @At("HEAD"), cancellable = true)
    private static void lightsabers$boostGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!LightsaberGlow.isLightsaberTargetStack()) {
            return;
        }

        if (!glint) {
            cir.setReturnValue(vertexConsumers.getBuffer(layer));
            return;
        }

        boolean transparentGlint = layer == TexturedRenderLayers.getItemTranslucentCull() || layer == TexturedRenderLayers.getBlockTranslucentCull();
        VertexConsumer base = vertexConsumers.getBuffer(layer);
        VertexConsumer primary = vertexConsumers.getBuffer(transparentGlint ? LightsaberGlow.getGlintTranslucent() : (solid ? LightsaberGlow.getGlint() : LightsaberGlow.getEntityGlint()));
        VertexConsumer secondary = vertexConsumers.getBuffer(LightsaberGlow.getGlintTranslucent());
        VertexConsumer stacked = unionDistinct(primary, secondary);
        cir.setReturnValue(unionDistinct(stacked, base));
    }

    @Inject(method = "getSpecialItemGlintConsumer", at = @At("HEAD"), cancellable = true)
    private static void lightsabers$boostSpecialGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, MatrixStack.Entry matrixEntry, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!LightsaberGlow.isLightsaberTargetStack()) {
            return;
        }

        VertexConsumer primary = vertexConsumers.getBuffer(LightsaberGlow.getGlint());
        VertexConsumer secondary = vertexConsumers.getBuffer(LightsaberGlow.getGlintTranslucent());
        VertexConsumer base = vertexConsumers.getBuffer(layer);
        cir.setReturnValue(unionDistinct(unionDistinct(primary, secondary), base));
    }

    private static VertexConsumer unionDistinct(VertexConsumer first, VertexConsumer second) {
        if (first == second) {
            return first;
        }
        return VertexConsumers.union(first, second);
    }
}
