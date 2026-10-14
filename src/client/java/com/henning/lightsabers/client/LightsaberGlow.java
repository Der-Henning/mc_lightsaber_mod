package com.henning.lightsabers.client;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.Set;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;

public final class LightsaberGlow {
    private static final Set<String> BLADE_TEXTURES = Set.of(
        "item/lightsaber", "item/blue_lightsaber", "item/green_lightsaber",
        "item/red_lightsaber", "item/purple_lightsaber", "item/yellow_lightsaber"
    );
    // Model data is immutable; cache geometry without retaining old resource packs.
    private static final Map<ItemQuads, ItemQuads> QUADS = Collections.synchronizedMap(new WeakHashMap<>());

    private LightsaberGlow() {
    }

    public static ItemQuads withBladeGlow(ItemQuads quads) {
        return QUADS.computeIfAbsent(quads, original -> ItemQuads.split(
            original.all().stream().map(LightsaberGlow::withBladeGlow).toList()
        ));
    }

    private static BakedQuad withBladeGlow(BakedQuad quad) {
        BakedQuad.MaterialInfo material = quad.materialInfo();
        var texture = material.sprite().contents().name();
        boolean blade = texture.getNamespace().equals("lightsabers") && BLADE_TEXTURES.contains(texture.getPath());
        // Enchanted blades can shimmer, but the halo and hilt never get glint stripes.
        BakedQuad.MaterialInfo glowMaterial = new BakedQuad.MaterialInfo(
            material.sprite(), material.layer(), material.itemRenderType(),
            blade ? LightsaberGlintRenderLayer.get(material.sprite().atlasLocation(), material.itemRenderType().hasBlending(), false) : material.itemRenderType(),
            blade ? LightsaberGlintRenderLayer.get(material.sprite().atlasLocation(), material.itemRenderType().hasBlending(), true) : material.itemRenderType(),
            material.tintIndex(), blade ? null : material.shadeDirectionOverride(), blade ? 15 : material.lightEmission()
        );
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(),
            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.direction(), glowMaterial);
    }
}
