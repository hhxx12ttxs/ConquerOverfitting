package com.eugen.manual.task;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eugen.database.entities.PDFEntry;
import com.eugen.database.entities.ProximityPDFEntry;
import com.eugen.database.service.PDFEntryService;
import com.google.common.collect.ImmutableList;

@Service
public class GenerateInsertStatements_26_NOV_2012 {
	
	private final Logger log = LoggerFactory.getLogger(GenerateInsertStatements_26_NOV_2012.class);
	
	@Autowired
	PDFEntryService service;
	
	
	/**
	 * We need to execute this task ONLY once, once in the lifetime of the application.
	 * So, I executed it on the 26-NOV-2012. If the DB is brought down and data is re-build in the
	 * DB, then this task needs to be executed again.
	 */
	//@Scheduled(cron="0 39 7 * * MON")
	public void insert() throws Exception {
		LocalDate today = new LocalDate(2012, 11, 26);
		LocalDate now   = LocalDate.now();
		
		log.info("===== Today is {} =====",today.toString("yyyy-MMM-dd"));
		log.info("===== Now is {} ====="  ,now.toString("yyyy-MMM-dd"));
		
		if(today.compareTo(now) != 0) {
			log.info("===== Task already executed =====");
			return;
		}
			
		LocalDate dosarDate = new LocalDate(2012,11,16);
		
		PDFEntry entry = new PDFEntry();
		entry.setAnexa(false); entry.setDosarDate(dosarDate); entry.setDosarID("84475/2011");
		entry.setDosarLink("http://cetatenie.just.ro/wp-content/uploads/Ordin-967P-din-16.11.20121.pdf");
		entry.setEntryNumber(365); entry.setFirstname("NATALIA"); entry.setLastname("TOMSA");
		
		ProximityPDFEntry proPdfEntry = new ProximityPDFEntry();
		proPdfEntry.setFirstLastName("TOMSA;NATALIA");
		proPdfEntry.setPdfEntry(entry);
		entry.setProximityEntry(proPdfEntry);
		
		PDFEntry entry2 = new PDFEntry();
		entry2.setAnexa(false); entry2.setDosarDate(dosarDate); entry2.setDosarID("82366/2011");
		entry2.setDosarLink("http://cetatenie.just.ro/wp-content/uploads/Ordin-967P-din-16.11.20121.pdf");
		entry2.setEntryNumber(389); entry2.setFirstname("VLADIMIR"); entry2.setLastname("VASILITA");
		
		ProximityPDFEntry proximityPDFEntry2 = new ProximityPDFEntry();
		proximityPDFEntry2.setFirstLastName("VLADIMIR;VASILITA");
		proximityPDFEntry2.setPdfEntry(entry2);
		entry2.setProximityEntry(proximityPDFEntry2);
		
		service.savePDFEntries(ImmutableList.of(entry, entry2));
		
		/*
		builder.put("365. TOMSA NATALIA (84475/2011) ", 
				"http://cetatenie.just.ro/wp-content/uploads/Ordin-967P-din-16.11.20121.pdf");
		builder.put("389.VASILITA VLADIMIR (82366/2011).", 
				"http://cetatenie.just.ro/wp-content/uploads/Ordin-967P-din-16.11.20121.pdf");
		*/
		
	}
}

