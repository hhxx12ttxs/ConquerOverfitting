package net.supahfly.ignition.model;

public enum Direction
{
	NORTH(1, 0, 1),
	NORTH_EAST(2, 1, 1),
	EAST(4, 1, 0),
	SOUTH_EAST(7, 1, -1),
	SOUTH(6, 0, -1),
	SOUTH_WEST(5, -1, -1),
	WEST(3, -1, 0),
	NORTH_WEST(0, -1, 1),
	NONE(-1, 0, 0);
	
	public final int deltaX;
	public final int deltaY;
	public final int value;
	
	private Direction(int value, int deltaX, int deltaY)
	{
		this.value = value;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	
	public static boolean isConnectable(int deltaX, int deltaY)
	{
		return Math.abs(deltaX) == Math.abs(deltaY) || deltaX == 0 || deltaY == 0;
	}
	
	public static Direction fromInteger(int value)
	{
		for (Direction direction : Direction.values())
		{
			if (direction.value == value)
			{
				return direction;
			}
		}
		
		return Direction.NONE;
	}
	
	public static Direction fromPositions(Position src, Position dst)
	{
		// TODO: make this work
		double dx = ((double)dst.x() - (double)src.x());
		double dy = ((double)dst.y() - (double)src.y());
		double angle = (Math.atan(dy / dx) * 180) / Math.PI;
		int direction = -1;
		
		if (Double.isNaN(angle))
		{
			return Direction.NONE;
		}
		
		if (Math.signum(dx) < 0)
		{
			angle += 180.0;
		}
		
		direction = (int)((((90 - angle) / 22.5) + 16) % 16);
		
		if (direction > -1)
		{
			direction >>= 1;
		}
		
		return Direction.fromInteger(direction);
	}
	
	public static Direction fromDelta(Position delta)
	{
		return fromDelta(delta.x(), delta.y());
	}
	
	public static Direction fromDelta(int deltaX, int deltaY)
	{
		for (Direction direction : Direction.values())
		{
			if (direction.deltaX == deltaX && direction.deltaY == deltaY)
			{
				return direction;
			}
		}
		
		/*if (deltaY == 1)
		{
			if (deltaX == 1)
			{
				return Direction.NORTH_EAST;
			}
			else if (deltaX == 0)
			{
				return Direction.NORTH;
			}
			else
			{
				return Direction.NORTH_WEST;
			}
		}
		else if (deltaY == -1)
		{
			if (deltaX == 1)
			{
				return Direction.SOUTH_EAST;
			}
			else if (deltaX == 0)
			{
				return Direction.SOUTH;
			}
			else
			{
				return Direction.SOUTH_WEST;
			}
		}
		else
		{
			if (deltaX == 1)
			{
				return Direction.EAST;
			}
			else if (deltaX == -1)
			{
				return Direction.WEST;
			}
		}*/
		
		return Direction.NONE;
	}
}

