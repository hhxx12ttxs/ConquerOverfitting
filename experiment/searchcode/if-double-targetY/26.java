UP = false; DOWN = false; RIGHT = false; LEFT = false;
}

public void drawTarget(GL gl) {
if (DOWN) targetY -= 0.015;
else if (UP) targetY += 0.015;
if (RIGHT) targetX += 0.015;
else if (LEFT) targetX -= 0.015;

if (targetX >= 0.469) targetX = 0.469;

