public void newLocation(double latitude, double longitude)
{
/** display the location */
newResult(&quot;Location&quot;, new float[] {(float) latitude, (float) longitude},&quot;%.6f&quot;);
public void newOrientation(double angle)
{
newResult(&quot;Angle&quot;, new float[] {(float)angle});
}
/** add implementation for the isTilt. This is how we know if the device should stop showing the image when tilted */

