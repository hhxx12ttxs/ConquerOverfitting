* @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
*
*/

public class EntityDroid extends Entity {

private double startY;
private double targetY;

public EntityDroid(World world) {
targetY = startY + worldObj.rand.nextDouble() * 5;
}

if (posY < targetY) {
motionY = 0.05;
}else{

