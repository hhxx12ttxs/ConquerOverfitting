while((x-x0)*(x-x0)+(y-y0)*(y-y0)<=R){
y++;
}
y--;
double B = x0+r;
double D = (x-x0)*(x-x0)+(y-y0)*(y-y0);
if(D<=R){
if(D>MAX){
while((x-x0)*(x-x0)+(y-y0)*(y-y0)<=R){
y++;
}
y--;
double B = x0-r;
double D = (x-x0)*(x-x0)+(y-y0)*(y-y0);
if(D<=R){
if(D>MAX){

