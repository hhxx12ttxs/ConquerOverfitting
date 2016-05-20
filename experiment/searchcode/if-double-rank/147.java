package ch.psi.fda.deserializer;

import hep.io.xdr.XDRInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import ch.psi.fda.core.messages.ComponentMetadata;
import ch.psi.fda.core.messages.DataMessage;
import ch.psi.fda.core.messages.DataMessageMetadata;
import ch.psi.fda.core.messages.DataQueue;
import ch.psi.fda.core.messages.EndOfStreamMessage;
import ch.psi.fda.core.messages.Message;
import ch.psi.fda.core.messages.StreamDelimiterMessage;

public class DataDeserializerMDA implements DataDeserializer {

	private static Logger logger = Logger.getLogger(DataDeserializerMDA.class.getName());
	
	private DataQueue queue;
	
	public DataDeserializerMDA(File file){
		
		try {
			RecursiveReturnContainer c = read(new FileInputStream(file));
			
			this.queue = new DataQueue(new LinkedBlockingQueue<Message>(), c.getMetadata());
			
			// Add data to queue
			for(Message m: c.getMessage()){
				queue.getQueue().put(m);
			}
			queue.getQueue().put(new EndOfStreamMessage());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public RecursiveReturnContainer read(InputStream in) throws IOException {
		logger.fine("Read MDA input stream");
		
		XDRInputStream x = new XDRInputStream(in);
		
		
		/**
		 * Read file header
		 */
		float version = x.readFloat();
		logger.fine("MDA - version:: "+version);
		int scanNumber = x.readInt();
		logger.fine("MDA - scan number:: "+scanNumber);
		int rank = x.readInt();
		logger.fine("MDA - rank:: "+rank);
		
		for(int i=rank;i>0; i--){
			// Read dimension size
			int dimension = x.readInt();
			logger.fine("MDA - dimension["+i+"] :: "+dimension);
		}
		
		int isRegular = x.readInt(); //(true=1, false=0)
		logger.fine("MDA - isRegular:: "+isRegular);
		int pExtra = x.readInt(); // Number of extra pvs
		logger.fine("MDA - pExtra:: "+pExtra);
		
		
		/**
		 * Read data
		 */
		RecursiveReturnContainer container = readData(x);
		
		/**
		 * Read extra PVs
		 */
		if(pExtra > 0){
			int length;
			logger.fine("Extra PVs");

			int numExtra = x.readInt();
			logger.fine("MDA - number of extra pvs:: "+numExtra);

			for(int i = 0;i<numExtra; i++){

				length = x.readInt();
				if(length > 0){
					String pvName = x.readString();
					logger.fine("MDA - pv name:: "+pvName);
				}

				length = x.readInt();
				if(length > 0){
					String pvDescription = x.readString(); // description
					logger.fine("MDA - pv desciption:: "+pvDescription);
				}
				
				int pvType = x.readInt();
				logger.fine("MDA - pv type:: "+pvType);

				
				int pvCount = 0;
				String pvUnit;
				if(pvType != 0){ // if pv type != DBR_STRING
					pvCount = x.readInt();
					logger.fine("MDA - pv count:: "+pvCount);
					length = x.readInt();
					if(length > 0){
						pvUnit = x.readString();
						logger.fine("MDA - pv unit:: "+ pvUnit);
					}
				}
				
				
				if(pvType == 0){ // pvType == DBR_STRING
					length = x.readInt();
					if(length > 0){
						String pvValue = x.readString();
						logger.fine("MDA - pv value:: "+pvValue);
					}
				}
				else if(pvType == 32){ // pvType == DBR_CTRL_CHAR
					for(int u=0;u<pvCount; u++){
						char pvValue = x.readChar();
						logger.fine("MDA - pv value:: "+pvValue);
					}
				}
				else if(pvType == 29){ // pvType == DBR_CTRL_SHORT
					for(int u=0;u<pvCount; u++){
						int pvValue = x.readInt();
						logger.fine("MDA - pv value:: "+pvValue);
					}
				}
				else if(pvType == 33){ // pvType == DBR_CTRL_LONG
					for(int u=0;u<pvCount; u++){
						int pvValue = x.readInt();
						logger.fine("MDA - pv value:: "+pvValue);
					}
				}
				else if(pvType == 30){ // pvType == DBR_CTRL_FLOAT
					for(int u=0;u<pvCount; u++){
						float pvValue = x.readFloat();
						logger.fine("MDA - pv value:: "+pvValue);
					}
				}
				else if(pvType == 34){ // pvType == DBR_CTRL_DOUBLE
					for(int u=0;u<pvCount; u++){
						double pvValue = x.readDouble();
						logger.fine("MDA - pv value:: "+pvValue);
					}
				}
				
			}
		}
		
		logger.fine("Reading and conversion done ...");
		
		return container;
	}

	
	private RecursiveReturnContainer readData(XDRInputStream x) throws IOException {
		
		int length;
		
		logger.fine("Read scan");
		
		/**
		 * Read scan header
		 */
		int scanRank = x.readInt();
		logger.fine("MDA - scan rank (this):: " + scanRank);
		int npts = x.readInt();
		logger.fine("MDA - number of requested points (npts):: " + npts);
		int currentPoint = x.readInt();
		logger.fine("MDA - current point:: " + currentPoint);

		// Read pointers to lower scans
		if(scanRank > 1){
 			// For 1D scans this block is never reached
			for(int i=0;i<npts;i++){
				int pointerLowerScans = x.readInt(); // pointer to lower scans
				logger.fine("MDA - pointer lower scans:: "+pointerLowerScans);
			}
		}
		
		
		/**
		 * Read scan info
		 */
		length = x.readInt();
		if(length > 0){
			String scanName = x.readString();
			logger.fine("MDA - scanName:: "+scanName);
		}

		length = x.readInt();
		if(length > 0){
			String scanTime = x.readString(); // timestamp
			logger.fine("MDA - scanTime:: "+ scanTime);
		}

		
		int numberOfPositioners = x.readInt();
		logger.fine("MDA - number of positioners:: "+numberOfPositioners);
		int numberOfDetectors = x.readInt();
		logger.fine("MDA - number of detectors:: "+ numberOfDetectors);
		int numberOfTriggers = x.readInt();
		logger.fine("MDA - number of triggers:: "+ numberOfTriggers);
		

		List<ComponentMetadata> componentMetadataList = new ArrayList<ComponentMetadata>();
		/**
		 * Read positioners metadata
		 */
		for(int i = 0; i<numberOfPositioners; i++){
			
			logger.fine("Read positioner metadata");
			
			int positionerNumber = x.readInt();
			logger.fine("MDA - positioner number:: "+positionerNumber);

			length = x.readInt();
			if(length>0){
				String positionerName = x.readString();
				logger.fine("MDA - positioner name:: "+positionerName);
				// MDA starts at dimension number 1 we start at 0
				componentMetadataList.add(new ComponentMetadata(positionerName,scanRank-1));
			}

			length = x.readInt();
			if(length > 0){
				String positionerDescription = x.readString();
				logger.fine("MDA - positioner description:: "+positionerDescription);
			}

			length = x.readInt();
			if(length > 0){
				String positionerStepMode = x.readString();
				logger.fine("MDA - positioner step mode:: "+positionerStepMode);
			}

			length = x.readInt();
			if(length > 0){
				String positionerUnit = x.readString();
				logger.fine("MDA - positioner unit:: "+positionerUnit);
			}

			length = x.readInt();
			if(length > 0){
				String readbackName = x.readString();
				logger.fine("MDA - readback name:: "+readbackName);
			}

			length = x.readInt();
			if(length > 0){
				String readbackDescription = x.readString();
				logger.fine("MDA - readback description:: "+readbackDescription);
			}

			length = x.readInt();
			if(length > 0){
				String readbackUnit = x.readString();
				logger.fine("MDA - readback unit:: "+readbackUnit);
			}
		}

		/**
		 * Read detector metadata
		 */
		for(int i=0; i<numberOfDetectors; i++){
			logger.fine("Read detector metadata");
			
			int detectorNumber = x.readInt();
			logger.fine("MDA - detector number:: "+ detectorNumber);
			
			length = x.readInt();
			if(length > 0){
				String detectorName = x.readString();
				logger.fine("MDA - detector name:: "+detectorName);
				// MDA starts at dimension number 1 we start at 0
				componentMetadataList.add(new ComponentMetadata(detectorName, scanRank-1));
			}

			length = x.readInt();
			if(length > 0){
				String detectorDescription = x.readString();
				logger.fine("MDA - detector description:: "+detectorDescription);
			}

			length = x.readInt();
			if(length > 0){
				String detectorUnit = x.readString();
				logger.fine("MDA - detector unit:: "+detectorUnit);
			}
		}

		/**
		 * Read trigger metadata
		 */
		for(int i=0; i<numberOfTriggers; i++){
			logger.fine("Read trigger metadata");
			
			int triggerNumber = x.readInt();
			logger.fine("MDA - trigger number:: "+triggerNumber);

			length = x.readInt();
			if(length > 0){
				String triggerName = x.readString();
				logger.fine("MDA - trigger name:: "+triggerName);
			}

			float triggerCommand = x.readFloat();
			logger.fine("MDA - trigger command:: "+triggerCommand);
		}
		
		
		ArrayList<Double[]> data = new ArrayList<Double[]>();
		
		/**
		 * Read positioner data (readback)
		 */
		for(int i = 0; i<numberOfPositioners; i++){
			
			logger.fine("Read positioner data");
			
			Double[] pdata = new Double[npts]; 

			StringBuffer b = new StringBuffer();
			b.append("[ ");
			for(int t=0; t<npts; t++){
				pdata[t]=x.readDouble();
				b.append(pdata[t]+" ");
			}
			b.append("]");
			logger.fine("MDA - positioner "+b.toString());
			
			data.add(pdata);
		}

		
		/**
		 * Read detector data
		 */
		for(int i=0; i<numberOfDetectors; i++){
			logger.fine("Read detector data");
			Double[] pdata = new Double[npts];
			
			StringBuffer b = new StringBuffer();
			b.append("[ ");
			for(int t=0; t<npts; t++){
				pdata[t] = new Double(x.readFloat());
				b.append(pdata[t]+" ");
			}
			b.append("]");
			logger.fine("MDA - detector "+b.toString());
			
			data.add(pdata);
		}
		
		logger.fine("Read scan done ...");
		
		
		RecursiveReturnContainer cont = new RecursiveReturnContainer();
		// Update component metadata
		cont.getMetadata().getComponents().addAll(componentMetadataList);
		
		if(scanRank > 1){
			/**
			 * Conversion logic if scan rank is > 1
			 */
			
			/**
			 * Read all scans recursively
			 */
			for(int i=0;i<npts;i++){
				
				// Recursive call
				RecursiveReturnContainer container = readData(x);
				
				if(i==0){
					// For the first scan of each dimension component data is read and stored
					cont.getMetadata().getComponents().addAll(container.getMetadata().getComponents());
				}
				
				logger.fine("Convert data structure [rank="+scanRank+"] ...");
				for(Message m: container.getMessage()){
					if(m instanceof DataMessage){
						// Add own data to message and pass it to container
						DataMessage mm = new DataMessage();
						for(Double[] d: data){
							mm.getData().add(d[i]);
						}
						mm.getData().addAll(((DataMessage) m).getData());
						cont.getMessage().add(mm);
					}
					else if(m instanceof StreamDelimiterMessage){
						// Just pass message to own container
						cont.getMessage().add(m);
					}
				}
				
				logger.fine("Conversion done [rank="+scanRank+"]");
			}
			
			// Add dimension delimiter message
			StreamDelimiterMessage m = new StreamDelimiterMessage(scanRank);
			cont.getMessage().add(m);
		}
		else{
			/**
			 * Conversion logic if scan rank is 1
			 */
			
			logger.fine("Convert data structure [rank="+scanRank+"]...");
			
			for(int t = 0; t<npts ; t++){
				DataMessage m = new DataMessage();
				for(Double[] d: data){
					m.getData().add(d[t]);
				}
				cont.getMessage().add(m);
			}
			
			// Add dimension delimiter message
			StreamDelimiterMessage m = new StreamDelimiterMessage(scanRank);
			cont.getMessage().add(m);
			
			
			logger.fine("Conversion done [rank="+scanRank+"]");
		}
				
		return(cont);
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.psi.fda.deserializer.DataDeserializer#getQueue()
	 */
	@Override
	public DataQueue getQueue() {
		return queue;
	}
	
}

/**
 * Helper class
 * @author ebner
 *
 */
class RecursiveReturnContainer{
	private List<Message> message = new ArrayList<Message>();
	private DataMessageMetadata metadata = new DataMessageMetadata();
	
	public List<Message> getMessage() {
		return message;
	}
	public DataMessageMetadata getMetadata() {
		return metadata;
	}
}

/**
*
* http://www.aps.anl.gov/bcda/synApps/sscan/saveData_fileFormat.txt
* 
* scan file format

* FILE HEADER
* 	xdr_float:	VERSION  (1.3)
* 	xdr_long:	scan number
* 	xdr_short	data's rank
* 	xdr_vector(rank, xdr_int) dims;
* 	xdr_int		isRegular (true=1, false=0)			
* 	xdr_long:	pointer to the extra pvs
* 
* 
* SCAN
* 	HEADER:
* 	xdr_short:		this scan's rank
* 	xdr_long:		number of requested points (NPTS)
* 	xdr_long:		current point (CPT)
* 	if the scan rank is > 1
* 	  xdr_vector(NPTS, xdr_long)	pointer to the lower scans
* 
* 	INFO:
* 	xdr_counted_string:	scan name
* 	xdr_counted_string:	time stamp
* 
* 
* 	xdr_int:		number of positioners
* 	xdr_int:		number of detectors
* 	xdr_int:		number of triggers
* 
* 	for each positioner
* 	  xdr_int:		positioner number
* 	  xdr_counted_string:	positioner name
* 	  xdr_counted_string:	positioner desc
* 	  xdr_counted_string:	positioner step mode
* 	  xdr_counted_string:	positioner unit
* 	  xdr_counted_string:	readback name
* 	  xdr_counted_string:	readback description
* 	  xdr_counted_string:	readback unit
* 
* 	for each detector
* 	  xdr_int:		detector number
* 	  xdr_counted_string:	detector name
* 	  xdr_counted_string:	detector desc
* 	  xdr_counted_string:	detector unit
* 
* 	for each trigger
* 	  xdr_int:		trigger number
* 	  xdr_counted_string:	trigger name
* 	  xdr_float:		trigger command
* 
* 	DATA:
* 	for each positioner
* 	  xdr_vector(NPTS, xdr_double):	readback array
* 
* 	for each detector
* 	  xdr_vector(NPTS, xdr_float):	detector array
* 
* [SCAN]
* ...
* ...
* ...
* [SCAN]
* 
* EXTRA PVs
* 	xdr_int:	number of extra pvs
* 
* 	for each pv
* 	  xdr_counted_string:	name
* 	  xdr_counted_string:	desc
* 	  xdr_int:		type
* 	  if type != DBR_STRING
* 	    xdr_long:		count
* 	    xdr_counted_string:	unit
* 
* 	  depending on the type:
* 	    DBR_STRING:
* 		xdr_counted_string:		value
* 	    DBR_CTRL_CHAR:
* 		xdr_vector(count, xdr_char):	value
* 	    DBR_CTRL_SHORT:
* 		xdr_vector(count, xdr_short):	value
* 	    DBR_CTRL_LONG:
* 		xdr_vector(count, xdr_long):	value
* 	    DBR_CTRL_FLOAT:
* 		xdr_vector(count, xdr_float):	value
* 	    DBR_CTRL_DOUBLE:
* 		xdr_vector(count, xdr_double):	value
* -----------------------------------------------------------------------
* 
* A 1D scan looks like this:
* 
* header
* extra PV's
* 
* A 2D scan looks like this
* 
* header
* scan2
* 	scan1
* 	scan1
* 	...
* extra PV's
* 
* A 3D scan looks like this
* 
* header
* scan3
* 	scan2
* 		scan1
* 		scan1
* 		...
* 	scan2
* 		scan1
* 		scan1
* 		...
* 	...
* extra PV's
* 
*/
