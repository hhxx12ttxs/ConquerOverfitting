import java.awt.Graphics2D;

public class BallProperties {
int x = 0;
int y = 0;
int xm = 1;
int ym = 1;

public void moveBall(int height, int width){
if (x + xm < 0)
xm =1;
if (x + xm > width - 30)

