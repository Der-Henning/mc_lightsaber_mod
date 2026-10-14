package com.henning.lightsabers.client;

import com.henning.lightsabers.LightsabersMod;
import com.henning.lightsabers.item.BlasterItem;
import com.henning.lightsabers.item.LightsaberItem;
import java.util.List;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class WeaponTooltips {
    private WeaponTooltips() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof LightsaberItem) {
                append(lines, "item.lightsabers.lightsaber.tooltip", ChatFormatting.AQUA);
                append(lines, "item.lightsabers.weapon.fire_notice", ChatFormatting.GOLD);
                append(lines, "item.lightsabers.lightsaber.deflect_notice", ChatFormatting.GRAY);
                append(lines, "item.lightsabers.lightsaber.mastery_notice", ChatFormatting.LIGHT_PURPLE);
                append(lines, "item.lightsabers.lightsaber.dual_wield_notice", ChatFormatting.RED);
                append(lines, "item.lightsabers.lightsaber.force_jump_notice", ChatFormatting.DARK_AQUA);
            } else if (stack.getItem() instanceof BlasterItem) {
                append(lines, "item.lightsabers.weapon.fire_notice", ChatFormatting.GOLD);
            } else if (stack.is(LightsabersMod.JEDI_TUNIC)) {
                append(lines, "item.lightsabers.jedi_armor.fall_notice", ChatFormatting.AQUA);
            }
        });
    }

    private static void append(List<Component> lines, String key, ChatFormatting color) {
        lines.add(Component.translatable(key).withStyle(color));
    }
}
