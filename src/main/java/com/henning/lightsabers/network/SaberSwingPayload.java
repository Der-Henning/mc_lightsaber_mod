package com.henning.lightsabers.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SaberSwingPayload(int playerId, boolean mainHand) implements CustomPacketPayload {
    public static final Type<SaberSwingPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("lightsabers", "saber_swing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SaberSwingPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SaberSwingPayload::playerId,
        ByteBufCodecs.BOOL, SaberSwingPayload::mainHand, SaberSwingPayload::new
    );

    @Override
    public Type<SaberSwingPayload> type() {
        return TYPE;
    }
}
