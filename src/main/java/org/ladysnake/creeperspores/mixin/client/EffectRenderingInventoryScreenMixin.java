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
package org.ladysnake.creeperspores.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.ladysnake.creeperspores.common.CreeperSporeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EffectsInInventory.class)
public abstract class EffectRenderingInventoryScreenMixin {

    @ModifyReturnValue(
            method = "getEffectName",
            at = @At("RETURN")
    )
    private Component creeperspores$modifyEffectName(Component original, MobEffectInstance mobEffectInstance) {
        MobEffect effect = mobEffectInstance.getEffect().value();

        if (effect instanceof CreeperSporeEffect sporeEffect) {
            MutableComponent customName = sporeEffect.getLocalizedName().plainCopy();

            if (mobEffectInstance.getAmplifier() >= 1 && mobEffectInstance.getAmplifier() <= 9) {
                customName.append(CommonComponents.SPACE)
                        .append(Component.translatable("enchantment.level." + (mobEffectInstance.getAmplifier() + 1)));
            }

            return customName;
        }

        return original;
    }
}
