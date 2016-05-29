public class Obstruction implements ImmovableObject {

private double lowerX, higherX;
private double lowerY, higherY;
private double lowerZ, higherZ;

public Obstruction(Vector position, double a)
{
if(position == null)
{
throw new IllegalArgumentException();

