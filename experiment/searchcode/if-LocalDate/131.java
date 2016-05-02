package com.eugen.database.service.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eugen.database.entities.FutureSearch;
import com.eugen.database.entities.QFutureSearch;
import com.eugen.database.repositories.FutureSearchRepository;
import com.eugen.database.service.FutureSearchService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.mysema.query.jpa.impl.JPAQuery;

@Service
@Transactional(readOnly=true)
class FutureSearchServiceImpl implements FutureSearchService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
		
	@PersistenceContext
	private EntityManager manager;
	
	@Autowired
	private FutureSearchRepository repository;
	
	//----------------------------------------------------------------------------------------------------------
	@Override
	public FutureSearch saveEntry(FutureSearch entry) {
		Preconditions.checkArgument(entry!=null, "Entry to save can not be null");
		return repository.saveAndFlush(entry);
	}

	//----------------------------------------------------------------------------------------------------------
	@Override
	public ImmutableList<FutureSearch> getFutureSearchesListWhereEntriesAreNotFound(LocalDate newestDosarDateInDB) {
		JPAQuery query = new JPAQuery(manager);
		QFutureSearch qFutureSearch = QFutureSearch.futureSearch;
		
		List<FutureSearch> futureSearches = 
				query.from(qFutureSearch)
		             .where(qFutureSearch.found.eq(false)
		            		 .and(qFutureSearch.lastSearchPerformedDate.before(newestDosarDateInDB)))
		             .listResults(qFutureSearch)
		             .getResults();
		
		ImmutableList<FutureSearch> result = ImmutableList.copyOf(futureSearches);
		return result;
	}

	//----------------------------------------------------------------------------------------------------------
	@Override
	public boolean isAnotherEmailAdditionPossible(String email, int numberOfEntriesAssosciatedWithOneEmail) {
		Preconditions.checkArgument(!Strings.nullToEmpty(email).trim().equals(""), "Email must be present");
		Preconditions.checkArgument(numberOfEntriesAssosciatedWithOneEmail > 0, 
				"numberOfEntriesAssosciatedWithOneEmail must be bigger then zero");
		
		JPAQuery query = new JPAQuery(manager);
		QFutureSearch qFutureSearch = QFutureSearch.futureSearch;
		long howMany = query.from(qFutureSearch)
				            .where(qFutureSearch.email.eq(email.trim()).and(qFutureSearch.found.eq(false)))
				            .count();
		return !(howMany >= numberOfEntriesAssosciatedWithOneEmail);
	}

	//----------------------------------------------------------------------------------------------------------
	@Override
	public void updateSuccessfulFutureSearchEntry(long id, LocalDate lastSearchDate) {
		Preconditions.checkArgument(id>0, "id must be present");
		FutureSearch futureSearch = repository.findOne(id);
		futureSearch.setFound(true);
		futureSearch.setLastSearchPerformedDate(lastSearchDate);
		//Because the save method is actually INSERT/UPDATE based on the presence of the id,
		//this will actually become UPDATE
		repository.saveAndFlush(futureSearch);
	}

	//----------------------------------------------------------------------------------------------------------
	@Override
	public boolean entryAlreadyExists(String email, String firstname, String lastname) {
		Preconditions.checkArgument(!Strings.nullToEmpty(email).trim().equals(""), "Email must be present");
		Preconditions.checkArgument(!Strings.nullToEmpty(firstname).trim().equals(""), "Firstname must be present");
		Preconditions.checkArgument(!Strings.nullToEmpty(lastname).trim().equals(""), "Lastname must be present");
		
		JPAQuery query = new JPAQuery(manager);
		QFutureSearch qFutureSearch = QFutureSearch.futureSearch;
		List<FutureSearch> result = 
				query.from(qFutureSearch)
				     .where(qFutureSearch.email.eq(email.trim())
				        .and(qFutureSearch.firstname.eq(firstname.trim()))
				        .and(qFutureSearch.lastname.eq(lastname.trim())))
				     .listResults(qFutureSearch)
				     .getResults();
		return result.size() > 0;
	}

	//----------------------------------------------------------------------------------------------------------
	@Override
	public ImmutableList<FutureSearch> getFutureValidSearchesForTheWeeklyEmailStatus() {		
		JPAQuery query = new JPAQuery(manager);
		QFutureSearch qFutureSearch = QFutureSearch.futureSearch;
		
		List<FutureSearch> allFutureSearches = 
				query.from(qFutureSearch)
				     .where(qFutureSearch.found.eq(false))
				     .listResults(qFutureSearch)
				     .getResults();
		
		return ImmutableList.copyOf(Collections2.filter(allFutureSearches, new LocalDatePredicate(LocalDate.now())));
	}
	
	//---------------------------------------------------------------------------------------------------------
	/**
	 * This class is used to filter the {@link FutureSearch} entities that are eligible
	 * for weekly status email(it compares if today % 7 == 0 with respect to registration date)
	 * @author eugenrabii
	 *
	 */
	private static final class LocalDatePredicate implements Predicate<FutureSearch> {
		
		private final LocalDate now;
		
		public LocalDatePredicate(LocalDate now) {
			this.now = now;
		}
		
		@Override
		public boolean apply(FutureSearch input) {
			//Users registered today, can not receive an email today also
			if(now.compareTo(input.getRegistrationDate()) == 0) return false;
			return (Days.daysBetween(now, input.getRegistrationDate()).getDays() % 7) == 0;
		}
	}

	//---------------------------------------------------------------------------------------------------------
	@Override
	public ImmutableList<FutureSearch> getFutureSearchesList() throws Exception {
		JPAQuery query = new JPAQuery(manager);
		QFutureSearch qFutureSearch = QFutureSearch.futureSearch;
		
		List<FutureSearch> futureSearches = 
				query.from(qFutureSearch)
		             .listResults(qFutureSearch)
		             .getResults();
		
		ImmutableList<FutureSearch> result = ImmutableList.copyOf(futureSearches);
		return result;
	}

	//---------------------------------------------------------------------------------------------------------------
	@Override
	public Optional<FutureSearch> isEntryFound(String firstname, String lastname, String email) throws Exception {
		log.info("===== Will look for firstname={}, lastname={}, email={} =====",
				new Object[]{firstname, lastname, email});
		JPAQuery query = new JPAQuery(manager);
		QFutureSearch qFutureSearch = QFutureSearch.futureSearch;
		
		FutureSearch result = query.from(qFutureSearch)
				                   .where(qFutureSearch.firstname.equalsIgnoreCase(firstname)
				                		   .and(qFutureSearch.lastname.equalsIgnoreCase(lastname))
				                		   .and(qFutureSearch.email.equalsIgnoreCase(email))
				                		   .and(qFutureSearch.found.eq(true))
				                   ).singleResult(qFutureSearch);	
		
		return Optional.fromNullable(result);
	}

	//---------------------------------------------------------------------------------------------------------------
	@Override
	public void updateFutureSearchFoundBackToFalse(long id) {
		Preconditions.checkArgument(id>0, "id must be present");
		FutureSearch futureSearch = repository.findOne(id);
		futureSearch.setFound(false);
		//Because the save method is actually INSERT/UPDATE based on the presence of the id,
		//this will actually become UPDATE
		repository.saveAndFlush(futureSearch);
	}

	//---------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isEntryGoingOnAValidLink(String firstname, String lastname,
			String email, LocalDate lastSearchDate) throws Exception {
		log.info("===== Will look for firstname={}, lastname={}, email={}, lastSearchDate={} =====",
				new Object[]{firstname, lastname, email, lastSearchDate.toString("yyyy-MMM-dd")});
		JPAQuery query = new JPAQuery(manager);
		QFutureSearch qFutureSearch = QFutureSearch.futureSearch;
		
		FutureSearch result = query.from(qFutureSearch)
				                   .where(qFutureSearch.firstname.equalsIgnoreCase(firstname)
				                		   .and(qFutureSearch.lastname.equalsIgnoreCase(lastname))
				                		   .and(qFutureSearch.email.equalsIgnoreCase(email))
				                		   .and(qFutureSearch.found.eq(true))
				                   ).singleResult(qFutureSearch);	
		/**
		 * I can't simply add .and(qFutureSearch.lastSearchPerformedDate.equals(lastSearchDate))
		 * because it will fail can not transform from Boolean to BooleanExpression..
		 */
		if(result.getLastSearchPerformedDate().equals(lastSearchDate)) return true;
		return false;
	}
}

