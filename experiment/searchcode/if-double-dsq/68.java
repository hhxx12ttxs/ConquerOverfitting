public void tick(double dt) {
super.tick(dt);
double dp = dt * props.projSpeed;
if(Vec2.dsq(target.pos, this.pos) <= dp*dp) {
for(Entity e : game.entities) { // TODO Quadtree opti
if(e instanceof Creep &amp;&amp; Vec2.dsq(e.pos, target.pos) <= props.aoe*props.aoe) {

