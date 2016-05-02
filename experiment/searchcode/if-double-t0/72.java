
package org.jlab.rec.ftof.reconstruction;

import org.jlab.rec.ftof.calibration.DetectorCalibration;
import org.jlab.rec.ftof.calibration.PaddleCalibration;
import org.jlab.rec.ftof.calibration.SidedDouble;
import org.jlab.rec.ftof.event.Paddle;
import org.jlab.rec.ftof.geometry.PanelGeometry;
import org.jlab.rec.ftof.utilities.Util;


/**
 * 
 * For a single Paddle object, this class converts left/right ADC and TDC values 
 * into left/right time and energy, respectively. 
 * Also works out the status word of the paddle which indicates the quality of the data.
 * 
 * @author acolvill
 *
 */


public class PaddleConvertor {
	
    // Data status, as used in CLAS6
    public final static int TDCL_GOOD         = 1;
    public final static int ADCL_GOOD         = 2;
    public final static int LEFT_OK           = 3;
    public final static int TDCR_GOOD         = 4;
    public final static int TDCS_ONLY         = 5;
    public final static int ADCL_TDCR         = 6;
    public final static int LEFT_OK_NO_ADCR   = 7; 
    public final static int ADCR_GOOD         = 8;
    public final static int TDCL_ADCR         = 9;
    public final static int ADCS_ONLY         = 10;
    public final static int LEFT_OK_NO_TDCR   = 11;
    public final static int RIGHT_OK          = 12;
    public final static int RIGHT_OK_NO_ADCL  = 13;
    public final static int RIGHT_OK_NO_TDCL  = 14;
    public final static int BOTH_OK           = 15;
	
    private final Paddle paddle;
    private final PanelGeometry panelGeom;
    private final PaddleCalibration cal;
    private final double adcL;
    private final double adcR;
    private final double tdcL;
    private final double tdcR;

    private double energyL;
    private double energyR;
    private double timeL;
    private double timeR;
    private double uncEnergyL;
    private double uncEnergyR;
    private double uncTimeL;
    private double uncTimeR;
	
	
    public PaddleConvertor(Paddle paddle, PanelGeometry panelGeom) {
	this.paddle = paddle;
	this.panelGeom = panelGeom;
	this.cal = paddle.getCalibration();
	this.adcL = paddle.getADC().getLeft();
	this.adcR = paddle.getADC().getRight();
	this.tdcL = paddle.getTDC().getLeft();
	this.tdcR = paddle.getTDC().getRight();
    }
	
	
     /**
     * Calculates left/right times, energies, uncertainties in these and 
     * the status of the paddle and saves them in the Paddle object
     * 
     */
	
    public void convertPaddle(){

	// convert ADC channels to energy
    	getEnergies(panelGeom.getThickness());

        // convert TDC channels to time
    	getTimes();        
        
    	// calculate status word for this paddle
    	int status = calculateStatus();  
    	paddle.setStatus(status);
    	                		  
    	// check that converted time data has at least one valid value.
        // If it has set paddle time and energy
    	if(validConvertedTime(status))
    	{
            SidedDouble energy = new SidedDouble(energyL, energyR);
            SidedDouble time = new SidedDouble(timeL, timeR);
            SidedDouble uncEnergy = new SidedDouble(uncEnergyL,uncEnergyR);
            SidedDouble uncTime = new SidedDouble(uncTimeL,uncTimeR);
            paddle.setEnergy(energy);
            paddle.setUncEnergy(uncEnergy);
            paddle.setTime(time);
            paddle.setUncTime(uncTime);
            
            Util.prn(10,"  Converter for paddle #" + paddle.getID() + ". " 
                         + " EL, ER are " 
                         + String.format("%.3f", energyL) + " " 
                         + String.format("%.3f", energyR) + ". TL, TR are "
                         + String.format("%.3f", timeL) + " " 
                         + String.format("%.3f", timeR) + "." );
            
        } else {		
            paddle.setValidStatus(false);
            Util.prn(10,"  Converter for paddle #" + paddle.getID() + ". "
                         + " Times are invalid." );
    	}
    		
	}
	
    
    
	
    /**
     * Calculates the energies in MeV of both sides of a paddle using the raw ADC channels data
     * and the paddle calibration. 
     * 
     * @param  thickness  thickness of paddle
     */
            
    private void getEnergies(double thickness)
    {
    	
       	SidedDouble NMIP = cal.getSidedValue(PaddleCalibration.Parameter.NMIP_ADC);
    	SidedDouble ped = cal.getSidedValue(PaddleCalibration.Parameter.PED);
             	
    	energyL = getEnergy(adcL,NMIP.getLeft(),ped.getLeft(),thickness);
    	energyR = getEnergy(adcR,NMIP.getRight(),ped.getRight(),thickness);
            	
    	SidedDouble uncNMIP = cal.getSidedValue(PaddleCalibration.Parameter.NMIP_ADC_U);
    	SidedDouble uncPed = cal.getSidedValue(PaddleCalibration.Parameter.PED_U);
    	        	
    	uncEnergyL = getEnergyUncertainty (energyL,uncPed.getLeft(),NMIP.getLeft(),uncNMIP.getLeft(),thickness);       	
    	uncEnergyR = getEnergyUncertainty (energyR,uncPed.getRight(),NMIP.getRight(),uncNMIP.getRight(),thickness);  
    	
    }
    
    
    /**
     * Calculates the energy in MeV of one side of a paddle using the supplied raw ADC channels data
     * and the paddle calibration.
     * 
     * @param  channels, the raw ADC value in channels
     * @param  NMIP, the configuration PaddleParameter NMIP_ADC
     * @param  pedestal, the configuration PaddleParameter PED
     * @param  thickness, the paddle thickness (depth) in cm
     * @return result, the energy in MeV.
     */
            
    private double getEnergy(double channels, double NMIP, double pedestal, double thickness)
    {
    	double result;
    	
    	if (channels-pedestal <= 0.0){
            result = 0.0;
        } else {
            result = ((channels-pedestal)/NMIP) *  DetectorCalibration.DEDX_NMIP * thickness;
        }     
        return result;  
    }
    
    
    /**
     * Calculates the uncertainty in the energy of one side of a paddle using the energy
     * and paddle calibration
     * 
     * @param energy, the energy of one side of a paddle
     * @param uncPedestal, calibration PaddleParameter PED_U
     * @param NMIP, calibration PaddleParameter NMIP_ADC
     * @param uncNMIP, calibration PaddleParameter NMIP_ADC_U
     * @param thickness, the paddle thickness (depth) 
     * @return the energy uncertainty
     */
            
    private double getEnergyUncertainty(double energy, double uncPedestal, 
                                        double NMIP, double uncNMIP, 
                                        double thickness)
    {
    	
      double K =  DetectorCalibration.DEDX_NMIP * thickness;
      if (NMIP > 0.0) {
    	  return (K/NMIP * Math.sqrt(
                              Math.pow(DetectorCalibration.ADC_JITTER,2) 
                            + Math.pow(uncPedestal,2) 
                            + Math.pow(energy,2)*Math.pow(uncNMIP,2)/Math.pow(K,2)));
      }
      return 0.0; 
      
    }


    /**
     * Calculates the times in ns of both sides of a paddle using the raw channels data
     * and the calibration.
     */
    
    private void getTimes()
    {
    	        	 
    	SidedDouble t0 = cal.getSidedValue(PaddleCalibration.Parameter.T0);
    	SidedDouble t1 = cal.getSidedValue(PaddleCalibration.Parameter.T1);
    	SidedDouble t2 = cal.getSidedValue(PaddleCalibration.Parameter.T2);
            	
    	SidedDouble walk0 = cal.getSidedValue(PaddleCalibration.Parameter.WALK0);
    	SidedDouble walk1 = cal.getSidedValue(PaddleCalibration.Parameter.WALK1);
    	SidedDouble walk2 = cal.getSidedValue(PaddleCalibration.Parameter.WALK2);
    	
    	SidedDouble ped = cal.getSidedValue(PaddleCalibration.Parameter.PED);
           	
    	// the following calibration parameters are the same for all paddles
    	double refADC = DetectorCalibration.REF_ADC;
    	double pulser = DetectorCalibration.PULSER;
    	double discThresh = DetectorCalibration.DISC_THRESH;
    	
    	timeL = getTime(tdcL, adcL, ped.getLeft(), discThresh, 
                        t0.getLeft(), t1.getLeft(), t2.getLeft(), 
                        walk0.getLeft(), walk1.getLeft(), walk2.getLeft(), 
                        refADC, pulser);
    	
    	timeR = getTime(tdcR, adcR, ped.getRight(), discThresh, 
                        t0.getRight(), t1.getRight(), t2.getRight(), 
                        walk0.getRight(), walk1.getRight(), walk2.getRight(), 
                        refADC, pulser);		
    	
    	SidedDouble uncPed = cal.getSidedValue(PaddleCalibration.Parameter.PED_U); 
    	
       	SidedDouble uncT0 = cal.getSidedValue(PaddleCalibration.Parameter.T0_U);
       	SidedDouble uncT1 = cal.getSidedValue(PaddleCalibration.Parameter.T1_U);
       	SidedDouble uncT2 = cal.getSidedValue(PaddleCalibration.Parameter.T2_U);
       	
      	SidedDouble uncWalk0 = cal.getSidedValue(PaddleCalibration.Parameter.WALK0_U);
      	SidedDouble uncWalk1 = cal.getSidedValue(PaddleCalibration.Parameter.WALK1_U);
       	SidedDouble uncWalk2 = cal.getSidedValue(PaddleCalibration.Parameter.WALK2_U);
            	
    	uncTimeL = getTimeUncertainty(tdcL, adcL, ped.getLeft(), uncPed.getLeft(), 
                        discThresh, t0.getLeft(), t1.getLeft(), t2.getLeft(),
                        walk0.getLeft(), walk1.getLeft(), walk2.getLeft(), 
                        refADC, uncT0.getLeft(), uncT1.getLeft(), uncT2.getLeft(), 
                        uncWalk0.getLeft(), uncWalk1.getLeft(), uncWalk2.getLeft());
    	
    	uncTimeR = getTimeUncertainty(tdcR, adcR, ped.getRight(), uncPed.getRight(), 
                        discThresh, t0.getRight(), t1.getRight(), t2.getRight(),
                        walk0.getRight(), walk1.getRight(), walk2.getRight(), 
                        refADC, uncT0.getRight(), uncT1.getRight(), uncT2.getRight(), 
                        uncWalk0.getRight(), uncWalk1.getRight(), uncWalk2.getRight());
    	
    }
    
         
    /**
     * Calculates the walk-corrected time in ns for a single side of a paddle
     * from supplied raw channel and calibration data. Cut and paste from CLAS6.
     * 
     * returned time = t0 + t1*tdc + t2*tdc^2 + time walk correction
     * where tdc is the input tdc value in channels
     * 
     *  
     * @param tdc, the raw tdc value in channels
     * @param adc, the raw adc value in channels
     * @param ped, the pedestal threshold in channels
     * @param discThresh, the ADC discriminator threshold in channels
     * @param t0, the constant coefficient in the above equation
     * @param t1, the linear coefficient in the above equation
     * @param t2, the quadratic coefficient in the above equation
     * @param walk0, a time walk coefficient
     * @param walk1, a time walk coefficient
     * @param walk2, a time walk coefficient
     * @param refADC, the reference ADC value
     * @param pulser, the pulser normalization constant
     * @return the time in ns
     */
    
    private double getTime(double tdc, double adc, 
                           double ped, double discThresh, 
                           double t0, double t1, double t2, 
                           double walk0, double walk1, double walk2, 
                           double refADC, double pulser)
    {
    	        
    	double pedSubADC = adc - ped;
    	double normADC = pedSubADC / discThresh;
    	double normMax = refADC / discThresh;
    	
    	// work out time without timewalk correction
    	double noTimewalkTime = pulser * (t0 + t1*tdc + t2*tdc*tdc);
    	
    	// check TDC value and pedestal subtracted ADC value
       	if (tdc <= 0) return(DetectorCalibration.TIME_UNDERFLOW);
    	if (tdc >= DetectorCalibration.TDC_MAX) return(DetectorCalibration.TIME_OVERFLOW);
    	if (   pedSubADC <= 0 || pedSubADC >= DetectorCalibration.ADC_MAX 
            || walk0 <= 0) return (noTimewalkTime);
    	
    	// work out timewalk correction
    	double timeWalk;
    	double walkMax;
    	if (normADC < walk0) {
            timeWalk = walk1 / Math.pow(normADC, walk2);
    	}  else{
            timeWalk =   walk1 * ( 1.0 + walk2 ) / Math.pow(walk0,walk2) 
                       - walk1 * walk2 * normADC / Math.pow(walk0,walk2+1.0);
    	}
    	if (normMax < walk0){
    		walkMax = walk1 / Math.pow(normMax, walk2);
    	}   else{
    		walkMax = walk1 * ( 1.0 + walk2 ) / Math.pow(walk0,walk2) 
                        - walk1 * walk2 * normMax / Math.pow(walk0,walk2+1.0);
    	}
    	
    	return ( noTimewalkTime + pulser * (walkMax - timeWalk) );
    	
   }
    
    
    /**
     * 
     * Calculates uncertainty in the time of one side of a paddle using just about 
     * every variable in existence.  Cut and paste from CLAS6.
     * 
     * @param tdc
     * @param adc
     * @param pedestal
     * @param dp, pedestal uncertainty
     * @param disc_thresh
     * @param t0
     * @param t1
     * @param t2
     * @param w0
     * @param w1
     * @param w2
     * @param refADC, reference ADC value
     * @param dt0, uncertainty in t0
     * @param dt1, uncertainty in t1
     * @param dt2, uncertainty in t2
     * @param dw0, uncertainty in w0
     * @param dw1, uncertainty in w1
     * @param dw2, uncertainty in w2
     * @return the time uncertainty
     */

    double getTimeUncertainty(double tdc, double adc, double pedestal, double dp, 
		       double disc_thresh, double t0, double t1, double t2, 
		       double w0, double w1, double w2, double refADC,
		       double dt0, double dt1, double dt2,
		       double dw0, double dw1, double dw2) { 


   	double ped_sub_adc = adc - pedestal;
   	double norm_adc = ped_sub_adc / disc_thresh;
   	double norm_max = refADC / disc_thresh;
   	
   	double dtdA = 0.0, dtdP = 0.0, dtdw1 = 0.0 , dtdw2 = 0.0 , dtdw0 = 0.0;
   	
   	double unc = Math.sqrt( dt0 * dt0 + tdc * tdc * dt1 * dt1 
                              + tdc * tdc * tdc * tdc * dt2 * dt2
                              + ( t1 + 2 * t2 * tdc ) * ( t1 + 2 * t2 * tdc ) *
                                Math.pow(DetectorCalibration.TDC_JITTER,2) );
   	
   	if (tdc <= 0 || tdc >= DetectorCalibration.TDC_MAX) return 0;
   	
   	if(    ped_sub_adc <= 0 || ped_sub_adc >= DetectorCalibration.ADC_MAX 
            || w0 <= 0) return(unc);

   	if (norm_adc < w0 && norm_max < w0){

   	/***************************************************************
   		In the following formulas, N=norm_adc, Nmax=norm_max.

                            w1           w1
    t = t(uncorrected) +   ----    -    ---- 
	          		 			w2	     w2
			      				Nmax          N
   	 ***************************************************************/

            dtdP  = w1 * w2 * Math.pow( norm_adc,-w2-1 ) / disc_thresh;
            dtdA  = -dtdP;
            dtdw0 = 0;
            dtdw1 = Math.pow(norm_max,-w2) - Math.pow(norm_adc,-w2);
            dtdw2 =   w1 * (Math.log(norm_max) / Math.pow(norm_max,w2) 
                    - Math.log(norm_adc) / Math.pow(norm_adc,w2));
  	}
   	
   	if (norm_adc < w0 && norm_max >= w0){

   	/***************************************************************   
                          w1(1+w2)       w1 w2 Nmax        w1
    t = t(uncorrected) +  --------   -   ----------   -   ----
                              w2               w2+1         w2
                            w0               w0            N
   	 ***************************************************************/

            dtdA  = w1 * w2 * Math.pow(norm_adc,-w2-1) / disc_thresh;
            dtdP  = -dtdA;
            dtdw0 = w1 * w2 * (1+w2) * Math.pow(w0,-w2-1) * (norm_max/w0-1);
            dtdw1 = (1+w2) * Math.pow(w0,-w2) - w2 * norm_max * Math.pow(w0,-w2-1) 
                  - Math.pow(norm_adc,-w2);
            dtdw2 = w1 * (Math.pow(w0,-w2) * ( ( 1-norm_max / w0 ) 
                       * ( 1+w2 * Math.log(w0) ) + Math.log(w0) )
   		   - Math.log(norm_adc) * Math.pow(norm_adc,-w2));
   	}
   	
   	if (norm_adc >= w0 && norm_max < w0){

   	/***************************************************************   
                            w1           w1(1+w2)       w1 w2 N
    t = t(uncorrected) +   ----    -    ---------   +   -------
	          	         		w2	     	   w2            w2+1
			      				Nmax            w0            w0
   	 ***************************************************************/  

            dtdA  = w1 * w2 * Math.pow(w0,-w2-1) / disc_thresh;
            dtdP  = -dtdA;
            dtdw0 = w1 * w2 * (1+w2) * Math.pow(w0,-w2-1) * (1-norm_adc/w0);
            dtdw1 = Math.pow(norm_max,-w2) - (1+w2) * Math.pow(w0,-w2) 
                  + w2 * norm_adc * Math.pow(w0,-w2-1);
            dtdw2 = w1 * (Math.log(norm_max) * Math.pow(norm_max,-w2) 
                  - Math.pow(w0,-w2) 
                    * ((1-norm_adc/w0) * (1+w2*Math.log(w0)) + Math.log(w0))); 
   	}
   	
   	if (norm_adc >= w0 && norm_max >= w0){

   	/***************************************************************   
                           w1 w2 (N - Nmax)         
    t = t(uncorrected) +   ----------------    
	          	           	 		w2+1
			          				w0
   	 ***************************************************************/  

            dtdA  = w1 * w2 * Math.pow(w0,-w2-1) / disc_thresh;
            dtdP  = -dtdA;
            dtdw0 = -w1 * w2 * (norm_adc-norm_max) * (w2+1) * Math.pow(w0,-w2-2);
            dtdw1 = w2 * (norm_adc-norm_max) * Math.pow(w0,-w2-1);
            dtdw2 = w1 * (norm_adc-norm_max) * Math.pow(w0,-w2-1) 
                       * (1 + w2*Math.log(w0));
   	}
   	
    return(Math.sqrt( unc * unc + dtdP * dtdP * dp * dp 
                    + dtdw0 * dtdw0 * dw0 * dw0 
                    + dtdw1 * dtdw1 * dw1 * dw1 
                    + dtdw2 * dtdw2 * dw2 * dw2
                    + dtdA * dtdA * Math.pow(DetectorCalibration.ADC_JITTER,2) ));
   	
    }

          
            
    
    
       
    /**
     * Calculates the integer status word of a paddle
     * as used in CLAS6 
     *
     * 
     * @param index, the position of the paddle in the FTOF panel bank
     * @return the integer status word
     */
            
    private int calculateStatus() {
    	
    	int status = 0;
    	
      	if(timeL > DetectorCalibration.TIME_UNDERFLOW && timeL < DetectorCalibration.TIME_OVERFLOW) status += TDCL_GOOD;
    	if(energyL > 0) status += ADCL_GOOD;
    	if(timeR > DetectorCalibration.TIME_UNDERFLOW && timeR < DetectorCalibration.TIME_OVERFLOW) status += TDCR_GOOD;
    	if(energyR > 0) status += ADCR_GOOD;
    	
    	return(status);
    
    }
    
    
    /**
     * Decides whether there is a valid converted time associated with a paddle
     * with a particular integer status word
     *          
     * @param status, the integer status word for the paddle
     * @return a boolean, true if there is a valid converted time, false otherwise
     */
    
    private boolean validConvertedTime(int status){
    	
       if(status  > 0         && 
          status != ADCS_ONLY &&
          status != ADCL_GOOD &&
          status != ADCR_GOOD){
    	   return true;
       }  else{
    	   return false;
       }
       
    }

}


