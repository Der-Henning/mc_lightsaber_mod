package com.henning.lightsabers.item;

import com.henning.lightsabers.LightsabersMod;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LightsaberItem extends Item {

    private static final float FORCE_JUMP_VERTICAL_BOOST = 1.04F;
    private static final float FORCE_JUMP_FORWARD = 1.5F;
    private static final int FORCE_JUMP_COOLDOWN_TICKS = 28;
    private static final long PERFECT_DEFLECT_WINDOW_TICKS = 8L;
    private static final Map<UUID, Long> PERFECT_DEFLECT_UNTIL_TICK =
        new ConcurrentHashMap<>();

    public LightsaberItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void postHurtEnemy(
        ItemStack stack,
        LivingEntity target,
        LivingEntity attacker
    ) {
        target.igniteForSeconds(4.0F);
        LightsabersMod.handleLightsaberAttack(attacker, target);
        super.postHurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult use(
        Level level,
        Player user,
        InteractionHand hand
    ) {
        if (
            hand == InteractionHand.OFF_HAND &&
            !(user.getMainHandItem().getItem() instanceof LightsaberItem)
        ) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (level instanceof ServerLevel serverLevel) {
                PERFECT_DEFLECT_UNTIL_TICK.put(
                    user.getUUID(),
                    serverLevel.getServer().getTickCount() +
                    PERFECT_DEFLECT_WINDOW_TICKS
                );
            }

            if (user.isShiftKeyDown() && user.onGround()) {
                performForceJump(level, user, hand);
                return InteractionResult.SUCCESS;
            }

            level.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                LightsabersMod.LIGHTSABER_SWING,
                SoundSource.PLAYERS,
                0.85F,
                1.0F
            );
            user.getCooldowns().addCooldown(user.getItemInHand(hand), 8);
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean hasPerfectDeflectWindow(
        Player player,
        ServerLevel level
    ) {
        Long untilTick = PERFECT_DEFLECT_UNTIL_TICK.get(player.getUUID());
        if (untilTick == null) {
            return false;
        }
        if (level.getServer().getTickCount() > untilTick) {
            PERFECT_DEFLECT_UNTIL_TICK.remove(player.getUUID());
            return false;
        }
        return true;
    }

    private void performForceJump(
        Level level,
        Player user,
        InteractionHand hand
    ) {
        Vec3 look = user.getViewVector(1.0F);
        Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSqr() > 1.0E-6D) {
            horizontalLook = horizontalLook.normalize();
        }

        Vec3 jumpBoost = new Vec3(
            horizontalLook.x * FORCE_JUMP_FORWARD,
            FORCE_JUMP_VERTICAL_BOOST,
            horizontalLook.z * FORCE_JUMP_FORWARD
        );

        user.setDeltaMovement(user.getDeltaMovement().add(jumpBoost));
        user.setOnGround(false);
        user.hurtMarked = true;
        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                new ClientboundSetEntityMotionPacket(serverPlayer)
            );
        }
        user
            .getCooldowns()
            .addCooldown(user.getItemInHand(hand), FORCE_JUMP_COOLDOWN_TICKS);

        level.playSound(
            null,
            user.getX(),
            user.getY(),
            user.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundSource.PLAYERS,
            0.95F,
            0.8F + user.getRandom().nextFloat() * 0.15F
        );

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                user.getX(),
                user.getY() + 0.1D,
                user.getZ(),
                20,
                0.22D,
                0.06D,
                0.22D,
                0.02D
            );
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                user.getX(),
                user.getY() + 0.9D,
                user.getZ(),
                10,
                0.15D,
                0.2D,
                0.15D,
                0.01D
            );
        }
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplay displayComponent,
        Consumer<Component> textConsumer,
        TooltipFlag type
    ) {
        textConsumer.accept(
            Component.translatable(
                "item.lightsabers.lightsaber.tooltip"
            ).withStyle(ChatFormatting.AQUA)
        );
        textConsumer.accept(
            Component.translatable(
                "item.lightsabers.weapon.fire_notice"
            ).withStyle(ChatFormatting.GOLD)
        );
        textConsumer.accept(
            Component.translatable(
                "item.lightsabers.lightsaber.deflect_notice"
            ).withStyle(ChatFormatting.GRAY)
        );
        textConsumer.accept(
            Component.translatable(
                "item.lightsabers.lightsaber.mastery_notice"
            ).withStyle(ChatFormatting.LIGHT_PURPLE)
        );
        textConsumer.accept(
            Component.translatable(
                "item.lightsabers.lightsaber.dual_wield_notice"
            ).withStyle(ChatFormatting.RED)
        );
        textConsumer.accept(
            Component.translatable(
                "item.lightsabers.lightsaber.force_jump_notice"
            ).withStyle(ChatFormatting.DARK_AQUA)
        );
    }
}
