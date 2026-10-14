package com.henning.lightsabers;

import com.henning.lightsabers.client.LightsabersClient;
import com.henning.lightsabers.mixin.ItemLayerRenderStateAccessor;
import com.henning.lightsabers.mixin.ItemStackRenderStateAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

public class RenderingGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            setMainArm(context, HumanoidArm.RIGHT);
            singleplayer.getServer().runCommand("item replace entity @p weapon.mainhand with lightsabers:lightsaber");
            singleplayer.getServer().runCommand("item replace entity @p weapon.offhand with lightsabers:red_lightsaber");
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitTicks(20);
            assertGlowMaterials(context);

            context.takeScreenshot("dual-sabers-idle");
            context.getInput().pressKey(options -> options.keyUse);
            context.waitTicks(2);
            assertSelectedHand(context, InteractionHand.MAIN_HAND, "Right mouse must swing only the physical right hand");
            singleplayer.getConnection().waitForServerboundPackets();
            singleplayer.getServer().runOnServer(server -> {
                var swing = singleplayer.getConnection().getServerPlayer().getCurrentSwing();
                if (swing == null || swing.hand() != InteractionHand.MAIN_HAND) {
                    throw new AssertionError("Right-hand swing must reach the server");
                }
            });
            context.takeScreenshot("right-mouse-right-hand");
            context.waitTicks(10);
            context.getInput().pressKey(options -> options.keyAttack);
            context.waitTicks(2);
            assertSelectedHand(context, InteractionHand.OFF_HAND, "Left mouse must swing only the physical left hand");
            context.takeScreenshot("left-mouse-left-hand");
            context.waitTicks(10);
            setMainArm(context, HumanoidArm.LEFT);
            context.getInput().pressKey(options -> options.keyAttack);
            context.waitTicks(2);
            assertSelectedHand(context, InteractionHand.MAIN_HAND, "Left mouse must still swing the physical left hand with left-handed settings");
            context.waitTicks(10);
            setMainArm(context, HumanoidArm.RIGHT);
            context.waitTicks(10);
            singleplayer.getServer().runCommand("item replace entity @p weapon.mainhand with lightsabers:blaster");
            singleplayer.getConnection().waitForClientboundPackets();
            context.waitTicks(10);
            context.getInput().pressKey(options -> options.keyUse);
            context.waitTicks(2);
            boolean wrongSwing = context.computeOnClient(client -> LightsabersClient.offhandSwingProgress(0.5F) > 0.0F);
            if (wrongSwing) throw new AssertionError("Firing a blaster must not animate the offhand saber");
            context.takeScreenshot("blaster-with-offhand-saber");

            singleplayer.getServer().runCommand("time set midnight");
            for (String saber : new String[] {"lightsaber", "blue_lightsaber", "green_lightsaber", "red_lightsaber", "purple_lightsaber", "yellow_lightsaber"}) {
                singleplayer.getServer().runCommand("item replace entity @p weapon.mainhand with lightsabers:" + saber);
                singleplayer.getConnection().waitForClientboundPackets();
                context.waitTicks(10);
                assertGlowMaterials(context);
                context.takeScreenshot("night-glow-" + saber);
            }
            context.waitTicks(10);
            context.takeScreenshot("night-glow-pulse");
            context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(5);
            context.takeScreenshot("third-person-glow");
        }
    }

    private static void assertGlowMaterials(ClientGameTestContext context) {
        context.runOnClient(client -> {
            var stack = client.player.getMainHandItem();
            if (stack.getItem().isFoil(stack)) throw new AssertionError("An unenchanted saber must not force a purple glint");
            ItemStackRenderState state = new ItemStackRenderState();
            client.getItemModelResolver().updateForLiving(state, client.player.getMainHandItem(), ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, client.player);
            ItemStackRenderStateAccessor layers = (ItemStackRenderStateAccessor) state;
            if (layers.lightsabers$getActiveLayerCount() != 2) throw new AssertionError("A saber needs a core and an aura model");
            boolean foundAura = false;
            for (int i = 0; i < layers.lightsabers$getActiveLayerCount(); i++) {
                var quads = ((ItemLayerRenderStateAccessor) layers.lightsabers$getLayers()[i]).lightsabers$getQuads();
                for (var quad : quads.all()) {
                    var material = quad.materialInfo();
                    String texture = material.sprite().contents().name().getPath();
                    if (texture.endsWith("_aura")) {
                        foundAura = true;
                        if (material.lightEmission() != 15 || !material.sprite().isAnimated() || !material.itemRenderType().hasBlending()
                            || material.itemGlintRenderType() != material.itemRenderType()) {
                            throw new AssertionError("Aura must be full-bright, animated, translucent and free of glint stripes");
                        }
                    } else if (texture.equals("item/lightsaber_hilt")) {
                        if (material.lightEmission() != 0 || material.itemGlintRenderType() != material.itemRenderType()) {
                            throw new AssertionError("Hilt must stay unlit and free of blade glint");
                        }
                    } else if (material.lightEmission() != 15) {
                        throw new AssertionError("Blade core must remain bright in darkness");
                    }
                }
            }
            if (!foundAura) throw new AssertionError("Missing animated aura texture");
        });
    }

    private static void setMainArm(ClientGameTestContext context, HumanoidArm arm) {
        context.runOnClient(client -> {
            client.options.mainHand().set(arm);
            client.options.broadcastOptions();
        });
        context.waitFor(client -> client.player != null && client.player.getMainArm() == arm);
    }

    private static void assertSelectedHand(ClientGameTestContext context, InteractionHand hand, String message) {
        boolean correct = context.computeOnClient(client -> {
            float main = LightsabersClient.mainhandSwingProgress(0.5F);
            float off = LightsabersClient.offhandSwingProgress(0.5F);
            var swing = client.player.getCurrentSwing();
            return (hand == InteractionHand.MAIN_HAND ? main > 0.0F && off == 0.0F : off > 0.0F && main == 0.0F)
                && swing != null && swing.hand() == hand;
        });
        if (!correct) throw new AssertionError(message);
    }
}
