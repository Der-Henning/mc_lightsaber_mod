package com.henning.lightsabers.mixin;

import com.henning.lightsabers.combat.SaberCombat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ItemStack.class)
public abstract class SaberDurabilityMixin {
    @ModifyArgs(method = "postHurtEnemy", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"))
    private void lightsabers$correctBreakSlot(Args args) {
        if (args.get(1) instanceof Player player) {
            InteractionHand hand = SaberCombat.currentHand(player);
            if (hand != null && (Object) this == player.getItemInHand(hand)) args.set(2, hand.asEquipmentSlot());
        }
    }
}
