package com.henning.lightsabers.client;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.item.LightsaberItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.InteractionHand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightsabersClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        "lightsabers-client"
    );
    private static final int OFFHAND_SWING_DELAY_TICKS = 1;
    private static int pendingOffhandSwings = 0;
    private static int offhandSwingDelayTicks = 0;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
            LightsabersMod.BLASTER_PROJECTILE,
            context -> new ThrownItemRenderer<>(context, 0.75F, true)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingOffhandSwings <= 0) {
                return;
            }
            if (client.player == null || client.level == null) {
                pendingOffhandSwings = 0;
                offhandSwingDelayTicks = 0;
                return;
            }

            boolean mainIsSaber =
                client.player.getMainHandItem().getItem() instanceof
                    LightsaberItem;
            boolean offIsSaber =
                client.player.getOffhandItem().getItem() instanceof
                    LightsaberItem;
            if (!mainIsSaber || !offIsSaber) {
                pendingOffhandSwings = 0;
                offhandSwingDelayTicks = 0;
                return;
            }

            if (offhandSwingDelayTicks > 0) {
                offhandSwingDelayTicks--;
                if (offhandSwingDelayTicks > 0) {
                    return;
                }
            }

            client.player.swing(InteractionHand.OFF_HAND, false);
            pendingOffhandSwings--;
            offhandSwingDelayTicks =
                pendingOffhandSwings > 0 ? OFFHAND_SWING_DELAY_TICKS : 0;
        });

        LOGGER.info("Lightsabers client initialized");
    }

    public static void onLocalAttackClick(Minecraft client) {
        if (
            client.player == null ||
            client.level == null ||
            client.screen != null
        ) {
            return;
        }

        boolean mainIsSaber =
            client.player.getMainHandItem().getItem() instanceof
                LightsaberItem;
        if (!mainIsSaber) {
            return;
        }

        client.player.playSound(
            LightsabersMod.LIGHTSABER_SWING,
            0.85F,
            1.0F + client.player.getRandom().nextFloat() * 0.08F
        );

        boolean offIsSaber =
            client.player.getOffhandItem().getItem() instanceof LightsaberItem;
        if (offIsSaber) {
            boolean queueWasEmpty = pendingOffhandSwings == 0;
            pendingOffhandSwings = Math.min(pendingOffhandSwings + 1, 4);
            if (queueWasEmpty && offhandSwingDelayTicks <= 0) {
                offhandSwingDelayTicks = OFFHAND_SWING_DELAY_TICKS;
            }
        }
    }
}
