package com.eugen.task;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;


import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.eugen.database.entities.PDFEntry;
import com.eugen.database.service.PDFEntryService;
import com.eugen.database.service.EntryFromHeaderService;
import com.eugen.database.service.SkippedEntryService;
import com.eugen.task.HandleExceptions.PLACES_OF_ERROR;
import com.eugen.util.email.EmailSubject;
import com.eugen.util.email.GenericEmailSenderInterface;
import com.eugen.util.email.velocityParams.DatabaseInsertStatusVelocityParameters;
import com.eugen.util.parselogic.CetatenieRomina;
import com.google.common.collect.ImmutableList;


/**
 * This class adds the database entries from each PDF file
 * It gets executed once a day
 * @author eugenerabii
 *
 */

@Service
class DatabaseInsertTask {
	private final Logger log = LoggerFactory.getLogger(DatabaseInsertTask.class);
	
	@Autowired
	GenericEmailSenderInterface emailSender;
	
	@Autowired
	PDFEntryService service;
	
	@Autowired
	CetatenieRomina cetatenieRomina;
	
	@Autowired
	DatabaseInsertTaskHelper databaseInsertTaskHelper;
	
	@Autowired
	EntryFromHeaderService headerService;
	
	@Autowired
	SkippedEntryService skippedService;
	
	@Autowired
	HandleExceptions handleExceptions;
	
	//------------------------------------------------------------------------------------------------------------------
	/**
	 * ? is allowed for the day-of-month and day-of-week and it means "no specific value"
	 */
	@Scheduled(cron="0 26 7 * * ?")
	private void generateDatabaseRows() {
		log.info("===== Executing period task =====");
		LocalDate newestDosarDateDatabase = null;
		LocalDate newestDosarDateWeb      = null;
		
		try{
			newestDosarDateDatabase = service.getNewestDosarDate();
			newestDosarDateWeb      = cetatenieRomina.newestDosarDateFromWeb();
			
			log.info("===== Most Recent Dosar Date in Database is {} " , newestDosarDateDatabase.toString("yyyy-MMM-dd"));
			log.info("===== Most Recent Dosar Date from Web    is {}"  , newestDosarDateWeb.toString("yyyy-MMM-dd"));
		} catch(Exception exception){ 
			handleExceptions.handleException(exception, PLACES_OF_ERROR.DATABASE_INSERT_TASK); 
			return; 
		}
		
		//There are no new dosare on the web, no need to update the DB
		if((newestDosarDateDatabase.compareTo(newestDosarDateWeb)) >= 0){
			log.info("===== Everything is in sync, no need to update the database =====");
			return;
		} else{
			SortedMap<LocalDate, Collection<String>> dosare = null;
			try {
				dosare = cetatenieRomina.getLinksForDownload(newestDosarDateDatabase).asMap();
			} catch (Exception e) {
				handleExceptions.handleException(e, PLACES_OF_ERROR.DATABASE_INSERT_TASK); 
				return;
			}
				
			int dosareParsedUnderCurrentExecution           = 0;
			int dosareThatWereCanceledUnderCurrentExecution = 0;
				
			for(Map.Entry<LocalDate, Collection<String>> entry : dosare.entrySet()){
				LocalDate date = entry.getKey();
				Collection<String> linkuri = entry.getValue();
				for(String link : linkuri){
					++dosareParsedUnderCurrentExecution;	
					Collection<PDFEntry> entries = null;
					try{
						entries = databaseInsertTaskHelper.getEntriesFromPDF(link, date);
						ImmutableList<PDFEntry> entriesFormASinglePDF = ImmutableList.copyOf(entries);
						service.savePDFEntries(entriesFormASinglePDF);
					} catch(Exception e){
						++dosareThatWereCanceledUnderCurrentExecution;
						handleExceptions.handleException(e, PLACES_OF_ERROR.DATABASE_INSERT_TASK);
						return;
					}	
				}
			}
				
			try{	
				long totalAmmountOfEntriesFromHeaders = headerService.getNumberOfEntriesFromAllHeaders();
				long currentNumberOfEntriesInDatabase = service.getNumberOfPDFEntries();
				long skippedEntries                   = skippedService.getAllEntriesNumber();
				
				DatabaseInsertStatusVelocityParameters.Builder builder = 
						new DatabaseInsertStatusVelocityParameters.Builder();
				builder.currentNumberOfEntriesInDatabase((int)currentNumberOfEntriesInDatabase)
				       .dosareParsedUnderCurrentExecution(dosareParsedUnderCurrentExecution)
				       .dosareThatWereCanceledUnderCurrentExecution(dosareThatWereCanceledUnderCurrentExecution)
				       .skippedEntries((int)skippedEntries)
				       .totalAmmountOfEntriesFromHeaders((int)totalAmmountOfEntriesFromHeaders);
				       
				emailSender.sendEmail("eugen.rabii@gmail.com", 
						builder.build(), 
						EmailSubject.DATABASE_INSERT_STATUS_MAIL, 
						Locale.ENGLISH);
				
				} catch(Exception e){
					handleExceptions.handleException(e, PLACES_OF_ERROR.DATABASE_INSERT_TASK);
				}	
			
			log.info("===== Done executing period task!! =====");		
		} 
	}
}

