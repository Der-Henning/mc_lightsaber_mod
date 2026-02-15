package com.henning.lightsabers.item;

import com.henning.lightsabers.LightsabersMod;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class LightsaberItem extends Item {
    public LightsaberItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setOnFireFor(4.0F);
        attacker.getEntityWorld().playSound(
                null,
                attacker.getX(),
                attacker.getY(),
                attacker.getZ(),
                LightsabersMod.LIGHTSABER_HIT,
                SoundCategory.PLAYERS,
                0.8F,
                0.95F + attacker.getRandom().nextFloat() * 0.1F
        );
        super.postHit(stack, target, attacker);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            world.playSound(
                    null,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    LightsabersMod.LIGHTSABER_ACTIVATE,
                    SoundCategory.PLAYERS,
                    0.85F,
                    1.0F
            );
            user.getItemCooldownManager().set(user.getStackInHand(hand), 8);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        textConsumer.accept(Text.translatable("item.lightsabers.lightsaber.tooltip").formatted(Formatting.AQUA));
        textConsumer.accept(Text.translatable("item.lightsabers.weapon.fire_notice").formatted(Formatting.GOLD));
    }
}
