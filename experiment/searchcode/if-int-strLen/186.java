/*
 * This file is part of Vanilla.
 *
 * Copyright (c) 2011-2012, VanillaDev <http://www.spout.org/>
 * Vanilla is licensed under the SpoutDev License Version 1.
 *
 * Vanilla is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Vanilla is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spout.vanilla.protocol.codec.entity.spawn;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import org.spout.api.protocol.MessageCodec;

import org.spout.vanilla.protocol.ChannelBufferUtils;
import org.spout.vanilla.protocol.msg.entity.spawn.EntityItemMessage;

public final class EntityItemCodec extends MessageCodec<EntityItemMessage> {
	public EntityItemCodec() {
		super(EntityItemMessage.class, 0x15);
	}

	@Override
	public EntityItemMessage decode(ChannelBuffer buffer) throws IOException {
		int id = buffer.readInt();
		int itemId = buffer.readUnsignedShort();
		int count = 1;
		int damage = 0;
		if (itemId != -1) {
			count = buffer.readUnsignedByte();
			damage = buffer.readUnsignedShort();
		}
		buffer.markReaderIndex();
		int strLen = buffer.readShort();
		if (strLen != -1) {
			buffer.resetReaderIndex();
			//TODO: NBT Data?
			ChannelBufferUtils.readCompound(buffer);
		}
		int x = buffer.readInt();
		int y = buffer.readInt();
		int z = buffer.readInt();
		int rotation = buffer.readUnsignedByte();
		int pitch = buffer.readUnsignedByte();
		int roll = buffer.readUnsignedByte();
		return new EntityItemMessage(id, itemId, count, (short) damage, x, y, z, rotation, pitch, roll);
	}

	@Override
	public ChannelBuffer encode(EntityItemMessage message) throws IOException {
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeInt(message.getEntityId());
		buffer.writeShort(message.getId());
		buffer.writeByte(message.getCount());
		buffer.writeShort(message.getDamage());
		buffer.writeShort(-1); //TODO: NBT data?
		buffer.writeInt(message.getX());
		buffer.writeInt(message.getY());
		buffer.writeInt(message.getZ());
		buffer.writeByte(message.getRotation());
		buffer.writeByte(message.getPitch());
		buffer.writeByte(message.getRoll());
		return buffer;
	}
}

