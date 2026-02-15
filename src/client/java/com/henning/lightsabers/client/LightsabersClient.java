package com.henning.lightsabers.client;

import com.henning.lightsabers.LightsabersMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightsabersClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("lightsabers-client");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                LightsabersMod.BLASTER_PROJECTILE,
                context -> new FlyingItemEntityRenderer<>(context, 0.75F, true)
        );
        LOGGER.info("Lightsabers client initialized");
    }
}
