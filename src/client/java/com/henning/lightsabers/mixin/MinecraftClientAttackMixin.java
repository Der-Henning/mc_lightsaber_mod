package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsabersClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientAttackMixin {
    @Inject(method = "doAttack", at = @At("HEAD"))
    private void lightsabers$onAttackClick(CallbackInfoReturnable<Boolean> cir) {
        LightsabersClient.onLocalAttackClick((MinecraftClient) (Object) this);
    }
}
