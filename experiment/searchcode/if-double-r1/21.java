public class DrawCirclesClass extends JComponent
{
double r1;
double r2;

public DrawCirclesClass (double r1, double r2)
{
this.r1 = r1;
Graphics2D g2 = (Graphics2D) g;
Ellipse2D.Double Circle = new Ellipse2D.Double(100-r1, 200-r1, r1*2, r1*2);
Ellipse2D.Double Circle2 = new Ellipse2D.Double(200-r2, 100-r2, r2*2, r2*2);

