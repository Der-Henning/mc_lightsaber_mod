package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlow;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {
    @Inject(method = "updateForLivingEntity", at = @At("HEAD"))
    private void lightsabers$setStackLiving(ItemRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, LivingEntity entity, CallbackInfo ci) {
        LightsaberGlow.setTargetStack(stack);
    }

    @Inject(method = "updateForNonLivingEntity", at = @At("HEAD"))
    private void lightsabers$setStackEntity(ItemRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, Entity entity, CallbackInfo ci) {
        LightsaberGlow.setTargetStack(stack);
    }

    @Inject(method = "clearAndUpdate", at = @At("HEAD"))
    private void lightsabers$setStackClearAndUpdate(ItemRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, World world, HeldItemContext context, int seed, CallbackInfo ci) {
        LightsaberGlow.setTargetStack(stack);
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void lightsabers$setStackUpdate(ItemRenderState renderState, ItemStack stack, ItemDisplayContext displayContext, World world, HeldItemContext context, int seed, CallbackInfo ci) {
        LightsaberGlow.setTargetStack(stack);
    }
}
