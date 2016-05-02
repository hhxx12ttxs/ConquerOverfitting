package com.eugen.database.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eugen.database.entities.PDFEntry;
import com.eugen.database.entities.QPDFEntry;
import com.eugen.database.repositories.PDFEntryRepository;
import com.eugen.database.service.PDFEntryService;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mysema.query.jpa.impl.JPAQuery;

/**
 * Service implementation for the {@link PDFEntryService} interface
 * @author eugenrabii
 *
 */
@Service
@Transactional(readOnly=true)
public class PDFEntryServiceImpl implements PDFEntryService  {
	
	@PersistenceContext
	EntityManager entityManager;
	
	@Autowired
	PDFEntryRepository repository;
	
	private static final String KEY_OLDEST_NAME="KEY_OLDEST_NAME";
	private static final String KEY_NEWEST_NAME="KEY_NEWEST_NAME";
	private final LoadingCache<String, LocalDate> oldestDateCache;
	private final LoadingCache<String, LocalDate> newestDateCache;
	private final Logger log = LoggerFactory.getLogger(PDFEntryServiceImpl.class);
	
	//---------------------------------------------------------------------------------------------------------
	//Create cache loaders, so that not every request goes to the DB
	public PDFEntryServiceImpl() {
		oldestDateCache = CacheBuilder.newBuilder()
				.maximumSize(2)
				.expireAfterWrite(12, TimeUnit.HOURS)
				.build(new CacheLoader<String, LocalDate>() {
					public LocalDate load(String key){
						log.info("===== Loading key {} to cache =====" , key);
						JPAQuery query = new JPAQuery(entityManager);
						QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
						LocalDate oldestDosarDate = query.from(qpdfEntry).uniqueResult(qpdfEntry.dosarDate.min());
						return oldestDosarDate;										
					}
				});
		
		newestDateCache = CacheBuilder.newBuilder()
				.maximumSize(2)
			    .expireAfterWrite(12, TimeUnit.HOURS)
				.build(new CacheLoader<String, LocalDate>() {
					public LocalDate load(String key){
						log.info("===== Loading key {} to cache =====" , key);
						JPAQuery query = new JPAQuery(entityManager);
						QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
						LocalDate newestDosarDate = query.from(qpdfEntry).uniqueResult(qpdfEntry.dosarDate.max());
						if(newestDosarDate == null) {
							//The idea is simple here:
							//1) We return the date that is the oldest on the Web - we manually took it
							//2) Since there are no rows in the DB, this is irrelevant almost
							log.info("===== Seems like the DB is empty and thus this is the first insert =====");
							log.info("===== Will return the date from the Web that we will take manually =====");
							return LocalDate.parse("07.03.2011", DateTimeFormat.forPattern("dd.MM.yyyy"));
						}
						return newestDosarDate;
					}
				});									  
	}
	
	//---------------------------------------------------------------------------------------------------------
	@Override
	public void savePDFEntries(ImmutableList<PDFEntry> entries) throws Exception {
		if(entries.size() == 0) return;
		log.debug("===== Will insert : {} =====" , entries.size());
		repository.save(entries);
		repository.flush();
	}
	
	//---------------------------------------------------------------------------------------------------------
	@Override
	public LocalDate getNewestDosarDate() throws Exception {
		log.info("===== Getting the newest dosarDate from the DB =====");
		//We need only one value in cache - the newestDate from the DB. Thus we set it's key to a static final value
		//since we do not really care about what they key name is.
		return newestDateCache.get(KEY_NEWEST_NAME);
	}
	
	//---------------------------------------------------------------------------------------------------------
	@Override
	public LocalDate getOldestDosarDate() throws Exception {
		//We need only one value in cache - the newestDate from the DB. Thus we set it's key to a static final value
		//since we do not really care about what they key name is.
		return oldestDateCache.get(KEY_OLDEST_NAME);
	}

	//---------------------------------------------------------------------------------------------------------
	@Override
	public long getNumberOfPDFEntries() {
		log.info("===== Counting number of current entries =====");
		JPAQuery query = new JPAQuery(entityManager);
		QPDFEntry qpdEntry = QPDFEntry.pDFEntry;
		return query.from(qpdEntry)
				    .count();
	}

	//---------------------------------------------------------------------------------------------------------
	@Override
	public ImmutableList<PDFEntry> getResultsFromSeachUnlimited(String firstname, String lastname) throws Exception {
		
		Preconditions.checkArgument(!Strings.nullToEmpty(firstname).trim().equals(""), "firstname must be present");
		Preconditions.checkArgument(!Strings.nullToEmpty(lastname).trim().equals(""), "lastname must be present");
		
		log.info("===== Searching for {} and {} " , firstname , lastname);
		JPAQuery query = new JPAQuery(entityManager);
		QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
		
		ImmutableList<PDFEntry> result = ImmutableList.copyOf(
				query.from(qpdfEntry).where(
						qpdfEntry.firstname.eq(firstname)
					    .and(qpdfEntry.lastname.eq(lastname)))
					 .listResults(qpdfEntry)
					 .getResults());
		log.info("==== There are : {} entries for firstname {} and lastname {} =====" , 
				new Object[] {result.size() , firstname, lastname});
		return result;
	}

	//---------------------------------------------------------------------------------------------------------
	@Override
	public ImmutableList<PDFEntry> getResultForUnknown(String lastOrFirstname) throws Exception {
		lastOrFirstname = lastOrFirstname.trim();
		/**
		 * What we get here is either the lastname of firstname.
		 * First thing to do is get all the entries that contain the "UNKNOWN" String in them
		 */
		
		LinkedList<PDFEntry> result = Lists.newLinkedList();
		
		JPAQuery query      = new JPAQuery(entityManager);
		QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
		
		List<PDFEntry> unknownEntries =  query.from(qpdfEntry)
		                                      .where(qpdfEntry.firstname.eq("UNKNOWN")
		                                    		  .or(qpdfEntry.lastname.eq("UNKNOWN")))
		                                      .listResults(qpdfEntry)
			                                  .getResults();
		
		for(PDFEntry unknownEntry : unknownEntries){
			if( unknownEntry.getFirstname().contains(lastOrFirstname) || 
			    unknownEntry.getLastname().contains(lastOrFirstname) ){
				result.add(unknownEntry);
			}
		}
		
		return ImmutableList.copyOf(result);
	}

	//---------------------------------------------------------------------------------------------------------
	@Override
	public int getNumberOfUnknownEntries() throws Exception {
		JPAQuery query = new JPAQuery(entityManager);
		QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
		return query.from(qpdfEntry)
		            .where(qpdfEntry.lastname.eq("UNKNOWN")
		            		.or(qpdfEntry.firstname.eq("UNKNOWN")))
		            .listResults(qpdfEntry)
		            .getResults().size();
	}

	//---------------------------------------------------------------------------------------------------------
	@Override
	public ImmutableList<PDFEntry> getResultsFromSearchLowerBounded(String firstname, String lastname,
			LocalDate lastSearchDate) throws Exception {
		JPAQuery query = new JPAQuery(entityManager);
		QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
		ImmutableList<PDFEntry> result = ImmutableList.copyOf(
				query.from(qpdfEntry).where(
						qpdfEntry.firstname.eq(firstname)
					    .and(qpdfEntry.lastname.eq(lastname))
					    .and(qpdfEntry.dosarDate.after(lastSearchDate)))
					 .listResults(qpdfEntry)
					 .getResults());
		return result;
	}

	
	//---------------------------------------------------------------------------------------------------------
	@Override
	public ImmutableList<PDFEntry> getResultForUnknowLowerBound(String lastORFirstname,
			LocalDate lastSearDate) throws Exception {
		lastORFirstname = lastORFirstname.trim();
		/**
		 * What we get here is either the lastname of firstname.
		 * First thing to do is get all the entries that contain the "UNKNOWN" String in them
		 */
		
		LinkedList<PDFEntry> result = Lists.newLinkedList();
		
		JPAQuery query      = new JPAQuery(entityManager);
		QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
		
		List<PDFEntry> unknownEntries =  query.from(qpdfEntry)
		                                      .where(qpdfEntry.firstname.eq("UNKNOWN")
		                                    		  .or(qpdfEntry.lastname.eq("UNKNOWN")))
		                                      .where(qpdfEntry.dosarDate.after(lastSearDate))
		                                      .listResults(qpdfEntry)
			                                  .getResults();
		
		for(PDFEntry unknownEntry : unknownEntries){
			if( unknownEntry.getFirstname().contains(lastORFirstname) || 
			    unknownEntry.getLastname().contains(lastORFirstname) ){
				result.add(unknownEntry);
			}
		}
		
		return ImmutableList.copyOf(result);
	}

	@Override
	public LocalDate getNewestDosarDateFromDB() throws Exception {
		JPAQuery query = new JPAQuery(entityManager);
		QPDFEntry qpdfEntry = QPDFEntry.pDFEntry;
		LocalDate newestDosarDate = query.from(qpdfEntry).uniqueResult(qpdfEntry.dosarDate.max());
		log.info("===== Newest dosarDate in the Db is : {}  =====", newestDosarDate.toString("yyyy-MMM-dd"));
		return newestDosarDate;
	}	
}

