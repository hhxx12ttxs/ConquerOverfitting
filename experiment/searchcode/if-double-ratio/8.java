double ratioY =(double)run.getContentPane().getHeight() / (double)run.SCR_HEIGHT;

if(ratioX<ratioY)
{
int screenYPosition = (int)((run.getContentPane().getHeight()-(ratioX*run.SCR_HEIGHT))/2.0);
double ratioY =(double)run.getContentPane().getHeight() / (double)run.SCR_HEIGHT;

if(ratioY<ratioX)
{
return (int)((double)value*ratioY);
}
else
{
return (int)((double)value*ratioX);

