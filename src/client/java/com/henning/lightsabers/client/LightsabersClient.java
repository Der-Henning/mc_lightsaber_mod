package com.henning.lightsabers.client;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.item.LightsaberItem;
import com.henning.lightsabers.combat.HandSwing;
import com.henning.lightsabers.combat.SaberCombat;
import com.henning.lightsabers.combat.SaberControls;
import com.henning.lightsabers.combat.SaberSwing;
import com.henning.lightsabers.network.SaberAttackPayload;
import com.henning.lightsabers.network.SaberSwingPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightsabersClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        "lightsabers-client"
    );
    private static final HandSwing MAINHAND_SWING = new HandSwing();
    private static final HandSwing OFFHAND_SWING = new HandSwing();
    private static LocalPlayer animationPlayer;

    @Override
    public void onInitializeClient() {
        WeaponTooltips.register();
        EntityRenderers.register(
            LightsabersMod.BLASTER_PROJECTILE,
            context -> new ThrownItemRenderer<>(context, 0.75F, true)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (animationPlayer != client.player || !isDualWielding(client)) {
                MAINHAND_SWING.reset();
                OFFHAND_SWING.reset();
                animationPlayer = client.player;
            }
            if (!isDualWielding(client) || client.isPaused()) {
                return;
            }
            MAINHAND_SWING.tick();
            OFFHAND_SWING.tick();
        });

        ClientPlayNetworking.registerGlobalReceiver(SaberSwingPayload.TYPE, (payload, context) -> {
            if (context.client().level == null) return;
            var entity = context.client().level.getEntity(payload.playerId());
            InteractionHand hand = payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            if (entity instanceof LivingEntity living && entity != context.player()
                && living.getItemInHand(hand).getItem() instanceof LightsaberItem) {
                SaberSwing.start(living, hand);
            }
        });

        LOGGER.info("Lightsabers client initialized");
    }

    public static void onLocalAttackClick(Minecraft client) {
        if (
            client.player == null ||
            client.level == null ||
            client.isPaused() || client.player.isSpectator()
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

    }

    public static boolean tryIndependentAttack(Minecraft client, boolean rightButton) {
        if (!isDualWielding(client) || (rightButton && client.player.isShiftKeyDown())) return false;
        if (client.isPaused() || client.player.isSpectator() || client.player.isHandsBusy()) return true;
        if (!ClientPlayNetworking.canSend(SaberAttackPayload.TYPE)) return false;
        boolean mainHand = SaberControls.usesMainHand(rightButton, client.player.getMainArm() == HumanoidArm.RIGHT);
        InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (!LightsabersMod.combatState(client.player).tryUse(SaberCombat.action(hand), client.player.tickCount,
            SaberCombat.SWING_COOLDOWN_TICKS)) return true;
        if (animationPlayer != client.player) {
            MAINHAND_SWING.reset();
            OFFHAND_SWING.reset();
            animationPlayer = client.player;
        }
        (mainHand ? MAINHAND_SWING : OFFHAND_SWING).start();
        SaberSwing.start(client.player, hand);
        client.player.playSound(LightsabersMod.LIGHTSABER_SWING, 0.85F, mainHand ? 1.0F : 1.12F);
        ClientPlayNetworking.send(new SaberAttackPayload(mainHand));
        return true;
    }

    public static float mainhandSwingProgress(float partialTick) {
        return MAINHAND_SWING.progress(partialTick);
    }

    public static float offhandSwingProgress(float partialTick) {
        return OFFHAND_SWING.progress(partialTick);
    }

    public static boolean isDualWielding(Minecraft client) {
        return client.player != null && client.level != null
            && SaberCombat.isDualWielding(client.player);
    }
}
