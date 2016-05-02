package safrain.nodelet.components.mover;

import safrain.nodelet.components.position.Position;
import safrain.nodelet.factory.BaseBuilder;
import safrain.nodelet.pool.Reset;
import safrain.nodelet.util.MathUtil;

/**
 * @author Safrain
 * 
 */
public class Mover {
	@Reset
	public Position position;
	// 移动速度
	@Reset
	public double speed;
	@Reset
	public double turnSpeed;
	@Reset
	public double acceleration;
	@Reset
	public double maxSpeed;
	/**
	 * 移动的方向
	 */
	@Reset
	public double angle;
	/**
	 * 移动的时候是否同步position的角度
	 */
	@Reset
	public boolean syncAngle;
	/**
	 * 是否激活，如果不激活则不会动
	 */
	@Reset
	private boolean enabled;

	@Reset
	private boolean hasTarget;
	@Reset
	private double targetX;
	@Reset
	private double targetY;
	@Reset
	private double targetAngle;

	/**
	 * 处理移动的absolute位置缓存
	 */
	private Position processBuffer = new Position();

	public void setMoveTarget(double targetX, double targetY) {
		this.targetX = targetX;
		this.targetY = targetY;
		this.angle = 0;// TODO:s
		position.getAbsolutePosition(processBuffer);
		targetAngle = MathUtil.angle(processBuffer.x, processBuffer.y, targetX,
				targetX);
	}

	public void enable() {
		enabled = true;
	}

	public void disable() {
		enabled = false;
		speed = 0d;
	}

	public void tick() {
		if (enabled) {
			processMove();
		}
	}

	/**
	 * 处理移动
	 * 
	 * @return 是否进行了移动
	 * 
	 */
	private void processMove() {
		if (!enabled) {
			return;
		}
		processAcceleraion();

		// 处理移动
		if (position.parent == null) {
			double angle;
			if (syncAngle) {
				angle = position.angle;
			} else {
				angle = this.angle;
			}
			position.set((position.x + MathUtil.cos(angle) * speed),
					(position.y - MathUtil.sin(angle) * speed), angle);
		}
	}

	/**
	 * 处理加速度
	 */
	private void processAcceleraion() {
		if (speed != maxSpeed) {
			if (acceleration != 0) {
				speed += acceleration;
				if (speed > maxSpeed) {
					speed = maxSpeed;
				}
			}
		}
	}

	private void processTurn() {
		double delta = targetAngle - getAngle();
		// convert delta to positive number between 0 to 360 for convenience
		delta = delta % 360;
		if (delta < 0) {
			delta += 360;
		}
		// if delta is smaller than turn speed, just turn delta
		double t;
		if (turnSpeed > delta) {
			t = delta;
		} else {
			t = turnSpeed;
		}
		// turn to the neareast direction
		if (delta > 180) {
			setAngle(angle - t);
		} else {
			setAngle(angle + t);
		}
	}

	public double getAngle() {
		if (syncAngle) {
			return position.angle;
		} else {
			return this.angle;
		}
	}

	public void setAngle(double angle) {
		if (syncAngle) {
			position.setAbsoluteAngle(angle);
		} else {
			this.angle = angle;
		}
	}

	/* Builder */
	public static class Builder extends BaseBuilder<Mover> {
		public double speed;
		public double acceleration;
		public boolean syncAngle;
		public double maxSpeed;
		public double angle;

		@Override
		public void fill(Mover object) {
			object.speed = speed;
			object.acceleration = acceleration;
			object.syncAngle = syncAngle;
			object.maxSpeed = maxSpeed;
			object.angle = angle;
		}

	}

}

