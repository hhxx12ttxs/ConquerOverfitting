int dir = rand.nextInt(4);

int nextx = this.getX();
int nexty = this.getY();

if (dir == 0) { // trying to move one right (east)
return;
} else {
nexty = this.getY() + 1;
}
} else if (dir == 3) { // trying to move one down (south)

