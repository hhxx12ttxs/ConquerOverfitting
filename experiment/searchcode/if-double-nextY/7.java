public class BossObject extends GameObject{

private int iterator;
private int radius;

private double nextX;
private double nextY;

public BossObject(int x, int y, int width, int height) {
nextY = radius * Math.cos(Math.toRadians(iterator));

x = (int)nextX + 250;
y = (int)nextY + 250;

iterator++;

if(iterator >= 360){
iterator = 0;

