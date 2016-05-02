package org.jlab.rec.ftof.event;
import org.jlab.geom.prim.Vector3D;

/**
 * The combined data from N related adjacent hits on a panel.
 * <p>
 * Data stored includes:
 * 
 *<ul>
 *<li>  Energy
 * <li> Time
 * <li> Position
 * <li> Uncertainties in the above three
 * <li> An array of paddle IDs of the hits that make up the cluster
 * <li> An array of the statuses of the hits that make up the cluster, as defined in {@link org.jlab.clas12.ftof.reconstruction.PaddleConvertor PaddleConvertor}
 * </ul>
 * @author acolvill
 *
 */


public class Cluster {
     
        private int    sectorID;
        private int    panelID;
	private int[]  paddleIDs;
	private int[]  paddleStatuses;
	private double energy;
	private double uncEnergy;
	private double time;
	private double uncTime;
	private Vector3D position;
	private Vector3D uncPosition;
	
	public Cluster(int secID, int panID, int[] iD, int[] status, 
                        double energy, double uncEnergy,
			double time, double uncTime, Vector3D position,
			Vector3D uncPosition) {
		
            this.sectorID = secID;
            this.panelID = panID;
            this.paddleIDs = iD;
            this.paddleStatuses = status;
            this.energy = energy;
            this.uncEnergy = uncEnergy;
            this.time = time;
            this.uncTime = uncTime;
            this.position = position;
            this.uncPosition = uncPosition;

        }

   
        

        public int getSectorID() {
            return sectorID;
	}

        public int getPanelID() {
            return panelID;
	}
        
	public int[] getPaddleIDs() {
            return paddleIDs;
	}

	public int[] getPaddleStatus() {
            return paddleStatuses;
	}

	public double getEnergy() {
            return energy;
	}

	public double getUncEnergy() {
            return uncEnergy;
	}

	public double getTime() {
            return time;
	}

        public void setTime(double t) {
            this.time = t;
	}

	public double getUncTime() {
            return uncTime;
	}

	public Vector3D getPosition() {
            return position;
	}

	public Vector3D getUncPosition() {
            return uncPosition;
	}
	
	public int getClusterLength(){
            return paddleIDs.length;
	}
        
        public int getStatus(){
            int status = 0;
            // cluster status is calculated from hit statuses
            for (int j = 0; j < getPaddleStatus().length; j++) {
                if (j == 0) {
                    status += getPaddleStatus()[j];
                } else {
                    status += Math.pow(100, j)
                            * getPaddleStatus()[j];
                }
            }
            return status;
	}

	@Override
	public String toString()
	{
	     return String.format("ID1: %3d Status1: %3d %n" +
	    		 			  "cluster length     : %8.6f %n" +
	     					  "energy             : %8.6f %n" +
	     					  "energyUnc          : %8.6f %n" +
	     					  "time               : %8.6f %n" +
	     					  "timeUnc            : %8.6f %n" +
	     		              "position (x,y,z)   : %6.3f, %6.3f, %6.3f %n" +
	     		              "positionUnc (x,y,z): %6.3f, %6.3f, %6.3f",
	    		 			   paddleIDs[0], paddleStatuses[0], 
	    		 			   paddleIDs.length*1.0,
	    		 			   energy, uncEnergy, time, uncTime,
	    		 			   position.x(), position.y(), position.z(),
	    		 			   uncPosition.x(), uncPosition.y(), uncPosition.z());
	 
	}
	
	
}

