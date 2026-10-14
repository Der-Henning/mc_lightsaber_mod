package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsabersClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientAttackMixin {
    @Shadow
    private int rightClickDelay;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void lightsabers$leftHandAttack(CallbackInfoReturnable<Boolean> cir) {
        if (LightsabersClient.tryIndependentAttack((Minecraft) (Object) this, false)) cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void lightsabers$rightHandAttack(CallbackInfo ci) {
        if (LightsabersClient.tryIndependentAttack((Minecraft) (Object) this, true)) {
            rightClickDelay = 4;
            ci.cancel();
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void lightsabers$preventDualSaberMining(boolean attacking, CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        if (LightsabersClient.isDualWielding(client)) {
            if (client.gameMode != null) client.gameMode.stopDestroyBlock();
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/component/SwingAnimation;Z)Z"))
    private void lightsabers$onAttackClick(CallbackInfoReturnable<Boolean> cir) {
        LightsabersClient.onLocalAttackClick((Minecraft) (Object) this);
    }
}
