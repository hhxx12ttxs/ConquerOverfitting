public class Fly_by_Star {
int   Xmax, Ymax, z;
double x, y;

Fly_by_Star( int width, int height )
y = y + (double)mx*srot/25 - (double)my*crot/25;
if (x<-Xmax)x+=2*Xmax;
if (x>Xmax)x-=2*Xmax;
if (y<-Ymax)y+=2*Ymax;

