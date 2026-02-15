package com.henning.lightsabers.client;

import com.henning.lightsabers.LightsabersMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class LightsaberGlow {
    private static final ThreadLocal<ItemStack> TARGET_STACK = new ThreadLocal<>();

    private LightsaberGlow() {
    }

    public static void setTargetStack(ItemStack stack) {
        TARGET_STACK.set(stack);
    }

    public static RenderLayer getGlint() {
        if (!isLightsaberTarget()) {
            return RenderLayers.glint();
        }
        return LightsaberGlintRenderLayer.GLINT;
    }

    public static RenderLayer getEntityGlint() {
        if (!isLightsaberTarget()) {
            return RenderLayers.entityGlint();
        }
        return LightsaberGlintRenderLayer.ENTITY_GLINT;
    }

    public static RenderLayer getArmorEntityGlint() {
        if (!isLightsaberTarget()) {
            return RenderLayers.armorEntityGlint();
        }
        return LightsaberGlintRenderLayer.ARMOR_ENTITY_GLINT;
    }

    public static RenderLayer getGlintTranslucent() {
        if (!isLightsaberTarget()) {
            return RenderLayers.glintTranslucent();
        }
        return LightsaberGlintRenderLayer.TRANSLUCENT_GLINT;
    }

    public static boolean isLightsaberTargetStack() {
        return isLightsaberTarget();
    }

    private static boolean isLightsaberTarget() {
        ItemStack stack = TARGET_STACK.get();
        return stack != null && !stack.isEmpty() && LightsabersMod.ALL_LIGHTSABERS.contains(stack.getItem());
    }
}
