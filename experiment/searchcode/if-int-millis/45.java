private int millis;

public Timer(int millis, GameLoop loop) {
this.millis = millis;
this.loop = loop;
long delta;
while(true) {
now = System.currentTimeMillis();
delta = now - lastTime;
if(delta >= millis) {

