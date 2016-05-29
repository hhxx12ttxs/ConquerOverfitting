public void render(GameContainer gc, StateBasedGame sb, Graphics gr) {
int invx = 1000;
int invy = 200;
if(draw){
invbg.draw(invx, invy);
plinv.getEqweapon().getIcon().draw(invx + 4, invy + 2);
}
if(plinv.getEqarmor() != null){
plinv.getEqarmor().getIcon().draw(invx + 43, invy + 2);

