package ex4;

public class testb {
int x,y;
testb(int a,int b){
x=a;y=b;
}
void change(int i,int j){
x=i;y=j;
this.order();
}
void order(){
int t;
if(x<y){
t=x;
x=y;
y=t;
}
}
}

