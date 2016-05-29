time += deltaTime;
if (!beingBuild) lifeTime += deltaTime;

if (time > COLOR_SPEED) {
time = 0;
float bounceFactor = MIN_LENGTH/aux.magnitude();

float invX = 1;
float invY = 1;

if (aux.x == 0) invY = -1;
else if (aux.y < 0) invX = -1;

