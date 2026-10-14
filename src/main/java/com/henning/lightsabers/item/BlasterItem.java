package com.henning.lightsabers.item;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.entity.BlasterProjectileEntity;
import com.henning.lightsabers.combat.CombatState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlasterItem extends Item {
    private static final int BLASTER_COOLDOWN_TICKS = 5;

    public BlasterItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(
        Level level,
        Player user,
        InteractionHand hand
    ) {
        ItemStack stack = user.getItemInHand(hand);
        CombatState.Action action = hand == InteractionHand.MAIN_HAND
            ? CombatState.Action.MAIN_HAND_BLASTER
            : CombatState.Action.OFF_HAND_BLASTER;
        if (!LightsabersMod.combatState(user).tryUse(action, user.tickCount, BLASTER_COOLDOWN_TICKS)) {
            // Let vanilla try the offhand when the main blaster is cooling down.
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                LightsabersMod.BLASTER_SHOOT,
                SoundSource.PLAYERS,
                0.6F,
                0.9F + level.getRandom().nextFloat() * 0.15F
            );
            BlasterProjectileEntity projectile = new BlasterProjectileEntity(
                level,
                user
            );
            Projectile.spawnProjectile(
                projectile,
                serverLevel,
                stack,
                p ->
                    p.shootFromRotation(
                        user,
                        user.getXRot(),
                        user.getYRot(),
                        0.0F,
                        2.5F,
                        0.5F
                    )
            );
            user.awardStat(Stats.ITEM_USED.get(this));
            if (!user.getAbilities().instabuild) {
                stack.hurtAndBreak(1, user, hand);
            }
        }
        return InteractionResult.CONSUME;
    }

}
