package com.henning.lightsabers.item;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.entity.BlasterProjectileEntity;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class BlasterItem extends Item {
    private static final long BLASTER_COOLDOWN_TICKS = 5L;
    private static final Map<UUID, Long> MAIN_HAND_COOLDOWN_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> OFF_HAND_COOLDOWN_UNTIL = new ConcurrentHashMap<>();

    public BlasterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        long now = world.getTime();
        Map<UUID, Long> cooldownMap = hand == Hand.MAIN_HAND ? MAIN_HAND_COOLDOWN_UNTIL : OFF_HAND_COOLDOWN_UNTIL;
        long cooldownUntil = cooldownMap.getOrDefault(user.getUuid(), 0L);
        if (cooldownUntil > now) {
            return ActionResult.FAIL;
        }

        world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                LightsabersMod.BLASTER_SHOOT,
                SoundCategory.PLAYERS,
                0.6F,
                0.9F + world.getRandom().nextFloat() * 0.15F
        );

        if (world instanceof ServerWorld serverWorld) {
            BlasterProjectileEntity projectile = new BlasterProjectileEntity(world, user);
            ProjectileEntity.spawn(projectile, serverWorld, stack, p -> p.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2.5F, 0.5F));
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        cooldownMap.put(user.getUuid(), now + BLASTER_COOLDOWN_TICKS);
        if (!user.getAbilities().creativeMode) {
            stack.damage(1, user, hand);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.translatable("item.lightsabers.weapon.fire_notice").formatted(Formatting.GOLD));
    }
}
