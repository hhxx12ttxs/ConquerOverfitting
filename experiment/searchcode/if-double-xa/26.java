this.h = 2;
bounce = 0.2;
this.xa = 0 + (random.nextDouble() - random.nextDouble()) * 0.5;
level.add(new Gore(x+random.nextDouble(), y+random.nextDouble()-1, xa, ya));
}

protected void hitWall(double xa, double ya) {
this.xa *= 0.8;

