public double masse;


public Bille(double ax,double ay,double dx){
//* dy=0 au dŽbut, dx=cste (m*g)
super(ax,ay,dx,0);
ec=0;
masse=10;
}


public void move(double pente) {

