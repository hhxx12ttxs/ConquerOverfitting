package safrain.nodelet.components.position;

import safrain.nodelet.factory.BaseBuilder;
import safrain.nodelet.game.Game;
import safrain.nodelet.pool.Reset;
import safrain.nodelet.util.MathUtil;

public class Position {
	@Reset
	public double x;
	@Reset
	public double y;
	@Reset
	public double angle;
	@Reset
	public Position parent;

	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public void set(double x, double y, double angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	public void setAbsolutePosition(Position position) {
		setAbsolutePosition(position, true);
	}

	public void setAbsolutePosition(Position position, boolean setAngle) {
		if (parent == null) {
			if (setAngle) {
				set(position.x, position.y, position.angle);
			} else {
				set(position.x, position.y);
			}
			return;
		}

		// 先把传进来的值保存起来，因为后面需要借用position对象以减少gc
		double ax = position.x;
		double ay = position.y;
		double aa = position.angle;
		// 取父坐标系的绝对坐标与绝对角度
		parent.getAbsolutePosition(position);
		double px = position.x;
		double py = position.y;
		double pa = position.angle;
		// 得出差
		double dx = ax - px;
		double dy = ay - py;
		// 反向旋转
		if (pa % 360 != 0) {
			double sin = MathUtil.sin(-pa);
			double cos = MathUtil.cos(-pa);
			x = dx * cos - dy * sin;
			y = dx * sin + dy * cos;
		} else {
			x = dx;
			y = dy;
		}
		// 还原传入的position对象
		position.x = ax;
		position.y = ay;
		position.angle = aa;
	}

	public void getAbsolutePosition(Position result) {
		if (parent != null) {
			parent.getAbsolutePosition(result);
			if (result.angle % 360 != 0) {
				double sin = MathUtil.sin(-result.angle);
				double cos = MathUtil.cos(-result.angle);
				result.x += x * cos + y * sin;
				result.y += -x * sin + y * cos;
			} else {
				result.x += x;
				result.y += y;
			}
			result.angle += angle;
		} else {
			result.x = x;
			result.y = y;
			result.angle = angle;
		}
	}

	public void setAbsoluteAngle(double angle) {
		if (parent == null) {
			this.angle = angle;
		} else {
			angle = angle - parent.getAbsoluteAngle();
		}
	}

	public double getAbsoluteAngle() {
		if (parent == null) {
			return angle;
		} else {
			return parent.getAbsoluteAngle() + angle;
		}
	}

	public Position getRootPosition() {
		if (parent == null) {
			return this;
		} else {
			return parent.getRootPosition();
		}
	}

	public void copyAbsoulte(Position position) {
		Position result = Game.instance.poolService.get(Position.class);
		position.getAbsolutePosition(result);
		this.setAbsolutePosition(result, true);
		Game.instance.poolService.recycle(result);
	}

	public static class Builder extends BaseBuilder<Position> {
		public double x;
		public double y;
		public double angle;

		@Override
		public void fill(Position t) {
			t.x = x;
			t.y = y;
			t.angle = angle;
		}

	}

}

