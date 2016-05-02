import java.awt.Graphics;
import java.awt.*;
import java.applet.*;

// -= ??????????? ? ???????? ????????????? ?? ???? =-
class triangle extends Canvas{
  long P[][] = new long[3][3]; // ?????????? ?????? + ????????????
 // ?????????? - ?????? ???????
  triangle(long x1,long y1,long x2,long y2,long x3,long y3,long l1,long l2,long l3){
	  P[0][0] = x1; P[0][1] = y1; P[0][2] = l1;
	  P[1][0] = x2; P[1][1] = y2; P[1][2] = l2;
	  P[2][0] = x3; P[2][1] = y3; P[2][2] = l3;
  };
 // ????? ???? ???????? ???????
  void swap( long a[], long b[], int cnt ){
    for(int i=0;i<cnt;i++){long temp=a[i]; a[i]=b[i]; b[i]=temp;}
  };
 // ???????? ????????????
  long linear( long r1,long r2,long a1,long a2,long a){
    if (a1==a2){ return a; }else{
    return r1+(r2-r1)*(a-a1)/(a2-a1);}
  }
 // ????? ?? ?????
  void draw(Graphics g){
  	long x1,x2,t,x,y,ly1,ly2,l;
	  for(int i=0;i<2;i++)
	    for(int j=i;j<3;j++)
		    if(P[i][1]>P[j][1]) swap(P[i],P[j],3);
    g.setColor(Color.green);
   	if ((P[0][1]!=P[1][1]) && (P[0][1]!=P[2][1]))
	    for (y=P[0][1];y<=P[1][1];y++){
//x1 = (long)(((P[2][0]-P[0][0])*(y-P[0][1])/(P[2][1]-P[0][1])))+P[0][0];
//x2 = (long)(((P[1][0]-P[0][0])*(y-P[0][1])/(P[1][1]-P[0][1])))+P[0][0];
        x1 = linear(P[0][0],P[2][0],P[0][1],P[2][1],y);
        x2 = linear(P[0][0],P[1][0],P[0][1],P[1][1],y);
        ly1 = linear(P[0][2],P[1][2],P[0][1],P[1][1],y);
        ly2 = linear(P[0][2],P[2][2],P[0][1],P[2][1],y);
    		if (x1>x2) { t=x1; x1=x2; x1=t; t=ly1; ly1=ly2; ly2=t; }
  	  	for (x=x1;x<=x2;x++){
          l = linear(ly2,ly1,x1,x2,x);
          g.setColor(new Color((int)l,(int)l,0));
          g.drawLine((int)x,(int)y,(int)x,(int)y);
        }
  	  };
	  if ((P[0][1]!=P[2][1]) && (P[1][1]!=P[2][1]))
	    for (y=P[1][1];y<=P[2][1];y++){
        x1 = linear(P[0][0],P[2][0],P[0][1],P[2][1],y);
        x2 = linear(P[1][0],P[2][0],P[1][1],P[2][1],y);
        ly1 = linear(P[1][2],P[2][2],P[1][1],P[2][1],y);
        ly2 = linear(P[0][2],P[2][2],P[0][1],P[2][1],y);
    		if (x1>x2) { t=x1; x1=x2; x1=t; t=ly1; ly1=ly2; ly2=t; }
        g.setColor(new Color((int)ly1,(int)ly1,0));
  	  	for (x=x1;x<=x2;x++){
          l = linear(ly2,ly1,x1,x2,x);
          g.setColor(new Color((int)l,(int)l,0));
          g.drawLine((int)x,(int)y,(int)x,(int)y);
        }
	  };
   // ???????? ?????????
    g.setColor(Color.red);
    Polygon p = new Polygon();
    for(int i=0;i<3;i++)
      p.addPoint((int)P[i][0],(int)P[i][1]);
    g.drawPolygon(p);
  };
};

public class tr_demo extends Applet{
  public void paint(Graphics g){
    triangle t = new triangle(200,100,450,400,400,250,250,250,12);
    t.draw(g);
  }
}

