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
package org.ladysnake.creeperspores.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.ladysnake.creeperspores.common.CreeperlingEntity;

public class CreeperlingEntityRenderer extends MobRenderer<CreeperlingEntity, CreeperRenderState, CreeperModel> {
    public static final Identifier DEFAULT_SKIN = Identifier.parse("textures/entity/creeper/creeper.png");

    private final Identifier texture;

    public CreeperlingEntityRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5f);
        this.addLayer(new CreeperlingChargeFeatureRenderer(this, context.getModelSet()));
        this.texture = texture;
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState livingEntityRenderState) {
        return texture;
    }

    @Override
    protected void scale(CreeperRenderState livingEntityRenderState, PoseStack poseStack) {
        super.scale(livingEntityRenderState, poseStack);
        poseStack.scale(0.5f, 0.5f, 0.5f);
    }

    @Override
    public void extractRenderState(CreeperlingEntity livingEntity, CreeperRenderState livingEntityRenderState, float f) {
        super.extractRenderState(livingEntity, livingEntityRenderState, f);
        livingEntityRenderState.isPowered = livingEntity.isPowered();
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    public static <E extends Entity> EntityRenderer<CreeperlingEntity, CreeperRenderState> createRenderer(EntityRendererProvider.Context context, EntityRendererProvider<E> factory) {
        EntityRenderer<?,?> baseRenderer = factory.create(context);
        Identifier texture = DEFAULT_SKIN;
//        try {
//            texture = ((EntityRendererAccessor) baseRenderer).invokeGetTextureLocation(null);
//        } catch (NullPointerException ignored) {
//            // This creeper renderer does not like nulls, fall back to default texture
//            texture = DEFAULT_SKIN;
//        }
        return new CreeperlingEntityRenderer(context, texture);
    }
    //TODO my update might be wrong
//
//    public CreeperlingEntityRenderer(EntityRendererProvider.Context context, Identifier texture) {
//        super(context, new CreeperModel<>(context.bakeLayer(ModelLayers.CREEPER)), 0.25F);
//        this.addLayer(new CreeperlingChargeFeatureRenderer(this, context.getModelSet()));
//        this.texture = texture;
//    }
//
//    @Override
//    protected void scale(CreeperlingEntity entity, PoseStack matrix, float tickDelta) {
//        matrix.scale(0.5f, 0.5f, 0.5f);
//    }
//
//    @Nullable
//    @Override
//    public Identifier getTextureLocation(CreeperlingEntity creeperling) {
//        return texture;
//    }
}
