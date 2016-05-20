/*
 * This file is part of InfObjects.
 *
 * Copyright (c) 2012, SpoutDev <http://www.spout.org/>
 * InfObjects is licensed under the SpoutDev License Version 1.
 *
 * InfObjects is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * InfObjects is distributed in the hope that it will be useful,
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
package org.spout.infobjects.util;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * This class performs ray tracing and iterates along blocks on a line
 */
public class BlockIterator implements Iterator<Block> {
	// World to iterate in
	private final World world;
	// Starting position
	private final Vector origin;
	// Direction of the ray
	private final Vector direction;
	// Max distance
	private final double range;
	// Current position
	private int x, y, z;
	// Step in blocks
	private int stepX, stepY, stepZ;
	// Step in distance
	private double tDeltaX, tDeltaY, tDeltaZ;
	// Current distance
	private double tMaxX, tMaxY, tMaxZ;

	/**
	 * Constructs the BlockIterator
	 *
	 * @param from The starting point
	 * @param to The end point
	 * @throws IllegalArgumentException If the worlds from both points differ.
	 */
	public BlockIterator(Location from, Location to) {
		if (!from.getWorld().equals(to.getWorld())) {
			throw new IllegalArgumentException("Cannot iterate between worlds.");
		}

		this.world = from.getWorld();
		this.origin = from.toVector();
		this.direction = to.toVector().subtract(origin).normalize();
		this.range = from.distance(to);

		reset();
	}

	/**
	 * Reset the iterator
	 */
	public final void reset() {
		x = origin.getBlockX();
		y = origin.getBlockY();
		z = origin.getBlockZ();

		double dx = direction.getX();
		double dy = direction.getY();
		double dz = direction.getZ();

		stepX = dx > 0 ? 1 : -1;
		stepY = dy > 0 ? 1 : -1;
		stepZ = dz > 0 ? 1 : -1;

		tDeltaX = (dx == 0) ? Double.MAX_VALUE : Math.abs(1 / dx);
		tDeltaY = (dy == 0) ? Double.MAX_VALUE : Math.abs(1 / dy);
		tDeltaZ = (dz == 0) ? Double.MAX_VALUE : Math.abs(1 / dz);

		tMaxX = (dx == 0) ? Double.MAX_VALUE : Math.abs((x + (dx > 0 ? 1 : 0) - origin.getX()) / dx);
		tMaxY = (dy == 0) ? Double.MAX_VALUE : Math.abs((y + (dy > 0 ? 1 : 0) - origin.getY()) / dy);
		tMaxZ = (dz == 0) ? Double.MAX_VALUE : Math.abs((z + (dz > 0 ? 1 : 0) - origin.getZ()) / dz);
	}

	@Override
	public boolean hasNext() {
		return (Math.min(Math.min(tMaxX, tMaxY), tMaxZ) <= range);
	}

	@Override
	public Block next() {
		if (tMaxX < tMaxY) {
			if (tMaxX < tMaxZ) {
				x += stepX;
				tMaxX += tDeltaX;
			} else {
				z += stepZ;
				tMaxZ += tDeltaZ;
			}
		} else {
			if (tMaxY < tMaxZ) {
				y += stepY;
				tMaxY += tDeltaY;
			} else {
				z += stepZ;
				tMaxZ += tDeltaZ;
			}
		}
		return world.getBlockAt(x, y, z);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Block removal is not supported by this iterator");
	}
}

