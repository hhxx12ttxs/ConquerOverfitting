//private ArrayList<Observer> listObserver = new ArrayList<Observer>();

AAlgoModel(int nx, int ny, double xmax, double ymax, double xmin, double ymin)
{
_nx=nx;
_ny=ny;
_ymin=model._ymin;
_ymax=model._ymax;
}

void zoom(int notches, double lx, double ly)
{
double zoom=((double) (100+5*notches))/100.;

