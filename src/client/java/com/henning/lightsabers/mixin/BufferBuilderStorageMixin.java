package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlintRenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBuffers.class)
public class BufferBuilderStorageMixin {

    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lightsabers$addGlintTypes(int sectionBufferCount, CallbackInfo ci) {
        LightsaberGlintRenderLayer.addGlintTypes(
            ((BufferSourceAccessor) bufferSource).lightsabers$getFixedBuffers()
        );
    }
}
