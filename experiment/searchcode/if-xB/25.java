import javax.microedition.lcdui.Graphics;

class Animation extends Thread
{
public Animation(Graphics gb, Image imageb, int xb, int yb, int offsetXb, int offsetYb)
{
image = imageb;
initX=xb;
initY=yb;
x=xb;
y=yb;
offsetX=offsetXb;

