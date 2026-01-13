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
package org.ladysnake.creeperspores.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.ladysnake.creeperspores.common.SporeSpreader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerExplosion.class)
public abstract class ServerExplosionMixin {

    @Shadow
    public abstract @Nullable LivingEntity getIndirectSourceEntity();

    @Shadow
    public abstract Vec3 center();

    @Definition(id = "entity", local = @Local(type = Entity.class))
    @Definition(id = "hurtServer", method = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
    @Definition(id = "level", field = "Lnet/minecraft/world/level/ServerExplosion;level:Lnet/minecraft/server/level/ServerLevel;")
    @Definition(id = "damageSource", field = "Lnet/minecraft/world/level/ServerExplosion;damageSource:Lnet/minecraft/world/damagesource/DamageSource;")
    @Definition(id = "damageCalculator", field = "Lnet/minecraft/world/level/ServerExplosion;damageCalculator:Lnet/minecraft/world/level/ExplosionDamageCalculator;")
    @Definition(id = "getEntityDamageAmount", method = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getEntityDamageAmount(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/entity/Entity;F)F")
    @Definition(id = "h", local = @Local(type = float.class, ordinal = 2)) //TODO changed to named in unobfuscated
    @Expression("entity.hurtServer(this.level, this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity, h))")
    @Inject(
            method = "hurtEntities",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private void spreadSpores(CallbackInfo ci, @Local Entity entity) {
        if (this.getIndirectSourceEntity() instanceof SporeSpreader) {
            ((SporeSpreader) this.getIndirectSourceEntity()).spreadSpores((ServerExplosion) (Object) this, center(), entity);
        }
    }
}
