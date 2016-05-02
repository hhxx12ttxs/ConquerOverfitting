package digitalmirror;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

import SimpleOpenNI.SimpleOpenNI;
import SimpleOpenNI.XnSkeletonJointPosition;
import SimpleOpenNI.XnVector3D;

public class Joint {
	
	private int jointId;
	private User user;
	
	private double absoluteTotalDistanceMoved = 0.0; // In meters
	private double distanceMoved = 0.0; // In meters
	private LinkedList<Double> distances = new LinkedList<Double>(); // In meters
	private double absoluteTotalEnergySpent = 0.0; // In joules
	private double energySpent = 0.0;
	private LinkedList<Double> energies = new LinkedList<Double>();
	private XnSkeletonJointPosition lastPosition = null;
	private Joint movementRelativeTo = null;
	private double positionConfidence = 1;
	
	static private HashMap<Integer, String> jointNames = new HashMap<Integer, String>();
	static private HashMap<Integer, Double> jointMasses = new HashMap<Integer, Double>();
	static private HashMap<Integer, Double> jointMaxEnergies = new HashMap<Integer, Double>();
	static private UndirectedGraph<Integer, Limb> humanJointStructure = new SimpleGraph<Integer, Limb>(Limb.class);
	
	static {
		// Names
		getJointNames().put(SimpleOpenNI.SKEL_HEAD, "Head");
		getJointNames().put(SimpleOpenNI.SKEL_NECK, "Neck");
		getJointNames().put(SimpleOpenNI.SKEL_LEFT_SHOULDER, "Left shoulder");
		getJointNames().put(SimpleOpenNI.SKEL_RIGHT_SHOULDER, "Right shoulder");
		getJointNames().put(SimpleOpenNI.SKEL_LEFT_ELBOW, "Left elbow");
		getJointNames().put(SimpleOpenNI.SKEL_RIGHT_ELBOW, "Right elbow");
		getJointNames().put(SimpleOpenNI.SKEL_LEFT_HAND, "Left hand");
		getJointNames().put(SimpleOpenNI.SKEL_RIGHT_HAND, "Right hand");
		getJointNames().put(SimpleOpenNI.SKEL_TORSO, "Torso");
		getJointNames().put(SimpleOpenNI.SKEL_LEFT_HIP, "Left hip");
		getJointNames().put(SimpleOpenNI.SKEL_RIGHT_HIP, "Right hip");
		getJointNames().put(SimpleOpenNI.SKEL_LEFT_KNEE, "Left knee");
		getJointNames().put(SimpleOpenNI.SKEL_RIGHT_KNEE, "Right knee");
		getJointNames().put(SimpleOpenNI.SKEL_LEFT_FOOT, "Left foot");
		getJointNames().put(SimpleOpenNI.SKEL_RIGHT_FOOT, "Right foot");
		
		// Masses
		// Taken from: http://books.google.com.au/books?id=SUqWUXGx5wQC&pg=PA302&dq=Segment+Properties+%28Tables+A.2.2+and+A.2.3%29&ei=8dWnSrPsJZvQNPPskZwK#v=onepage&q=Segment%20Properties%20(Tables%20A.2.2%20and%20A.2.3)&f=false
		getJointMasses().put(SimpleOpenNI.SKEL_HEAD, 0.05);
		getJointMasses().put(SimpleOpenNI.SKEL_NECK, 0.021);
		getJointMasses().put(SimpleOpenNI.SKEL_LEFT_SHOULDER, 0.05);
		getJointMasses().put(SimpleOpenNI.SKEL_RIGHT_SHOULDER, 0.05);
		getJointMasses().put(SimpleOpenNI.SKEL_LEFT_ELBOW, 0.033);
		getJointMasses().put(SimpleOpenNI.SKEL_RIGHT_ELBOW, 0.033);
		getJointMasses().put(SimpleOpenNI.SKEL_LEFT_HAND, 0.019);
		getJointMasses().put(SimpleOpenNI.SKEL_RIGHT_HAND, 0.019);
		getJointMasses().put(SimpleOpenNI.SKEL_TORSO, 0.303);
		getJointMasses().put(SimpleOpenNI.SKEL_LEFT_HIP, 0.04);
		getJointMasses().put(SimpleOpenNI.SKEL_RIGHT_HIP, 0.04);
		getJointMasses().put(SimpleOpenNI.SKEL_LEFT_KNEE, 0.105);
		getJointMasses().put(SimpleOpenNI.SKEL_RIGHT_KNEE, 0.105);
		getJointMasses().put(SimpleOpenNI.SKEL_LEFT_FOOT, 0.045);
		getJointMasses().put(SimpleOpenNI.SKEL_RIGHT_FOOT, 0.045);
		
		// Max energies
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_HEAD, 400.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_NECK, 300.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_LEFT_SHOULDER, 700.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_RIGHT_SHOULDER, 700.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_LEFT_ELBOW, 5000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_RIGHT_ELBOW, 5000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_LEFT_HAND, 10000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_RIGHT_HAND, 10000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_TORSO, 15000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_LEFT_HIP, 700.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_RIGHT_HIP, 700.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_LEFT_KNEE, 5000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_RIGHT_KNEE, 5000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_LEFT_FOOT, 10000.0);
		getJointMaxEnergies().put(SimpleOpenNI.SKEL_RIGHT_FOOT, 10000.0);
		
		// Build human structure
		for (Integer jointId : getJointNames().keySet()) {
			getHumanJointStructure().addVertex(jointId);
		}
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_TORSO);
		
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_LEFT_SHOULDER);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_LEFT_ELBOW);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_LEFT_ELBOW, SimpleOpenNI.SKEL_LEFT_HAND);
		
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_RIGHT_SHOULDER);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_RIGHT_ELBOW);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_RIGHT_ELBOW, SimpleOpenNI.SKEL_RIGHT_HAND);
		
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_LEFT_HIP);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_LEFT_HIP, SimpleOpenNI.SKEL_LEFT_KNEE);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT);
		
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_RIGHT_HIP);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE);
		getHumanJointStructure().addEdge(SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT);
	}
	
	public Joint(User user, int id) {
		jointId = id;
		this.user = user;
	}
	
	public static int getJointIdFromKey(String key) {
		try {
			return Integer.parseInt(SimpleOpenNI.class.getField(key).get(SimpleOpenNI.class).toString());
		} catch (IllegalArgumentException e1) {
			return Integer.MIN_VALUE;
		} catch (SecurityException e1) {
			return Integer.MIN_VALUE;
		} catch (IllegalAccessException e1) {
			return Integer.MIN_VALUE;
		} catch (NoSuchFieldException e1) {
			return Integer.MIN_VALUE;
		}
	}
	
	
	/**
	 * Gets real world positions
	 * @return
	 */
	public XnSkeletonJointPosition getCurrentPosition() {
		XnSkeletonJointPosition vector3d = new XnSkeletonJointPosition();
		setPositionConfidence(
				getUser().getMirror().getContext().getJointPositionSkeleton(user.getUserId(), jointId, vector3d)
				? 1 : 0);
		return vector3d;
	}
	
	/**
	 * Get the skeleton joint pos on the screen, not real-world screen
	 */
	public int[] getCurrentPositionOnCanvas(DigitalMirror mirror) {
		Integer[] coords = new Integer[2];
		
		XnVector3D jointVector3d = getCurrentPosition().getPosition();
		XnVector3D canvasVector3d = new XnVector3D();
		
		mirror.getContext().convertRealWorldToProjective(jointVector3d, canvasVector3d);
		
		coords[0] = (int) Math.round(canvasVector3d.getX());
		coords[1] = (int) Math.round(canvasVector3d.getY());
		
		int[] tmpCoords = mirror.convertSourceCoordsToCanvasCoords(coords[0], coords[1],
				mirror.getContext().depthWidth(), mirror.getContext().depthHeight(), User.userScaling);
		
		return tmpCoords;
	}
	
	public void record() {
		if(getLastPosition() == null) {
			setLastPosition(getCurrentPosition());
			return;
		}
		
		// Add and calc the distances
		XnSkeletonJointPosition currentPosition = getCurrentPosition();
		double distance = calculateDistanceMoved(getLastPosition(), currentPosition);
		setLastPosition(currentPosition);
		getDistances().addFirst(distance);
		
		addToAbsoluteDistanceMoved(distance);
		addToDistanceMoved(distance);
		
		// Energies
		Long intervalTime = System.currentTimeMillis() - getUser().getSkeletonLastRecordings().getFirst();
		double energy = calculateEnergySpent(distance, intervalTime);
		getEnergies().addFirst(energy);
		
		addToAbsoluteTotalEnergySpent(energy);
		addToEnergySpent(energy);
	}
	
	/**
	 * Calculate energy spent during the specified time. Returned in joules.
	 * @param distance In meters
	 * @param time In millisec
	 * @return
	 */
	protected double calculateEnergySpent(double distance, Long time) {
		// Convert time to seconds
		double elapsedTime = (double) time / 1000; // millisec to sec
		
		// KE = (1/2) * m * v^2
		double ke = 0.5 * (getJointMasses().get(getJointId()) * getUser().getUserMass()) * Math.pow(((double) distance / elapsedTime), 2);
		
		return ke;
	}
	
	protected double calculateDistanceMoved(XnSkeletonJointPosition lastPosition, XnSkeletonJointPosition currentPosition) {
		double distance = 0;
		
		XnVector3D lastPos = lastPosition.getPosition();
		XnVector3D curPos = currentPosition.getPosition();
		if(getPositionConfidence() < 0.5) {
			return distance;
		}
		
		final XnVector3D relativePoint;
		
		if((getMovementRelativeTo() != null)
				&& (relativePoint = getMovementRelativeTo().getCurrentPosition().getPosition()) != null) {
			XnVector3D relativeLastPos = new XnVector3D();
			relativeLastPos.setX(relativePoint.getX() - lastPos.getX());
			relativeLastPos.setY(relativePoint.getY() - lastPos.getY());
			relativeLastPos.setZ(relativePoint.getZ() - lastPos.getZ());
				
			XnVector3D relativeCurrentPos = new XnVector3D();
			relativeCurrentPos.setX(relativePoint.getX() - curPos.getX());
			relativeCurrentPos.setY(relativePoint.getY() - curPos.getY());
			relativeCurrentPos.setZ(relativePoint.getZ() - curPos.getZ());
			
			lastPos = relativeLastPos;
			curPos = relativeCurrentPos;
		}
		
		/*distance = Math.sqrt(Math.pow(lastPos.getX() - curPos.getX(), 2)
				+ Math.pow(lastPos.getY() - curPos.getY(), 2)
				+ Math.pow(lastPos.getZ() - curPos.getZ(), 2));*/
		distance = Utils.distanceBetweenTwoPoints(lastPos.getX(), lastPos.getY(), lastPos.getZ(),
				curPos.getX(), curPos.getY(), curPos.getZ());
		
		// Distance in mm, conver to meters
		distance = distance / 1000;
		
		// Distance cannot be over 3 m (fixes some random errors in data)
		distance = distance > 3 ? 3 : distance;
		
		return distance;
	}
	
	public static double calculateDistanceBetweenTwoJoints(User user, Integer fromJointId, Integer toJointId) {
		
		//GraphPath<Integer, DefaultEdge> path = new DijkstraShortestPath(getHumanJointStructure(), fromJointId, toJointId).getPath();
		
		List<Limb> path = DijkstraShortestPath.findPathBetween(getHumanJointStructure(), fromJointId, toJointId);
		
		double distance = 0;
		
		if(path != null) {
			for (Limb limb : path) {
				XnVector3D fromVector3d = user.getJoint((Integer) limb.getSource()).getCurrentPosition().getPosition();
				XnVector3D toVector3d = user.getJoint((Integer) limb.getTarget()).getCurrentPosition().getPosition();
				
				distance += Utils.distanceBetweenTwoPoints(fromVector3d.getX(), fromVector3d.getY(), fromVector3d.getZ(),toVector3d.getX(), toVector3d.getY(), toVector3d.getZ());
			}
		}
		
		return distance;
	}
	
	public void removeLastRecording() {
		// Distances
		addToDistanceMoved(0 - getDistances().removeLast());
		
		// Energies
		addToEnergySpent(0 - getEnergies().removeLast());
	}
	
	
	/// Getters and setters

	public static HashMap<Integer, String> getJointNames() {
		return jointNames;
	}
	
	public static HashMap<Integer, Double> getJointMasses() {
		return jointMasses;
	}

	public int getJointId() {
		return jointId;
	}

	public User getUser() {
		return user;
	}

	public double getAbsoluteTotalDistanceMoved() {
		return absoluteTotalDistanceMoved;
	}

	public void setAbsoluteTotalDistanceMoved(double absoluteTotalDistanceMoved) {
		this.absoluteTotalDistanceMoved = absoluteTotalDistanceMoved;
	}
	
	public double addToAbsoluteDistanceMoved(double add) {
		this.absoluteTotalDistanceMoved += add;
		return this.absoluteTotalDistanceMoved;
	}

	public double getDistanceMoved() {
		return distanceMoved;
	}

	public void setDistanceMoved(double distanceMoved) {
		this.distanceMoved = distanceMoved;
	}
	
	public double addToDistanceMoved(double add) {
		this.distanceMoved += add;
		return this.distanceMoved;
	}

	public LinkedList<Double> getDistances() {
		return distances;
	}

	public XnSkeletonJointPosition getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(XnSkeletonJointPosition lastPosition) {
		this.lastPosition = lastPosition;
	}

	public void setMovementRelativeTo(Joint movementRelativeTo) {
		this.movementRelativeTo = movementRelativeTo;
	}

	public Joint getMovementRelativeTo() {
		return movementRelativeTo;
	}

	public void setAbsoluteTotalEnergySpent(double absoluteTotalEnergySpent) {
		this.absoluteTotalEnergySpent = absoluteTotalEnergySpent;
	}

	public double addToAbsoluteTotalEnergySpent(double add) {
		this.absoluteTotalEnergySpent += add;
		return this.absoluteTotalEnergySpent;
	}
	
	public double getAbsoluteTotalEnergySpent() {
		return absoluteTotalEnergySpent;
	}

	public void setEnergySpent(double energySpent) {
		this.energySpent = energySpent;
	}
	
	public double addToEnergySpent(double add) {
		this.energySpent += add;
		return this.energySpent;
	}

	public double getEnergySpent() {
		return energySpent;
	}

	public LinkedList<Double> getEnergies() {
		return energies;
	}

	public void setPositionConfidence(double positionConfidence) {
		this.positionConfidence = positionConfidence;
	}

	public double getPositionConfidence() {
		return positionConfidence;
	}

	public static void setJointMaxEnergies(HashMap<Integer, Double> jointMaxEnergies) {
		Joint.jointMaxEnergies = jointMaxEnergies;
	}

	public static HashMap<Integer, Double> getJointMaxEnergies() {
		return jointMaxEnergies;
	}

	public static void setJointMasses(HashMap<Integer, Double> jointMasses) {
		Joint.jointMasses = jointMasses;
	}

	public static void setJointNames(HashMap<Integer, String> jointNames) {
		Joint.jointNames = jointNames;
	}

	public static void setHumanJointStructure(UndirectedGraph<Integer, Limb> humanJointStructure) {
		Joint.humanJointStructure = humanJointStructure;
	}

	public static UndirectedGraph<Integer, Limb> getHumanJointStructure() {
		return humanJointStructure;
	}
}

