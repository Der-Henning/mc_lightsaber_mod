package com.henning.lightsabers.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.SwingState.class)
public interface SwingStateInvoker {
    @Invoker("start")
    void lightsabers$start(InteractionHand hand, SwingAnimation animation, int duration);
}
