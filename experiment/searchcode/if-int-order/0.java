package ex4;

public class test {
int x,y,z;
test(int a,int b){
x=a;y=b;
this.order(a,b);
void order(int a,int b){
int t;
if(x<y){
t=x;
x=y;
y=t;
}
}
void order(int a,int b,int c){
int t;

