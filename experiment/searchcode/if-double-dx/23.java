package be.zedutchgandalf.ld48;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class Player extends Entity implements KeyListener, MouseListener, MouseMotionListener {
	double rotation;
	static Player instance;
	ArrayList<Enemy> spottedBy;
	int text = -1, trace = 50, shoot = 40;
	boolean canSee;

	public Player(Main m, double x, double y, int width, int height, int lives) {
		super(m, x, y, width, height, lives, 0xFFFF0000);
		instance = this;
		render = true;
		canSee = true;
	}

	public void render() {
		int rot = (int) (rotation / Math.PI * 180);
		rot += 22;
		Graphic r;
		switch ((int) ((rot % 360) / 45) + 1) {
			case 1:
				r = gPlayerE;
				break;
			case 2:
				r = gPlayerSE;
				break;
			case 3:
				r = gPlayerS;
				break;
			case 4:
				r = gPlayerSW;
				break;
			case 5:
				r = gPlayerW;
				break;
			case 6:
				r = gPlayerNW;
				break;
			case 7:
				r = gPlayerN;
				break;
			case 0:
				r = gPlayerNE;
				break;
			default:
				r = gPlayerN;
		}
		main.screen.drawImage(r, (int) x, (int) y);
	}

	@Override
	public synchronized void update() {
		if (shoot > 0)
			shoot--;
		trace--;
		if (trace == 0 && canSee) {
			trace = 25;
			for (int i = -45; i < 45; i++) {
				double a = (double) (i) / 180 * Math.PI;
				new Ray(main, x + width / 2, y + height / 2, 2, 2, Math.cos(rotation + a) * 3, Math.sin(rotation + a) * 3, this);
			}
		}
		if (spottedBy != null && spottedBy.size() == 0 && text != -1) {
			Main.removeText(text);
			text = -1;
		}
		for (Entity e : entities) {
			if ((e instanceof Vault) && e.intersect(this))
				((Vault) e).get();
			if (e != this && !(e instanceof Ray) && !(e instanceof Vault) && e.intersect(x + dx, y + dy, width, height))
				return;
		}
		super.update();
	}

	public void cleanLevel() {
		for (int i = 0; i < Entity.entities.size(); i++) {
			if (!(Entity.entities.get(i) instanceof Player))
				Entity.entities.remove(i--);
		}
	}

	@Override
	public void seen(Entity e) {
		if (text == -1) {
			main.siren.play();
			Main.score -= 50;
			text = Main.write("You have been spotted! Run!", Main.inverse(Main.backgroundColor), 85 * Main.SCALE, 2 + 6 * Main.SCALE);
			if (spottedBy == null)
				spottedBy = new ArrayList<Enemy>();
			spottedBy.add((Enemy) e);
		}
		((Enemy) e).agroOn(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if ((e.getButton() == MouseEvent.BUTTON1 && (Level.current != null && Level.current.complete)) || (main.lose && main.secretEnd))
			if (!Main.instance.win && !(main.lose && main.secretEnd))
				main.nextLevel = true;
			else
				main.restart();
		if (e.getButton() == MouseEvent.BUTTON1 && QuitButton.instance != null && QuitButton.instance.intersect(e.getX() / Main.SCALE, e.getY() / Main.SCALE, 1, 1))
			main.stop(0);
		if (e.getButton() == MouseEvent.BUTTON1 && main.lose && !main.secretEnd) {
			main.restartLevel = true;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if (shoot == 0) {
			shoot = 40;
			double dX = ((e.getX() / Main.SCALE) - x);
			double dY = ((e.getY() / Main.SCALE) - y);
			double d = Math.sqrt(dX * dX + dY * dY);
			dX /= d / 3;
			dY /= d / 3;
			new Bullet(main, x + width / 2, y + height / 2, 2, 2, dx + dX, dy + dY, this);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_Z:
			case KeyEvent.VK_UP:
				dy = -1;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				dy = +1;
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_Q:
			case KeyEvent.VK_LEFT:
				dx = -1;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				dx = +1;
				break;
			case KeyEvent.VK_SPACE:
				System.out.println(x + ", " + y);
				break;
			default:
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_Z:
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				dy++;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				dy--;
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_Q:
			case KeyEvent.VK_LEFT:
				dx++;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				dx--;
				break;
			default:
				break;
		}

		if (dx > 1)
			dx = 1;
		if (dy > 1)
			dy = 1;
		if (dx < -1)
			dx = -1;
		if (dy < -1)
			dy = -1;
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		double mY = e.getY() / (double) (Main.SCALE);
		double mX = e.getX() / (double) (Main.SCALE);
		rotation = Math.atan((mY - y) / (mX - x));
		if (mX - x < 0)
			rotation += Math.PI;
	}
}

