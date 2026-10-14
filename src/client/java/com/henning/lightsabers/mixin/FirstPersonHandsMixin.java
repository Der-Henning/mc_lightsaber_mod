package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsabersClient;
import com.henning.lightsabers.client.OffhandSwingRenderState;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItems.class)
public abstract class FirstPersonHandsMixin {
    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void lightsabers$extractOffhandSwing(LocalPlayer player, float partialTick,
        FirstPersonHandsAndItemsRenderState state, CallbackInfo ci) {
        ((OffhandSwingRenderState) state).lightsabers$setOffhandSwing(LightsabersClient.offhandSwingProgress(partialTick));
        ((OffhandSwingRenderState) state).lightsabers$setMainhandSwing(LightsabersClient.mainhandSwingProgress(partialTick));
    }
}
