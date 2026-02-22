package com.henning.lightsabers.item;

import com.henning.lightsabers.LightsabersMod;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LightsaberItem extends Item {

    private static final float FORCE_JUMP_VERTICAL_BOOST = 1.04F;
    private static final float FORCE_JUMP_FORWARD = 1.5F;
    private static final int FORCE_JUMP_COOLDOWN_TICKS = 28;
    private static final long PERFECT_DEFLECT_WINDOW_TICKS = 8L;
    private static final Map<UUID, Long> PERFECT_DEFLECT_UNTIL_TICK =
        new ConcurrentHashMap<>();

    public LightsaberItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void postHit(
        ItemStack stack,
        LivingEntity target,
        LivingEntity attacker
    ) {
        target.setOnFireFor(4.0F);
        LightsabersMod.handleLightsaberAttack(attacker, target);
        super.postHit(stack, target, attacker);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (
            hand == Hand.OFF_HAND &&
            !(user.getMainHandStack().getItem() instanceof LightsaberItem)
        ) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            if (world instanceof ServerWorld serverWorld) {
                PERFECT_DEFLECT_UNTIL_TICK.put(
                    user.getUuid(),
                    serverWorld.getTime() + PERFECT_DEFLECT_WINDOW_TICKS
                );
            }

            if (user.isSneaking() && user.isOnGround()) {
                performForceJump(world, user, hand);
                return ActionResult.SUCCESS;
            }

            world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                LightsabersMod.LIGHTSABER_SWING,
                SoundCategory.PLAYERS,
                0.85F,
                1.0F
            );
            user.getItemCooldownManager().set(user.getStackInHand(hand), 8);
        }
        return ActionResult.SUCCESS;
    }

    public static boolean hasPerfectDeflectWindow(
        PlayerEntity player,
        ServerWorld world
    ) {
        Long untilTick = PERFECT_DEFLECT_UNTIL_TICK.get(player.getUuid());
        if (untilTick == null) {
            return false;
        }
        if (world.getTime() > untilTick) {
            PERFECT_DEFLECT_UNTIL_TICK.remove(player.getUuid());
            return false;
        }
        return true;
    }

    private void performForceJump(World world, PlayerEntity user, Hand hand) {
        Vec3d look = user.getRotationVec(1.0F);
        Vec3d horizontalLook = new Vec3d(look.x, 0.0D, look.z);
        if (horizontalLook.lengthSquared() > 1.0E-6D) {
            horizontalLook = horizontalLook.normalize();
        }

        Vec3d jumpBoost = new Vec3d(
            horizontalLook.x * FORCE_JUMP_FORWARD,
            FORCE_JUMP_VERTICAL_BOOST,
            horizontalLook.z * FORCE_JUMP_FORWARD
        );

        user.setVelocity(user.getVelocity().add(jumpBoost));
        user.setOnGround(false);
        user.velocityDirty = true;
        if (user instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.sendPacket(
                new EntityVelocityUpdateS2CPacket(serverPlayer)
            );
        }
        user
            .getItemCooldownManager()
            .set(user.getStackInHand(hand), FORCE_JUMP_COOLDOWN_TICKS);

        world.playSound(
            null,
            user.getX(),
            user.getY(),
            user.getZ(),
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
            SoundCategory.PLAYERS,
            0.95F,
            0.8F + user.getRandom().nextFloat() * 0.15F
        );

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
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
            serverWorld.spawnParticles(
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
    public void appendTooltip(
        ItemStack stack,
        Item.TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> textConsumer,
        TooltipType type
    ) {
        textConsumer.accept(
            Text.translatable("item.lightsabers.lightsaber.tooltip").formatted(
                Formatting.AQUA
            )
        );
        textConsumer.accept(
            Text.translatable("item.lightsabers.weapon.fire_notice").formatted(
                Formatting.GOLD
            )
        );
        textConsumer.accept(
            Text.translatable(
                "item.lightsabers.lightsaber.deflect_notice"
            ).formatted(Formatting.GRAY)
        );
        textConsumer.accept(
            Text.translatable(
                "item.lightsabers.lightsaber.mastery_notice"
            ).formatted(Formatting.LIGHT_PURPLE)
        );
        textConsumer.accept(
            Text.translatable(
                "item.lightsabers.lightsaber.dual_wield_notice"
            ).formatted(Formatting.RED)
        );
        textConsumer.accept(
            Text.translatable(
                "item.lightsabers.lightsaber.force_jump_notice"
            ).formatted(Formatting.DARK_AQUA)
        );
    }
}
