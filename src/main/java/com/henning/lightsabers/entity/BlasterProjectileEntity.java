package com.henning.lightsabers.entity;

import com.henning.lightsabers.LightsabersMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class BlasterProjectileEntity extends ThrownItemEntity {
    private int lifeTicks;

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
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            if (entityHitResult.getEntity().damage(serverWorld, this.getDamageSources().thrown(this, this.getOwner()), 6.0F)) {
                if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                    livingEntity.setOnFireFor(2.0F);
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
}
