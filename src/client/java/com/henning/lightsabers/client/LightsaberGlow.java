package com.henning.lightsabers.client;

import com.henning.lightsabers.LightsabersMod;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class LightsaberGlow {
    private static final ThreadLocal<ItemStack> TARGET_STACK = new ThreadLocal<>();
    private static final ThreadLocal<SubmitNodeStorage.ItemSubmit>
        RENDERING_SUBMIT = new ThreadLocal<>();
    private static final Set<SubmitNodeStorage.ItemSubmit> LIGHTSABER_SUBMITS =
        Collections.newSetFromMap(new IdentityHashMap<>());

    private LightsaberGlow() {
    }

    public static void setTargetStack(ItemStack stack) {
        TARGET_STACK.set(stack);
    }

    public static void markCurrentTargetSubmit(SubmitNodeStorage.ItemSubmit submit) {
        if (isLightsaberTarget()) {
            LIGHTSABER_SUBMITS.add(submit);
        }
    }

    public static void beginRenderingSubmit(SubmitNodeStorage.ItemSubmit submit) {
        RENDERING_SUBMIT.set(submit);
    }

    public static void endRenderingSubmit(SubmitNodeStorage.ItemSubmit submit) {
        RENDERING_SUBMIT.remove();
        LIGHTSABER_SUBMITS.remove(submit);
    }

    public static RenderType getGlint() {
        if (!isRenderingLightsaberSubmit()) {
            return RenderTypes.glint();
        }
        return LightsaberGlintRenderLayer.GLINT;
    }

    public static RenderType getEntityGlint() {
        if (!isRenderingLightsaberSubmit()) {
            return RenderTypes.entityGlint();
        }
        return LightsaberGlintRenderLayer.ENTITY_GLINT;
    }

    public static RenderType getArmorEntityGlint() {
        if (!isRenderingLightsaberSubmit()) {
            return RenderTypes.armorEntityGlint();
        }
        return LightsaberGlintRenderLayer.ARMOR_ENTITY_GLINT;
    }

    public static RenderType getGlintTranslucent() {
        if (!isRenderingLightsaberSubmit()) {
            return RenderTypes.glintTranslucent();
        }
        return LightsaberGlintRenderLayer.TRANSLUCENT_GLINT;
    }

    public static boolean isRenderingLightsaberSubmit() {
        SubmitNodeStorage.ItemSubmit submit = RENDERING_SUBMIT.get();
        return submit != null && LIGHTSABER_SUBMITS.contains(submit);
    }

    private static boolean isLightsaberTarget() {
        ItemStack stack = TARGET_STACK.get();
        return stack != null && !stack.isEmpty() && LightsabersMod.ALL_LIGHTSABERS.contains(stack.getItem());
    }
}
