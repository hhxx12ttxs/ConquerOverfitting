@Override
public void move(float offset)
{
int alpha = getAlpha()+getAlphaSpeed();
if (alpha > 255 || alpha < 0)
{
setAlphaSpeed(-getAlphaSpeed());
}
setAlpha(alpha);
}
}

