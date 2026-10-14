package com.henning.lightsabers.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SaberAttackPayload(boolean mainHand) implements CustomPacketPayload {
    public static final Type<SaberAttackPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("lightsabers", "saber_attack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaberAttackPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, SaberAttackPayload::mainHand, SaberAttackPayload::new
    );

    @Override
    public Type<SaberAttackPayload> type() {
        return TYPE;
    }
}
