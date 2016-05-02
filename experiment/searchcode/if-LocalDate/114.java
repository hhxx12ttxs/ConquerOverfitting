package com.eugen.database.service;

import org.joda.time.LocalDate;

import com.eugen.database.entities.FutureSearch;
import com.eugen.database.entities.PDFEntry;
import com.google.common.collect.ImmutableList;

/**
 * Service Interface for the {@link PDFEntry} entity
 * @author eugenrabii
 *
 */
public interface PDFEntryService {
	/**
	 * 
	 * @param entries an {@link ImmutableList} of {@link PDFEntry} that needs to be saved to the DB
	 * @throws Exception
	 */
	public void savePDFEntries(ImmutableList<PDFEntry> entries) throws Exception;
	
	
	/**
	 * 
	 * @return {@link LocalDate} most recent dosar date from DB,
	 * newest date if you want.
	 * @param invalidate, 
	 * @throws Exception
	 */
	public LocalDate getNewestDosarDate() throws Exception;
	
	
	/**
	 * 
	 * @return {@link LocalDate} last recent dosar date from DB,
	 * oldest date if you want.
	 * @throws Exception
	 */
	public LocalDate getOldestDosarDate() throws Exception;
	
	
	/**
	 * 
	 * @return total Number of all {@link PDFEntry} from the DB
	 * @throws Exception
	 */
	public long getNumberOfPDFEntries() throws Exception;
	
	
	/**
	 * This method is used for an un-bounded search. It is used when daily searches 
	 * are performed on the {@link FutureSearch} entries
	 * 
	 * @param firstname
	 * @param lastname
	 * @return {@link ImmutableList} of {@link PDFEntry} that was retrieved from the DB with all params passed in
	 * empty if such an entry is not present. Notice that this method differes from the one above with the fact that it does 
	 * not have startDate and endDate.
	 * @throws Exception
	 */
	public ImmutableList<PDFEntry> getResultsFromSeachUnlimited(String firstname, String lastname) throws Exception;
	
	
	/**
	 * This method is here to return the values for the case when the {@link PDFEntry} is missing either the firstname 
	 * or the lastname. For example it is stored in PDF like this:
	 * 
	 * 57. RABII (57/2012)
	 * 
	 * These entries will be skipped and inserted in {@link PDFEntry} table manually.
	 * Instead of the part that is missing for example we insert the String "UNKNOWN".
	 * In the case above the firstname will be "UNKNOWN" and lastname will be "RABII"
	 * 
	 * To notice is that this method searches for SQL code with LIKE and not =.
	 * 
	 * @param lastOrFirstname
	 * @return
	 * @throws Exception
	 */
	public ImmutableList<PDFEntry> getResultForUnknown(String lastOrFirstname) throws Exception;
	
	/**
	 * 
	 * @return int number of entries that have either the firstname and lastname equal to "UNKNOWN"
	 */
	public int getNumberOfUnknownEntries() throws Exception;
	
	/**
	 * Search for {@link PDFEntry}s only that are newer then lastSearchDate
	 * This is needed for {@link FutureSearch}s when we do not what to search over entries 
	 * that the user was searched in when he/she initially looked for his/her firstname/lastname
	 * @param lastSearchDate
	 * @return
	 */
	public ImmutableList<PDFEntry> getResultsFromSearchLowerBounded(String firstname, String lastname,
			LocalDate lastSearchDate) throws Exception ;
	
	/**
	 * search with a lower bound in lastSearchDate
	 * @param lastORFirstname
	 * @param lastSearcDate
	 * @return
	 * @throws Exception
	 */
	public ImmutableList<PDFEntry> getResultForUnknowLowerBound(String lastORFirstname, LocalDate lastSearcDate) throws Exception;
	
	/**
	 * This method return the newestDosarDate that at the moment is stored in the DB
	 * as apposed to the one that is stored in the cache
	 * @return
	 * @throws Exception
	 */
	public LocalDate getNewestDosarDateFromDB() throws Exception;
}

