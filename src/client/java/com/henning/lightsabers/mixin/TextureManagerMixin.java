package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlintRenderLayer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void lightsabers$registerGlintTextures(CallbackInfo ci) {
        LightsaberGlintRenderLayer.registerTextures((TextureManager) (Object) this);
    }
}
