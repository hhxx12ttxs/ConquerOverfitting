package balls;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import swingFrontEnd.GameInfo;

import Helpers.Config;
import Helpers.GameManager;
import Helpers.ImageHelper;
import Helpers.MapData;
import Helpers.TestHelper;

public abstract class TowerBall extends Ball {

	public int createFlag = 0;
	protected int xSlotNum;
	protected int ySlotNum;
	protected int size;
	protected int scope;
	protected int attack;
	protected String bulletName;

	public TowerBall(int x, int y, int size) {
		this(x / Config.slotWidth, y / Config.slotHeight, size, null);
	}

	// public TowerBall(int xSlotNum, int ySlotNum, int size) {
	// this(xSlotNum, ySlotNum, size, "");
	// }

	public TowerBall(int xSlotNum, int ySlotNum, int size, String imagePath) {
		super(xSlotNum * Config.slotWidth, ySlotNum * Config.slotHeight,
				Config.slotWidth * size, Config.slotHeight * size, imagePath);
		this.xSlotNum = xSlotNum;
		this.ySlotNum = ySlotNum;
		drawTower();
	}

	public abstract void drawTower();

	public BufferedImage getImage() {
		if (this.getImagePath() == null)
			return null;
		if (this.image == null) {
			try {
				BufferedImage originalImage = ImageIO.read(new File(this
						.getImagePath()));
				this.image = ImageHelper.resizeImage(Config.ImageWidth, Config.ImageHeight, originalImage,
						originalImage.getType());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return this.image;
	}

	public boolean defend() {
		for (int i = 0; i < GameInfo.balls.size(); i++) {
			Ball ball = GameInfo.balls.get(i);
			if (ball instanceof DragonBall) {
				int ballX = ball.getX();
				int ballY = ball.getY();
				if (this.isInScope(ballX, ballY)) {
					return this.attack(ball);
				}
			}
		}
		return false;
	}

	public boolean attack(Ball ball) {
//		int angle = this.calculateAngle(this.getX(), this.getY(), ball.getX(), ball.getY());
//		GameInfo.currentMap[this.getY()/Config.slotHeight][this.getX()/Config.slotWidth] = this.getMapID() + angle;
		GameManager gameManager = GameManager.getInstance();
		gameManager.addBall(this.getBulletName(), this.getX(), this.getY(),
				ball);
		return true;
	}

	public int calculateAngle(double thisX, double thisY, double toX, double toY) {
		double dx = Math.abs(toX - thisX);
		int r = 0;
		if (toY > thisY - dx * 0.7 && toY < thisY + dx * 0.7)
			r = 2;
		else if (toY >= thisY + dx * 0.7 && toY <= thisY + dx * 2.1)
			r = 1;
		else if (toY > thisY + dx * 2.1)
			r = 0;
		else if (toY <= thisY - dx * 0.7 && toY >= thisY - dx * 2.1)
			r = 3;
		else
			r = 4;
		
		if(thisX < toX){
			return 8 - r; 
		}
		return r;
	}

	public boolean isInScope(int ballX, int ballY) {
		int scope = this.getScope();
		int x = this.getX();
		int y = this.getY();
		return (Math.pow(ballX - x, 2) + Math.pow(ballY - y, 2) <= Math.pow(
				scope, 2));
	}

	public Object getShape() {
		return new Ellipse2D.Double(getX(), getY(), 1, 1);
	}

	public int getScope() {
		return scope;
	}

	public void setScope(int scope) {
		this.scope = scope;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public String getBulletName() {
		return bulletName;
	}

	public void setBulletName(String bulletName) {
		this.bulletName = bulletName;
	}

	public abstract int getMapID();

	public abstract void setMapID(int mapID);

	public int getX() {
		return this.xSlotNum * Config.slotWidth;
	}

	public int getY() {
		return this.ySlotNum * Config.slotHeight;
	}
	public abstract int getCost();
}
