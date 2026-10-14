package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.OffhandSwingRenderState;
import com.henning.lightsabers.item.LightsaberItem;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsRendererMixin {
    @ModifyArgs(method = "submitHandsWithItems", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;submitArmWithItem(Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"))
    private void lightsabers$animateOffhand(Args args) {
        FirstPersonHandsAndItemsRenderState state = args.get(1);
        if (state.mainHandItem.getItem() instanceof LightsaberItem
            && state.offHandItem.getItem() instanceof LightsaberItem) {
            OffhandSwingRenderState swings = (OffhandSwingRenderState) state;
            args.set(5, args.get(4) == InteractionHand.MAIN_HAND ? swings.lightsabers$getMainhandSwing() : swings.lightsabers$getOffhandSwing());
        }
    }
}
