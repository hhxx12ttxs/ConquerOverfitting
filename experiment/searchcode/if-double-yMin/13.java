static double xres = 1; //pixels per unit
static double ymin;
static double ymax;
static double yres = 1;
public Window(double xmin, double xmax, double ymin, double ymax)
{
this(xmin, xmax, 1, ymin, ymax, 1);
}
public Window(double xmi, double xma, double xre, double ymi, double yma, double yre)

