package ants;


public  class Point
{
	public double x;
	public double y;

	public Point(Point another)
	{
		this.x = another.getX();
		this.y = another.getY();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Point)) return false;

		Point point = (Point) o;

		if (Double.compare(point.x, x) != 0) return false;
		if (Double.compare(point.y, y) != 0) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result;
		long temp;
		temp = x != +0.0d ? Double.doubleToLongBits(x) : 0L;
		result = (int) (temp ^ (temp >>> 32));
		temp = y != +0.0d ? Double.doubleToLongBits(y) : 0L;
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public Point(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}


	public void setX(double x)
	{
		this.x = x;
	}

	public void setY(double y)
	{
		this.y = y;
	}
}

