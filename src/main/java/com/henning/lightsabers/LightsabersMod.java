package com.henning.lightsabers;

import com.henning.lightsabers.entity.BlasterProjectileEntity;
import com.henning.lightsabers.item.BlasterItem;
import com.henning.lightsabers.item.LightsaberItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class LightsabersMod implements ModInitializer {
    public static final String MOD_ID = "lightsabers";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final SoundEvent LIGHTSABER_ACTIVATE = registerSound("lightsaber.activate");
    public static final SoundEvent LIGHTSABER_HIT = registerSound("lightsaber.hit");
    public static final SoundEvent BLASTER_SHOOT = registerSound("blaster.shoot");
    public static final EntityType<BlasterProjectileEntity> BLASTER_PROJECTILE = registerBlasterProjectile("blaster_projectile");
    public static final Item BLASTER = registerBlaster("blaster");
    public static final Item CYAN_LIGHTSABER = registerLightsaber("lightsaber");
    public static final Item BLUE_LIGHTSABER = registerLightsaber("blue_lightsaber");
    public static final Item GREEN_LIGHTSABER = registerLightsaber("green_lightsaber");
    public static final Item RED_LIGHTSABER = registerLightsaber("red_lightsaber");
    public static final Item PURPLE_LIGHTSABER = registerLightsaber("purple_lightsaber");
    public static final Item YELLOW_LIGHTSABER = registerLightsaber("yellow_lightsaber");
    public static final RegistryKey<EquipmentAsset> JEDI_ARMOR_ASSET = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of(MOD_ID, "jedi"));
    public static final ArmorMaterial JEDI_ARMOR_MATERIAL = new ArmorMaterial(
            22,
            Map.of(
                    EquipmentType.HELMET, 2,
                    EquipmentType.CHESTPLATE, 5,
                    EquipmentType.LEGGINGS, 4,
                    EquipmentType.BOOTS, 2
            ),
            18,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            ItemTags.REPAIRS_LEATHER_ARMOR,
            JEDI_ARMOR_ASSET
    );
    public static final Item JEDI_HOOD = registerArmor("jedi_hood", EquipmentType.HELMET);
    public static final Item JEDI_TUNIC = registerArmor("jedi_tunic", EquipmentType.CHESTPLATE);
    public static final Item JEDI_PANTS = registerArmor("jedi_pants", EquipmentType.LEGGINGS);
    public static final Item JEDI_BOOTS = registerArmor("jedi_boots", EquipmentType.BOOTS);

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
        LOGGER.info("Initializing {}", MOD_ID);
    }

    private static Item registerLightsaber(String id) {
        RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(MOD_ID, id));
        return Registry.register(
                Registries.ITEM,
                key,
                new LightsaberItem(new Item.Settings()
                        .registryKey(key)
                        .maxCount(1)
                        .maxDamage(2048)
                        .fireproof()
                        .rarity(Rarity.RARE)
                        .sword(ToolMaterial.NETHERITE, 8.0F, -2.2F))
        );
    }

    private static Item registerBlaster(String id) {
        RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(MOD_ID, id));
        return Registry.register(
                Registries.ITEM,
                key,
                new BlasterItem(new Item.Settings()
                        .registryKey(key)
                        .maxCount(1)
                        .maxDamage(465)
                        .rarity(Rarity.UNCOMMON))
        );
    }

    private static SoundEvent registerSound(String id) {
        Identifier identifier = Identifier.of(MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    private static Item registerArmor(String id, EquipmentType equipmentType) {
        RegistryKey<Item> key = RegistryKey.of(Registries.ITEM.getKey(), Identifier.of(MOD_ID, id));
        return Registry.register(
                Registries.ITEM,
                key,
                new Item(new Item.Settings()
                        .registryKey(key)
                        .maxCount(1)
                        .armor(JEDI_ARMOR_MATERIAL, equipmentType))
        );
    }

    private static EntityType<BlasterProjectileEntity> registerBlasterProjectile(String id) {
        Identifier identifier = Identifier.of(MOD_ID, id);
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, identifier);
        return Registry.register(
                Registries.ENTITY_TYPE,
                identifier,
                EntityType.Builder.<BlasterProjectileEntity>create(BlasterProjectileEntity::new, SpawnGroup.MISC)
                        .dropsNothing()
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(4)
                        .trackingTickInterval(10)
                        .build(key)
        );
    }
}
