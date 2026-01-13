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
package org.ladysnake.creeperspores.client.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import org.ladysnake.creeperspores.CreeperSpores;

public record CreeperlingFertilizationPayload(int id) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreeperlingFertilizationPayload> ID = new CustomPacketPayload.Type<>(CreeperSpores.CREEPERLING_FERTILIZATION_PACKET);

    public static final StreamCodec<RegistryFriendlyByteBuf, CreeperlingFertilizationPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CreeperlingFertilizationPayload::id,
            CreeperlingFertilizationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}