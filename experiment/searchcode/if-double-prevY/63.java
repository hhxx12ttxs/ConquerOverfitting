private boolean jump;
private boolean midAir;

protected double prevY;

private String code;

protected boolean rewinding;
dy += START_JUMP_DY;
}

public void updateDy(double dt)
{
if(jump)
{
dy -= dt*JUMP_DY;

