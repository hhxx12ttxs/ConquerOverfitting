package org.jlab.rec.ftof.event;

import java.util.Comparator;

import org.jlab.geom.prim.Vector3D;

/**
 * 
 * GEMC equivalent of {@link Hit Hit}.
 * 
 * Data stored includes:
 * 
 *<ul>
 *<li> GEMC particle ID
 *<li> GEMC entrance energy
 *<li> GEMC energy deposited
 *<li> GEMC time
 *<li> GEMC position
 *<li> GEMC momentum 
 *</ul>
 * 
 * @author acolvill
 *
 */


public class GEMCHit {

	
	private double GEMCparticleID;
	private double GEMCenergyEntrance;
	private double GEMCenergyDeposited;
	private double GEMCtime;
	private Vector3D GEMCpos;
	private Vector3D GEMCmomentum;

	public GEMCHit(double GEMCparticleID, double GEMCenergyEntrance,
                        double GEMCenergyDeposited, double GEMCtime, Vector3D GEMCpos,
			Vector3D GEMCmomentum) {
		
		this.GEMCparticleID = GEMCparticleID;
		this.GEMCenergyEntrance = GEMCenergyEntrance ;
		this.GEMCenergyDeposited = GEMCenergyDeposited;
		this.GEMCtime = GEMCtime ;
		this.GEMCpos = GEMCpos;
		this.GEMCmomentum = GEMCmomentum;
		
	}
	
	public double getGEMCparticleID() {
		return GEMCparticleID;
	}

	public double getGEMCenergyEntrance() {
		return GEMCenergyEntrance;
	}

	public double getGEMCenergyDeposited() {
		return GEMCenergyDeposited;
	}

	public double getGEMCtime() {
		return GEMCtime;
	}

	public Vector3D getGEMCpos() {
		return GEMCpos;
	}

	public Vector3D getGEMCmomentum() {
		return GEMCmomentum;
	}
	
	// Comparator
    public static class EnergyDeposited implements Comparator<GEMCHit> {
        @Override
        public int compare(GEMCHit arg0, GEMCHit arg1) {
        	if ((arg0.GEMCenergyDeposited - arg1.GEMCenergyDeposited) < 0){
        		return(1);
        		}
        	if ((arg0.GEMCenergyDeposited - arg1.GEMCenergyDeposited) > 0){
        		return(-1);
        		}
        	return(0);
       }
    }


}

