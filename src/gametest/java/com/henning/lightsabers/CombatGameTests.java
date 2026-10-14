package com.henning.lightsabers;

import com.henning.lightsabers.combat.SaberCombat;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class CombatGameTests {
    @GameTest
    public void bothHandsCanHitTheSameTargetIndependently(GameTestHelper helper) {
        Player player = armedPlayer(helper, true);
        LivingEntity target = target(helper, 2);
        aimAt(helper, player, target);
        float initialHealth = target.getHealth();
        helper.assertTrue(SaberCombat.attack(player, InteractionHand.MAIN_HAND), "Main-hand attack accepted");
        float mainDamage = initialHealth - target.getHealth();
        helper.assertTrue(mainDamage > 1.0F, "Main saber damages the aimed target");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1 && player.getOffhandItem().getDamageValue() == 0,
            "Main-hand click only wears the main saber");
        helper.assertTrue(SaberCombat.attack(player, InteractionHand.OFF_HAND), "Offhand has an independent cooldown");
        float offDamage = initialHealth - mainDamage - target.getHealth();
        helper.assertTrue(Math.abs(offDamage - mainDamage) < 0.01F, "Identical offhand saber deals its own full attack damage");
        helper.assertTrue(player.getOffhandItem().getDamageValue() == 1, "Only the selected offhand saber loses durability");
        helper.assertTrue(target.isOnFire(), "Both saber attacks ignite their target");
        helper.assertFalse(SaberCombat.attack(player, InteractionHand.OFF_HAND), "Repeated offhand packets are rate limited");
        helper.assertTrue(SaberCombat.currentHand(player) == null, "Attack context is cleared after the hit");
        helper.succeed();
    }

    @GameTest
    public void saberCannotHitThroughWallsOrWithoutTwoSabers(GameTestHelper helper) {
        Player player = armedPlayer(helper, true);
        LivingEntity target = target(helper, 2);
        aimAt(helper, player, target);
        helper.setBlock(2, 1, 2, Blocks.STONE);
        helper.setBlock(2, 2, 2, Blocks.STONE);
        helper.assertTrue(SaberCombat.attack(player, InteractionHand.OFF_HAND), "A blocked attack still swings");
        helper.assertTrue(target.getHealth() == 100.0F && player.getOffhandItem().getDamageValue() == 0,
            "A wall prevents target damage and weapon wear");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(LightsabersMod.BLASTER));
        helper.assertFalse(SaberCombat.attack(player, InteractionHand.OFF_HAND), "Blaster plus offhand saber cannot use dual controls");
        helper.succeed();
    }

    @GameTest
    public void identicalBlastersShootIndependently(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(LightsabersMod.BLASTER));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(LightsabersMod.BLASTER));
        var level = helper.getLevel();
        helper.assertTrue(LightsabersMod.BLASTER.use(level, player, InteractionHand.MAIN_HAND) == InteractionResult.CONSUME,
            "First main-hand shot succeeds");
        helper.assertTrue(LightsabersMod.BLASTER.use(level, player, InteractionHand.MAIN_HAND) == InteractionResult.PASS,
            "Cooling main blaster must allow vanilla to try the offhand");
        helper.assertTrue(LightsabersMod.BLASTER.use(level, player, InteractionHand.OFF_HAND) == InteractionResult.CONSUME,
            "Identical offhand blaster has an independent cooldown");
        helper.assertTrue(player.getMainHandItem().getDamageValue() == 1 && player.getOffhandItem().getDamageValue() == 1,
            "Each fired blaster loses exactly one durability");
        helper.succeed();
    }

    @GameTest
    public void jediTunicReducesFallDamageOnce(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(LightsabersMod.JEDI_TUNIC));
        player.hurtServer(helper.getLevel(), player.damageSources().fall(), 10.0F);
        helper.assertTrue(Math.abs(player.getHealth() - 17.6F) < 0.01F, "Tunic reduces 10 fall damage to 2.4");
        Player shortFall = helper.makeMockPlayer(GameType.SURVIVAL);
        shortFall.setItemSlot(EquipmentSlot.CHEST, new ItemStack(LightsabersMod.JEDI_TUNIC));
        shortFall.hurtServer(helper.getLevel(), shortFall.damageSources().fall(), 4.0F);
        helper.assertTrue(shortFall.getHealth() == 20.0F, "Short falls do not hurt while wearing the tunic");
        helper.succeed();
    }

    private static Player armedPlayer(GameTestHelper helper, boolean dual) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(LightsabersMod.CYAN_LIGHTSABER));
        if (dual) player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(LightsabersMod.CYAN_LIGHTSABER));
        return player;
    }

    private static LivingEntity target(GameTestHelper helper, int x) {
        LivingEntity entity = helper.spawnWithNoFreeWill(EntityTypes.COW, x, 1, 1);
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
        entity.setHealth(100.0F);
        return entity;
    }

    private static void aimAt(GameTestHelper helper, Player player, LivingEntity target) {
        for (int tick = 0; tick < 20; tick++) player.tick();
        player.setPos(target.position().add(0.0D, 0.0D, 2.0D));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getBoundingBox().getCenter());
        player.setYHeadRot(player.getYRot());
    }
}
