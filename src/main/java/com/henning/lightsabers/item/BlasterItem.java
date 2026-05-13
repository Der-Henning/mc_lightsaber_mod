package com.henning.lightsabers.item;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.entity.BlasterProjectileEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class BlasterItem extends Item {
    private static final long BLASTER_COOLDOWN_TICKS = 5L;
    private static final Map<UUID, Long> MAIN_HAND_COOLDOWN_UNTIL =
        new ConcurrentHashMap<>();
    private static final Map<UUID, Long> OFF_HAND_COOLDOWN_UNTIL =
        new ConcurrentHashMap<>();

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
        long now = level.getServer() == null
            ? 0L
            : level.getServer().getTickCount();
        Map<UUID, Long> cooldownMap =
            hand == InteractionHand.MAIN_HAND
                ? MAIN_HAND_COOLDOWN_UNTIL
                : OFF_HAND_COOLDOWN_UNTIL;
        long cooldownUntil = cooldownMap.getOrDefault(user.getUUID(), 0L);
        if (cooldownUntil > now) {
            return InteractionResult.FAIL;
        }

        level.playSound(
            null,
            user.getX(),
            user.getY(),
            user.getZ(),
            LightsabersMod.BLASTER_SHOOT,
            SoundSource.PLAYERS,
            0.6F,
            0.9F + level.getRandom().nextFloat() * 0.15F
        );

        if (level instanceof ServerLevel serverLevel) {
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
        }

        user.awardStat(Stats.ITEM_USED.get(this));
        cooldownMap.put(user.getUUID(), now + BLASTER_COOLDOWN_TICKS);
        if (!user.getAbilities().instabuild) {
            stack.hurtAndBreak(1, user, hand);
        }
        return InteractionResult.CONSUME;
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
                "item.lightsabers.weapon.fire_notice"
            ).withStyle(ChatFormatting.GOLD)
        );
    }
}
