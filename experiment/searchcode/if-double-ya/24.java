bounce = 0.8;
this.xa = 0 + (random.nextDouble() - random.nextDouble()) * 1.5;
this.ya = -1 + (random.nextDouble() - random.nextDouble()) * 1.5;
level.add(new Gore(x + random.nextDouble(), y + random.nextDouble() - 1, xa, ya));
}

protected void hitWall(double xa, double ya) {
this.xa *= 0.9;

