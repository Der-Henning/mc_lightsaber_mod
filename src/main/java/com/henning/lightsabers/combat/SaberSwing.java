package com.henning.lightsabers.combat;

import com.henning.lightsabers.mixin.LivingSwingAccessor;
import com.henning.lightsabers.mixin.SwingStateInvoker;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

public final class SaberSwing {
    private SaberSwing() {
    }

    public static void start(LivingEntity entity, InteractionHand hand) {
        var animation = entity.getItemInHand(hand).getAttackAnimation();
        LivingSwingAccessor accessor = (LivingSwingAccessor) entity;
        // Vanilla rejects the other hand during an active swing; explicitly select this one.
        ((SwingStateInvoker) accessor.lightsabers$getSwingState()).lightsabers$start(
            hand, animation, accessor.lightsabers$getModifiedSwingDuration(animation));
    }
}
