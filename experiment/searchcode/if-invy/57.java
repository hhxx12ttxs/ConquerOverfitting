@Override
public void draw(Canvas canvas) {
for (Invader invader:invadersList) {
if(invader != null){
int invY = (y-this.posY)/(SpaceInvaderActivity.dm.widthPixels/10)+1;

if(invaderExist(invX, invY)){
this.suppUnInvader(invX,invY);

