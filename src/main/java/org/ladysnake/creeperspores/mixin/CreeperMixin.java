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
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.ladysnake.creeperspores.CreeperEntry;
import org.ladysnake.creeperspores.CreeperSpores;
import org.ladysnake.creeperspores.common.CreeperlingEntity;
import org.ladysnake.creeperspores.common.SporeSpreader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster implements SporeSpreader {

    @Unique private TriState giveSpores = TriState.DEFAULT;

    protected CreeperMixin(EntityType<? extends Monster> type, Level world) {
        super(type, world);
    }

    @Unique
    private boolean shouldSpreadSpores() {
        return this.giveSpores == TriState.TRUE || (this.giveSpores == TriState.DEFAULT && !this.isNoAi());
    }

    @Override
    public void spreadSpores(Explosion explosion, Vec3 center, Entity affectedEntity) {
        if (affectedEntity instanceof LivingEntity victim && this.shouldSpreadSpores()) {
            double exposure = ServerExplosion.getSeenPercent(center, victim);
            CreeperEntry creeperEntry = CreeperEntry.get(this.getType());
            if (creeperEntry != null) {
                Holder<MobEffect> sporeEffectEntry = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(creeperEntry.sporeEffect());
                if (sporeEffectEntry != null) {
                    victim.addEffect(new MobEffectInstance(sporeEffectEntry, (int) Math.round(CreeperSpores.MAX_SPORE_TIME * exposure)));
                }
            }
        }
    }

    //TODO: should change to modify return value
    @Definition(id = "mobInteract", method = "Lnet/minecraft/world/entity/monster/Monster;mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;")
    @Definition(id = "player", local = @Local(type = Player.class, argsOnly = true))
    @Definition(id = "interactionHand", local = @Local(type = InteractionHand.class, argsOnly = true))
    @Expression("super.mobInteract(player, interactionHand)")
    @Inject(
            method = "mobInteract",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            cancellable = true
    )
    private void interactSpawnEgg(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        CreeperEntry creeperEntry = CreeperEntry.get(this.getType());
        if (creeperEntry != null && CreeperlingEntity.interactSpawnEgg(player, this, stack, creeperEntry)) {
            cir.setReturnValue(InteractionResult.SUCCESS); // Return ActionResult.SUCCESS instead of true
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void writeCustomDataToTag(ValueOutput tag, CallbackInfo ci) {
        if (this.giveSpores != TriState.DEFAULT) {
            tag.putBoolean(CreeperSpores.GIVE_SPORES_TAG, this.giveSpores.get());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void readCustomDataFromTag(ValueInput tag, CallbackInfo ci) {
        if (tag.contains(CreeperSpores.GIVE_SPORES_TAG)) {
            this.giveSpores = TriState.of(tag.getBooleanOr(CreeperSpores.GIVE_SPORES_TAG, false));
        }
    }
}
