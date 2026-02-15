package com.henning.lightsabers.client;

import com.henning.lightsabers.mixin.RenderLayerInvoker;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.OutputTarget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.TextureTransform;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class LightsaberGlintRenderLayer {
    private static final Identifier ITEM_GLINT_TEXTURE = Identifier.of("lightsabers", "textures/misc/glint_white.png");
    private static final Identifier ENTITY_GLINT_TEXTURE = Identifier.of("lightsabers", "textures/misc/glint_white.png");

    public static final RenderLayer GLINT = createGlint();
    public static final RenderLayer ENTITY_GLINT = createEntityGlint();
    public static final RenderLayer ARMOR_ENTITY_GLINT = createArmorEntityGlint();
    public static final RenderLayer TRANSLUCENT_GLINT = createTranslucentGlint();

    private LightsaberGlintRenderLayer() {
    }

    public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map) {
        add(map, GLINT);
        add(map, ENTITY_GLINT);
        add(map, ARMOR_ENTITY_GLINT);
        add(map, TRANSLUCENT_GLINT);
    }

    private static void add(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map, RenderLayer renderLayer) {
        if (!map.containsKey(renderLayer)) {
            map.put(renderLayer, new BufferAllocator(renderLayer.getExpectedBufferSize()));
        }
    }

    private static RenderLayer createGlint() {
        return RenderLayerInvoker.lightsabers$invokeOf("lightsaber_glint",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .texture("Sampler0", ITEM_GLINT_TEXTURE)
                        .textureTransform(TextureTransform.GLINT_TEXTURING)
                        .build());
    }

    private static RenderLayer createEntityGlint() {
        return RenderLayerInvoker.lightsabers$invokeOf("lightsaber_entity_glint",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .texture("Sampler0", ITEM_GLINT_TEXTURE)
                        .textureTransform(TextureTransform.ENTITY_GLINT_TEXTURING)
                        .build());
    }

    private static RenderLayer createArmorEntityGlint() {
        return RenderLayerInvoker.lightsabers$invokeOf("lightsaber_armor_entity_glint",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .texture("Sampler0", ENTITY_GLINT_TEXTURE)
                        .textureTransform(TextureTransform.ARMOR_ENTITY_GLINT_TEXTURING)
                        .layeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .build());
    }

    private static RenderLayer createTranslucentGlint() {
        return RenderLayerInvoker.lightsabers$invokeOf("lightsaber_glint_translucent",
                RenderSetup.builder(RenderPipelines.GLINT)
                        .texture("Sampler0", ITEM_GLINT_TEXTURE)
                        .textureTransform(TextureTransform.GLINT_TEXTURING)
                        .outputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .build());
    }
}
