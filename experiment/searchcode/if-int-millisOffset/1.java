* control timer is a timer that  can be used e.g. as a stop watch.
*
* @example ControlP5timer
*/
public class ControlTimer {

long millisOffset;

int ms, s, m, h, d;
public void update() {
current = (int)time();
if(current>previous+10) {
ms = (int)(current * _mySpeed);
s = (int) (((current * _mySpeed) / 1000));

