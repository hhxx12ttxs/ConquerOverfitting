public double currentY = 0.0;

public double targetX = 0.0;
public double targetY = 0.0;


public IARandomWayPoint(int maxX, int maxY, double avatarSpeed)
currentY = Math.random() * (double) maxY;

targetX = Math.random() * (double) maxX;
targetY = Math.random() * (double) maxY;
}

public int[] think(double time)

