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

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.ladysnake.creeperspores.CreeperGrief;
import org.ladysnake.creeperspores.CreeperSpores;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Shadow public abstract GameRules getGameRules();

    @Inject(method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getDestructionType(Lnet/minecraft/world/GameRules$Key;)Lnet/minecraft/world/explosion/Explosion$DestructionType;"), cancellable = true)
    private void griefLessExplosion(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, CallbackInfoReturnable<Explosion> cir) {
        if (entity instanceof CreeperEntity creeper) {
            CreeperGrief grief = this.getGameRules().get(CreeperSpores.CREEPER_GRIEF).get();
            if (!grief.shouldGrief(creeper.shouldRenderOverlay())) {
                Explosion explosion = new Explosion((World) (Object) this, entity, damageSource, behavior, x, y, z, power, createFire, Explosion.DestructionType.KEEP);
                explosion.collectBlocksAndDamageEntities();
                explosion.affectWorld(particles);
                cir.setReturnValue(explosion);
            }
        }
    }
}