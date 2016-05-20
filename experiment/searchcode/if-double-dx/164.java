package be.zedutchgandalf.ld48;

public class Enemy extends Entity {

	double rotation;
	int trace, shoot;
	double sX, sY;
	boolean canShoot;

	public Enemy(Main m, double x, double y, int width, int height, int lives, double rotation) {
		super(m, x, y, width, height, lives, 0xFF00FF00);
		this.rotation = rotation;
		trace = 50;
		shoot = 0;
		canShoot = false;
	}

	@Override
	public void render() {
		if (render) {
			int rot = (int) (rotation / Math.PI * 180);
			rot += 22;
			Graphic r;
			switch ((int) ((rot % 360) / 45)) {
				case 0:
					r = gEnemyE;
					break;
				case 1:
					r = gEnemySE;
					break;
				case 2:
					r = gEnemyS;
					break;
				case 3:
					r = gEnemySW;
					break;
				case 4:
					r = gEnemyW;
					break;
				case 5:
					r = gEnemyNW;
					break;
				case 7:
					r = gEnemyNE;
					break;
				default:
				case 6:
					r = gEnemyN;
					break;
			}
			main.screen.drawImage(r, (int) x, (int) y);
		}
	}

	@Override
	public void hit() {
		if (Player.instance.spottedBy != null && Player.instance.spottedBy.contains(this)) {
			Player.instance.spottedBy.remove(this);
		}
		for (Entity e : Entity.entities)
			if (e instanceof Ray && ((Ray) e).origin == this)
				e.destroy = true;
		Level.current.enemies--;
		super.hit();
	}

	@Override
	public void update() {
		super.update();
		trace--;
		if (trace == 0) {
			trace = 25;
			for (int i = -45; i < 45; i++) {
				double a = (double) (i) / 180 * Math.PI;
				new Ray(main, x + width / 2, y + height / 2, 2, 2, Math.cos(rotation + a) * 5, Math.sin(rotation + a) * 5, this);
			}
		}
		if (canShoot) {
			shoot--;
			if (shoot <= 0) {
				shoot = 38;
				double dX = sX - x;
				double dY = sY - y;
				double d = Math.sqrt(dX * dX + dY * dY);
				dX /= d;
				dY /= d;
				new Bullet(main, x, y, 2, 2, dX, dY, this);
			}
		}
	}

	public void agroOn(Player player) {
		canShoot = true;
		sX = player.x;
		sY = player.y;
	}
}

