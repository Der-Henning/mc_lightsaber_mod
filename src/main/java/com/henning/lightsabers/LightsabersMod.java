package com.henning.lightsabers;

import com.henning.lightsabers.entity.BlasterProjectileEntity;
import com.henning.lightsabers.item.BlasterItem;
import com.henning.lightsabers.item.LightsaberItem;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

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
            .buildAndRegister(Identifier.of(MOD_ID, "lightsaber_ignites_wood"));
    private static final ThreadLocal<Boolean> FALL_DAMAGE_REDUCTION_ACTIVE =
        ThreadLocal.withInitial(() -> false);
    private static final float JEDI_TUNIC_EXTRA_SAFE_FALL_DAMAGE = 4.0F;
    private static final float JEDI_TUNIC_FALL_DAMAGE_MULTIPLIER = 0.4F;
    private static final float DUAL_WIELD_BONUS_DAMAGE = 4.0F;
    private static final float DUAL_WIELD_BONUS_FIRE_SECONDS = 2.0F;
    private static final int DUAL_WIELD_OFFHAND_COOLDOWN_TICKS = 6;
    private static final AtomicInteger OFFHAND_STRIKE_DEBUG_COUNTER =
        new AtomicInteger();
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
    public static final RegistryKey<EquipmentAsset> JEDI_ARMOR_ASSET =
        RegistryKey.of(
            EquipmentAssetKeys.REGISTRY_KEY,
            Identifier.of(MOD_ID, "jedi")
        );
    public static final ArmorMaterial JEDI_ARMOR_MATERIAL = new ArmorMaterial(
        22,
        Map.of(
            EquipmentType.HELMET,
            2,
            EquipmentType.CHESTPLATE,
            5,
            EquipmentType.LEGGINGS,
            4,
            EquipmentType.BOOTS,
            2
        ),
        18,
        SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        ItemTags.REPAIRS_LEATHER_ARMOR,
        JEDI_ARMOR_ASSET
    );
    public static final Item JEDI_HOOD = registerArmor(
        "jedi_hood",
        EquipmentType.HELMET
    );
    public static final Item JEDI_TUNIC = registerArmor(
        "jedi_tunic",
        EquipmentType.CHESTPLATE
    );
    public static final Item JEDI_PANTS = registerArmor(
        "jedi_pants",
        EquipmentType.LEGGINGS
    );
    public static final Item JEDI_BOOTS = registerArmor(
        "jedi_boots",
        EquipmentType.BOOTS
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

    public static final ItemGroup LIGHTSABERS_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        Identifier.of(MOD_ID, "main"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.lightsabers.main"))
            .icon(() -> new ItemStack(CYAN_LIGHTSABER))
            .entries((displayContext, entries) -> {
                ALL_WEAPONS.forEach(entries::add);
                ALL_JEDI_ARMOR.forEach(entries::add);
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
                if (world.isClient() || player.isSpectator()) {
                    return ActionResult.PASS;
                }
                if (!(world instanceof ServerWorld serverWorld)) {
                    return ActionResult.PASS;
                }

                if (
                    !(player.getStackInHand(hand).getItem() instanceof
                            LightsaberItem)
                ) {
                    return ActionResult.PASS;
                }
                if (
                    !serverWorld
                        .getGameRules()
                        .getValue(LIGHTSABER_IGNITES_WOOD_RULE)
                ) {
                    return ActionResult.PASS;
                }

                BlockState hitState = serverWorld.getBlockState(pos);
                if (!isWoodBlock(hitState)) {
                    return ActionResult.PASS;
                }

                BlockPos firePos = pos.offset(direction);
                if (
                    tryIgnite(serverWorld, firePos) ||
                    tryIgnite(serverWorld, pos.up())
                ) {
                    serverWorld.playSound(
                        null,
                        pos,
                        SoundEvents.ITEM_FLINTANDSTEEL_USE,
                        SoundCategory.BLOCKS,
                        0.8F,
                        1.0F + serverWorld.getRandom().nextFloat() * 0.2F
                    );
                }

                return ActionResult.PASS;
            }
        );
    }

    private static boolean isWoodBlock(BlockState state) {
        return (
            state.isIn(BlockTags.LOGS) ||
            state.isIn(BlockTags.PLANKS) ||
            state.isIn(BlockTags.WOODEN_SLABS) ||
            state.isIn(BlockTags.WOODEN_STAIRS) ||
            state.isIn(BlockTags.WOODEN_FENCES) ||
            state.isIn(BlockTags.WOODEN_DOORS) ||
            state.isIn(BlockTags.WOODEN_TRAPDOORS) ||
            state.isIn(BlockTags.WOODEN_PRESSURE_PLATES) ||
            state.isIn(BlockTags.WOODEN_BUTTONS)
        );
    }

    private static boolean tryIgnite(
        net.minecraft.world.World world,
        BlockPos firePos
    ) {
        if (!world.getBlockState(firePos).isAir()) {
            return false;
        }

        BlockState fireState = AbstractFireBlock.getState(world, firePos);
        if (!fireState.canPlaceAt(world, firePos)) {
            return false;
        }
        return world.setBlockState(firePos, fireState, Block.NOTIFY_ALL);
    }

    private static void registerJediRobeFallDamageReduction() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
            (entity, source, amount) -> {
                if (FALL_DAMAGE_REDUCTION_ACTIVE.get()) {
                    return true;
                }
                if (!(entity instanceof PlayerEntity player)) {
                    return true;
                }
                if (
                    !(entity.getEntityWorld() instanceof
                            ServerWorld serverWorld)
                ) {
                    return true;
                }
                if (!source.isOf(DamageTypes.FALL)) {
                    return true;
                }
                if (
                    player
                        .getEquippedStack(
                            net.minecraft.entity.EquipmentSlot.CHEST
                        )
                        .getItem() !=
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
                    entity.damage(serverWorld, source, reducedAmount);
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
            attacker instanceof PlayerEntity player &&
            !(player.getMainHandStack().getItem() instanceof LightsaberItem)
        ) {
            return;
        }
        if (attacker.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(
                null,
                attacker.getX(),
                attacker.getY(),
                attacker.getZ(),
                LIGHTSABER_HIT,
                SoundCategory.PLAYERS,
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
        if (!(attacker instanceof PlayerEntity player)) {
            return;
        }
        if (!(player.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        if (!(player.getMainHandStack().getItem() instanceof LightsaberItem)) {
            return;
        }

        net.minecraft.item.ItemStack offhandStack = player.getOffHandStack();
        if (!(offhandStack.getItem() instanceof LightsaberItem)) {
            return;
        }
        if (player.getItemCooldownManager().isCoolingDown(offhandStack)) {
            return;
        }

        player
            .getItemCooldownManager()
            .set(offhandStack, DUAL_WIELD_OFFHAND_COOLDOWN_TICKS);
        if (!player.getAbilities().creativeMode) {
            offhandStack.damage(1, player, Hand.OFF_HAND);
        }

        if (
            target.damage(
                serverWorld,
                player.getDamageSources().playerAttack(player),
                DUAL_WIELD_BONUS_DAMAGE
            )
        ) {
            target.setOnFireFor(DUAL_WIELD_BONUS_FIRE_SECONDS);
        }
    }

    private static Item registerLightsaber(String id) {
        RegistryKey<Item> key = RegistryKey.of(
            Registries.ITEM.getKey(),
            Identifier.of(MOD_ID, id)
        );
        return Registry.register(
            Registries.ITEM,
            key,
            new LightsaberItem(
                new Item.Settings()
                    .registryKey(key)
                    .maxCount(1)
                    .maxDamage(2048)
                    .fireproof()
                    .rarity(Rarity.RARE)
                    .sword(ToolMaterial.NETHERITE, 8.0F, -2.2F)
            )
        );
    }

    private static Item registerBlaster(String id) {
        RegistryKey<Item> key = RegistryKey.of(
            Registries.ITEM.getKey(),
            Identifier.of(MOD_ID, id)
        );
        return Registry.register(
            Registries.ITEM,
            key,
            new BlasterItem(
                new Item.Settings()
                    .registryKey(key)
                    .maxCount(1)
                    .maxDamage(465)
                    .rarity(Rarity.UNCOMMON)
            )
        );
    }

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of(MOD_ID, id);
        return Registry.register(
            Registries.SOUND_EVENT,
            identifier,
            SoundEvent.of(identifier)
        );
    }

    private static Item registerArmor(String id, EquipmentType equipmentType) {
        RegistryKey<Item> key = RegistryKey.of(
            Registries.ITEM.getKey(),
            Identifier.of(MOD_ID, id)
        );
        return Registry.register(
            Registries.ITEM,
            key,
            new Item(
                new Item.Settings()
                    .registryKey(key)
                    .maxCount(1)
                    .armor(JEDI_ARMOR_MATERIAL, equipmentType)
            ) {
                @Override
                public void appendTooltip(
                    ItemStack stack,
                    TooltipContext context,
                    TooltipDisplayComponent displayComponent,
                    Consumer<Text> textConsumer,
                    TooltipType type
                ) {
                    if (equipmentType == EquipmentType.CHESTPLATE) {
                        textConsumer.accept(
                            Text.translatable(
                                "item.lightsabers.jedi_armor.fall_notice"
                            ).formatted(Formatting.AQUA)
                        );
                    }
                }
            }
        );
    }

    private static EntityType<
        BlasterProjectileEntity
    > registerBlasterProjectile(String id) {
        Identifier identifier = Identifier.of(MOD_ID, id);
        RegistryKey<EntityType<?>> key = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE,
            identifier
        );
        return Registry.register(
            Registries.ENTITY_TYPE,
            identifier,
            EntityType.Builder.<BlasterProjectileEntity>create(
                BlasterProjectileEntity::new,
                SpawnGroup.MISC
            )
                .dropsNothing()
                .dimensions(0.25F, 0.25F)
                .maxTrackingRange(4)
                .trackingTickInterval(10)
                .build(key)
        );
    }
}
