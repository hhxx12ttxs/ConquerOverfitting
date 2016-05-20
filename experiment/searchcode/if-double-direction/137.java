package com.diamonddev.payup.game.component;

import org.cogaen.box2d.PhysicsBody;
import org.cogaen.box2d.Pose2D;
import org.cogaen.entity.UpdateableComponent;
import org.cogaen.event.EventService;
import org.cogaen.logging.LoggingService;
import org.cogaen.lwjgl.input.ControllerState;
import org.cogaen.lwjgl.scene.Camera;
import org.cogaen.lwjgl.scene.ForceVisualizerService;
import org.cogaen.lwjgl.scene.SceneService;
import org.cogaen.math.EaseInOut;
import org.cogaen.math.Vector2;
import org.cogaen.name.CogaenId;
import org.cogaen.time.TimeService;
import org.cogaen.time.Timer;

import com.diamonddev.payup.game.PlayState;
import com.diamonddev.payup.game.event.MapBoundsEvent;
import com.diamonddev.payup.game.util.VectorTool;

public class WheelComponent extends UpdateableComponent {

	private static final double EPSILON = 1.0;
	private static final double THRUST_FORCE = 100;
	private static final double TORQUE = 10;
	private static final double MAX_ANGLE = Math.PI * 0.25;
	private static final double LONGITUDAL_FORCE = 10;
	private static final double TANGENTIAL_FORCE = 100;
	private static final double DRIVE_FORCE = 120;
	private ControllerState ctrlState;
	private PhysicsBody body;
	private double oldThrust = 0;
	private double px;
	private double py;
	private boolean canSteer;
	private double speed = 0;
	private double angle = 0;
	private Pose2D pose;
	private EaseInOut easeAngle = new EaseInOut(0, 0.2);
	private Timer timer;

	public WheelComponent(boolean canSteer, double x, double y) {
		this.px = x;
		this.py = y;
		this.canSteer = canSteer;
	}

	@Override
	public void engage() {
		super.engage();
		this.ctrlState = (ControllerState) getParent().getAttribute(
				ControllerState.ID);
		this.body = (PhysicsBody) getParent().getAttribute(
				PhysicsBody.PHYSICS_BODY_ATTRIB);

		this.pose = (Pose2D) getParent().getAttribute(Pose2D.ATTR_ID);
		this.timer = TimeService.getInstance(getCore()).getTimer();
	}

	@Override
	public void update() {
		ForceVisualizerService fvSrv = ForceVisualizerService
				.getInstance(getCore());

		// adjust steering angle
		if (this.canSteer) {
			this.easeAngle.setTargetValue(-this.ctrlState
					.getHorizontalPosition() * MAX_ANGLE);
			this.easeAngle.update(this.timer.getDeltaTime());
			this.angle = this.easeAngle.getCurrentValue();
		}

		// apply longitudal friction
		Vector2 pos = new Vector2();
		this.body.getWorldPoint(px, py, pos);
		Vector2 direction = new Vector2(0, 1);
		direction.rotate(this.angle + this.pose.getAngle());

		Vector2 velocity = new Vector2();
		this.body.getVelocity(px, py, velocity);

		double v = direction.dot(velocity);
		if (Math.abs(v) > EPSILON) {
			direction.scale(LONGITUDAL_FORCE * -Math.signum(v));
		} else {
			direction.scale(LONGITUDAL_FORCE * -(v / EPSILON));
		}
		body.applyForce(direction.x, direction.y, px, py);
//		fvSrv.addForce(pos.x, pos.y, direction.x, direction.y);

		// apply tangetial friction
		direction.set(1, 0);
		direction.rotate(this.angle + this.pose.getAngle());

		v = direction.dot(velocity);
		if (Math.abs(v) > EPSILON) {
			direction.scale(TANGENTIAL_FORCE * -Math.signum(v));
		} else {
			direction.scale(TANGENTIAL_FORCE * -(v / EPSILON));
		}
		body.applyForce(direction.x, direction.y, px, py);
//		fvSrv.addForce(pos.x, pos.y, direction.x, direction.y);
		if (!canSteer) {
			direction.set(0, 1);
			double force = this.ctrlState.getVerticalPosition();
			if(force<0){
				force = -0.5;
			}
			direction.scale(DRIVE_FORCE * force);
			body.applyRelativeForce(direction.x, direction.y, px, py);
		}
	}

}

