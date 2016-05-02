package com.eugen.util.parselogic;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eugen.database.service.PDFEntryService;
import com.eugen.util.HTMLSourceOfOrdineANC;
import com.google.common.base.Strings;
import com.google.common.collect.TreeMultimap;

/**
 * Class that deals with some PDF parsing operations
 * @author eugene
 *
 */
@Component
public class CetatenieRomina {
	
	@Autowired
	PDFEntryService pdfEntryService;

	
    private final Logger log = LoggerFactory.getLogger(CetatenieRomina.class);
    private final String DOSARE_LINK    = "http://cetatenie.just.ro/";
    private final String DOSARE_PATTERN = "wp-content/(documents|uploads)/(O|o).+?(\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d(\\-)?(\\d)?)\\.pdf";
    private final String newestDosarDateWebRegex = "Data de.+?(\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d)";
    private HTMLSourceOfOrdineANC htmlSourceOfOrdineANC = new HTMLSourceOfOrdineANC();
    
 
    /**
     * Get all new links from the web that are "new" relative to the ones we have in database.
     * <br/>
     * This is achieved by getting only those files from the Web that are older then (in case they exist) the ones on the web
     * <br/>
     * <b>Implementation details:</br>
     * 1) We get the whole page as String, then split it on the newestDosarDateInDatabase as String. 
     *    The first part of the page is the one we are interested in.
     * 2) In the String obtained from (1) we apply the pattern and extract the dates and links   
     * 
     * @param newestDosarDateInDatabase from this {@link LocalDate} start searching on the web if there are more dosare to take
     * @return a {@link TreeMultimap} of all entries that need to be added to the database
     * @throws IOException 
     */
    public TreeMultimap<LocalDate, String> getLinksForDownload(LocalDate newestDosarDateInDatabase) throws Exception {
    	
    	TreeMultimap<LocalDate, String> result = TreeMultimap.create();
    	String newestDosarDateInDatabaseAsString = newestDosarDateInDatabase.toString("dd.MM.yyyy");
    	String dosarePageSource                  = htmlSourceOfOrdineANC.getSourceOfHMTLPage();
    	
    	long currentNumberOfEntries = pdfEntryService.getNumberOfPDFEntries();
    	//long currentNumberOfEntries = 0;
    	
    	log.info("===== There are {} entries in the DB at the moment =====", currentNumberOfEntries);
    	String firstPartOfPage = "";
    	
    	//Check if this is the first insert that we need to perform
    	if(currentNumberOfEntries == 0){
    		log.info("===== This is the first insert, no need to parse the page at all =====");
    		firstPartOfPage = dosarePageSource;
    	} else {
    		String splittedParts [] = dosarePageSource.split(newestDosarDateInDatabaseAsString);
        	if(splittedParts.length <= 1 ) 
        		throw new IOException("Ordine page is broken or layout has changed");
        	firstPartOfPage = splittedParts[0];
    	}
    	    	
    	Pattern p = Pattern.compile(DOSARE_PATTERN);
    	Matcher m = p.matcher(firstPartOfPage);
    	
    	while(m.find()){
    		String dosarLink = DOSARE_LINK + m.group(0);
    		String dosarDateFromWeb = m.group(3);
    		if(dosarDateFromWeb.length() > 10) dosarDateFromWeb = dosarDateFromWeb.substring(0,10);
    		result.put(LocalDate.parse(dosarDateFromWeb, DateTimeFormat.forPattern("dd.MM.yyyy")), dosarLink);
    	}
    	
    	log.info("===== There are {} new dosar to parse =====", result.size());
    	
    	return result;
    }
    
    //----------------------------------------------------------------------------------------------------
    /**
     * This method acts as a safety net in calculating how many PDF files are present
     * on the DOSARE_LINK page. <br/>
     * We do this by applying the pattern ">\\d+P<"
     * @return number of PDF Files that are on the Web
     * @throws IOException when the pattern could not be retrieved
     */
    public int howManyPDFAreThereOnThePage() throws IOException {
    	String dosarePageSource = htmlSourceOfOrdineANC.getSourceOfHMTLPage();
    	Pattern p = Pattern.compile(">\\d+P<");
    	Matcher m = p.matcher(dosarePageSource);
    	int i = 0;
    	while(m.find()){
    		++i;
    	}
    	if(i == 0) 
    		throw new IOException("Seems like the Ordine ANC page is not working or the layout of the page has changed");
    	log.info("===== There are : {} dosare on the Web Page =====", i);
    	return i;
    }
    
    /**
     * returns the newest dosar date that is present on the web
     * @return
     */
    public LocalDate newestDosarDateFromWeb() throws IOException {
    	String sourceOfDosarePage = htmlSourceOfOrdineANC.getSourceOfHMTLPage();
    	Pattern p = Pattern.compile(newestDosarDateWebRegex);
    	Matcher m = p.matcher(sourceOfDosarePage);
    	
    	String newestDosarDateWebAsString = "";
    	
    	while(m.find()){
    		newestDosarDateWebAsString = m.group(1);
    		log.info("===== Newest Dosar Date on the Web is : {} =====", newestDosarDateWebAsString);
    		//After the first one is found, exit immediately
    		break;
    	}
    	if(Strings.isNullOrEmpty(newestDosarDateWebAsString)) 
    		throw new IOException("Could not retrieve the newest dosar date from the web");
    	
		return LocalDate.parse(newestDosarDateWebAsString, DateTimeFormat.forPattern("dd.MM.yyyy"));
    }
}

