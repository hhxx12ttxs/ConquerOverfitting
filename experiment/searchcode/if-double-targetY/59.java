private double targetY;

public EntityBalloon(World world, double x, double y, double z){
this(world);

posX = x;
targetY = startY + worldObj.rand.nextDouble()* 5; //ah yes then lets get a new target!
}

if (posY < targetY){ //are we bellow the targe?

