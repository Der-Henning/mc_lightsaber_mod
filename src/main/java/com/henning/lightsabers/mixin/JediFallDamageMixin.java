package com.henning.lightsabers.mixin;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.combat.JediProtection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class JediFallDamageMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float lightsabers$reduceFallDamage(float amount, ServerLevel level, DamageSource source, float originalAmount) {
        if ((Object) this instanceof Player player
            && source.is(DamageTypes.FALL)
            && player.getItemBySlot(EquipmentSlot.CHEST).is(LightsabersMod.JEDI_TUNIC)) {
            return JediProtection.reduceFallDamage(amount);
        }
        return amount;
    }
}
