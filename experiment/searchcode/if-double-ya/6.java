damage = 0.3;

if (dir == 0) {
xa = -1;
ya = 0;
} else if (dir == 2) {
xa = 0;
}
}

public void update() {
if (collision(xa, ya)) remove();
move();
}

public double getDamage() {
return damage;
}
}

