this.xa = 0 + (random.nextDouble() - random.nextDouble()) * 1.5;
this.ya = -1 + (random.nextDouble() - random.nextDouble()) * 1.5;

life = random.nextInt(90) + 60;
ya *= Level.FRICTION;
ya += Level.GRAVITY * 0.5;
level.add(new Gore(x + random.nextDouble(), y + random.nextDouble() - 1, xa, ya));

