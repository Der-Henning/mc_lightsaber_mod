package com.henning.lightsabers.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingSwingAccessor {
    @Accessor("swingState")
    LivingEntity.SwingState lightsabers$getSwingState();

    @Invoker("getModifiedSwingDuration")
    int lightsabers$getModifiedSwingDuration(SwingAnimation animation);
}
