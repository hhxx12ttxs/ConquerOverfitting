double boxHeight = ballContainer.getBoxHeight();

if(b1.getX()-b1.getRadius()<0 || b1.getX()+b1.getRadius()>boxWidth){
b1.setVX(-b1.getVX());
return;
}
if(b1.getY()-b1.getRadius()<0 || b1.getY()+b1.getRadius()>boxHeight){

