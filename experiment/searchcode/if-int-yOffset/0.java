private Graphics g;
private static final int scale = 4;
private int yoffset = 2;
private int xoffset = 2;
public void drawProfile(int type, int x, int y)
{
xoffset = xx+x;
yoffset = yy+y;
int[][] points = getPoints(type);
if ( points!=null )

