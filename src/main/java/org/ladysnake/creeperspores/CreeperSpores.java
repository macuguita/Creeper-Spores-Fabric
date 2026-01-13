/*
 * Creeper Spores
 * Copyright (C) 2019-2023 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package org.ladysnake.creeperspores;

import com.google.common.base.Suppliers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.ladysnake.creeperspores.client.payload.CreeperlingFertilizationPayload;
import org.ladysnake.creeperspores.common.CreeperSporeEffect;
import org.ladysnake.creeperspores.common.CreeperlingEntity;
import org.w3c.dom.Attr;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CreeperSpores implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("creeper-spores");

    /**
     * Identifiers corresponding to entity types that should be {@linkplain #registerCreeperLike(Identifier, EntityType)
     * registered as creeper likes} if and when the entity type gets registered to {@link BuiltInRegistries#ENTITY_TYPE}.
     */
    public static final Set<Identifier> CREEPER_LIKES = new HashSet<>(Arrays.asList(
            Identifier.fromNamespaceAndPath("minecraft", "creeper"),
            Identifier.fromNamespaceAndPath("mobz", "creep_entity"),
            Identifier.fromNamespaceAndPath("mobz", "crip_entity")
    ));

    public static final TagKey<Block> CREEPERLING_CAMOUFLAGE = TagKey.create(Registries.BLOCK, id("creeperling_camouflage"));
    public static final TagKey<Item> FERTILIZERS = TagKey.create(Registries.ITEM, id("fertilizers"));
    public static final TagKey<Item> SUPER_FERTILIZERS = TagKey.create(Registries.ITEM, id("super_fertilizers"));
    public static final TagKey<DamageType> SPAWNS_MORE_CREEPERLINGS = TagKey.create(Registries.DAMAGE_TYPE, id("spawns_more_creeperlings"));
    public static final TagKey<DamageType> EXTRA_CREEPER_DAMAGE = TagKey.create(Registries.DAMAGE_TYPE, id("extra_creeper_damage"));

    public static final Identifier CREEPERLING_FERTILIZATION_PACKET = id("creeperling-fertilization");
    public static final String GIVE_SPORES_TAG = "cspores:giveSpores";
    public static final int MAX_SPORE_TIME = 20 * 180;

    public static final GameRule<CreeperGrief> CREEPER_GRIEF = GameRuleBuilder.forEnum(CreeperGrief.CHARGED)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(id("creeper-grief"));

    public static final GameRule<Double> CREEPER_REPLACE_CHANCE = GameRuleBuilder.forDouble(0.2)
            .range(0.0d, 1.0d)
            .category(GameRuleCategory.MOBS)
            .buildAndRegister(id("creeper-replace-chance"));

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("creeperspores", path);
    }

    public static <T> void visitRegistry(Registry<T> registry, BiConsumer<Identifier, T> visitor) {
        RegistryEntryAddedCallback.event(registry).register((index, identifier, entry) -> visitor.accept(identifier, entry));
        new HashSet<>(registry.keySet()).forEach(id -> {
            Optional<Holder.Reference<T>> regId = registry.get(id);
            if (regId.isEmpty()) return;
            visitor.accept(id, regId.get().value());
        });
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(CreeperlingFertilizationPayload.ID, CreeperlingFertilizationPayload.CODEC);
        visitRegistry(BuiltInRegistries.ENTITY_TYPE, (id, type) -> {
            if (CREEPER_LIKES.contains(id)) {
                // can't actually check that the entity type is living, so just hope nothing goes wrong
                @SuppressWarnings("unchecked") EntityType<? extends LivingEntity> livingType = (EntityType<? extends LivingEntity>) type;
                registerCreeperLike(id, livingType);
            }
        });
    }

    @ApiStatus.Internal
    public static void registerCreeperLike(Identifier id) {
        // can't actually check that the entity type is living, so just hope nothing goes wrong
        // the cast to Optional<?> is not optional, according to javac
        @SuppressWarnings("unchecked") Optional<EntityType<? extends LivingEntity>> creeperType = (Optional<EntityType<? extends LivingEntity>>) (Optional<?>) BuiltInRegistries.ENTITY_TYPE.getOptional(id);
        if (creeperType.isPresent()) {
            registerCreeperLike(id, creeperType.get());
        } else {
            CREEPER_LIKES.add(id);
        }
    }

    @ApiStatus.Internal
    public static void registerCreeperLike(Identifier id, EntityType<? extends LivingEntity> type) {
        String prefix = id.getNamespace().equals("minecraft") ? "" : (id.toString().replace(':', '_') + "_");
        EntityType<CreeperlingEntity> creeperlingType = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                CreeperSpores.id(prefix + "creeperling"),
                createCreeperlingType(type, prefix)
        );
        CreeperSporeEffect sporesEffect = Registry.register(
                BuiltInRegistries.MOB_EFFECT,
                CreeperSpores.id(prefix + "creeper_spore"),
                createCreeperSporesEffect(type)
        );
        CreeperEntry.register(type, creeperlingType, sporesEffect);
    }

    @Contract(pure = true)
    private static CreeperSporeEffect createCreeperSporesEffect(EntityType<?> creeperType) {
        return new CreeperSporeEffect(MobEffectCategory.NEUTRAL, 0x22AA00, creeperType);
    }

    @Contract(pure = true)
    private static EntityType<CreeperlingEntity> createCreeperlingType(EntityType<? extends LivingEntity> creeperType, String prefix) {
        Supplier<CreeperEntry> kind = Suppliers.memoize(() -> CreeperEntry.get(creeperType));

        ResourceKey<EntityType<?>> resKey = ResourceKey.create(Registries.ENTITY_TYPE, id(prefix + "creeperling"));
        EntityType<CreeperlingEntity> creeperlingType = FabricEntityType.Builder.createLiving(
                        (EntityType.EntityFactory<CreeperlingEntity>) (type, world) ->
                                new CreeperlingEntity(Objects.requireNonNull(kind.get()), world),
                        MobCategory.MISC,
                        creeperlingEntityLiving -> creeperlingEntityLiving
                                .defaultAttributes(() ->
                                        Mob.createMobAttributes()
                                                .add(Attributes.MAX_HEALTH, 10.0)
                                                .add(Attributes.MOVEMENT_SPEED, 0.24)
                                                .add(Attributes.TEMPT_RANGE, 16.0)
                                )
                )
                .sized(creeperType.getWidth() / 2f, creeperType.getHeight() / 2f) // Corrected
                .clientTrackingRange(64) // 64 blocks tracking range
                .updateInterval(1)
                .build(resKey);

        return creeperlingType;
    }
}
