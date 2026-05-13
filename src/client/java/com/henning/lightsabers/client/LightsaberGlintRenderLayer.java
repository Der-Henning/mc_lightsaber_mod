package com.henning.lightsabers.client;

import com.henning.lightsabers.mixin.RenderLayerInvoker;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class LightsaberGlintRenderLayer {
    private static final Identifier ITEM_GLINT_TEXTURE =
        Identifier.fromNamespaceAndPath(
            "lightsabers",
            "textures/misc/glint_white.png"
        );
    private static final Identifier ENTITY_GLINT_TEXTURE =
        Identifier.fromNamespaceAndPath(
            "lightsabers",
            "textures/misc/glint_white.png"
        );

    public static final RenderType GLINT = createGlint();
    public static final RenderType ENTITY_GLINT = createEntityGlint();
    public static final RenderType ARMOR_ENTITY_GLINT =
        createArmorEntityGlint();
    public static final RenderType TRANSLUCENT_GLINT =
        createTranslucentGlint();

    private LightsaberGlintRenderLayer() {
    }

    public static void addGlintTypes(
        Map<RenderType, ByteBufferBuilder> map
    ) {
        add(map, GLINT);
        add(map, ENTITY_GLINT);
        add(map, ARMOR_ENTITY_GLINT);
        add(map, TRANSLUCENT_GLINT);
    }

    private static void add(
        Map<RenderType, ByteBufferBuilder> map,
        RenderType renderType
    ) {
        if (!map.containsKey(renderType)) {
            map.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
        }
    }

    private static RenderType createGlint() {
        return RenderLayerInvoker.lightsabers$invokeCreate("lightsaber_glint",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", ITEM_GLINT_TEXTURE)
                        .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                        .createRenderSetup());
    }

    private static RenderType createEntityGlint() {
        return RenderLayerInvoker.lightsabers$invokeCreate("lightsaber_entity_glint",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", ITEM_GLINT_TEXTURE)
                        .setTextureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
                        .createRenderSetup());
    }

    private static RenderType createArmorEntityGlint() {
        return RenderLayerInvoker.lightsabers$invokeCreate("lightsaber_armor_entity_glint",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", ENTITY_GLINT_TEXTURE)
                        .setTextureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .createRenderSetup());
    }

    private static RenderType createTranslucentGlint() {
        return RenderLayerInvoker.lightsabers$invokeCreate("lightsaber_glint_translucent",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .withTexture("Sampler0", ITEM_GLINT_TEXTURE)
                        .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .createRenderSetup());
    }
}
