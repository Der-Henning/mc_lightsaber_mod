package com.henning.lightsabers.item;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.combat.CombatState;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LightsaberItem extends Item {

    private static final float FORCE_JUMP_VERTICAL_BOOST = 1.04F;
    private static final float FORCE_JUMP_FORWARD = 1.5F;
    private static final int FORCE_JUMP_COOLDOWN_TICKS = 28;
    private static final int PERFECT_DEFLECT_WINDOW_TICKS = 8;

    public LightsaberItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void postHurtEnemy(
        ItemStack stack,
        LivingEntity target,
        LivingEntity attacker
    ) {
        target.igniteForSeconds(4.0F);
        LightsabersMod.handleLightsaberAttack(attacker);
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
            CombatState state = LightsabersMod.combatState(user);
            if (user.isShiftKeyDown() && user.onGround()) {
                if (!state.tryUse(CombatState.Action.FORCE_JUMP, user.tickCount, FORCE_JUMP_COOLDOWN_TICKS)) {
                    return InteractionResult.FAIL;
                }
                state.openPerfectDeflectWindow(user.tickCount, PERFECT_DEFLECT_WINDOW_TICKS);
                performForceJump(level, user);
                return InteractionResult.SUCCESS;
            }

            if (!state.tryUse(CombatState.Action.SABER_USE, user.tickCount, 8)) {
                return InteractionResult.FAIL;
            }
            state.openPerfectDeflectWindow(user.tickCount, PERFECT_DEFLECT_WINDOW_TICKS);

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
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean hasPerfectDeflectWindow(
        Player player
    ) {
        return LightsabersMod.combatState(player).hasPerfectDeflectWindow(player.tickCount);
    }

    private void performForceJump(
        Level level,
        Player user
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
        user.syncVelocity = true;
        if (user instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(
                new ClientboundSetEntityMotionPacket(serverPlayer)
            );
        }

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

}
