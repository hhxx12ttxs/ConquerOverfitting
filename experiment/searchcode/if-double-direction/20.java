int d = super.direction;
if (direction % 8 + 1 == d) {
direction = d % 8 + 1;
laser.setDirection(direction);
return laser;
} else if ((direction + 6) % 8 + 1 == d) {
direction = (d + 6) % 8 + 1;

