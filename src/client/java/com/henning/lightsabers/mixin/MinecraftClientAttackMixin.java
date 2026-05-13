package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsabersClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftClientAttackMixin {
    @Inject(method = "startAttack", at = @At("HEAD"))
    private void lightsabers$onAttackClick(CallbackInfoReturnable<Boolean> cir) {
        LightsabersClient.onLocalAttackClick((Minecraft) (Object) this);
    }
}
