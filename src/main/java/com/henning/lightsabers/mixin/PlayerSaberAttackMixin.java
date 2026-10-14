package com.henning.lightsabers.mixin;

import com.henning.lightsabers.combat.SaberCombat;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerSaberAttackMixin {
    @Inject(method = "createAttackSource", at = @At("HEAD"), cancellable = true)
    private void lightsabers$independentDamageSource(ItemStack weapon, CallbackInfoReturnable<DamageSource> cir) {
        Player player = (Player) (Object) this;
        if (SaberCombat.currentHand(player) != null) cir.setReturnValue(SaberCombat.damageSource(player));
    }

    @Inject(method = "getWeaponItem", at = @At("HEAD"), cancellable = true)
    private void lightsabers$selectedWeapon(CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) (Object) this;
        InteractionHand hand = SaberCombat.currentHand(player);
        if (hand != null) cir.setReturnValue(player.getItemInHand(hand));
    }

    @Inject(method = "getAttackStrengthScale", at = @At("HEAD"), cancellable = true)
    private void lightsabers$independentStrength(float partialTick, CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (SaberCombat.currentHand(player) != null) cir.setReturnValue(SaberCombat.attackStrength(player, 1.0F));
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double lightsabers$selectedDamage(double original) {
        Player player = (Player) (Object) this;
        InteractionHand hand = SaberCombat.currentHand(player);
        return hand == null ? original : SaberCombat.attributeValue(player, hand, Attributes.ATTACK_DAMAGE);
    }
}
