


public class BoxShape extends RectangleShape
{
protected double height;

public BoxShape()
public void setDimension(double l, double w, double h)
{
super.setDimension(l, w);

if (h >= 0)
height = h;

