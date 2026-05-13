package com.henning.lightsabers;

import com.henning.lightsabers.entity.BlasterProjectileEntity;
import com.henning.lightsabers.item.BlasterItem;
import com.henning.lightsabers.item.LightsaberItem;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightsabersMod implements ModInitializer {

    public static final String MOD_ID = "lightsabers";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final SoundEvent LIGHTSABER_SWING = registerSound(
        "lightsaber.swing"
    );
    public static final SoundEvent LIGHTSABER_HUM = registerSound(
        "lightsaber.hum"
    );
    public static final SoundEvent LIGHTSABER_HIT = registerSound(
        "lightsaber.hit"
    );
    public static final SoundEvent BLASTER_SHOOT = registerSound(
        "blaster.shoot"
    );

    public static final GameRule<Boolean> LIGHTSABER_IGNITES_WOOD_RULE =
        GameRuleBuilder.forBoolean(false)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(id("lightsaber_ignites_wood"));

    private static final ThreadLocal<Boolean> FALL_DAMAGE_REDUCTION_ACTIVE =
        ThreadLocal.withInitial(() -> false);
    private static final float JEDI_TUNIC_EXTRA_SAFE_FALL_DAMAGE = 4.0F;
    private static final float JEDI_TUNIC_FALL_DAMAGE_MULTIPLIER = 0.4F;
    private static final float DUAL_WIELD_BONUS_DAMAGE = 4.0F;
    private static final float DUAL_WIELD_BONUS_FIRE_SECONDS = 2.0F;
    private static final int DUAL_WIELD_OFFHAND_COOLDOWN_TICKS = 6;

    public static final EntityType<BlasterProjectileEntity> BLASTER_PROJECTILE =
        registerBlasterProjectile("blaster_projectile");

    public static final Item BLASTER = registerBlaster("blaster");
    public static final Item CYAN_LIGHTSABER = registerLightsaber("lightsaber");
    public static final Item BLUE_LIGHTSABER = registerLightsaber(
        "blue_lightsaber"
    );
    public static final Item GREEN_LIGHTSABER = registerLightsaber(
        "green_lightsaber"
    );
    public static final Item RED_LIGHTSABER = registerLightsaber(
        "red_lightsaber"
    );
    public static final Item PURPLE_LIGHTSABER = registerLightsaber(
        "purple_lightsaber"
    );
    public static final Item YELLOW_LIGHTSABER = registerLightsaber(
        "yellow_lightsaber"
    );

    public static final ResourceKey<EquipmentAsset> JEDI_ARMOR_ASSET =
        ResourceKey.create(EquipmentAssets.ROOT_ID, id("jedi"));
    public static final ArmorMaterial JEDI_ARMOR_MATERIAL = new ArmorMaterial(
        22,
        Map.of(
            ArmorType.HELMET,
            2,
            ArmorType.CHESTPLATE,
            5,
            ArmorType.LEGGINGS,
            4,
            ArmorType.BOOTS,
            2
        ),
        18,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        ItemTags.REPAIRS_LEATHER_ARMOR,
        JEDI_ARMOR_ASSET
    );

    public static final Item JEDI_HOOD = registerArmor(
        "jedi_hood",
        ArmorType.HELMET
    );
    public static final Item JEDI_TUNIC = registerArmor(
        "jedi_tunic",
        ArmorType.CHESTPLATE
    );
    public static final Item JEDI_PANTS = registerArmor(
        "jedi_pants",
        ArmorType.LEGGINGS
    );
    public static final Item JEDI_BOOTS = registerArmor(
        "jedi_boots",
        ArmorType.BOOTS
    );

    public static final List<Item> ALL_LIGHTSABERS = List.of(
        CYAN_LIGHTSABER,
        BLUE_LIGHTSABER,
        GREEN_LIGHTSABER,
        RED_LIGHTSABER,
        PURPLE_LIGHTSABER,
        YELLOW_LIGHTSABER
    );
    public static final List<Item> ALL_WEAPONS = List.of(
        BLASTER,
        CYAN_LIGHTSABER,
        BLUE_LIGHTSABER,
        GREEN_LIGHTSABER,
        RED_LIGHTSABER,
        PURPLE_LIGHTSABER,
        YELLOW_LIGHTSABER
    );
    public static final List<Item> ALL_JEDI_ARMOR = List.of(
        JEDI_HOOD,
        JEDI_TUNIC,
        JEDI_PANTS,
        JEDI_BOOTS
    );

    public static final CreativeModeTab LIGHTSABERS_GROUP = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        id("main"),
        FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.lightsabers.main"))
            .icon(() -> new ItemStack(CYAN_LIGHTSABER))
            .displayItems((displayContext, entries) -> {
                ALL_WEAPONS.forEach(entries::accept);
                ALL_JEDI_ARMOR.forEach(entries::accept);
            })
            .build()
    );

    @Override
    public void onInitialize() {
        registerLightsaberBlockIgnition();
        registerJediRobeFallDamageReduction();
        LOGGER.info("Initializing {}", MOD_ID);
    }

    private static void registerLightsaberBlockIgnition() {
        AttackBlockCallback.EVENT.register(
            (player, world, hand, pos, direction) -> {
                if (world.isClientSide() || player.isSpectator()) {
                    return InteractionResult.PASS;
                }
                if (!(world instanceof ServerLevel serverLevel)) {
                    return InteractionResult.PASS;
                }

                if (
                    !(player.getItemInHand(hand).getItem() instanceof
                            LightsaberItem)
                ) {
                    return InteractionResult.PASS;
                }
                if (
                    !serverLevel
                        .getGameRules()
                        .get(LIGHTSABER_IGNITES_WOOD_RULE)
                ) {
                    return InteractionResult.PASS;
                }

                BlockState hitState = serverLevel.getBlockState(pos);
                if (!isWoodBlock(hitState)) {
                    return InteractionResult.PASS;
                }

                BlockPos firePos = pos.relative(direction);
                if (
                    tryIgnite(serverLevel, firePos) ||
                    tryIgnite(serverLevel, pos.above())
                ) {
                    serverLevel.playSound(
                        null,
                        pos,
                        SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.0F + serverLevel.getRandom().nextFloat() * 0.2F
                    );
                }

                return InteractionResult.PASS;
            }
        );
    }

    private static boolean isWoodBlock(BlockState state) {
        return (
            state.is(BlockTags.LOGS, ignored -> true) ||
            state.is(BlockTags.PLANKS, ignored -> true) ||
            state.is(BlockTags.WOODEN_SLABS, ignored -> true) ||
            state.is(BlockTags.WOODEN_STAIRS, ignored -> true) ||
            state.is(BlockTags.WOODEN_FENCES, ignored -> true) ||
            state.is(BlockTags.WOODEN_DOORS, ignored -> true) ||
            state.is(BlockTags.WOODEN_TRAPDOORS, ignored -> true) ||
            state.is(BlockTags.WOODEN_PRESSURE_PLATES, ignored -> true) ||
            state.is(BlockTags.WOODEN_BUTTONS, ignored -> true)
        );
    }

    private static boolean tryIgnite(Level level, BlockPos firePos) {
        if (!level.getBlockState(firePos).isAir()) {
            return false;
        }

        BlockState fireState = BaseFireBlock.getState(level, firePos);
        if (!fireState.canSurvive(level, firePos)) {
            return false;
        }
        return level.setBlock(firePos, fireState, Block.UPDATE_ALL);
    }

    private static void registerJediRobeFallDamageReduction() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
            (entity, source, amount) -> {
                if (FALL_DAMAGE_REDUCTION_ACTIVE.get()) {
                    return true;
                }
                if (!(entity instanceof Player player)) {
                    return true;
                }
                if (!(entity.level() instanceof ServerLevel serverLevel)) {
                    return true;
                }
                if (!source.is(DamageTypes.FALL)) {
                    return true;
                }
                if (
                    player.getItemBySlot(EquipmentSlot.CHEST).getItem() !=
                    JEDI_TUNIC
                ) {
                    return true;
                }

                float adjustedAmount = Math.max(
                    0.0F,
                    amount - JEDI_TUNIC_EXTRA_SAFE_FALL_DAMAGE
                );
                float reducedAmount =
                    adjustedAmount * JEDI_TUNIC_FALL_DAMAGE_MULTIPLIER;
                if (reducedAmount <= 0.0F) {
                    return false;
                }

                FALL_DAMAGE_REDUCTION_ACTIVE.set(true);
                try {
                    entity.hurtServer(serverLevel, source, reducedAmount);
                } finally {
                    FALL_DAMAGE_REDUCTION_ACTIVE.set(false);
                }
                return false;
            }
        );
    }

    public static void handleLightsaberAttack(
        LivingEntity attacker,
        LivingEntity target
    ) {
        if (
            attacker instanceof Player player &&
            !(player.getMainHandItem().getItem() instanceof LightsaberItem)
        ) {
            return;
        }
        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                null,
                attacker.getX(),
                attacker.getY(),
                attacker.getZ(),
                LIGHTSABER_HIT,
                SoundSource.PLAYERS,
                0.85F,
                0.98F + attacker.getRandom().nextFloat() * 0.08F
            );
        }
        tryDualWieldOffhandStrike(attacker, target);
    }

    public static void tryDualWieldOffhandStrike(
        LivingEntity attacker,
        LivingEntity target
    ) {
        if (!(attacker instanceof Player player)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof LightsaberItem)) {
            return;
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (!(offhandStack.getItem() instanceof LightsaberItem)) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(offhandStack)) {
            return;
        }

        player
            .getCooldowns()
            .addCooldown(offhandStack, DUAL_WIELD_OFFHAND_COOLDOWN_TICKS);
        if (!player.getAbilities().instabuild) {
            offhandStack.hurtAndBreak(1, player, InteractionHand.OFF_HAND);
        }

        if (
            target.hurtServer(
                serverLevel,
                player.damageSources().playerAttack(player),
                DUAL_WIELD_BONUS_DAMAGE
            )
        ) {
            target.igniteForSeconds(DUAL_WIELD_BONUS_FIRE_SECONDS);
        }
    }

    private static Item registerLightsaber(String itemId) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(itemId));
        return Registry.register(
            BuiltInRegistries.ITEM,
            key,
            new LightsaberItem(
                new Item.Properties()
                    .setId(key)
                    .stacksTo(1)
                    .durability(2048)
                    .fireResistant()
                    .rarity(Rarity.RARE)
                    .sword(ToolMaterial.NETHERITE, 8.0F, -2.2F)
            )
        );
    }

    private static Item registerBlaster(String itemId) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(itemId));
        return Registry.register(
            BuiltInRegistries.ITEM,
            key,
            new BlasterItem(
                new Item.Properties()
                    .setId(key)
                    .stacksTo(1)
                    .durability(465)
                    .rarity(Rarity.UNCOMMON)
            )
        );
    }

    private static SoundEvent registerSound(String soundId) {
        Identifier identifier = id(soundId);
        return Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            identifier,
            SoundEvent.createVariableRangeEvent(identifier)
        );
    }

    private static Item registerArmor(String itemId, ArmorType armorType) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(itemId));
        return Registry.register(
            BuiltInRegistries.ITEM,
            key,
            new Item(
                new Item.Properties()
                    .setId(key)
                    .stacksTo(1)
                    .humanoidArmor(JEDI_ARMOR_MATERIAL, armorType)
            ) {
                @Override
                public void appendHoverText(
                    ItemStack stack,
                    TooltipContext context,
                    TooltipDisplay displayComponent,
                    Consumer<Component> textConsumer,
                    TooltipFlag type
                ) {
                    if (armorType == ArmorType.CHESTPLATE) {
                        textConsumer.accept(
                            Component.translatable(
                                "item.lightsabers.jedi_armor.fall_notice"
                            ).withStyle(ChatFormatting.AQUA)
                        );
                    }
                }
            }
        );
    }

    private static EntityType<
        BlasterProjectileEntity
    > registerBlasterProjectile(String entityId) {
        Identifier identifier = id(entityId);
        ResourceKey<EntityType<?>> key = ResourceKey.create(
            Registries.ENTITY_TYPE,
            identifier
        );
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            key,
            EntityType.Builder.<BlasterProjectileEntity>of(
                BlasterProjectileEntity::new,
                MobCategory.MISC
            )
                .noLootTable()
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(key)
        );
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
