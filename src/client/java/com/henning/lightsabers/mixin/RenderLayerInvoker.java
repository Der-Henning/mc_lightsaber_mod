package com.henning.lightsabers.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderLayerInvoker {
    @Invoker("create")
    static RenderType lightsabers$invokeCreate(String name, RenderSetup setup) {
        throw new UnsupportedOperationException();
    }
}
