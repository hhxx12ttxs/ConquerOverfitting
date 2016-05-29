public void invX() {

velX = -velX;

}

public void invY() {

velY = -velY;

}

public void xL() {

velX = -1;
public void move() {

x += velX;
y -= velY;

}

public boolean up() {

if (velY > 0) return true; else return false;

