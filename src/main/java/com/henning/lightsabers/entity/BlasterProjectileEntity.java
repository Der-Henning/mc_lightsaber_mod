package com.henning.lightsabers.entity;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.item.LightsaberItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class BlasterProjectileEntity extends ThrowableItemProjectile {
    private int lifeTicks;
    private float impactDamage = 6.0F;
    private float impactFireSeconds = 2.0F;

    private enum DeflectTier {
        NONE,
        NORMAL,
        PERFECT,
    }

    public BlasterProjectileEntity(
        EntityType<? extends BlasterProjectileEntity> entityType,
        Level level
    ) {
        super(entityType, level);
    }

    public BlasterProjectileEntity(Level level, LivingEntity owner) {
        super(
            LightsabersMod.BLASTER_PROJECTILE,
            owner,
            level,
            new ItemStack(Items.FIRE_CHARGE)
        );
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                this.getX(),
                this.getY(),
                this.getZ(),
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D
            );
        }
        if (lifeTicks > 50) {
            this.discard();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (
            entityHitResult.getEntity() instanceof LivingEntity livingEntity &&
            this.level() instanceof ServerLevel serverLevel
        ) {
            DeflectTier deflectTier = getDeflectTier(
                livingEntity,
                serverLevel
            );
            if (deflectTier != DeflectTier.NONE) {
                deflect(serverLevel, livingEntity, deflectTier);
                this.discard();
                return;
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            if (
                entityHitResult
                    .getEntity()
                    .hurtServer(
                        serverLevel,
                        this.damageSources().thrown(this, this.getOwner()),
                        impactDamage
                    )
            ) {
                if (entityHitResult.getEntity() instanceof LivingEntity target) {
                    target.igniteForSeconds(impactFireSeconds);
                }
                this.playSound(SoundEvents.FIRE_EXTINGUISH, 0.2F, 1.7F);
            }
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        this.discard();
    }

    private DeflectTier getDeflectTier(
        LivingEntity defender,
        ServerLevel serverLevel
    ) {
        if (!defender.isShiftKeyDown()) {
            return DeflectTier.NONE;
        }

        boolean hasLightsaber =
            defender.getMainHandItem().getItem() instanceof LightsaberItem ||
            defender.getOffhandItem().getItem() instanceof LightsaberItem;
        if (!hasLightsaber || this.getDeltaMovement().lengthSqr() < 1.0E-4) {
            return DeflectTier.NONE;
        }

        Vec3 incomingDirection = this.getDeltaMovement().normalize().scale(-1.0);
        Vec3 lookDirection = defender.getViewVector(1.0F).normalize();
        if (lookDirection.dot(incomingDirection) <= 0.35D) {
            return DeflectTier.NONE;
        }

        if (
            defender instanceof Player player &&
            LightsaberItem.hasPerfectDeflectWindow(player, serverLevel)
        ) {
            return DeflectTier.PERFECT;
        }
        return DeflectTier.NORMAL;
    }

    private void deflect(
        ServerLevel serverLevel,
        LivingEntity defender,
        DeflectTier tier
    ) {
        Vec3 lookDirection = defender.getViewVector(1.0F).normalize();
        Vec3 deflectDirection = lookDirection;

        if (tier == DeflectTier.PERFECT) {
            Entity attacker = this.getOwner();
            if (attacker instanceof LivingEntity && attacker.isAlive()) {
                Vec3 towardAttacker = attacker
                    .getEyePosition()
                    .subtract(defender.getEyePosition());
                if (towardAttacker.lengthSqr() > 1.0E-4D) {
                    deflectDirection = towardAttacker.normalize();
                }
            }
        }

        Vec3 spawnPosition = defender
            .getEyePosition()
            .add(deflectDirection.scale(0.6D));

        BlasterProjectileEntity deflectedProjectile =
            new BlasterProjectileEntity(serverLevel, defender);
        deflectedProjectile.setPos(
            spawnPosition.x,
            spawnPosition.y - 0.1D,
            spawnPosition.z
        );

        double speed = tier == DeflectTier.PERFECT
            ? Math.max(3.8D, this.getDeltaMovement().length() * 1.6D)
            : Math.max(2.5D, this.getDeltaMovement().length());
        double spreadStrength = tier == DeflectTier.PERFECT ? 0.02D : 0.08D;
        Vec3 spread = new Vec3(
            (defender.getRandom().nextFloat() - 0.5F) * spreadStrength,
            (defender.getRandom().nextFloat() - 0.5F) * spreadStrength,
            (defender.getRandom().nextFloat() - 0.5F) * spreadStrength
        );
        deflectedProjectile.setDeltaMovement(
            deflectDirection.scale(speed).add(spread)
        );

        if (tier == DeflectTier.PERFECT) {
            deflectedProjectile.setImpactProfile(10.0F, 4.0F);
        }

        serverLevel.addFreshEntity(deflectedProjectile);

        int sparks = tier == DeflectTier.PERFECT ? 20 : 10;
        serverLevel.sendParticles(
            ParticleTypes.ELECTRIC_SPARK,
            this.getX(),
            this.getY(),
            this.getZ(),
            sparks,
            0.08D,
            0.08D,
            0.08D,
            0.01D
        );
        serverLevel.playSound(
            null,
            defender.getX(),
            defender.getY(),
            defender.getZ(),
            LightsabersMod.LIGHTSABER_HIT,
            SoundSource.PLAYERS,
            tier == DeflectTier.PERFECT ? 1.0F : 0.85F,
            (tier == DeflectTier.PERFECT ? 1.35F : 1.15F) +
            defender.getRandom().nextFloat() *
            0.1F
        );
    }

    public BlasterProjectileEntity setImpactProfile(
        float damage,
        float fireSeconds
    ) {
        this.impactDamage = damage;
        this.impactFireSeconds = fireSeconds;
        return this;
    }
}
