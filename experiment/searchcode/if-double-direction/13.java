Point2D p = creature.getPosition();
double x = p.getX();
double y = p.getY();
double direction = creature.getDirection();
Dimension s = env.getSize();

if (x > s.getWidth() / 2) {
x = s.getWidth() - x;
direction = setDirectionBounceX(direction);

