
package org.jlab.rec.ftof.reconstruction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.jlab.geom.base.Detector;

import org.jlab.geom.prim.Vector3D;
import org.jlab.rec.ftof.calibration.DetectorCalibration;
import org.jlab.rec.ftof.event.Cluster;
import org.jlab.rec.ftof.event.GEMCCluster;
import org.jlab.rec.ftof.event.GEMCHit;
import org.jlab.rec.ftof.event.Hit;
import org.jlab.rec.ftof.event.Paddle;
import org.jlab.rec.ftof.event.Panel;
import org.jlab.rec.ftof.geometry.PanelGeometry;
import org.jlab.rec.ftof.FTOFReconstruction;


/**
 * Class to find clustering efficiencies and optimal clustering parameters.
 * 
 * Never used in production mode.
 * 
 * 
 * @author acolvill
 *
 */

public class ConfigPanelReconstructionCLAS12 {

	private Panel panelData;
	private PanelGeometry panelGeom;
        private Detector ftofDetector;

        private Paddle[] paddleArray;
	
	private double uncertaintyMultipleTime;
	private double uncertaintyMultipleYPosition;
	
	private double differenceTime;
	private double differenceYPosition;
	
	public final static double timeStart =0.25 ;
	public final static double timeStop =16;
	public final static double timeStep =1;
	
	public final static double yStart=1;
	public final static double yStop=80;
	public final static double yStep=5;
	
	public final static boolean findPeak=true;
		
	/**
     * Creates an object to reconstruct a panel of the detector.
     *
     * @param panelData the data of the panel being reconstructed
     */
		
    public ConfigPanelReconstructionCLAS12(Panel panelData, 
                                           PanelGeometry panelGeom,
                                           Detector ftofDetector)
    {
        
    	this.panelData = panelData;
    	this.panelGeom = panelGeom;
        
    }
    
    
    /**
     * Reconstructs the data of the panel.
     * Loops over paddles, reconstructing them using PaddleReconstruction,
 then finds and adds clusters to the Panel using findClusters()
     */
    
    public void reconstructPanel(boolean useTracking, boolean processExactGEMCvalues)
    {
    	
    	setupClusteringParameters();
    	
    	 // convert raw ADC/TDC to energy and time
    	
    	for (Paddle paddle : panelData.getPaddles()) {
    		
            PaddleConvertor  convertor = new PaddleConvertor(paddle,panelGeom);
            convertor.convertPaddle();
   	 	}
    	
    	// remove paddles with no valid time
   	 
    	panelData.removeInvalidPaddles();
    	
    	// reconstruct all paddles i.e. combine left and right time and energies
   	    	    	 
   		for (Paddle paddle : panelData.getPaddles()) {
   			
   				PaddleReconstruction reconstruction = new PaddleReconstruction(paddle, panelGeom);
   				reconstruction.reconstructPaddle(useTracking);
            
        }
                           
        paddleArray = panelData.getPaddles().toArray(new Paddle[panelData.getNPaddles()]);
        
        // find reconstruction clusters between adjacent related paddles, find ideal clusters
        // using GEMC breach condition, compare ideal and reconstruction clusters.
        
        if(panelData.isValid()) {
        
        	findClusters();
        	findBreachClusters();
        	calculateClusterLengthEfficiencies();	
        
        	if (processExactGEMCvalues) {
        	  makeGEMCclustersFromReconstructionClusters();
        	}
        
        }
        
    }


    
    /**
     * 
     * Sets clustering parameters to those specified in DetectorCalibration
     * 
     */
    
	private void setupClusteringParameters() {
		
		if(DetectorCalibration.RELATED_METHOD.equals("uncertainty")) {   	
    		if(panelData.getName().ID==1) {
    			uncertaintyMultipleTime = DetectorCalibration.PANEL_1A_UNCERTAINTY_MULTIPLE_TIME;
    			uncertaintyMultipleYPosition = DetectorCalibration.PANEL_1A_UNCERTAINTY_MULTIPLE_YPOSITION;
    		}
    		if(panelData.getName().ID==2) {
    			uncertaintyMultipleTime = DetectorCalibration.PANEL_1B_UNCERTAINTY_MULTIPLE_TIME;
    			uncertaintyMultipleYPosition = DetectorCalibration.PANEL_1B_UNCERTAINTY_MULTIPLE_YPOSITION;
    		}
    		if(panelData.getName().ID==3) {
    			uncertaintyMultipleTime = DetectorCalibration.PANEL_2_UNCERTAINTY_MULTIPLE_TIME;
    			uncertaintyMultipleYPosition = DetectorCalibration.PANEL_2_UNCERTAINTY_MULTIPLE_YPOSITION;
			}
    	}
    	
    	if(DetectorCalibration.RELATED_METHOD.equals("absolute")) {
    		if(panelData.getName().ID==1) {
    			differenceTime = DetectorCalibration.PANEL_1A_DIFFERENCE_TIME;
    			differenceYPosition = DetectorCalibration.PANEL_1A_DIFFERENCE_YPOSITION;
    		}
    		if(panelData.getName().ID==2) {
    			differenceTime = DetectorCalibration.PANEL_1B_DIFFERENCE_TIME;
    			differenceYPosition = DetectorCalibration.PANEL_1B_DIFFERENCE_YPOSITION;
    		}
    		if(panelData.getName().ID==3) {
    			differenceTime = DetectorCalibration.PANEL_2_DIFFERENCE_TIME;
    			differenceYPosition = DetectorCalibration.PANEL_2_DIFFERENCE_YPOSITION;
			}
    	}
	}
     
    
    /**
     * Identifies clusters on a panel, creates them, then adds them to the panelData object
     * 
     */
        
    private void findClusters() {
    	
       	int numberInChain=1;
    	int second, firstInChain;
    	boolean chainEnded;
		Hit firstHit = null, secondHit = null;
		    	
		// loop over all paddle hits on a panel
		// note that the index 'first' is also altered within the for loop
				
    	for (int first = 0; first < paddleArray.length; first++  ) {
			
    		  // reset chain properties
    		
			  firstInChain = first;  
			  numberInChain = 1;
			  chainEnded = false;
			  
			  // whilst you still have a chain of adjacent and matched in space and time
			  // hits keep looking to add hits onto the chain
			
			  while( ! chainEnded ) {
			
				  second = first + 1;
			  
				  if (second >= paddleArray.length)   second=first;
			  
				  firstHit = paddleArray[first].getHit();
				  
				 // check paddles are next door to each other
				  
				  if (adjacent(first, second)) {
			
					  secondHit = paddleArray[second].getHit();
					  
					  // The mechanism by which two paddles are deemed related depends on DetectorCalibration.RELATED_METHOD 
                      
					  if(DetectorCalibration.RELATED_METHOD.equals("uncertainty")) {
						  
						  if(findPeak) {
						  
							  // find optimal uncertainty multiples
						  
							  findOptimalUncMultiples(paddleArray[first],paddleArray[second]);
						  
						  }
						  						  
						  //reset clustering parameters
						  
						  setupClusteringParameters();
						  
						  // check paddle hits match in space and time
					  					  
						  if(notMatchedInSpaceOrTime(firstHit,secondHit)) {
							  
							  chainEnded = true;
											  
						  }else {
  					  						 						  
							  // update extended chain properties
  						  
							  first = second;
							  numberInChain += 1;
						  }
					  }
					  
					  if(DetectorCalibration.RELATED_METHOD.equals("absolute")) {
						  
						  if(findPeak) {
						  
							  // find optimal absolute differences
						  
							  findOptimalAbsoluteDifferences(paddleArray[first],paddleArray[second]);
						  
						  }
						  
						  //reset clustering parameters
						  
						  setupClusteringParameters();
						  
						  // check paddle hits match in space and time
					  					  
						  if(timesSimilar(paddleArray[first].getHit(), paddleArray[second].getHit(), differenceTime) && yPositionsSimilar(paddleArray[first].getHit(), paddleArray[second].getHit(), differenceYPosition)) {
							  // update extended chain properties
							  first = second;
							  numberInChain += 1;			  
						  }else {
  					  						 						  
							  chainEnded = true;
							  
						  }
					  }
						   					  
				  }else {
						  chainEnded = true;
				  }
						  
			  }
			  
			  // now that we have a chain of adjacent related paddles, what you do with it depends
			  // on its length
					  
			  if ( numberInChain == 1 ){
				  
				  // copy over single hit to cluster
				  
				  makeCluster(paddleArray[firstInChain]);
				  
			  }else{
				  
				  	// optionally combine all the adjacent related hits into cluster
				  	
				  	if (DetectorCalibration.COMBINE_ALL_HITS){
				  			 
				  		makeSpecifiedIntoCluster(numberInChain, firstInChain);
				  			 
				  	}else{
				  			 
				  		 // combine hits in this chain into clusters of size maxClusterLength
				  		 
				  		 boolean hitsRemainInChain = true;
				  			 
				  		 while (hitsRemainInChain == true){
				  			 
				  			 // if chain is at, or less than, maxClusterLength, combine all hits in this chain
				  				 
				  			 if (numberInChain <= DetectorCalibration.MAX_CLUSTER_LENGTH){
				  					
				  				 makeSpecifiedIntoCluster(numberInChain, firstInChain);
				  					
				  				 hitsRemainInChain = false;
				  					 
				  			 }else{
				  				 
				  				    // chain is longer than maxClusterLength so have to decide 
				  				    // how to combine in clusters of length maxClusterLength
				  				 
				  				    // if the first set of maxClusterLength hits is better matched than
				  				    // the second overlapping set, make first set into cluster
				  				    // there are two methods to decide which set is better matched
				  				  				  								  				 
				  				 	if (  ( DetectorCalibration.RELATED_METHOD.equals("uncertainty") && 
				  				 			firstPairMatchBetterUncertainties(paddleArray[firstInChain].getHit(),
				  				 			paddleArray[firstInChain+1].getHit(),
				  				 			paddleArray[firstInChain+DetectorCalibration.MAX_CLUSTER_LENGTH-1].getHit(),
				  				 			paddleArray[firstInChain+DetectorCalibration.MAX_CLUSTER_LENGTH].getHit())  )
				  				 			||
				  				 		  ( DetectorCalibration.RELATED_METHOD.equals("absolute")  &&
				  				 			firstPairMatchBetterAbsolutes(paddleArray[firstInChain].getHit(),
						  				 	paddleArray[firstInChain+1].getHit(),
						  				 	paddleArray[firstInChain+DetectorCalibration.MAX_CLUSTER_LENGTH-1].getHit(),
						  				 	paddleArray[firstInChain+DetectorCalibration.MAX_CLUSTER_LENGTH].getHit())  )  ){
				  					
				  				 			// combine first maxClusterLength number of hits
				  							
				  				 			makeSpecifiedIntoCluster(DetectorCalibration.MAX_CLUSTER_LENGTH,firstInChain);
				  					  
				  				 			// reset pointer to point to remainder of chain
				  				 			
				  				 			firstInChain = firstInChain + DetectorCalibration.MAX_CLUSTER_LENGTH;
				  				 			numberInChain = numberInChain - DetectorCalibration.MAX_CLUSTER_LENGTH;
				  							  					
				  				 	}else{
				  				 	       
				  				 		    //  make hit unique to first set into a single hit cluster
				  					
				  				 			makeCluster(paddleArray[firstInChain]);
				  					
				  				 			// reset pointer to point to remainder of chain
				  				 			
				  				 			firstInChain += 1;
				  				 			numberInChain -= 1;
				  								  					
				  				 	}
				  			 }
				  		 }	 
				  	}
			  } 
    	}
    	
     }
    
    
    /**
     * Finds the optimal absolute difference in time and y 
     * 
     * @param paddle
     * @param paddle2
     */
    
	private void findOptimalAbsoluteDifferences(Paddle paddle, Paddle paddle2) {
		
		Hit firstHit = paddle.getHit();
		
	    Hit secondHit = paddle2.getHit();
	    
	    boolean goodDecisiont=false;
		
	    String relatedDecisionFilenameOut1at = FTOFReconstruction.userOutputFileName.replace(".ev", "1a.dat");
            String relatedDecisionFilenameOut1bt = FTOFReconstruction.userOutputFileName.replace(".ev", "1b.dat");
            String relatedDecisionFilenameOut2t = FTOFReconstruction.userOutputFileName.replace(".ev", "2.dat");
	
		
		PrintWriter decisionOutput1at  = null;
		PrintWriter decisionOutput1bt  = null;
		PrintWriter decisionOutput2t  = null;
		
		try {
			decisionOutput1at = new PrintWriter(new FileWriter(relatedDecisionFilenameOut1at,true));
			decisionOutput1bt = new PrintWriter(new FileWriter(relatedDecisionFilenameOut1bt,true));
			decisionOutput2t = new PrintWriter(new FileWriter(relatedDecisionFilenameOut2t,true));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		
		String w = new String();
			  
		for(double i= yStart;i<yStop;i=i+yStep) {
			for(double j= timeStart;j<timeStop;j=j+timeStep) {
				if(timesSimilar(firstHit, secondHit,j) && yPositionsSimilar(firstHit, secondHit,i)) {
					if (breachOccursCorrected(paddle,paddle2,panelGeom.getThickness())) {
						goodDecisiont = true;
					}else {
						goodDecisiont = false;
					}
		    	}else {
		    		if (breachOccursCorrected(paddle,paddle2,panelGeom.getThickness())) {
		    			goodDecisiont = false;
		    		}else {
		    			goodDecisiont = true;
		    		}
				  
		     	}
			    						  
			 	if(goodDecisiont) {
			 			w += "1"; 
			 	}else {
			 			w += "0"; 
			 	}
		     
			 	if(!(i+yStep>=yStop && j+timeStep >=timeStop)) {
			 			w += "	";
			 	}
		    }
			    						  
		}
			
		if(panelData.getName().ID==1) {		
			decisionOutput1at.println(w);
		}
		if(panelData.getName().ID==2) {		
		  decisionOutput1bt.println(w);
		}
		if(panelData.getName().ID==3) {		
			decisionOutput2t.println(w);
		}
		  
	    decisionOutput1at.close();
		decisionOutput1bt.close();
		decisionOutput2t.close();
		
	}

	
	/**
	 * 
	 * Finds the optimal uncertainty multiple in time and y
	 * 
	 * @param paddle
	 * @param paddle2
	 */

	private void findOptimalUncMultiples(Paddle paddle, Paddle paddle2) {
				
		Hit firstHit = paddle.getHit();
		
	    Hit secondHit = paddle2.getHit();
	    
	    String relatedDecisionFilenameOut1at = FTOFReconstruction.userOutputFileName.replace(".ev", "1a.dat");
		String relatedDecisionFilenameOut1bt = FTOFReconstruction.userOutputFileName.replace(".ev", "1b.dat");
		String relatedDecisionFilenameOut2t = FTOFReconstruction.userOutputFileName.replace(".ev", "2.dat");
			
		PrintWriter decisionOutput1at  = null;
		PrintWriter decisionOutput1bt  = null;
		PrintWriter decisionOutput2t  = null;
		
		try {
			decisionOutput1at = new PrintWriter(new FileWriter(relatedDecisionFilenameOut1at,true));
			decisionOutput1bt = new PrintWriter(new FileWriter(relatedDecisionFilenameOut1bt,true));
			decisionOutput2t = new PrintWriter(new FileWriter(relatedDecisionFilenameOut2t,true));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String w = new String();
			  
		for(double i= yStart;i<yStop;i=i+yStep) {
		   	for(double j= timeStart;j<timeStop;j=j+timeStep) {
					uncertaintyMultipleYPosition =i;
					uncertaintyMultipleTime = j; 
					if(notMatchedInSpaceOrTime(firstHit,secondHit)) {
						 if (breachOccursCorrected(paddle,paddle2,panelGeom.getThickness())) {
							  w += "0"; 
						  }else {
							  w += "1"; 
						  } 
					}else {
  						   					  
	  					  if (breachOccursCorrected(paddle,paddle2,panelGeom.getThickness())) {
							  w += "1";
						  }else {
							  w += "0";
						  }
  						
  					}
					if(!(i+yStep>=yStop && j+timeStep >=timeStop)) {
					   	 w += "	";
					}
				}
		  }
		  
		  if(panelData.getName().ID==1) {		
			  decisionOutput1at.println(w);
		  }
		  if(panelData.getName().ID==2) {		
			  decisionOutput1bt.println(w);
		  }
		  if(panelData.getName().ID==3) {		
			  decisionOutput2t.println(w);
		  }
		 		  
		  decisionOutput1at.close();
		  decisionOutput1bt.close();
		  decisionOutput2t.close();
		
	}

	
	/**
	 * 
	 * Finds ideal set of clusters using the GEMC breach condition
	 * 
	 */

	public void findBreachClusters(){
    	
       	int numberInChain=1;
    	int second, firstInChain;
    	boolean chainEnded;

    	
		// loop over all paddle hits on a panel
		// note that the index 'first' is also altered within the for loop
		
		
    	for (int first = 0; first < paddleArray.length; first++  ) {
			
    		  // reset chain properties
    		
			  firstInChain = first;  
			  numberInChain = 1;
			  chainEnded = false;
			  
			  // whilst you still have a chain of adjacent and matched in space and time
			  // hits keep looking to add hits onto the chain
			
			  while( ! chainEnded ) {
			
				  second = first + 1;
			  
				  if (second >= paddleArray.length)   second=first;
			  
				  // check paddles are next door to each other
				  
				  if (adjacent(first, second)) {
			
					  // check paddle hits match in space and time
					   
					  if(breachOccursCorrected(paddleArray[first],paddleArray[second],panelGeom.getThickness())) {
						  first = second;
	  					  numberInChain += 1;
	  					  chainEnded = false;
										  
  					  }else {
  						  
  						  // update extended chain properties
  						  
						  chainEnded = true;
	  				  }
					  
					   					  
				  }else {
						  chainEnded = true;
				  }
						  
			  }
			  
			  // now that we have a chain of adjacent related paddles, what you do with it depends
			  // on its length
					  
			  if ( numberInChain == 1 ){
				  
				  // copy over single hit to cluster
				  
				  makeBreachCluster(paddleArray[firstInChain]);
				  
			  }else{
				  
				  	// optionally combine all the adjacent related hits into cluster
						  			 
				  makeSpecifiedIntoBreachCluster(numberInChain, firstInChain);
				  			 
			  }
    	
    	}
    }
    
	
	
	/**
	 * 
	 *  Compare reconstruction and ideal clusters  
	 */
		
   	private void calculateClusterLengthEfficiencies() {
   		
   		String relatedDecisionFilenameOut1at =  FTOFReconstruction.userOutputFileName.replace(".ev", "ClusterEFfic1a.dat");
		String relatedDecisionFilenameOut1bt =  FTOFReconstruction.userOutputFileName.replace(".ev", "ClusterEFfic1b.dat");
		String relatedDecisionFilenameOut2t =  FTOFReconstruction.userOutputFileName.replace(".ev", "ClusterEFfic2.dat");
				
		PrintWriter decisionOutput1a  = null;
		PrintWriter decisionOutput1b  = null;
		PrintWriter decisionOutput2  = null;
				
		try {
			decisionOutput1a = new PrintWriter(new FileWriter(relatedDecisionFilenameOut1at,true));
			decisionOutput1b = new PrintWriter(new FileWriter(relatedDecisionFilenameOut1bt,true));
			decisionOutput2 = new PrintWriter(new FileWriter(relatedDecisionFilenameOut2t,true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int maxClusterLength = 26;
   		
		int breachFound[] = new int[maxClusterLength];
		int reconFoundCorrectly[] = new int[maxClusterLength];
		int reconFalseClusters[] = new int[maxClusterLength];
		
		for(GEMCCluster breachCluster : panelData.getGEMCClusters()) {
			
			breachFound[breachCluster.getClusterLength()]++;
			
		}
		
		// work out frequency with which the reconstruction correctly finds breach clusters of given length
				  	
		for(GEMCCluster breachCluster : panelData.getGEMCClusters()) {
   			for(Cluster cluster : panelData.getClusters()) {
				if(breachCluster.getPaddleID()[0] == cluster.getPaddleIDs()[0]) {
					if (breachCluster.getClusterLength()==cluster.getClusterLength()) {
						
						reconFoundCorrectly[breachCluster.getClusterLength()]++;
					    
			    	}
				}
			}
   		}
		
		// work out frequency with which the reconstruction incorrectly identifies clusters of a given length
		
		boolean found=false;
		
		for(Cluster cluster : panelData.getClusters()) {
			found=false;
   			for(GEMCCluster breachCluster : panelData.getGEMCClusters()) {
				if(breachCluster.getPaddleID()[0] == cluster.getPaddleIDs()[0]) {
					if (breachCluster.getClusterLength()==cluster.getClusterLength()) {
						
							found = true;
						
					}
				}
			}
   			if(!found){
   				reconFalseClusters[cluster.getClusterLength()]++;
   			}
   		}
		
		int recon=0;
		int breach=0;
		
		for(Cluster cluster : panelData.getClusters()) {
			recon=recon+cluster.getClusterLength();
		}
		
		for(GEMCCluster cluster : panelData.getGEMCClusters()) {
			breach=breach+cluster.getClusterLength();
		}
		
		//output to file
				
		String q = new String();
		
		for (int i=1; i<10; i++){
			q+= Integer.toString(breachFound[i]); q+= "	";
		}
		
		for (int i=1; i<10; i++){
			q+= Integer.toString(reconFoundCorrectly[i]); q+= "	";
		}
		
		for (int i=1; i<10; i++){
			q+= Integer.toString(reconFalseClusters[i]); q+= "	";
		}
		
		if(panelData.getName().ID==1) {		
			  decisionOutput1a.println(q);
		}
		
		if(panelData.getName().ID==2) {		
		  decisionOutput1b.println(q);
		}
		
		if(panelData.getName().ID==3) {		
		 decisionOutput2.println(q);
		}
		
		decisionOutput1a.close();
		decisionOutput1b.close();
		decisionOutput2.close();
   	}
   	
   	
   	
    /**
     * Create GEMC clusters equivalent to reconstruction clusters
     *  
     */
    
    private void makeGEMCclustersFromReconstructionClusters() {
		
    	
    	for( Cluster cluster : panelData.getClusters()){
    		
    		  Paddle[] clusterConstituents = new Paddle[cluster.getClusterLength()];
    		
    		  int i=0;
    		  
    		  for (int id : cluster.getPaddleIDs()) {
    			  
    			  	Paddle paddle = panelData.getPaddle(id);
    		        		    	
    		      	clusterConstituents[i]  = paddle;
    		      	i++;
    		  }
    		  
    		  makeGEMCCluster(clusterConstituents);
    		  
         }
    }
   	
    
    /**
     * Makes a cluster from specified elements in paddleArray
     * 
     * 
     * @param numberInChain
     * @param firstInChain
     * @param lastInChain
     */


	private void makeSpecifiedIntoCluster(int numberInChain, int firstInChain) {
		
		int lastInChain = firstInChain + numberInChain -1;
		
		Paddle[] clusterConstituents = new Paddle[numberInChain];
			  
		int j=0;
			  
		for (int i = firstInChain; i <= (lastInChain); i++){
		          clusterConstituents[j]  = paddleArray[i];
				  j++;
		 }
			  
		 makeCluster(clusterConstituents);
		 
	}
	
	
	/**
	 * 
	 * Makes a GEMC cluster using the specified elements in paddleArray
	 * 
	 * @param numberInChain
	 * @param firstInChain
	 */
	
	private void makeSpecifiedIntoBreachCluster(int numberInChain, int firstInChain) {
		
		int lastInChain = firstInChain + numberInChain -1;
		
		Paddle[] clusterConstituents = new Paddle[numberInChain];
			  
		int j=0;
			  
		for (int i = firstInChain; i <= (lastInChain); i++){
		          clusterConstituents[j]  = paddleArray[i];
				  j++;
		 }
			  
		 makeGEMCCluster(clusterConstituents);
		
	}
			  

	
	/**
	 * Determines if two hits are NOT matched in space or time
	 * from CLAS6
	 * 
	 * @param firstHit
	 * @param secondHit
	 * @return  true if NOT matched in either space or time, false otherwise
	 */

	private boolean notMatchedInSpaceOrTime(Hit firstHit, Hit secondHit) {
		
		return (    timeDifference(firstHit, secondHit) >  timeUncertainty(firstHit, secondHit)
		         || yDifference(firstHit, secondHit)    >  yUncertainty(firstHit, secondHit)  );
		
	}
	
	
	
	 /**
     * Checks if a given paddle and the one next to it (with higher index) are adjacent
     * Adjacent paddles have consecutive IDs.
     * 
     * @param index1, the lower paddle index in paddleArray
     * @param index2, the higher paddle index in paddleArray
     * @return true if they are adjacent, false otherwise
     */
	
	private boolean adjacent(int index1, int index2) {
		
		return ( paddleArray[index2].getID() == ( paddleArray[index1].getID() +1 ) );
		
	}
	
	
	
	/**
	 * Calculates the absolute time difference between two hits
	 * 
	 * @param hit1
	 * @param hit2
	 * @return the absolute time difference
	 */
		
	private double timeDifference(Hit hit1, Hit hit2) {
		
		return ( Math.abs( hit1.getTime() - hit2.getTime() ) );
			
	}
	
	
	
	/**
	 * Calculates the combined uncertainty in the times of two hits
	 * from CLAS6
	 * 
	 * @param hit1
	 * @param hit2
	 * @return the combined uncertainty
	 */
	
	private double timeUncertainty(Hit hit1, Hit hit2) {
		
		double hit1TimeUnc = hit1.getUncTime();
		double hit2TimeUnc = hit2.getUncTime();
		
		return ( uncertaintyMultipleTime * Math.sqrt( Math.pow(hit1TimeUnc, 2) + Math.pow(hit2TimeUnc, 2) ) );
		
	}
	
	
	
	/**
	 * Calculates the absolute difference in y positions of two hits
	 * 
	 * @param hit1
	 * @param hit2
	 * @return the absolute difference in y
	 */
		
	private double yDifference(Hit hit1, Hit hit2) {
		
        return ( Math.abs( hit1.getPosition().y() - hit2.getPosition().y() ) );
        
	}
	
	
	
	/**
	 * Calculates the combined uncertainty in the y positions of two hits
	 * from CLAS6
	 * 
	 * @param hit1
	 * @param hit2
	 * @return the combined uncertainty in y
	 */
		
	private double yUncertainty(Hit hit1, Hit hit2) {
		
		double hit1YUnc = hit1.getUncPosition().y();
		double hit2YUnc = hit2.getUncPosition().y();
		
		return  ( uncertaintyMultipleYPosition * Math.sqrt(Math.pow(hit1YUnc, 2) + Math.pow(hit2YUnc, 2) ) );
		
	}
	
	
	/**
	 * Determines if the first pair of two pairs of hits matches in time or space
	 * better than the second pair based on uncertainty multiples
	 * 
	 * from CLAS6
	 * 
	 * @param firstHit
	 * @param secondHit
	 * @param thirdHit
	 * @param fourthHit
	 * @return true if the first pair matches better in time or space, false otherwise
	 */
	
	private boolean firstPairMatchBetterUncertainties(Hit firstHit, Hit secondHit,
			Hit thirdHit, Hit fourthHit) {
		
		return  ( timeUncertainty(thirdHit, fourthHit) >  timeUncertainty(firstHit, secondHit)
			    || yUncertainty(thirdHit, fourthHit)    >  yUncertainty(firstHit, secondHit) );
		
	}
	
	
	/**
	 * Determines if the first pair of two pairs of hits matches in time or space
	 * better than the second pair based on absolute differences.
	 * 
	 * @param firstHit
	 * @param secondHit
	 * @param thirdHit
	 * @param fourthHit
	 * @return true if the first pair matches better in time or space, false otherwise
	 */
	
	private boolean firstPairMatchBetterAbsolutes(Hit firstHit, Hit secondHit,
	 		Hit thirdHit, Hit fourthHit) {
			
		return  ( yDifference(thirdHit, fourthHit)    >  yDifference(firstHit, secondHit)
		|| timeDifference(thirdHit, fourthHit) >  timeDifference(firstHit, secondHit));
			
		}
	
	
	        
    /**
     * Creates a single cluster by copying over details from a single hit, 
 then adds it to the Panel.
     * @param hit, the Hit that will be made into a Clusters
     */
    
    private void makeCluster(Paddle paddle) {
    	
    	int[] iD = new int[1];
    	iD[0] = paddle.getID();
    	int[] status = new int[1];
    	status[0] = paddle.getStatus();
    	Hit hit = paddle.getHit();
    	
    	Cluster cluster = new Cluster(paddle.getSectorID(),
                                      paddle.getPanel().getID(),iD, status,
                                      hit.getEnergy(), hit.getUncEnergy(),
                                      hit.getTime(), hit.getUncTime(), 
                                      hit.getPosition(), hit.getUncPosition());
    	
        panelData.addCluster(cluster);
        
    }
    
    
    /**
     * Creates a single GEMCClusters by copying over details from a single GEMCHit,
 then adds it to the Panel
     * 
     * @param paddle
     */
    
    private void makeBreachCluster(Paddle paddle) {
    	
    	int[] iD = new int[1];
    	iD[0] = paddle.getID();
    	GEMCHit hit = paddle.getGEMChit();
        	
    	GEMCCluster cluster = new GEMCCluster(iD,hit.getGEMCenergyDeposited(),hit.getGEMCtime(),hit.getGEMCpos());
    	
        panelData.addGEMCCluster(cluster);
        
    }
    
    
    /**
     * Creates a cluster by combining properties of multiple hits, 
 then adds it to the Panel.
     * 
     * Energy is the sum of all hit energies
     * Time and y position are energy weighted averages
     * x and z position are simple averages
     * 
     * @param hit[], the Hit array that will be made into a Clusters
     */
    
    
    private void makeCluster(Paddle[] clusterConstituents) {
		
    	
    	//TODO you can do this with fewer loops, but performance improvement minimal
    	
    	double energySum=0, uncEnergy=0;
    	double time=0, uncTime=0;
        	
    	double x=0, y=0, z=0, uncX=0, uncY=0, uncZ=0;
    	
    	int clusterLength = clusterConstituents.length;
    	
    	Hit[] hitConstituents = new Hit[clusterConstituents.length];
    	
    	for(int i=0;i<clusterLength;i++) {
    		hitConstituents[i] = clusterConstituents[i].getHit();
    	}
    	
        // work out cluster properties
    	
       	for (Hit hit : hitConstituents){
    		energySum += hit.getEnergy();
    	}
    	
       	double[] energyWeight = new double[clusterLength];
       	
       	if (energySum>0.0) {
    		
    		for (int i=0; i<clusterLength; i++){
    			energyWeight[i] = hitConstituents[i].getEnergy() / energySum;
    		}

    	}else {
    		
    		//this was done in CLAS6, not clear if necessary
    		
    		for (int i=0; i<clusterLength; i++){
    		     energyWeight[i] = 1.0 / clusterLength ;
    		}
    	}
       	
       	double sumOfSquares = 0.0;
       	
       	for (Hit hit : hitConstituents){
       		sumOfSquares += Math.pow(hit.getUncEnergy(), 2);
       	}
       	
       	uncEnergy = Math.sqrt(sumOfSquares);
       	
       	// combine times using energy weighted average
        
       	if (DetectorCalibration.COMBINATION_METHOD.equals("average")) {
       	
       		for (int i=0; i<clusterLength; i++){
       			time += energyWeight[i] * hitConstituents[i].getTime() ;
       		}
       		
       		uncTime = getEnergyWeightedUnc(hitConstituents, energyWeight, energySum, "time");
       		
       	}
       	
       	// use earliest time from all hits in the cluster
       	
    	if (DetectorCalibration.COMBINATION_METHOD.equals("earliest")) {
    		
    		time = 10000;
    		
    		for (Hit hit : hitConstituents){
           		if (hit.getTime() < time) {
           			time = hit.getTime();
           			uncTime = hit.getUncTime();
           		}
           	}
      	}
    	
    	// combine positions 
    	
    	if (DetectorCalibration.COMBINATION_METHOD.equals("average")) {
    		
    		for (Hit hit : hitConstituents){
           		x += hit.getPosition().x();
           	}
        	
        	x = x / clusterLength;
        	
        	for (Hit hit : hitConstituents){
        		uncX +=  Math.pow((1.0 / clusterLength),2) * Math.pow(hit.getUncPosition().x(),2); 
        	}
        	
        	uncX = Math.sqrt(uncX);
        	
           	for (int i=0; i<clusterLength; i++){
           		y += energyWeight[i] * hitConstituents[i].getPosition().y() ;
            		
           	}
           	
        	uncY = getEnergyWeightedUnc(hitConstituents, energyWeight, energySum, "y");
           	
        	for (Hit hit : hitConstituents){
           		z += hit.getPosition().z();
           	}
        	
        	z = z / clusterLength;
        	
        	for (Hit hit : hitConstituents){
        		uncZ +=  Math.pow((1.0 / clusterLength),2) * Math.pow(hit.getUncPosition().z(),2); 
        	}
        	
        	uncZ = Math.sqrt(uncZ);
    	}
            	
    	// use earliest hit position     	
    	
    	if(DetectorCalibration.COMBINATION_METHOD.equals("earliest")) {
    		
    		double tempTime = 10000;
    		
    		for (Hit hit : hitConstituents){
    			if (hit.getTime() < tempTime) {
    				tempTime = hit.getTime();
           			x = hit.getPosition().x();
           			y = hit.getPosition().y();
           			z = hit.getPosition().z();
           			uncX = hit.getUncPosition().x();
           			uncY = hit.getUncPosition().y();
           			uncZ = hit.getUncPosition().z();
           		}
           	}
    	}
    	
    	Vector3D position = new Vector3D(x,y,z);
    	    	
    	Vector3D uncPosition = new Vector3D (uncX, uncY, uncZ);
    	    
    	// create cluster and add to Panel
    	
    	int[] iD = new int[clusterLength];
    	int[] status = new int[clusterLength];
    	
    	for (int i=0; i<clusterLength; i++){
    		iD[i] = clusterConstituents[i].getID();
    		status[i] = clusterConstituents[i].getStatus();
       	}
    	
       	Cluster cluster = new Cluster(clusterConstituents[0].getSectorID(),
                                      clusterConstituents[0].getPanel().getID(),
                                      iD, status, energySum, uncEnergy,
                                      time, uncTime, position, uncPosition);

    	panelData.addCluster(cluster);
		
	}
    
    /**
     * Combines GEMC hits into a GEMC cluster
     * 
     * 
     * @param clusterConstituents
     */
    
    
    private void makeGEMCCluster(Paddle[] clusterConstituents) {
		 
    	double energySum=0;
    	double time=0;
        	
    	double x=0, y=0, z=0;
    	
    	int clusterLength = clusterConstituents.length;
    	
    	GEMCHit[] hitConstituents = new GEMCHit[clusterConstituents.length];
    	
    	for(int i=0;i<clusterLength;i++) {
    		hitConstituents[i] = clusterConstituents[i].getGEMChit();
    	}
    	
        // work out cluster properties
    	
       	for (GEMCHit hit : hitConstituents){
    		energySum += hit.getGEMCenergyDeposited();
    	}
    	
       	double[] energyWeight = new double[clusterLength];
       	
       	for (int i=0; i<clusterLength; i++){
    			energyWeight[i] = hitConstituents[i].getGEMCenergyDeposited() / energySum;
    	}
       	
	    // combine times using energy weighted average
        
       	if (DetectorCalibration.COMBINATION_METHOD.equals("average")) {
       	
       		for (int i=0; i<clusterLength; i++){
       			time += energyWeight[i] * hitConstituents[i].getGEMCtime() ;
       		}
             		
       	}
       	
       	// use earliest time from all hits in the cluster
       	
    	if (DetectorCalibration.COMBINATION_METHOD.equals("earliest")) {
    		
    		time = 10000;
    		
    		for (GEMCHit hit : hitConstituents){
           		if (hit.getGEMCtime() < time) {
           			time = hit.getGEMCtime();
           		}
           	}
      	}
    	
    	if (DetectorCalibration.COMBINATION_METHOD.equals("average")) {
    	
    		for (GEMCHit hit : hitConstituents){
    			x += hit.getGEMCpos().x();
    		}
    	
    		x = x / clusterLength;
    	
    		for (int i=0; i<clusterLength; i++){
    			y += energyWeight[i] * hitConstituents[i].getGEMCpos().y() ;
        		
    		}
       	
    		for (GEMCHit hit : hitConstituents){
    			z += hit.getGEMCpos().z();
    		}
    	
    		z = z / clusterLength;
    	
    	}
    	
    	if (DetectorCalibration.COMBINATION_METHOD.equals("earliest")) {
    		
    		double tempTime = 10000;
    		
    		for (GEMCHit hit : hitConstituents){
           		if (hit.getGEMCtime() < tempTime) {
           			tempTime = hit.getGEMCtime();
           			x = hit.getGEMCpos().x();
           			y = hit.getGEMCpos().y();
           			z = hit.getGEMCpos().z();
           		}
           	}
    		
    	}
    	
    	Vector3D position = new Vector3D(x,y,z);
    	
    	int[] iD = new int[clusterLength];
    	
    	for (int i=0; i<clusterLength; i++){
    		iD[i] = clusterConstituents[i].getID();
       	}
       
    	GEMCCluster cluster = new GEMCCluster(iD,energySum,time, position);
    	
    	panelData.addGEMCCluster(cluster);
		 
	 }

    
    /**
     * Determines if two hits have times within a given range.
     * 
     * @param firstHit
     * @param secondHit
     * @param range
     * @return true if hits have times within range, false otherwise
     */
    
    private boolean timesSimilar(Hit firstHit, Hit secondHit, double range) {
		if( Math.abs(firstHit.getTime() - secondHit.getTime()) < range){
			return true;
		}
		return false;
	}
    
    
    /**
     * 
     * Determines if two hits have y positions within a given range
     * 
     * @param firstHit
     * @param secondHit
     * @param range
     * @return  true if hits have y positions within given range, false otherwise
     */
	
    private boolean yPositionsSimilar(Hit firstHit, Hit secondHit, double range) {
		
		if (Math.abs(firstHit.getPosition().y()-secondHit.getPosition().y()) < range) {
			return true;
		}
		return false;
	}
    
    
    /**
     * Works out the uncertainty of an energy weighted quantity of arbitrary length
     * 
     * 
     * @param clusterConstituents
     * @param energyWeight
     * @param energySum
     * @param mode
     * @return the energy weighted uncertainty
     */
    

	private double getEnergyWeightedUnc(Hit[] clusterConstituents, double[] energyWeight, double energySum, String mode) {
		
		int clusterLength = clusterConstituents.length;
		
		double dQdQX[] = new double[clusterLength];
		double dQdEX[] = new double[clusterLength];
		
		double energy[] = new double[clusterLength];
		double uncEnergy[] = new double[clusterLength];
		double quantity[] = new double[clusterLength];
		double uncQuantity[] = new double[clusterLength];
		
		for (int k=0 ; k < clusterLength ; k++){
			
			energy[k] = clusterConstituents[k].getEnergy();
			uncEnergy[k] = clusterConstituents[k].getUncEnergy();
			
			if (mode.equals("y")){
				quantity[k] = clusterConstituents[k].getPosition().y();
				uncQuantity[k] = clusterConstituents[k].getUncPosition().y();
			}
			
			if (mode.equals("time")){
				quantity[k] = clusterConstituents[k].getTime();
				uncQuantity[k] = clusterConstituents[k].getUncTime();
			}
			
		}
		
		if (energySum > 0.00) {
			
			dQdQX = energyWeight;
			
			double positiveValueSum = 0.0;
			double negativeValueSum = 0.0;
			
			for ( int i=0 ; i < clusterLength ; i++ ){
				
				for ( int j=0 ; j < clusterLength ; j++ ){
					
					if ( j != i ){
						positiveValueSum += energy[j] * quantity[i];
						negativeValueSum += energy[j] * quantity[j];
					}
				}
				
				dQdEX[i] = (positiveValueSum - negativeValueSum) / Math.pow(energySum, 2);
				positiveValueSum = 0.0;
				negativeValueSum = 0.0;
				
			}
						
		}else {
			
			for ( int i=0 ; i < clusterLength ; i++ ){
				dQdQX[i] = 1.0 / clusterLength;
			}
			
		}
		
		double SquaredResult = 0.0;
		
		for (int k=0 ; k < clusterLength ; k++){
			SquaredResult += Math.pow(dQdQX[k],2) * Math.pow(uncQuantity[k], 2);
			SquaredResult += Math.pow(dQdEX[k],2) * Math.pow(uncEnergy[k], 2);
		}
		
		return ( Math.sqrt(SquaredResult));
	
	}
	
	
	
	/**
	 * 
	 * Works out, using GEMC positions and momentum, if two paddles have been triggered by one particle
	 * 
	 * @param firstPaddle
	 * @param secondPaddle
	 * @param thickness
	 * @return  true if two paddles have been triggered by the same particle, false otherwise
	 */
	
	private boolean breachOccursCorrected(Paddle firstPaddle, Paddle secondPaddle, double thickness) {
		
		// find line between paddles in XZ plane
		// average of reconstruction hits, as hits are at paddle centres
		
		boolean breach1 = false;
		boolean breach2 = false;
		
		double breachCentreX = (firstPaddle.getHit().getPosition().x() + secondPaddle.getHit().getPosition().x()) / 2;		
		double breachCentreZ = (firstPaddle.getHit().getPosition().z() + secondPaddle.getHit().getPosition().z()) / 2;		
		
		Vector3D panelNormalSector = panelGeom.getNormalVector();
			
		double gradientBreachLine = panelNormalSector.z() / panelNormalSector.x();
		
		double zInterceptBreachLine = breachCentreZ - ( gradientBreachLine *  breachCentreX );
		
			
		// normalise GEMChit momentum vector (not really necessary)
		
		double px2 = secondPaddle.getGEMChit().getGEMCmomentum().x();
		double py2 = secondPaddle.getGEMChit().getGEMCmomentum().y();
		double pz2 = secondPaddle.getGEMChit().getGEMCmomentum().z();
		
		double magnitudeMomentum = Math.sqrt( Math.pow(px2,2) + Math.pow(py2, 2) + Math.pow(pz2,2) );
		
		double normPx2 = px2/magnitudeMomentum;
		double normPz2 = pz2/magnitudeMomentum;
		
		// work out gradient of GEMC hit path
		
		double gradientHitPath = normPz2 / normPx2;
		
		if((gradientBreachLine - gradientHitPath) != 0.0){
		
			// work out z intercept of line through GEMC hit point 
		
			double zInterceptHitPath =  secondPaddle.getGEMChit().getGEMCpos().z() - gradientHitPath * secondPaddle.getGEMChit().getGEMCpos().x();
		
			// work out intercept breach line and GEMC hit path
		
			double interceptX = ( zInterceptHitPath - zInterceptBreachLine )  / (gradientBreachLine - gradientHitPath);
		
			double interceptZ = gradientBreachLine * interceptX + zInterceptBreachLine;
			
			// work out distance along line between breach centre and breach line intercept
		
			double breachDistanceFromCentre = Math.sqrt( Math.pow(breachCentreZ - interceptZ, 2) + Math.pow(breachCentreX - interceptX, 2) );
		
			//  work out if breachDistanceFromCentre is within breach line segment
		
			if (breachDistanceFromCentre <= (thickness / 2) ){
			
				breach1 = true;
			}else{
				breach1 = false;
			}
		
		}else{
			breach1 = false;
		}
		
		// repeat for paddle one
		
		px2 = firstPaddle.getGEMChit().getGEMCmomentum().x();
		py2 = firstPaddle.getGEMChit().getGEMCmomentum().y();
		pz2 = firstPaddle.getGEMChit().getGEMCmomentum().z();
		
		magnitudeMomentum = Math.sqrt( Math.pow(px2,2) + Math.pow(py2, 2) + Math.pow(pz2,2) );
		
		normPx2 = px2/magnitudeMomentum;
		normPz2 = pz2/magnitudeMomentum;
		
		// work out gradient of GEMC hit path
		
		gradientHitPath = normPz2 / normPx2;
		
		if((gradientBreachLine - gradientHitPath) != 0.0){
		
			// work out z intercept of line through GEMC hit point 
		
			double zInterceptHitPath =  firstPaddle.getGEMChit().getGEMCpos().z() - gradientHitPath * firstPaddle.getGEMChit().getGEMCpos().x();
		
			// work out intercept breach line and GEMC hit path
		
			double interceptX = ( zInterceptHitPath - zInterceptBreachLine )  / (gradientBreachLine - gradientHitPath);
			
			double interceptZ = gradientBreachLine * interceptX + zInterceptBreachLine;
		
			// work out distance along line between breach centre and breach line intercept
		
			double breachDistanceFromCentre = Math.sqrt( Math.pow(breachCentreZ - interceptZ, 2) + Math.pow(breachCentreX - interceptX, 2) );
		
			//  work out if breachDistanceFromCentre is within breach line segment
		
			if (breachDistanceFromCentre <= (thickness / 2) ){
				
				breach2 = true;
			}else{
				breach2 = false;
			}
		
		}else{
			breach2 = false;
		}
		
		if ((breach1 || breach2)&&
				timesSimilar(firstPaddle.getGEMChit(),secondPaddle.getGEMChit(),10)) {
			return true;
		}else {
			return false;
		}
		
		
	}

	
	/**
	 * 
	 * Calculates if two GEMCHits have times within a specific range
	 * 
	 * @param firstHit
	 * @param secondHit
	 * @param range
	 * @return true if time difference is within specific range, false otherwise
	 */
	
	private boolean timesSimilar(GEMCHit firstHit, GEMCHit secondHit, double range) {
		if( Math.abs(firstHit.getGEMCtime() - secondHit.getGEMCtime()) < range){
			return true;
		}
		return false;
	}
	
	
	/**
	 *  Calculates if two GEMCHits have y positions within a specific range 
	 * 
	 * @param firstHit
	 * @param secondHit
	 * @param range
	 * @return  true if y difference is within range, false otherwise
	 */
	
	private boolean yPositionsSimilar(GEMCHit firstHit, GEMCHit secondHit, double range) {
		
		if (Math.abs(firstHit.getGEMCpos().y()-secondHit.getGEMCpos().y()) < range) {
			return true;
		}
		return false;
	}
    
}

