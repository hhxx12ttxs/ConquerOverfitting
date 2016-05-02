package org.jlab.rec.ftof.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jlab.geom.prim.Vector3D;

import org.jlab.rec.ftof.FTOFReconstruction;
import org.jlab.rec.ftof.io.FtofRawData;
import org.jlab.rec.ftof.reconstruction.ReverseEngineer;
import org.jlab.rec.ftof.event.Panel;
import org.jlab.rec.ftof.event.GEMCHit;
import org.jlab.rec.ftof.event.Paddle;
import org.jlab.rec.ftof.event.EventData;
import org.jlab.rec.ftof.event.Sector;
import org.jlab.rec.ftof.geometry.FtofGeometry;
import org.jlab.rec.ftof.calibration.PaddleCalibration;
import org.jlab.rec.ftof.calibration.DetectorCalibration;
import org.jlab.rec.ftof.calibration.SidedDouble;
import org.jlab.rec.ftof.utilities.Util;


/**
 *  
 * Reads the paddle data from the evio input RAW and/or GEMC banks, then
 * creates {@link Paddle} objects to store the raw ADC and TDC data
 * as well as GEMS data.
 * <p>
 * The result is a list of the {@link PaddleData paddles and its location},
 * to fill the {@link org.jlab.clas12.ftof.event.EventData event data}
 * and start the reconstruction algorithm.
 * 
 * @author acolvill
 *
 */


public class InputGetter {

    private final FtofGeometry    geometry;
    private final DetectorCalibration calibration;
    private PaddleCalibration cal;
    
    
    public InputGetter (FtofGeometry geom, DetectorCalibration cal)
    {
        geometry = geom;
        calibration = cal;    
    }

    
    
    
    /**
     * Get input RAW and GEMC data from arrays in FtofRawData
 for a panel of a given type for all sectors. 
     * 
     * @param panelType is the type of a panel 
     * @param hasRAW if event has RAW bank for a given panel
     * @param hasGEMC if event has GEMC bank for a given panel
     * @param event is EvioDataEvent event
     * @param eventData where bank data are transfered to
     */

    public int fillPanel(Panel.PanelType panelType, 
                         boolean hasRAW, boolean hasGEMC,
                         FtofRawData ftofRawData, FtofGEMCData ftofGEMCData, 
                         boolean reverseEngineer,
		  	 boolean processExactGEMCvalues,
                         boolean inputContainsGEMCMomentum,
                         boolean correctGEMCTimeForBackground,
                         EventData eventData) {
        
        // return if neither RAW DATA nor GEMC bank is found
	if ( !hasRAW && !hasGEMC) {            
            return 0;
        }

	// loop through entries in the banks and save paddle data into eventData
        // pay attention to reverseEngineer!
	for (Paddle paddle : getPaddles(panelType, ftofRawData, ftofGEMCData,
                               reverseEngineer, processExactGEMCvalues,
                               inputContainsGEMCMomentum, correctGEMCTimeForBackground )) {
            int sector = paddle.getSectorID();
	            	
            //TODO this is code to get rid of GEMC multiple hits on same paddle in same event
            boolean alreadyGotThisPaddle=false;
            for (Paddle pad : eventData.getPaddleList(sector, panelType)) {
                if (pad.getID()==paddle.getID()) alreadyGotThisPaddle=true;
            }
                        
            // Add paddles to eventData
	    // exclPaddleuding any panel that has paddles with multiple
	    // hits on a single paddle when using ConfigPanelReconstructionCLAS12
	    if(alreadyGotThisPaddle) {
	        eventData.setValidPanel(sector,panelType,false);
            } else {
	        eventData.addPaddle(paddle);
	    }
	}
        
        int NumberOfFiredPaddles = 0;
        for(Sector sector : eventData.getSectors() ) {
          NumberOfFiredPaddles += sector.getPanel(panelType).getPaddles().size();
        }
        
        Util.prn(10,"  In total " + NumberOfFiredPaddles + " good bank section(s) found"
                    + " in RAW bank and " + ftofGEMCData.numberOfItems 
                    + " section(s) found in GEMC bank.");
        
	return 1;
        
        
    }  // end of setPanel(...)
    
    
    
    
        

    /**
     * Gets the raw paddle data from the FTOF input bank data.
     * 
     * @param panel
     * @param reverseEngineer , if true, raw ADC and TDC are worked out using GEMC energy/time/position
     * @param processExactGEMCvalues , if true, GEMCHits are created and fed through reconstruction
     * @param containsMomentum , if true, EVIO input contains momentum fields
     * @param correctGEMCTimeForBackground
     * @return a list of PaddleData containing the Paddle and its location in the detector
     */
        
    public final List<Paddle> getPaddles(Panel.PanelType panel,
                                         FtofRawData ftofRawData, 
                                         FtofGEMCData ftofGEMCData,
                                         boolean reverseEngineer,
                                         boolean processExactGEMCvalues,
                                         boolean containsMomentum,
                                         boolean correctGEMCTimeForBackground) {
        	
        List<Paddle> listOfPaddles = new ArrayList<>();            
        int adcL, adcR, tdcL, tdcR;
        GEMCHit GEMChit;
            
        for (int i = 0; i < ftofRawData.numberOfItems; i++) {

            // Get the sector, and id, panel is passed in as argument
            int sector = ftofRawData.sectorID[i];
            int id = ftofRawData.paddleID[i];
            adcL = adcR = tdcL = tdcR = 0;
            GEMChit = null;

            // check these are valid
            if (validSectorAndPaddleID(geometry, sector, panel, id)) {

                // Get the calibration of the strip
                cal = calibration.get(sector, panel, id);

                if (!reverseEngineer) {
                    // get raw ADC and TDC from input bank	
                    adcL = ftofRawData.ADCL[i];
                    tdcL = ftofRawData.TDCL[i];
                    adcR = ftofRawData.ADCR[i];
                    tdcR = ftofRawData.TDCR[i];
                }                
                
                if (reverseEngineer && ftofGEMCData.numberOfItems > 0) {

                    // get raw ADC and TDC by reverse engineering GEMC time, energy, position in input bank
                    double GEMCenergy = ftofGEMCData.energyDeposited[i];
                    double GEMCtime;
                    if (correctGEMCTimeForBackground) {
                        GEMCtime = ftofGEMCData.time[i] - 50;
                    } else {
                        GEMCtime = ftofGEMCData.time[i];
                    }

                    double GEMCx = ftofGEMCData.x[i] / 10.0;
                    double GEMCy = ftofGEMCData.y[i] / 10.0;
                    double GEMCz = ftofGEMCData.z[i] / 10.0;

                    Vector3D GEMCposCLAS = new Vector3D(GEMCx, GEMCy, GEMCz);

                    ReverseEngineer reverser = 
                      new ReverseEngineer(GEMCenergy, GEMCtime, GEMCposCLAS,
                                          cal, calibration, geometry,
                                          id, sector, panel);

                    adcL = reverser.getADCL();
                    adcR = reverser.getADCR();
                    tdcL = reverser.getTDCL();
                    tdcR = reverser.getTDCR();
                    
                }

                if (processExactGEMCvalues && ftofGEMCData.numberOfItems > 0) {

                    // get GEMC hit properties
                    double GEMCenergyDeposited = ftofGEMCData.energyDeposited[i];
                    double GEMCenergyEntrance = ftofGEMCData.energyEntrance[i];
                    double GEMCparticleID = ftofGEMCData.particleID[i];

                    double GEMCtime;
                    if (correctGEMCTimeForBackground) {
                        GEMCtime = ftofGEMCData.time[i] - 50;
                    } else {
                        GEMCtime = ftofGEMCData.time[i];
                    }

                    double GEMCx = ftofGEMCData.x[i] / 10.0;
                    double GEMCy = ftofGEMCData.y[i] / 10.0;
                    double GEMCz = ftofGEMCData.z[i] / 10.0;
                    Vector3D GEMCposCLAS = new Vector3D(GEMCx, GEMCy, GEMCz);

                    Vector3D GEMCmomSector = new Vector3D(0.0, 0.0, 0.0);

                    if (containsMomentum) {
                    	//double GEMCpx = bankHolder.getDoubleValue(TagNums.NUM_PX, i);
                        //double GEMCpy = bankHolder.getDoubleValue(TagNums.NUM_PY, i);
                        //double GEMCpz = bankHolder.getDoubleValue(TagNums.NUM_PZ, i);
                        //Vector3D GEMCmomentumCLAS =  new Vector3D(GEMCpx, GEMCpy, GEMCpz);
                        //GEMCmomSector = DetectorGeometry.CLAS2sector(GEMCmomentumCLAS, sector);
                    }

                    GEMChit = new GEMCHit(GEMCparticleID, GEMCenergyEntrance,
                            GEMCenergyDeposited, GEMCtime,
                            GEMCposCLAS, GEMCmomSector);

                }

                SidedDouble adc = new SidedDouble(adcL, adcR);
                SidedDouble tdc = new SidedDouble(tdcL, tdcR);

                Paddle paddle;
                if (processExactGEMCvalues && GEMChit != null) {
                    // create paddle with GEMC hit information
                    paddle = new Paddle(sector, panel, id, cal, adc, tdc, GEMChit);
                } else {
                    // create paddle without GEMC hit information
                    paddle = new Paddle(sector, panel, id, cal, adc, tdc);
                }

                if (reverseEngineer) {
                    if (validRawTime(tdcL, tdcR)) {
                        //System.out.println("*** VALID ***");
                        listOfPaddles.add(paddle); 
                    } else 
                    {
                        //System.out.println("*** NOT VALID ***");
                    }
                } else {
                    listOfPaddles.add(paddle);
                }

            } else {
                throw new ArrayIndexOutOfBoundsException("Wrong sector: " + sector + " or paddle ID: " + id);
            }
        }
            
             return listOfPaddles;
        }
        
        
        
        
    /**
     * Determines if the raw data has a valid sector id (1-6)
     * and paddle id (1 - number of paddles in type of panel)
     * 
     * @param sector
     * @param panel
     * @param id
     * @return true if both sector and paddle id valid, false otherwise
     */
          
    private boolean validSectorAndPaddleID(FtofGeometry detectorGeom, int sector, Panel.PanelType panel, int id) {
        if (sector >= 1 && sector <= 6 & id >= 1 && id <= panel.nPaddles){ 
            return true;
        } else {
            return false;
        }
    } 
       	
       	
        
        
       	/**
         * Calculates whether there is a valid raw time (in channels) in at least one TDC
         * attached to a paddle.  Only used with reversed GEMC data.  This is done in PaddleCorrector for non GEMC data.
         * 
         *@return true if there is one valid raw TDC time, false otherwise
         */
              
        private boolean validRawTime(int tdcL, int tdcR) {
        	            
            double TDC_MAX = DetectorCalibration.TDC_MAX;
            if(tdcL <= 0       &&   tdcR  <= 0)        return false;
            if(tdcL >= TDC_MAX &&   tdcR  >= TDC_MAX)  return false;
            if(tdcL <= 0       &&   tdcR  >= TDC_MAX)  return false;
            if(tdcL >= TDC_MAX &&   tdcR  <= 0)        return false;
            return true;
        	
        }

                    
}




// this class is not used. Not sure if it's needed.
class TagNums {
    // input 
    // tags	
    public static final int TAG_PANEL_1A = 60;
    public static final int TAG_PANEL_1B = 70;
    public static final int TAG_PANEL_2  = 80;
	
    // nums for int banks, if no momentum in GEMC output	
    public static final int NUM_INPUT_SECTOR = 20;
    public static final int NUM_INPUT_PADDLE = 21;
    public static final int NUM_ADCL         = 22;
    public static final int NUM_ADCR         = 23;
    public static final int NUM_TDCL         = 24;
    public static final int NUM_TDCR         = 25;
    
    // nums for int banks if momentum in GEMC output
    public static final int NUM_SECTOR_MOM = 21;
    public static final int NUM_PADDLE_MOM = 22;
    public static final int NUM_ADCL_MOM   = 23;
    public static final int NUM_ADCR_MOM   = 24;
    public static final int NUM_TDCL_MOM   = 25;
    public static final int NUM_TDCR_MOM   = 26;
		
    // nums for double test banks
    public static final int NUM_E_DEP       = 1;    
    public static final int NUM_X_POS       = 2;
    public static final int NUM_Y_POS       = 3;
    public static final int NUM_Z_POS       = 4;
    public static final int NUM_INPUT_TIME  = 8;
    public static final int NUM_LX_POS      = 5;
    public static final int NUM_LY_POS      = 6;
    public static final int NUM_LZ_POS      = 7;
    public static final int NUM_E_ENT       = 13;
    public static final int NUM_PARTICLEID  = 9;
    
    // momentum may or may not be in the input depending on version of GEMC output bank
    public static final int NUM_PX = 18;
    public static final int NUM_PY = 19;
    public static final int NUM_PZ = 20;
    
    // output 
    //tags
    public static final int TAG_FTOF_CONVERTED_RAW_OUTPUT = 800;
    public static final int TAG_FTOF_HIT_OUTPUT = 810;
    public static final int TAG_FTOF_CLUSTER_OUTPUT = 820;
  	
    // integer nums		
    public static final int NUM_OUTPUT_SECTOR      = 1;
    public static final int NUM_OUTPUT_PANEL       = 2;
    public static final int NUM_PADDLEID_1         = 3;
    public static final int NUM_PADDLE_STATUS_1    = 4;
  	
    // hit, cluster nums
    public static final int NUM_ENERGY           = 5;
    public static final int NUM_UNC_ENERGY       = 6;
    public static final int NUM_OUTPUT_TIME      = 7; 
    public static final int NUM_UNC_TIME         = 8;
    public static final int NUM_POSITION_X       = 9;
    public static final int NUM_POSITION_Y       = 10;
    public static final int NUM_POSITION_Z       = 11;
    public static final int NUM_UNC_POSITION_X    = 12;
    public static final int NUM_UNC_POSITION_Y    = 13;
    public static final int NUM_UNC_POSITION_Z    = 14;
  		
    // converted raw nums	
    public static final int NUM_ENERGY_LEFT        = 5;
    public static final int NUM_ENERGY_RIGHT       = 6;
    public static final int NUM_UNC_ENERGY_LEFT    = 7; 
    public static final int NUM_UNC_ENERGY_RIGHT   = 8;
    public static final int NUM_TIME_LEFT          = 9;
    public static final int NUM_TIME_RIGHT         = 10;
    public static final int NUM_UNC_TIME_LEFT      = 11;
    public static final int NUM_UNC_TIME_RIGHT     = 12;
  	
}






