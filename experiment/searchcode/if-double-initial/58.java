* https://github.com/fogodev
*/
public class Frog
{

private double initialPositionX;
private double initialPositionY;
public void move(double dTime)
{
if(this.initialPositionX < this.finalPositionX){
this.initialPositionX += 300 * dTime;

