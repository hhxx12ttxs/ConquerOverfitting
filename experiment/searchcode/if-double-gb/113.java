package systemsoftbuilder.entity;

import java.text.DecimalFormat;

public class SoftEntity
{

	private String category;
	private String name;
	private double price;
	private double size;
	private String formattedSize;

	public SoftEntity()
	{
	}

	public SoftEntity(String category, String name, double price, double size)
	{
		this.category = category;
		this.name = name;
		this.price = price;
		this.size = size;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public double getSize()
	{
		return size;
	}

	public void setSize(double size)
	{
		this.size = size;
	}

	public String getFormatSize()
	{
		if (formattedSize == null)
			calculateSize();
		return formattedSize;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(price);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(size);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SoftEntity other = (SoftEntity) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(price) != Double
				.doubleToLongBits(other.price))
			return false;
		if (Double.doubleToLongBits(size) != Double
				.doubleToLongBits(other.size))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return name + "\nprice : $" + price + ", size : " + getFormatSize()
				+ "\n";
	}

	private String calculateSize()
	{
		String exponent = " B";
		int index = 1024;
		int divider = 1;
		int step = 0;
		while (size / divider > 1024)
		{
			step++;
			divider = (int) Math.pow(index, step);
		}
		switch (step)
		{
		case 1:
			exponent = " kB";
			break;
		case 2:
			exponent = " MB";
			break;
		case 3:
			exponent = " GB";
			break;
		default:
			break;
		}
		formattedSize = new DecimalFormat(".##").format(size / divider)
				+ exponent;
		return formattedSize;
	}

}

