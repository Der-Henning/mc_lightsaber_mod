package com.henning.lightsabers.combat;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.item.LightsaberItem;
import com.henning.lightsabers.network.SaberAttackPayload;
import com.henning.lightsabers.network.SaberSwingPayload;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class SaberCombat {
    public static final int SWING_COOLDOWN_TICKS = 6;
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(
        Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("lightsabers", "lightsaber")
    );

    private record Attack(Player player, InteractionHand hand, float strength) {
    }
    private static final ThreadLocal<Attack> CURRENT_ATTACK = new ThreadLocal<>();

    private SaberCombat() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(SaberAttackPayload.TYPE, SaberAttackPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SaberSwingPayload.TYPE, SaberSwingPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SaberAttackPayload.TYPE, (payload, context) ->
            attack(context.player(), payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
    }

    public static boolean isDualWielding(Player player) {
        return player.getMainHandItem().getItem() instanceof LightsaberItem
            && player.getOffhandItem().getItem() instanceof LightsaberItem;
    }

    public static CombatState.Action action(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? CombatState.Action.MAIN_HAND_SABER : CombatState.Action.OFF_HAND_SABER;
    }

    /** The packet contains only a hand; the server chooses the target and checks reach/occlusion. */
    public static boolean attack(Player player, InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel level) || !isDualWielding(player)
            || player.isSpectator() || !player.isAlive() || player.isUsingItem() || player.isSleeping()) {
            return false;
        }
        CombatState state = LightsabersMod.combatState(player);
        CombatState.Action action = action(hand);
        double speed = attributeValue(player, hand, Attributes.ATTACK_SPEED);
        float strength = state.attackStrength(action, player.tickCount, 20.0D / Math.max(0.1D, speed));
        if (!state.tryUse(action, player.tickCount, SWING_COOLDOWN_TICKS)) {
            return false;
        }

        ItemStack weapon = player.getItemInHand(hand);
        SaberSwing.start(player, hand);
        level.playSound(player, player.getX(), player.getY(), player.getZ(),
            LightsabersMod.LIGHTSABER_SWING, SoundSource.PLAYERS, 0.85F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            for (ServerPlayer observer : PlayerLookup.tracking(serverPlayer)) {
                if (observer != player && ServerPlayNetworking.canSend(observer, SaberSwingPayload.TYPE)) {
                    ServerPlayNetworking.send(observer, new SaberSwingPayload(player.getId(), hand == InteractionHand.MAIN_HAND));
                }
            }
        }

        AttackRange range = weapon.getOrDefault(DataComponents.ATTACK_RANGE, AttackRange.defaultFor(player));
        var hit = range.getClosesetHit(player, 1.0F,
            entity -> entity != player && entity.isAlive() && entity.isPickable() && !entity.isSpectator());
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            AttackBlockCallback.EVENT.invoker().interact(player, level, hand, blockHit.getBlockPos(), blockHit.getDirection());
            return true;
        }
        if (hit instanceof EntityHitResult entityHit
            && player.isWithinAttackRange(weapon, entityHit.getEntity().getBoundingBox(), 0.0D)
            && AttackEntityCallback.EVENT.invoker().interact(player, level, hand, entityHit.getEntity(), entityHit) == InteractionResult.PASS) {
            Attack previous = CURRENT_ATTACK.get();
            CURRENT_ATTACK.set(new Attack(player, hand, strength));
            try {
                // Keep vanilla damage, enchantments, PvP checks, hit effects and item durability.
                player.attack(entityHit.getEntity());
            } finally {
                if (previous == null) CURRENT_ATTACK.remove();
                else CURRENT_ATTACK.set(previous);
            }
        }
        return true;
    }

    public static InteractionHand currentHand(Player player) {
        Attack attack = CURRENT_ATTACK.get();
        return attack != null && attack.player() == player ? attack.hand() : null;
    }

    public static float attackStrength(Player player, float original) {
        Attack attack = CURRENT_ATTACK.get();
        return attack != null && attack.player() == player ? attack.strength() : original;
    }

    public static DamageSource damageSource(Player player) {
        return new DamageSource(player.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DAMAGE_TYPE), player);
    }

    public static double attributeValue(Player player, InteractionHand hand, Holder<Attribute> attribute) {
        if (hand == InteractionHand.MAIN_HAND) return player.getAttributeValue(attribute);
        AttributeInstance current = player.getAttribute(attribute);
        if (current == null) return player.getAttributeValue(attribute);
        AttributeInstance virtual = new AttributeInstance(attribute, ignored -> {});
        virtual.replaceFrom(current);
        player.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
            if (type.equals(attribute)) virtual.removeModifier(modifier.id());
        });
        player.getOffhandItem().forEachModifier(EquipmentSlot.MAINHAND, (type, modifier) -> {
            if (type.equals(attribute)) virtual.addOrUpdateTransientModifier(modifier);
        });
        return virtual.getValue();
    }
}
