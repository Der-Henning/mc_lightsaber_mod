package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.OffhandSwingRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FirstPersonHandsAndItemsRenderState.class)
public abstract class FirstPersonHandsRenderStateMixin implements OffhandSwingRenderState {
    @Unique
    private float lightsabers$mainhandSwing;

    public float lightsabers$getMainhandSwing() {
        return lightsabers$mainhandSwing;
    }

    public void lightsabers$setMainhandSwing(float progress) {
        lightsabers$mainhandSwing = progress;
    }

    @Unique
    private float lightsabers$offhandSwing;

    public float lightsabers$getOffhandSwing() {
        return lightsabers$offhandSwing;
    }

    public void lightsabers$setOffhandSwing(float progress) {
        lightsabers$offhandSwing = progress;
    }
}
