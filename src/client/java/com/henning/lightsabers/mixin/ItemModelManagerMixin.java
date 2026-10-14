package com.henning.lightsabers.mixin;

import com.henning.lightsabers.client.LightsaberGlow;
import com.henning.lightsabers.item.LightsaberItem;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public abstract class ItemModelManagerMixin {
    @Inject(method = "updateForTopItem", at = @At("RETURN"))
    private void lightsabers$applyBladeGlow(ItemStackRenderState state, ItemStack stack,
        ItemDisplayContext displayContext, Level level, ItemOwner owner, int seed, CallbackInfo ci) {
        if (!(stack.getItem() instanceof LightsaberItem)) {
            return;
        }
        ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) state;
        for (int i = 0; i < accessor.lightsabers$getActiveLayerCount(); i++) {
            ItemStackRenderState.LayerRenderState layer = accessor.lightsabers$getLayers()[i];
            layer.setQuads(LightsaberGlow.withBladeGlow(((ItemLayerRenderStateAccessor) layer).lightsabers$getQuads()));
        }
    }
}
