private final int length;
private int topSpeed;
private int topSpeedWhenDeflated;
private boolean isLightOn;
private int lightIntensity;
this.topSpeed = topSpeed;
}
public void forward(SpeedSensor speedSensor)
{
if(!tire.getIsVeryDeflated())
{
if(speed < topSpeed)

