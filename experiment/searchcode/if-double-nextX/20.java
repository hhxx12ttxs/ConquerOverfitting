public class BossObject extends GameObject{

private int iterator;
private int radius;

private double nextX;
private double nextY;

public BossObject(int x, int y, int width, int height) {
public void update(){
super.update();
if(health <= 0){
isAlive = false;
}

nextX = radius * Math.sin(Math.toRadians(iterator));

