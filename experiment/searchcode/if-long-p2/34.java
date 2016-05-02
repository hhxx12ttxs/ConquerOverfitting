package com.smappee.nilm;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.logging.Logger;

import com.smappee.entities.Sample;




public class Filter  implements Serializable {

	private static final long serialVersionUID = -4458304114812544566L;
	private static Logger log;
	private static Integer  MaxFilterID=0;
	private Long    DeviceID=0L;
	private Integer	FilterID=0;
	private String  sLabel="";
	private Long 	p1 = 0L; 	//Long.MAX_VALUE;
	private Long 	p2 = 0L;	//Long.MAX_VALUE;
	private Long 	p3 = 0L;	//Long.MAX_VALUE;
	private Long 	CountON = 0L;
	private Long 	CountOFF = 0L;
	private Long 	LastStatus=0L;

	private Long 	Dev = 20L;			// Deviation
	private Filter 	NextFilter=null;	// Next Filter to test..
	
	Filter()
	{  if( log== null )
	 	 log = Logger.getLogger( Filter.class.getName() );
	   this.FilterID = ++MaxFilterID;
	}
	
	int filterSample( Sample aS ){
		// The delta values!!
		return this.filterSample( aS.getdP1kW().longValue() ,  aS.getdP2kW().longValue() ,  aS.getdP3kW().longValue(), aS.getDeviceID() );
	}
	
	int filterSample( Long sP1, Long sP2, Long sP3, Long aDeviceID )
 	 {	Long sP1abs, sP2abs, sP3abs;

 	 
 	 	if( log== null )
 	 	 log = Logger.getLogger( Filter.class.getName() );
 	
 	 	// If this Filter not for this Device, then jump to next Filter..
 	 	// Set the DeviceID for this Filter
 	 	/*
	 	if( aDeviceID != this.DeviceID )
	 	 {	if( this.NextFilter != null )
	 		   return( this.NextFilter.filterSample ( sP1, sP2, sP3, aDeviceID ));
	 	 	else
	 	 	{ 	// No filter found for this DeviceID!!
	 	 		log.info("Adding new Filter for Sample with : " + sP1 +", " + sP2 + ", " + sP3 + "DeviceID " + aDeviceID );
		 	 	this.NextFilter = new Filter();
		 	 	this.NextFilter.setFilter( sP1, sP2, sP3, aDeviceID);
		 	 	return 0;
	 	 	}
	 	 }
	 	 */
	 	
 	 	// Absolute values
 	 	sP1abs = Math.abs( sP1 );
 	 	sP2abs = Math.abs( sP2 );
 	 	sP3abs = Math.abs( sP3 );

 	 	// Test if Sample is meaningful, i.e. values are relevant
		if(  (sP1abs > Dev) || (sP2abs  > Dev) || ( sP3abs  > Dev) )	// Valid Sample 
 		  {	
		    Long lSum = Math.abs(sP1abs*p1 + sP2abs*p2 + sP3abs*p3 )*1000L;
	        if( p1 * p2 * p3 != 0 )	// Valid Filter!
		      lSum /= (p1*p1 + p2*p2 +p3*p3 );
		    
		    // Values are useful, at least one that is not 0 or close to 0 (> Dev) !
		    log.info("Checking this valid Sample with : " + sP1abs +", " + sP2abs + ", " + sP3abs + "DeviceID " + aDeviceID +" lSum " + lSum + " Filter p1 " + p1 +" p2 " + p2 + " p3 " +p3 + "this.DeviceID " + this.DeviceID );
		    
		    if( (lSum > 900) && (lSum < 1100) )
//		     if( aDeviceID == this.DeviceID )		// Corresponding Device!
 		     {
 		    	// Here we can add importance to ON / OFF status as expected..
 		    	// We should get the opposite of last time, kept in LastStatus..
 		    	
 		    	// Found match..
 		    	// Find sign, was it on or off?
 		    	//Long lSign = sP1/p1 + sP2/p2 + sP3/p3;
		    	Long lSign = (sP1*p1 + sP2*p2 + sP3*p3 );
 		    	if( lSign > 0 )
 		          { CountON++;  LastStatus = 1L;  // We switched on!		          
 		          }
 		    	else
 		    	  { CountOFF++; LastStatus = -1L;  // We switched off;
 		    	  }
 		    	// Filter matched
 		    	return 1;
 		     }
		    
 		    // no match, but test or create other filter
 		    if( this.NextFilter != null )
 		    	return( this.NextFilter.filterSample(sP1, sP2, sP3, aDeviceID ));	// Test Next Filter .. and if at end of list of Filters (NextFilter = null 
 		 	else // Add a new Filter for this new Sample.. and for this DeviceID
 		    {	this.NextFilter = new Filter();
 		    	this.setFilter( sP1.longValue(), sP2.longValue(), sP3.longValue(), aDeviceID);
 		    	return 0;
 		    }
 		 }
		
		// Sample not useful
		return -1;
 	 }

	void setFilter( Long sP1, Long sP2, Long sP3, Long aID ) 
	 {
		// Absolute values
 	 	Long sP1abs = Math.abs( sP1 );
 	 	Long sP2abs = Math.abs( sP2 );
 	 	Long sP3abs = Math.abs( sP3 );
 	 	this.setDeviceID( aID );
 	 	
		if(  (sP1abs >= Dev) || (sP2abs  >= Dev) || (Math.abs( sP3abs ) >= Dev) )			// >= here   other place >   otherwise could loop for ever
		{
			// New Filter created for this Sample
			//if( sP1abs > Dev )
				this.p1 = sP1abs;
			//if( sP2abs > Dev )
				this.p2 = sP2abs;
			//if( sP3abs > Dev )
				this.p3 = sP3abs;
			return;
		}
		this.p1= Long.MAX_VALUE;	// This may not happen, but set p1 very high to make this filter never work..  and avoid division by zero
	 }

	void setDeviceID( Long lDeviceID )
	 {  this.DeviceID = lDeviceID;
	 }
	
	void setLabel( String aLabel )
	 {	this.sLabel = aLabel;
	 }
	
	void XmppToUser()
	 {
		if( sLabel.matches( "" ) )
		 {
			// we still do know this Load
			// if CountON and CountOFF are reasonable ..
			if( (CountON > 10L) && (CountOFF > 8L) )    // 8 and 10, only missed 2 
			 { // Ask user what Load this is..
			   // If user is in the home, !!  combine with check of presence??
			   // And not during inital init !!	would be an avalanche of XMPP messages..
			 }
		 }
	 }
	
	void printResults( PrintWriter pWriter )
	{   
		pWriter.print("<tr><td>" + this.DeviceID + "</td>");
		pWriter.print("<td>" 	+ this.FilterID + "</td");
		pWriter.print("<td>" 		+ this.sLabel 	+ "</td> ");

		if( this.p1 < 1000000L  ) 
				pWriter.print( "<td>" + this.p1 +"</td>");
		else	pWriter.print( "<td></td>");
		
		if( this.p2 < 1000000L ) 
				pWriter.print( "<td>" + this.p2 +"</td>");
		else	pWriter.print( "<td></td>");
		
		if( this.p3 < 1000000L ) 
				pWriter.print( "<td>" + this.p3 + "</td>");
		else	pWriter.print( "<td></td>");
		
		pWriter.print( "<td>" 	+ this.CountON + "</td>");
		pWriter.print( "<td>" 	+ this.CountOFF +"</td></tr>");
		
        if( this.NextFilter != null)
          this.NextFilter.printResults(pWriter);
	}

	
}

