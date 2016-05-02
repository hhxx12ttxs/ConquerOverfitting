package com.eugen.database.service.impl.test;

import junit.framework.Assert;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.eugen.database.entities.FutureSearch;
import com.eugen.database.repositories.FutureSearchRepository;
import com.eugen.database.service.FutureSearchService;
import com.google.common.collect.ImmutableList;

/**
 * A pretty big class that does some integration tests on the {@link FutureSearchService} methods
 * I could refactor it/document, etc, but since these are only tests..
 * @author eugenrabii
 *
 */

@DirtiesContext
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(locations={"classpath:/applicationContext-test.xml"})
public class FutureSearchServiceImplTest {
	
	@Autowired
	FutureSearchService futureSearchService;
	
	@Autowired
	FutureSearchRepository futureSearchRepository;
	
	private EmbeddedDatabase database;
	
	@Before
	public void setUp(){
		database = new EmbeddedDatabaseBuilder().build();
		Assert.assertNotNull(database); 
	}
	
	@After
	public void tearDown(){
		database.shutdown();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSaveEntryException() throws Exception {
		FutureSearch futureSearch=null;
		futureSearchService.saveEntry(futureSearch);
	}
	
	@Test
	public void testSaveEntry() throws Exception {
		FutureSearch futureSearch = new FutureSearch();
		futureSearch.setEmail("john.smith@company.com");
		futureSearch.setFirstname("John");
		futureSearch.setFound(false);
		futureSearch.setLanguage("en");
		
		FutureSearch returnedFutureSearch = futureSearchService.saveEntry(futureSearch);
		Assert.assertEquals("john.smith@company.com", returnedFutureSearch.getEmail());
		Assert.assertEquals("John", returnedFutureSearch.getFirstname());
		Assert.assertEquals(false, returnedFutureSearch.isFound());
		Assert.assertEquals("en", returnedFutureSearch.getLanguage());
	}
	
	@Test
	public void testGetFutureSearchesList() throws Exception {
		FutureSearch futureSearch1 = new FutureSearch();
		futureSearch1.setFound(false); futureSearch1.setEmail("firstEmail@mail.com");
		futureSearch1.setLastSearchPerformedDate(LocalDate.now());
		
		//This entry will not be retrieved
		FutureSearch futureSearch2 = new FutureSearch();
		futureSearch2.setFound(true); futureSearch2.setEmail("secondEmail@mail.com");
		futureSearch2.setLastSearchPerformedDate(LocalDate.now());
		
		FutureSearch futureSearch3 = new FutureSearch();
		futureSearch3.setFound(false); futureSearch3.setEmail("thirdEmail@mail.com");
		futureSearch3.setLastSearchPerformedDate(LocalDate.now().plus(Days.FIVE));
		
		futureSearchService.saveEntry(futureSearch1);
		futureSearchService.saveEntry(futureSearch2);
		futureSearchService.saveEntry(futureSearch3);
		
		ImmutableList<FutureSearch> resultFromDBNotFound = 
				futureSearchService.getFutureSearchesListWhereEntriesAreNotFound(LocalDate.now().plus(Days.FIVE));
		ImmutableList<FutureSearch> allResultsFromDB     = futureSearchService.getFutureSearchesList();
		
		Assert.assertEquals(1, resultFromDBNotFound.size());
		Assert.assertEquals(3, allResultsFromDB.size());
		
		//Check if the two email concatenated are actually the ones that have their entries (found field) set in false
		String concatenatedEmailFromDB = resultFromDBNotFound.get(0).getEmail();
		if( !(("firstEmail@mail.com".equals(concatenatedEmailFromDB)) || 
		    ("thirdEmail@mail.com".equals(concatenatedEmailFromDB))) ) {
			Assert.fail("Un-correct values were taken from the DB!");
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateEmailWithAtMostNEntriesNull() throws Exception {
		futureSearchService.isAnotherEmailAdditionPossible(null, 0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateEmailWithAtMostNEntriesEmpty() throws Exception {
		futureSearchService.isAnotherEmailAdditionPossible("", 0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateEmailWithAtMostNEntriesEmptyNotTimmed() throws Exception {
		futureSearchService.isAnotherEmailAdditionPossible("   ", 0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateEmailWithAtMostNEntriesZeroEmails() throws Exception {
		futureSearchService.isAnotherEmailAdditionPossible("1", 0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testValidateEmailWithAtMostNEntriesMinusEmails() throws Exception {
		futureSearchService.isAnotherEmailAdditionPossible("1", -3);
	}
	
	@Test
	public void testValidateEmailWith2Entries() throws Exception {
		FutureSearch futureSearch1 = new FutureSearch();
		futureSearch1.setEmail("eugen.rabii@gmail.com"); futureSearch1.setFirstname("Eugene");
		
		FutureSearch futureSearch2 = new FutureSearch();
		futureSearch2.setEmail("eugen.rabii@gmail.com"); futureSearch2.setFirstname("Valentina");
		
		futureSearchService.saveEntry(futureSearch1); futureSearchService.saveEntry(futureSearch2);
				
		Assert.assertEquals(true, futureSearchService.isAnotherEmailAdditionPossible("eugen.rabii@gmail.com", 3));
		Assert.assertEquals(false, futureSearchService.isAnotherEmailAdditionPossible("eugen.rabii@gmail.com", 2));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testUpdateSuccessfulFutureSearchEntryIdLessThenZero() throws Exception {
		futureSearchService.updateSuccessfulFutureSearchEntry(-3, LocalDate.now());
	}
	
	//Small hack is present in this test. For whatever reason, when I do setId on the FutureSearch, hsqldb ignores it
	//and sets it to whatever values it wants. Thus I get the ID from the Firstname actually.
	//This is irrelevant, because I never set the id manually it real code.
	@Test
	public void testUpdateSuccessfulFutureSearchEntry() throws Exception {
		FutureSearch futureSearch = new FutureSearch();
		futureSearch.setFound(false);
		futureSearch.setFirstname("Eugene5773");
		futureSearch.setRegistrationDate(LocalDate.now().plus(Days.SEVEN));
		
		futureSearchService.saveEntry(futureSearch);
		
		//We do not really use this method, it's for testing purposes here only
		ImmutableList<FutureSearch> futureSearchs = futureSearchService.getFutureValidSearchesForTheWeeklyEmailStatus();
		
		long idInteredIn = 0l;
		for(FutureSearch innerFutureSearch : futureSearchs){
			if("Eugene5773".equals(innerFutureSearch.getFirstname())) {
				idInteredIn = innerFutureSearch.getId();
				break;
			}
		}
		
		futureSearchService.updateSuccessfulFutureSearchEntry(idInteredIn, LocalDate.now());
		
		FutureSearch updatedFutureSearch = futureSearchRepository.findOne(idInteredIn);
		Assert.assertEquals(true, updatedFutureSearch.isFound());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEntryAlreadyExistsNullEmail() throws Exception {
		futureSearchService.entryAlreadyExists(null, "first", "last");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEntryAlreadyExistsEmptyEmail() throws Exception {
		futureSearchService.entryAlreadyExists("  ", "first", "last");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEntryAlreadyExistsNullFirstname() throws Exception {
		futureSearchService.entryAlreadyExists("email", null, "last");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEntryAlreadyExistsEmptyFirstname() throws Exception {
		futureSearchService.entryAlreadyExists("email", "  ", "last");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEntryAlreadyExistsNullLastname() throws Exception {
		futureSearchService.entryAlreadyExists("email", " first ", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testEntryAlreadyExistsEmptyLastname() throws Exception {
		futureSearchService.entryAlreadyExists("email", " first ", "  ");
	}
	
	@Test
	public void testEntryAlreadyExistsTrueAndTrim() throws Exception {
		FutureSearch futureSearch = new FutureSearch();
		futureSearch.setEmail("eugen.rabii@gmail.com");
		futureSearch.setFirstname("eugene");
		futureSearch.setLastname("rabii");
		futureSearchService.saveEntry(futureSearch);
		
		Assert.assertEquals(true, futureSearchService.entryAlreadyExists("eugen.rabii@gmail.com", " eugene ", "   rabii "));
	}
	
	@Test
	public void testEntryAlreadyExistsAlmostTrueAndTrim() throws Exception {
		FutureSearch futureSearch = new FutureSearch();
		futureSearch.setEmail("eugen.rabii@gmail.com");
		//Notice the 'e' is missing
		futureSearch.setFirstname("eugen");
		futureSearch.setLastname("rabii");
		futureSearchService.saveEntry(futureSearch);
		
		Assert.assertEquals(false, futureSearchService.entryAlreadyExists("eugen.rabii@gmail.com", " eugene ", "   rabii "));
	}
	
	@Test
	public void testEntryAlreadyExistsFalse() throws Exception {
		FutureSearch futureSearch = new FutureSearch();
		futureSearch.setEmail("eugen.rabii@gmail.com");
		futureSearch.setFirstname("eugene");
		futureSearch.setLastname("rabii");
		futureSearchService.saveEntry(futureSearch);
		
		Assert.assertEquals(false, futureSearchService.entryAlreadyExists("eugen.rabii@gmail.com", "john", "smith"));
	}
	
	//will return an empty list, since there are no entries like this
	@Test
	public void tesGetFutureValidSearchesForTheWeeklyEmailStatus() throws Exception {
		LocalDate now = LocalDate.now();
		LocalDate invalidDate1 = now.plus(Days.FIVE);
		LocalDate invalidDate2 = now.plus(Days.SIX);
		
		FutureSearch invalid1 = new FutureSearch();
		invalid1.setRegistrationDate(invalidDate1); invalid1.setFound(false);
		
		FutureSearch invalid2 = new FutureSearch();
		invalid2.setRegistrationDate(invalidDate2); invalid2.setFound(false);
		
		futureSearchService.saveEntry(invalid1);
		futureSearchService.saveEntry(invalid2);
				
		Assert.assertEquals(0, futureSearchService.getFutureValidSearchesForTheWeeklyEmailStatus().size());
	}
	
	//Will return one, users registered today can't not receive an email today also. For example:
	//suppose that a user registered on the 14.09.2012, because 14 % 7 == 0, it means that it could get am email
	//today also, which is invalid
	@Test
	public void testGetFutureValidSearchesForTheWeeklyEmailStatus2() throws Exception {
		LocalDate now = LocalDate.now();		
		LocalDate nowPlusFive = now.plus(Days.FIVE);
		LocalDate nowPlusSeven = now.plus(Days.SEVEN);
		
		FutureSearch futureSearchNow = new FutureSearch();
		futureSearchNow.setRegistrationDate(now); futureSearchNow.setFound(false);
		
		
		FutureSearch futureSearchPlusFive = new FutureSearch();
		futureSearchPlusFive.setRegistrationDate(nowPlusFive); futureSearchPlusFive.setFound(false);
		
		FutureSearch futureSearchPlusSeven = new FutureSearch();
		futureSearchPlusSeven.setRegistrationDate(nowPlusSeven); futureSearchPlusSeven.setFound(false);
		futureSearchPlusSeven.setEmail("eugen.rabii@gmail.com");
		
		futureSearchService.saveEntry(futureSearchNow); 
		futureSearchService.saveEntry(futureSearchPlusFive);
		futureSearchService.saveEntry(futureSearchPlusSeven);
				
		Assert.assertEquals(1,futureSearchService.getFutureValidSearchesForTheWeeklyEmailStatus().size());
		Assert.assertEquals("eugen.rabii@gmail.com", futureSearchService.getFutureValidSearchesForTheWeeklyEmailStatus().get(0).getEmail());
		
	}
	
	//We should return only two entries here, the plusSeven and plus14
	@Test
	public void testGetFutureValidSearchesForTheWeeklyEmailStatus3() throws Exception {
		LocalDate now = LocalDate.now();
		LocalDate nowPlusOne = now.plus(Days.ONE);
		LocalDate nowPlusSeven = now.plus(Days.SEVEN);
		LocalDate nowPlus14 = now.plus(Days.SEVEN).plus(Days.SEVEN);
		
		FutureSearch futureSearchNow = new FutureSearch();
		futureSearchNow.setFound(false); futureSearchNow.setEmail("now"); 
		futureSearchNow.setRegistrationDate(now);
		
		FutureSearch futureSearchPlusOne = new FutureSearch();
		futureSearchPlusOne.setFound(false); futureSearchNow.setEmail("plusOne"); 
		futureSearchPlusOne.setRegistrationDate(nowPlusOne);
		
		FutureSearch futureSearchPlusSeven = new FutureSearch(); 
		futureSearchPlusSeven.setFound(false); futureSearchPlusSeven.setEmail("plusSeven");
		futureSearchPlusSeven.setRegistrationDate(nowPlusSeven);
		
		FutureSearch futureSearchPlus14 = new FutureSearch();
		futureSearchPlus14.setFound(false); futureSearchPlus14.setEmail("plus14");
		futureSearchPlus14.setRegistrationDate(nowPlus14);
		
		futureSearchService.saveEntry(futureSearchNow); futureSearchService.saveEntry(futureSearchPlusOne);
		futureSearchService.saveEntry(futureSearchPlusSeven); futureSearchService.saveEntry(futureSearchPlus14);
		
		Assert.assertEquals(2, futureSearchService.getFutureValidSearchesForTheWeeklyEmailStatus().size());
		Assert.assertEquals("plusSeven", futureSearchService.getFutureValidSearchesForTheWeeklyEmailStatus().get(0).getEmail());
		Assert.assertEquals("plus14", futureSearchService.getFutureValidSearchesForTheWeeklyEmailStatus().get(1).getEmail());
	}
	
	/**
	 * Test that some user that want to be re-searched, really is set as one
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {
		FutureSearch futureSearch = new FutureSearch();
		futureSearch.setEmail("eugen.rabii@gmail.com");
		futureSearch.setFirstname("Eugene");
		futureSearch.setLastname("Rabii");
		futureSearch.setFound(true);
		futureSearch.setLanguage("en");
		futureSearch.setLastSearchPerformedDate(LocalDate.now());
		
		futureSearchService.saveEntry(futureSearch);
		FutureSearch inDB = futureSearchService.isEntryFound("Eugene", "Rabii", "eugen.rabii@gmail.com").get();
		futureSearchService.updateFutureSearchFoundBackToFalse(inDB.getId());
		
		FutureSearch afterUpdate = futureSearchService.getFutureSearchesList().get(0);
		
		Assert.assertEquals(false, afterUpdate.isFound());
		Assert.assertEquals(LocalDate.now(), afterUpdate.getLastSearchPerformedDate());
	}
	
	@Test
	public void test1() throws Exception {
		FutureSearch futureSearch = new FutureSearch();
		futureSearch.setEmail("eugen.rabii@gmail.com");
		futureSearch.setFirstname("Eugene");
		futureSearch.setLastname("Rabii");
		futureSearch.setFound(true);
		futureSearch.setLanguage("en");
		futureSearch.setLastSearchPerformedDate(LocalDate.now());
		
		futureSearchService.saveEntry(futureSearch);
		
		boolean result = 
				futureSearchService.isEntryGoingOnAValidLink("Eugene", "Rabii", "eugen.rabii@gmail.com", LocalDate.now());
		Assert.assertEquals(result, true);
	}
}

