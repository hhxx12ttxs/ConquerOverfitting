for (int i=0;i<8;i++){
Vector3 newPos=new Vector3(getPosicao());
double dif=0.01;
if (i==0)newPos.x=newPos.x*(1+dif);
if (i==1)newPos.x=newPos.x*(1-dif);
if (i==2)newPos.y=newPos.y*(1+dif);
if (i==3)newPos.y=newPos.y*(1-dif);

