public class Mobile extends Entity {

public void move(int xa, int za, double rot) {
if (xa != 0 &amp;&amp; za != 0) {
return;
}
double nx = xa * Math.cos(rot) + za * Math.sin(rot);
double nz = za * Math.cos(rot) - xa * Math.sin(rot);

