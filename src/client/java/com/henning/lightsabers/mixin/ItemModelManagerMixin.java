package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlow;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public class ItemModelManagerMixin {
    @Inject(method = "updateForLiving", at = @At("HEAD"))
    private void lightsabers$setStackLiving(
        ItemStackRenderState renderState,
        ItemStack stack,
        ItemDisplayContext displayContext,
        LivingEntity entity,
        CallbackInfo ci
    ) {
        LightsaberGlow.setTargetStack(stack);
    }

    @Inject(method = "updateForNonLiving", at = @At("HEAD"))
    private void lightsabers$setStackEntity(
        ItemStackRenderState renderState,
        ItemStack stack,
        ItemDisplayContext displayContext,
        Entity entity,
        CallbackInfo ci
    ) {
        LightsaberGlow.setTargetStack(stack);
    }

    @Inject(method = "updateForTopItem", at = @At("HEAD"))
    private void lightsabers$setStackTopItem(
        ItemStackRenderState renderState,
        ItemStack stack,
        ItemDisplayContext displayContext,
        Level level,
        ItemOwner owner,
        int seed,
        CallbackInfo ci
    ) {
        LightsaberGlow.setTargetStack(stack);
    }

    @Inject(method = "appendItemLayers", at = @At("HEAD"))
    private void lightsabers$setStackAppendLayers(
        ItemStackRenderState renderState,
        ItemStack stack,
        ItemDisplayContext displayContext,
        Level level,
        ItemOwner owner,
        int seed,
        CallbackInfo ci
    ) {
        LightsaberGlow.setTargetStack(stack);
    }
}
