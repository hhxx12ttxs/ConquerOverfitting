void setSpeed(double inVX, double inVY) {
vx = inVX;
vy = inVY;
}

@Override
void setRad(double inR) {
rad = inR;
case ENEMY_FIGURE_RUN: //this one will run towards the figure
// only move the enemy if it isn&#39;t dying
if (!dying) {
double figX = figure.getPosX();

