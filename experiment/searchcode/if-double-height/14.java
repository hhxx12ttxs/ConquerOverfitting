
public class Box extends Rectangle
{
private double height;

public Box()	//default constructor
public void setDimension(double l, double w, double h)
{
super.setDimension(l, w);

if(h >= 0)
height = h;

