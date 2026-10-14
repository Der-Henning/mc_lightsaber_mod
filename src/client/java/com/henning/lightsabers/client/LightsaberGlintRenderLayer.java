package com.henning.lightsabers.client;

import com.henning.lightsabers.mixin.RenderLayerInvoker;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;

public final class LightsaberGlintRenderLayer {
    private static final Identifier GLINT_TEXTURE = Identifier.fromNamespaceAndPath(
        "lightsabers", "textures/misc/glint_white.png"
    );
    private record Key(Identifier atlas, boolean translucent, boolean special) {
    }
    private static final Map<Key, RenderType> TYPES = new ConcurrentHashMap<>();

    private LightsaberGlintRenderLayer() {
    }

    public static void registerTextures(TextureManager textures) {
        textures.registerForNextReload(GLINT_TEXTURE);
        // Iris binds vanilla glint samplers even for custom glint render types.
        textures.registerForNextReload(ItemFeatureRenderer.ENCHANTED_GLINT_ITEM);
        textures.registerForNextReload(ItemFeatureRenderer.ENCHANTED_GLINT_ARMOR);
    }

    public static RenderType get(Identifier atlas, boolean translucent, boolean special) {
        return TYPES.computeIfAbsent(new Key(atlas, translucent, special), LightsaberGlintRenderLayer::create);
    }

    private static RenderType create(Key key) {
        var pipeline = key.translucent()
            ? (key.special() ? RenderPipelines.ITEM_TRANSLUCENT_GLINT_SPECIAL : RenderPipelines.ITEM_TRANSLUCENT_GLINT)
            : (key.special() ? RenderPipelines.ITEM_CUTOUT_GLINT_SPECIAL : RenderPipelines.ITEM_CUTOUT_GLINT);
        var builder = RenderSetup.builder(pipeline)
            .withTexture("Sampler0", key.atlas())
            .withTexture("GlintSampler", GLINT_TEXTURE)
            .setTextureTransform(TextureTransform.GLINT_TEXTURING)
            .useLightmap().useOverlay().affectsCrumbling()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);
        if (key.translucent()) {
            builder.setOitPipelines(key.special() ? RenderPipelines.OIT_ITEM_GLINT_SPECIAL : RenderPipelines.OIT_ITEM_GLINT)
                .sortOnUpload();
        }
        return RenderLayerInvoker.lightsabers$invokeCreate("lightsaber_glint_" + key, builder.createRenderSetup());
    }
}
