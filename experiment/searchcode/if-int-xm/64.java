this.sprite=Sprite.player_back;
}

public void update() {

byte xm=0,ym=0;

if(stCD>0)
stCD--;

if(canMove) {
if(input.up) {
ym=16;
sprite=Sprite.player_back;
}
else if(input.left) {
xm=-16;
sprite=Sprite.player_side;

