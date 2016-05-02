
package org.jlab.rec.ftof.reconstruction;

import org.jlab.rec.ftof.calibration.PaddleCalibration;
import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.ftof.event.Paddle;
import org.jlab.rec.ftof.event.Hit;
import org.jlab.rec.ftof.geometry.PanelGeometry;
import org.jlab.rec.ftof.utilities.Util;


/**
 * Reconstructs the data of a single paddle of the detector.
 * <p>
 * Combines the data from both sides of the paddle into a {@link Hit Hit}, and adds the Hit to 
 * the {@link Paddle Paddle}.
 * <p>
 * The Hit position, energy and time are worked out using different methods depending on
 * the status of the paddle as defined in {@link PaddleConvertor PaddleConvertor}.
 *
 * @author acolvill
 */

public class PaddleReconstruction {
	
    private final Paddle paddle;
    private final PanelGeometry panelGeom;
    private final double padLength;
    
    private double timeL;
    private double timeR;
    private double energyL;
    private double energyR;
	
    private double veffL;
    private double veffR;
    private double attenL;
    private double attenR;
    private double yOffset;

    private double distanceFromCentre;

    double uncDistanceFromCentre = 0.0;

    private Vector3D paddleCentreVector;
    private Vector3D paddleDirectionVector;
    private double uncTimeL;
    private double uncTimeR;

    private double uncVeffL;
    private double uncVeffR;

    private double uncEnergyL;
    private double uncEnergyR;

    private double uncAttenL;
    private double uncAttenR;
	
	
	
    /**
     * Creates an object to reconstruct a paddle of the detector.
     *
     * @param pad , the Paddle being reconstructed
     * @param panelGeom , the PaddleGeometry
     */
        
    public PaddleReconstruction(Paddle pad, PanelGeometry panGeom)
    {
    	
        this.paddle = pad;
        this.panelGeom = panGeom;
        this.padLength = panelGeom.getLength( pad.getID() );
        
        //this.length = panGeom;
        
        timeL = paddle.getTime().getLeft();
	timeR = paddle.getTime().getRight();	
	uncTimeL = paddle.getUncTime().getLeft();
	uncTimeR = paddle.getUncTime().getRight();
		
	energyL = paddle.getEnergy().getLeft();
    	energyR = paddle.getEnergy().getRight();
    	uncEnergyL = paddle.getUncEnergy().getLeft();
    	uncEnergyR = paddle.getUncEnergy().getRight();
       	   
        PaddleCalibration cal = paddle.getCalibration();
    	
	veffL = cal.getSidedValue(PaddleCalibration.Parameter.VEFF).getLeft();
    	veffR = cal.getSidedValue(PaddleCalibration.Parameter.VEFF).getRight();
    	uncVeffL = cal.getSidedValue(PaddleCalibration.Parameter.VEFF_U).getLeft();
    	uncVeffR = cal.getSidedValue(PaddleCalibration.Parameter.VEFF_U).getRight();
        	
    	attenL = cal.getSidedValue(PaddleCalibration.Parameter.ATTEN).getLeft();
    	attenR = cal.getSidedValue(PaddleCalibration.Parameter.ATTEN).getRight();
    	uncAttenL = cal.getSidedValue(PaddleCalibration.Parameter.ATTEN_U).getLeft();
    	uncAttenR = cal.getSidedValue(PaddleCalibration.Parameter.ATTEN_U).getRight();
    	    	
    	yOffset = cal.getValue(PaddleCalibration.Parameter.YOFFSET);
    	
    }
    
    
    /**
     * Reconstructs the paddle, by calculating the Hit, then adding it to the Paddle
     */
  
    public void reconstructPaddle(boolean useTracking)
    {
      paddle.setHit(calculateHit(useTracking));    
    }
    
    
    /**
     * Calculates the fields of the Hit.  Position, energy and time are 
     * worked out differently depending on the status of the paddle 
     * as defined in PaddleReader.
     *
     */
    
    private Hit calculateHit(boolean useTracking)
    {
    	
    	paddleCentreVector = panelGeom.getCentreVector(paddle.getID());
    	paddleDirectionVector = panelGeom.getDirectionVector();
    	    	
    	// get distance, energy and time in different ways depending on status of paddle
    	double time      = 0.0;
    	double energy    = 0.0;
    	double uncTime   = 0.0;
    	double uncEnergy = 0.0;
    	        
        if (useTracking) {
            System.err.println("PaddleReconstruction: " 
                              +"useTraking is not implemented yet ");
        }
        
        
    	switch(paddle.getStatus()) {
    	            
    	case PaddleConvertor.TDCL_GOOD: 
    		
            if (useTracking) {
                // TODO should have tracking input
            }
            distanceFromCentre = 0.0;
            time = getTimeOneSided("left");
            uncDistanceFromCentre = getDistanceUncFromLength();
            uncTime = getTimeUncFromLength("left");
            break;
    		
    	case PaddleConvertor.LEFT_OK:
    		
            if (useTracking) {
                // TODO should have tracking input
            }
            distanceFromCentre = 0.0;
            time = getTimeOneSided("left");
            energy = getEnergyOneSided("left");
            uncDistanceFromCentre = getDistanceUncFromLength();
            uncEnergy = getEnergyUncOneSided("left"); 
            uncTime = getTimeUncFromLength("left");	
            break;      
    		
    	case PaddleConvertor.TDCR_GOOD:   
    	
            if (useTracking) {
                // TODO should have tracking input
            }
            distanceFromCentre = 0.0;  
            time = getTimeOneSided("right");
            uncDistanceFromCentre = getDistanceUncFromLength();
            uncTime = getTimeUncFromLength("right");
            break; 
			
    	case PaddleConvertor.TDCS_ONLY:
    		
            distanceFromCentre = getDistanceFromTime();
            time = getTime();
            uncDistanceFromCentre = getDistanceUncFromTime();
            uncTime = getTimeUncAltVersion();
            break; 
    		    		
    	case PaddleConvertor.ADCL_TDCR:
    		
            if (useTracking) {
               // TODO should have tracking input
            }
            distanceFromCentre = 0.0;
            time = getTimeOneSided("right");
            energy = getEnergyOneSided("left");
            uncDistanceFromCentre = getDistanceUncFromLength();
            uncTime = getTimeUncFromLength("right");
            break;
    	
    	case PaddleConvertor.LEFT_OK_NO_ADCR:
    		
            distanceFromCentre = getDistanceFromTime();
            time = getTime();
            energy = getEnergyOneSided("left"); 
            uncDistanceFromCentre = getDistanceUncFromTime();
            uncTime = getTimeUncAltVersion();
            uncEnergy = getEnergyUncOneSided("left"); 
            break;
    		
       	case PaddleConvertor.TDCL_ADCR:
       		
            if (useTracking) {
            // TODO should have tracking input
            }
            distanceFromCentre = 0.0;  
            energy = getEnergyOneSided("right");
            time = getTimeOneSided("left");
            uncDistanceFromCentre = getDistanceUncFromLength();
            uncTime = getTimeUncFromLength("right");
            uncEnergy = getEnergyUncOneSided("right"); 
            break;         
           
    	case PaddleConvertor.LEFT_OK_NO_TDCR:
    		
            if (useTracking) {
            // TODO should have tracking input
            }
            distanceFromCentre = getDistanceFromEnergy();
            time = getTimeOneSided("left");
            energy = getEnergy();
            uncDistanceFromCentre = getDistanceUncFromEnergy();
            uncTime = getTimeUncOneSided("left");
            uncEnergy = getEnergyUncMissingTDC();
            break;  
    	
    	case PaddleConvertor.RIGHT_OK: 
    		
            if (useTracking) {
                // TODO should have tracking input
            }
            distanceFromCentre = 0.0;
            time = getTimeOneSided("right");
            energy = getEnergyOneSided("right");
            uncDistanceFromCentre = getDistanceUncFromLength();
            uncTime = getTimeUncFromLength("right");
            uncEnergy = getEnergyUncOneSided("right");
            break;
    		
    	case PaddleConvertor.RIGHT_OK_NO_ADCL:
    		
            distanceFromCentre = getDistanceFromTime();
            time = getTime();
            energy = getEnergyOneSided("right");
            uncDistanceFromCentre = getDistanceUncFromTime();
            uncTime = getTimeUncAltVersion();
            uncEnergy = getEnergyUncOneSided("right");
            break;  
    		
    	case PaddleConvertor.RIGHT_OK_NO_TDCL: 
    		
            if (useTracking) {
                // TODO should have tracking input
            }
            distanceFromCentre = getDistanceFromEnergy();
            time = getTimeOneSided("right");
            energy = getEnergy();
            uncDistanceFromCentre = getDistanceUncFromEnergy();
            uncTime = getTimeUncOneSided("right");
            uncEnergy = getEnergyUncMissingTDC();
            break;
    		
    	case PaddleConvertor.BOTH_OK:
    		
            distanceFromCentre = getDistanceFromTime();
            time = getTime();
            energy = getEnergy();
            uncDistanceFromCentre = getDistanceUncFromTime();
            uncEnergy = getEnergyUnc();
            uncTime = getTimeUncAltVersion();
            break;
        
    	}
    	
        
    	// calculate vector pointing from paddle center along the paddle to the hit
    	//Vector3D hitPosition = paddleDirectionVector.scale(distanceFromCentre);
        Vector3D hitPosition = 
          new Vector3D(paddleDirectionVector.x(),paddleDirectionVector.y(),paddleDirectionVector.z());
        hitPosition.scale(distanceFromCentre);

    	// calculate vector pointing from target to the hit
        Vector3D hitPositionSector = 
          new Vector3D(hitPosition.x(),hitPosition.y(),hitPosition.z());
        hitPositionSector.add(paddleCentreVector);

                
     	// uncertainty in X and Z comes from geometry service
	double deltaZ = panelGeom.getExtentZ();
	double deltaX = panelGeom.getExtentX();
    	
    	Vector3D uncHitPosition 
            = new Vector3D(deltaX/2,uncDistanceFromCentre,deltaZ/2);
    	
        Hit hit = new Hit(energy, time, uncEnergy, uncTime, 
                          hitPositionSector, uncHitPosition);
        
        Util.prn(10,"  Reconstructed hit for paddle #" + paddle.getID()
                     + ". E= " +  String.format("%.3f", energy) 
                     + " T= " + String.format("%.3f", time)
                     + " XYZ= " + String.format("%.1f", hitPositionSector.x() )
                     + " "      + String.format("%.1f", hitPositionSector.y() )
                     + " "      + String.format("%.1f", hitPositionSector.z() )
                    );
        
        Util.prn(11,"-----Hit data------------------------------------");
        Util.prn(11,"" + hit);
        Util.prn(11,"--------------------------------------------------");

        
    	return hit;
    	
    }
    

    /**
     * Calculates the hit position from the center of the paddle using tracking. 
     * Used only if getDistanceFromTime() cannot be used, due to missing 
     * TDC data on one side.
     */
        
    private double getDistanceFromTracking()
    {
    	//TODO
    	return(0.0);
    }
    
    
    /**
     * Calculates the hit position from the center of the paddle using left and 
     * right TDC times.  Best case scenario.  Can only be used when we have both of those times.
     */
    
     private double getDistanceFromTime() {
    	  return ( veffL * veffR * (timeL-timeR-yOffset) / (veffL+veffR) ) ;
    }
    
    
     //TODO I switched around left and right in the getDistanceFromEnergyFunction, is this correct?
     
    /**
     * Calculates the hit position from the centre of the paddle using left and 
     * right ADC energies.  Approximation.  Used only when there is no other method
     * available.
     */
    
    private double getDistanceFromEnergy() {
      	return( attenL * attenR * Math.log(energyR/energyL) / (attenL+attenR) );
    }
    
    
    /**
     * Calculates the hit time based on data from both TDCs.
     * Best case scenario. 
     */
       
    private double getTime() {
    	return ( (timeL+timeR)/2.0 - padLength/(veffL+veffR) 
                                   - distanceFromCentre * (veffR-veffL) / (2.0*veffR*veffL) ) ;
    }
    
    
    /**
     * Calculates the hit time based on data from only one TDC.
     *  
     */
    
    private double getTimeOneSided(String side) {
    	if(side.equals("left")) {
            return( timeL - padLength/(2.*veffL) - distanceFromCentre/veffL - yOffset/2 );
        }	
    	if(side.equals("right")){
            return( timeR - padLength/(2.*veffR) + distanceFromCentre/veffR + yOffset/2 );
        }
    	return(0.0);
    }
        
    
    /**
     * Calculates the hit energy based on data from both TDCs.
     * Best case scenario. 
     */
    
    private double getEnergy() {
                
    	return( Math.sqrt(energyL * energyR 
                * Math.exp( ( (padLength/2.) * (attenR+attenL) + distanceFromCentre * (attenR-attenL))
                            / (attenL*attenR) ) ) );
    }
    
    
    /**
     * Calculates the hit energy based on data from only one ADC
     * 
     */
    
    private double getEnergyOneSided(String side) {
    	if(side.equals("left")){
            return( energyL * Math.exp( (padLength/2. + distanceFromCentre) / attenL ) );
        }
    	if(side.equals("right")){
            return( energyR * Math.exp( (padLength/2. - distanceFromCentre) / attenR ) );
        }
    	return(0.0);
    }
    
    
    /**
     * Calculates the energy uncertainty when both sides of TDCS and ADCS are present
     * 
     * @return the uncertainty
     */
        
    private double getEnergyUnc() {
		
    	double dEdy,dEdEl,dEdEr,dEdLl,dEdLr,ex;

    	ex    = Math.exp(distanceFromCentre * ( attenR - attenL ) / ( 2.0 * attenR * attenL ) );
    	dEdEl = 0.5 * Math.sqrt( energyR / energyL ) * ex;
    	dEdEr = 0.5 * Math.sqrt( energyL / energyR ) * ex;
    	dEdy  = Math.sqrt( energyL * energyR ) * ex * ( attenL - attenR ) / ( 2.0 * attenL * attenR );
    	dEdLr = Math.sqrt( energyL * energyR ) * ex * distanceFromCentre / ( 2.0 * attenL * attenR );
    	dEdLl = - dEdLr;
    	
    	return(Math.sqrt( dEdEl * dEdEl * uncEnergyL * uncEnergyL
    			 		+ dEdy  * dEdy  * Math.pow(uncDistanceFromCentre, 2)
    					+ dEdEr * dEdEr * uncEnergyR * uncEnergyR
    					+ dEdLl * dEdLl * uncAttenL * uncAttenL
    					+ dEdLr * dEdLr * uncAttenR * uncAttenR ) );
        	
	}
	
    
    /**
     * Calculates the energy uncertainty when both ADCS are present, but a TDC is missing
     * 
     * @return the uncertainty
     */
        
	private double getEnergyUncMissingTDC() {
	
		double dEdEl,dEdLl,dEdLr;

		dEdEl = Math.exp( ( attenR - attenL ) / 2.0 / ( attenR + attenL ) );
		dEdLr = energyL * attenL * dEdEl / ( attenL + attenR ) / ( attenL + attenR );
		dEdLl = - energyL * attenR * dEdEl / ( attenL + attenR ) / ( attenL + attenR );
		
		return( Math.sqrt(  dEdEl * dEdEl * uncEnergyL * uncEnergyL
			         + dEdLl * dEdLl * uncAttenL * uncAttenL
			         + dEdLr * dEdLr * uncAttenR * uncAttenR ) );
		
	}
	
	
	/**
     * Calculates the energy uncertainty when one ADC is missing
     * 
     * @return the uncertainty
     */
	    
    private double getEnergyUncOneSided(String side) {
		
    	double dEdy,dEdEl,dEdEr,dEdLl,dEdLr; 
    	
    	if(side.equals("left")){
    		
    		dEdEl = Math.exp( distanceFromCentre / attenL );
    		dEdy  = dEdEl / attenL;
    		dEdLl = - dEdEl * distanceFromCentre / ( attenL * attenL ) ;

    		return ( Math.sqrt( dEdEl * dEdEl * uncEnergyL * uncEnergyL +
    				 			dEdy  * dEdy  * Math.pow(uncDistanceFromCentre, 2) +
    				 			dEdLl * dEdLl * uncAttenL  * uncAttenL ) );
       	}
	
		if(side.equals("right")){
			
			 dEdEr = Math.exp(-distanceFromCentre/attenR);
			 dEdy  = - dEdEr / attenR;
			 dEdLr = dEdEr * distanceFromCentre / ( attenR * attenR );
			 return ( Math.sqrt( dEdEr * dEdEr * uncEnergyR * uncEnergyR +
					        dEdy  * dEdy  * Math.pow(uncDistanceFromCentre, 2) +
				            dEdLr * dEdLr * uncAttenR  * uncAttenR ) );
			
    	}
		
		return(0.0);
	
   	
	}
    
    
    /**
     * Calculates the time uncertainty when both TDCs are present
     * CLAS12 corrected version
     * 
     * @return the uncertainty
     */
  

    private double getTimeUncAltVersion() {
 
    		double dtdvl,dtdvr,dtdtl,dtdtr, dtdy;
 
    				double vl  =  veffL;
    				double vr  =  veffR;
    				double dvl =  uncVeffL;
    				double dvr =  uncVeffR;
    				
    				dtdvl = distanceFromCentre / (2*vl*vl);
    				dtdvr = - distanceFromCentre / (2*vr*vr);
    				dtdy = (vl - vr) / (2*vl*vr);
    				dtdtl= 0.5;
    				dtdtr= 0.5;
 
    				return(Math.sqrt(   dtdvl * dtdvl * dvl * dvl
    						+ dtdvr * dtdvr * dvr * dvr 
    						+ dtdtl * dtdtl * uncTimeL * uncTimeL
    						+ dtdtr * dtdtr * uncTimeR * uncTimeR 
    						+ dtdy * dtdy * uncDistanceFromCentre * uncDistanceFromCentre) );
 
    }

    

    /**
     * Calculates the time uncertainty when you are missing a TDC, but have both ADCS
     * 
     * @return the uncertainty
     */
       
	private double getTimeUncOneSided(String side) {
		
		if(side.equals("left")){
			
    		return(Math.sqrt(  uncTimeL * uncTimeL 
    				   		 + uncDistanceFromCentre * uncDistanceFromCentre / ( veffR * veffR )
    				   		 + distanceFromCentre * distanceFromCentre * uncVeffR
    				   		 * uncVeffR / Math.pow( veffR , 4) ) );
    	}
	
		if(side.equals("right")){
			
			return(Math.sqrt(  uncTimeR * uncTimeR 
						     + uncDistanceFromCentre * uncDistanceFromCentre / ( veffR * veffR )
							 + distanceFromCentre * distanceFromCentre * uncVeffR
							 * uncVeffR / Math.pow(veffR, 4) ) );
    	}
	
		return(0.0);
	
	}
	
	
	 /**
     * Calculates the time uncertainty when you only have one TDC, and one ADC 
     * 
     * @return the uncertainty
     */	
	
	private double getTimeUncFromLength(String side) {
		
		if(side.equals("left")){
    		return( panelGeom.getLength(paddle.getID()) / veffL / Math.sqrt(12) );
    	}
	
		if(side.equals("right")){
			return( panelGeom.getLength(paddle.getID()) / veffR / Math.sqrt(12) );
    	}
	
	    return(0);
	}

	
	/**
     * Calculates the hit distanceFromCentre uncertainty when you are missing a TDC, but have both ADCS
     * 
     * @return the uncertainty
     */	

	private double getDistanceUncFromEnergy() {
		
		  double dydLl,dydLr,dydEl,dydEr;

		  dydLl = Math.log( energyL / energyR ) * Math.pow( attenL / (attenL+attenR), 2);
		  dydLr = Math.log( energyL / energyR )  * Math.pow( attenR / (attenL+attenR), 2);
		  dydEl = attenL * attenR / energyL / ( attenL + attenR );
		  dydEr = attenL * attenR / energyR / ( attenL + attenR );

		  return(Math.sqrt(   dydLl * dydLl * uncAttenL  * uncAttenL
				  			+ dydLr * dydLr * uncAttenR  * uncAttenR
				  			+ dydEl * dydEl * uncEnergyL * uncEnergyL
				  			+ dydEr * dydEr * uncEnergyR * uncEnergyR ) );
		
	}
	
	
	/**
     * Calculates the hit distanceFromCentre uncertainty when you have both TDCs
     * 
     * @return the uncertainty
     */
	
	private double getDistanceUncFromTime() {
		
		  double dydtl,dydtr,dydvl,dydvr;
		
		  dydtl = veffL * veffR / ( veffL + veffR );
		  dydtr = -dydtl;
		  dydvl = Math.pow(veffR / (veffL+veffR), 2) *  (timeL - timeR);
		  dydvr = Math.pow(veffL / (veffL+veffR), 2) *  (timeL - timeR);

		  return(Math.sqrt(   dydtl * dydtl * uncTimeL * uncTimeL 
			      			+ dydtr * dydtr * uncTimeR * uncTimeR
			      			+ dydvl * dydvl * uncVeffL * uncVeffL
			      			+ dydvr * dydvr * uncVeffR * uncVeffR  )  );
		
	}

	
	
	/**
     * Calculates the hit distanceFromCentre uncertainty when you are missing a TDC
     * 
     * @return the uncertainty
     */
	
	private double getDistanceUncFromLength() {
		
		return ( panelGeom.getLength(paddle.getID()) / Math.sqrt(12.0) );
				
	}


}

