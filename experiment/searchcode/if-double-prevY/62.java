package slimevoid.infection.core.cutscene;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public abstract class Scene3D extends Scene {

	public Scene3D() {
		super(0);
		
		lastDuration = 0;
		posX = prevX = player.posX;
		posY = prevY = player.posY;
		posZ = prevZ = player.posZ;
		rotYaw = prevYaw = player.rotationYaw;
		rotPitch = prevPitch = player.rotationPitch;
		
		waypoints = new HashMap<Waypoint, Long>();
		init();
	}
	
	protected void init() {
		initWaypoints();
	}
	
	protected abstract void initWaypoints();
	
	protected void addWaypoint(Waypoint waypoint, long duration) {
		lastDuration += duration;
		long startTime = lastDuration;
		waypoints.put(waypoint, startTime);
	}
	
	private Waypoint getPrevWaypoint(long time) {
		if(waypoints.isEmpty()) {
			return null;
		}
		Waypoint waypoint = null;
		for(Entry<Waypoint, Long> entry : waypoints.entrySet()) {
			if(time >= entry.getValue()) {
				if(waypoint == null || time - waypoints.get(waypoint) > time - entry.getValue()) {
					waypoint = entry.getKey();
				}
			}
		}
		return waypoint;
	}
	
	private Waypoint getNextWaypoint(long time) {
		if(waypoints.isEmpty()) {
			return null;
		}
		Waypoint waypoint = null;
		for(Entry<Waypoint, Long> entry : waypoints.entrySet()) {
			if(time <= entry.getValue()) {
				if(waypoint == null || waypoints.get(waypoint) - time > entry.getValue() - time) {
					waypoint = entry.getKey();
				}
			}
		}
		return waypoint;
	}
	
	@Override
	public void start() {
		super.start();
		player.capabilities.isFlying = true;
	}
	
	@Override
	public void setupCamera(float frameDelta, long time) {
		super.setupCamera(frameDelta, time);
		player.capabilities.isFlying = true;
		camX = prevX + (posX - prevX) * frameDelta;
		camY = prevY + (posY - prevY) * frameDelta;
		camZ = prevZ + (posZ - prevZ) * frameDelta;
		camYaw = prevYaw + (rotYaw - prevYaw) * frameDelta;
		camPitch = prevPitch + (rotPitch - prevPitch) * frameDelta;
		player.setPositionAndRotation(camX, camY, camZ, camYaw, camPitch);
		player.setLocationAndAngles(camX, camY - player.yOffset, camZ, camYaw, camPitch);
	}
	
	@Override
	public void render2D(float frameDelta, long time) {
		super.render2D(frameDelta, time);
	}
	
	@Override
	public void onTick(long time) {
		if(getPrevWaypoint(time) != null && getNextWaypoint(time) != null) {
			long startTime = waypoints.get(getPrevWaypoint(time));
			long endTime = waypoints.get(getNextWaypoint(time));
			long wtime = time - startTime;
			long wduration = endTime - startTime;
			Waypoint waypoint;
			
			if(wduration != 0) {
				waypoint = Waypoint.calcInterCoords(getPrevWaypoint(time), getNextWaypoint(time), wtime / (float)wduration);
			} else {
				waypoint = getNextWaypoint(time);
			}
			
			prevX = posX;
			prevY = posY;
			prevZ = posZ;
			prevYaw = rotYaw;
			prevPitch = rotPitch;
			posX = waypoint.getX();
			posY = waypoint.getY();
			posZ = waypoint.getZ();
			rotYaw = waypoint.getYaw();
			rotPitch = waypoint.getPitch();
		}
	}
	
	@Override
	public long getDuration() {
		return lastDuration;
	}
	
	@Override
	public void end() {
		super.end();
		player.capabilities.isFlying = false;
	}
	
	private long lastDuration;
	private final Map<Waypoint, Long> waypoints;
	protected double posX, posY, posZ;
	protected float rotYaw, rotPitch;
	protected double prevX, prevY, prevZ;
	protected float prevYaw, prevPitch;
	protected double camX, camY, camZ;
	protected float camYaw, camPitch;
}
