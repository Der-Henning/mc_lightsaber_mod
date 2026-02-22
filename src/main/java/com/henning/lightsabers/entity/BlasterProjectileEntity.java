package com.henning.lightsabers.entity;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.item.LightsaberItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlasterProjectileEntity extends ThrownItemEntity {
    private int lifeTicks;
    private float impactDamage = 6.0F;
    private float impactFireSeconds = 2.0F;

    private enum DeflectTier {
        NONE,
        NORMAL,
        PERFECT
    }

    public BlasterProjectileEntity(EntityType<? extends BlasterProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public BlasterProjectileEntity(World world, LivingEntity owner) {
        super(LightsabersMod.BLASTER_PROJECTILE, owner, world, new ItemStack(Items.FIRE_CHARGE));
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (lifeTicks > 50) {
            this.discard();
        }
    }

    @Override
    protected double getGravity() {
        return 0.0;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && this.getEntityWorld() instanceof ServerWorld serverWorld) {
            DeflectTier deflectTier = getDeflectTier(livingEntity, serverWorld);
            if (deflectTier != DeflectTier.NONE) {
                deflect(serverWorld, livingEntity, deflectTier);
                this.discard();
                return;
            }
        }

        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            if (entityHitResult.getEntity().damage(serverWorld, this.getDamageSources().thrown(this, this.getOwner()), impactDamage)) {
                if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                    livingEntity.setOnFireFor(impactFireSeconds);
                }
                this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.2F, 1.7F);
            }
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.discard();
    }

    private DeflectTier getDeflectTier(LivingEntity defender, ServerWorld serverWorld) {
        if (!defender.isSneaking()) {
            return DeflectTier.NONE;
        }

        boolean hasLightsaber = defender.getMainHandStack().getItem() instanceof LightsaberItem
                || defender.getOffHandStack().getItem() instanceof LightsaberItem;
        if (!hasLightsaber || this.getVelocity().lengthSquared() < 1.0E-4) {
            return DeflectTier.NONE;
        }

        Vec3d incomingDirection = this.getVelocity().normalize().multiply(-1.0);
        Vec3d lookDirection = defender.getRotationVec(1.0F).normalize();
        if (lookDirection.dotProduct(incomingDirection) <= 0.35D) {
            return DeflectTier.NONE;
        }

        if (defender instanceof PlayerEntity player && LightsaberItem.hasPerfectDeflectWindow(player, serverWorld)) {
            return DeflectTier.PERFECT;
        }
        return DeflectTier.NORMAL;
    }

    private void deflect(ServerWorld serverWorld, LivingEntity defender, DeflectTier tier) {
        Vec3d lookDirection = defender.getRotationVec(1.0F).normalize();
        Vec3d deflectDirection = lookDirection;

        if (tier == DeflectTier.PERFECT) {
            Entity attacker = this.getOwner();
            if (attacker instanceof LivingEntity && attacker.isAlive()) {
                Vec3d towardAttacker = attacker.getEyePos().subtract(defender.getEyePos());
                if (towardAttacker.lengthSquared() > 1.0E-4D) {
                    deflectDirection = towardAttacker.normalize();
                }
            }
        }

        Vec3d spawnPosition = defender.getEyePos().add(deflectDirection.multiply(0.6D));

        BlasterProjectileEntity deflectedProjectile = new BlasterProjectileEntity(serverWorld, defender);
        deflectedProjectile.setPosition(spawnPosition.x, spawnPosition.y - 0.1D, spawnPosition.z);

        double speed = tier == DeflectTier.PERFECT
                ? Math.max(3.8D, this.getVelocity().length() * 1.6D)
                : Math.max(2.5D, this.getVelocity().length());
        double spreadStrength = tier == DeflectTier.PERFECT ? 0.02F : 0.08F;
        Vec3d spread = new Vec3d(
                (defender.getRandom().nextFloat() - 0.5F) * spreadStrength,
                (defender.getRandom().nextFloat() - 0.5F) * spreadStrength,
                (defender.getRandom().nextFloat() - 0.5F) * spreadStrength
        );
        deflectedProjectile.setVelocity(deflectDirection.multiply(speed).add(spread));

        if (tier == DeflectTier.PERFECT) {
            deflectedProjectile.setImpactProfile(10.0F, 4.0F);
        }

        serverWorld.spawnEntity(deflectedProjectile);

        int sparks = tier == DeflectTier.PERFECT ? 20 : 10;
        serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(), this.getZ(), sparks, 0.08, 0.08, 0.08, 0.01);
        serverWorld.playSound(
                null,
                defender.getX(),
                defender.getY(),
                defender.getZ(),
                LightsabersMod.LIGHTSABER_HIT,
                SoundCategory.PLAYERS,
                tier == DeflectTier.PERFECT ? 1.0F : 0.85F,
                (tier == DeflectTier.PERFECT ? 1.35F : 1.15F) + defender.getRandom().nextFloat() * 0.1F
        );
    }

    public BlasterProjectileEntity setImpactProfile(float damage, float fireSeconds) {
        this.impactDamage = damage;
        this.impactFireSeconds = fireSeconds;
        return this;
    }
}
