package niverapps.com.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringsTable {
	/**
	 * @author IAT
	 */
	private String strTable2D[][] = null;
	private ArrayList<String> listFields = null;
	private ArrayList<StringsRecord> listRecords = null;
	public String strFieldID = ""; //$NON-NLS-1$
	private String strFieldShown = "";	 //$NON-NLS-1$
	private List<String> listChanges = new ArrayList<String>(); 
	
	public int numFields = 0;
	public int numRecords = 0;
	private Messages messages = new Messages(Messages.SPANISH);
	
	private String strErrorLog = ""; //$NON-NLS-1$
	private boolean Valid = true; 

	public StringsTable (String strTable2D[][], String strFieldID, String strFieldShown, String strsRemovedColumns[]){
		
		// return on empty matrix or empty strFiledID
		if (strTable2D == null || strFieldID == null || strFieldID.length() == 0 || strFieldShown == null || strFieldShown.length() == 0) {
			Valid = false;			
			return;
		}

		this.strFieldID = strFieldID;
		this.strFieldShown = strFieldShown;
		
		cleanListChanges();
		initializeStringsTable(strTable2D, strFieldID, strFieldShown, strsRemovedColumns);
	}	

	public List<String> initializeStringsTable(String strTable2D[][], String strFieldID, String strFieldShown, String strsRemovedColumns[]) {
		
		// Remove unused Colummns  
		if (strsRemovedColumns != null && strsRemovedColumns.length > 0){
			for (String strRemovedColumn : strsRemovedColumns) {
				strTable2D = RemoveMatrixColumn(strTable2D,strRemovedColumn);
			}
		}
		
		String FirstRowMatrix[][] = new String[1][strTable2D[0].length];
		FirstRowMatrix[0] = strTable2D[0];
		setStringsTable(FirstRowMatrix);
		listChanges = appendRecords(strTable2D);
		
		return listChanges;
	}

	static public String[][] invertTableRows(String strTable2D[][]) {
		if (isValidMatrix(strTable2D)) {			
			String matrixReorderedValues[][] = new String[strTable2D.length][strTable2D[0].length];
			
			matrixReorderedValues[0] = strTable2D[0];
			for (int nRow = 1; nRow < strTable2D.length; nRow++) {
				matrixReorderedValues[matrixReorderedValues.length-nRow] = strTable2D[nRow];
			}		
			
			strTable2D = matrixReorderedValues;
		}
			
		return strTable2D;
	}
	
	static public boolean isValidMatrix(String strTable2D[][]) {
		boolean isValid = false;
		if (strTable2D != null && strTable2D.length > 1 && strTable2D[0].length > 0) {
			isValid = true;			
		}
		
		return isValid;
	}

	
	public String [][] mergeFieldsInStringsTable(String strFirstField, String strSeparator, String strSecondField, String strDestinationField) {
		// Nothing to do if fields not available
		if (!listFields.contains(strFirstField)) {
			appendToErrorLog(messages.getString("StringsTable.UnkownField")+strFirstField);			 //$NON-NLS-1$			
		}		
		if (!listFields.contains(strSecondField)) {
			appendToErrorLog(messages.getString("StringsTable.UnkownField")+strSecondField);			 //$NON-NLS-1$						
		}
		if (!listFields.contains(strDestinationField)) {
			appendToErrorLog(messages.getString("StringsTable.UnkownField")+strDestinationField);			 //$NON-NLS-1$						
		}		
		if (!listFields.contains(strFirstField) || !listFields.contains(strSecondField) || !listFields.contains(strDestinationField))
			return strTable2D;
		
		String strsFirst[] = getFieldColumn(strFirstField);
		String strsSecond[] = getFieldColumn(strSecondField);
		
		if (strsFirst.length != strsSecond.length) {
			appendToErrorLog("Different Columns size");
			return strTable2D;
		}
		
		// Merge Column Values
		String strsDestination[] = new String[numRecords];
		
		for (int nRecord = 0; nRecord < numRecords; nRecord++) {
			strsDestination[nRecord] = strsFirst[nRecord]+strSeparator+strsSecond[nRecord]; 
		}
		
		strTable2D = setFieldInStringsTable(strDestinationField, strsDestination);

		return strTable2D;
	}
	
	public String [][] setFieldInStringsTable(String strField, String strInitVal) {
		
		// Nothing to do if already defined or invalid initial string
		if (!listFields.contains(strField)) {
			appendToErrorLog(messages.getString("StringsTable.UnkownField")+strField);			 //$NON-NLS-1$
		}
		if (strInitVal == null) {
			appendToErrorLog(messages.getString("StringsTable.UnkownField")+strField);			 //$NON-NLS-1$
		}
		if (!listFields.contains(strField) || strInitVal == null)
			return strTable2D;
				
		numFields = listFields.size();
		numRecords = listRecords.size();
		
		String strsInitVal[] = createStringsArray(numRecords, strInitVal);
		strTable2D = setFieldInStringsTable(strField, strsInitVal);
		
		return strTable2D;
	}

	public String [][] setFieldInStringsTable(String strField, String strsColumnVals[]) {
		
		// Nothing to do if already defined or invalid initial string
		if (!listFields.contains(strField)) {
			appendToErrorLog(messages.getString("StringsTable.UnkownField")+strField);			 //$NON-NLS-1$
			return strTable2D;			
		}
		
		if (strsColumnVals == null || strsColumnVals.length == 0) {
			appendToErrorLog(messages.getString("StringsTable.InvalidColumnValues")+strField);			 //$NON-NLS-1$
			return strTable2D;
		}

		numFields = listFields.size();
		numRecords = listRecords.size();
		
		if (strsColumnVals.length != numRecords) {
			appendToErrorLog(messages.getString("StringsTable.InvalidLengthOfColumnValues")+Integer.toString(numRecords)+messages.getString("StringsTable.ValuesBut")+Integer.toString(strsColumnVals.length)+messages.getString("StringsTable.provided")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return strTable2D;
		}
		
		int indexField = getFieldIndex(strField);
		
		int nRow = 1;
		for (StringsRecord record : listRecords) {
			String strsVals[] = record.getVals();
			ArrayList<String> listVals = toArrayList(strsVals);
			listVals.set(indexField,strsColumnVals[nRow-1]);
			strTable2D[nRow] = toStringsArray(listVals);
			nRow++; 			
		}

		setStringsTable(strTable2D);
		
		return strTable2D;
	}
	
	public String [][] removeFieldFromStringsTable(String strField) {
		
		if (!strField.equalsIgnoreCase(strFieldID) && !strField.equalsIgnoreCase(strFieldShown)) {
			int indexRemovedRow = getFieldIndex(strField);		
			if (indexRemovedRow >= 0) {
				strTable2D = removeColumnFromStringsTable(strTable2D, indexRemovedRow);
				setStringsTable(strTable2D);
			}
		} else {
			if (strField.equalsIgnoreCase(strFieldID)) 
				appendToErrorLog(messages.getString("StringsTable.KeyColumnCannotBeRemoved")); //$NON-NLS-1$
			
			if (strField.equalsIgnoreCase(strFieldShown)) 
				appendToErrorLog(messages.getString("StringsTable.ShownColumnCannotBeRemoved")); //$NON-NLS-1$
			
		}			
		
		return strTable2D;
	}
	
	public String [][] createFieldInStringsTable(String strField, String strInitVal) {
		createFieldInStringsTable(strField);
		strTable2D = setFieldInStringsTable(strField, strInitVal);
		return strTable2D;
	}

	public String [][] createFieldInStringsTable(String strField) {

		// Nothing to do if already defined or invalid initial string
		if (listFields.contains(strField))
			return strTable2D;

		listFields.add(strField);
		numFields = listFields.size();
		numRecords = listRecords.size();
		
		strTable2D = new String[numRecords+1][numFields];
		strTable2D[0] = toStringsArray(listFields);
		
		int nRow = 1;
		for (StringsRecord record : listRecords) {
			String strsVals[] = record.getVals();
			ArrayList<String> listVals = toArrayList(strsVals);
			listVals.add(""); //$NON-NLS-1$
			strTable2D[nRow] = toStringsArray(listVals);
			nRow++; 			
		}

		setStringsTable(strTable2D);
		
		return strTable2D;
	}


	public List<String> appendRecords(String strNewTable2D[][]){

		// return on empty matrix or empty strFiledID
		if (strNewTable2D == null)
			return listChanges;

		// Get number of records
		numRecords = strNewTable2D.length - 1;
		String strAppendMessage = "";
		
		if (strNewTable2D.length > 0) {			
			// Get Fields
			numFields = strNewTable2D[0].length;
			ArrayList<String> NewFields = toArrayList(strNewTable2D[0]);

			// Check equivalent Fields in new table
			if (!NewFields.equals(listFields)) {
				appendToErrorLog(messages.getString("StringsTable.InvalidMerge")); //$NON-NLS-1$
				return listChanges;
			}

			if (numRecords > 0 && numFields > 0) {
				// Remove first row with field names
				strNewTable2D = removeRowFromStringsTable (strNewTable2D, 0);
				
				// Populate Records
				for (String strsVals[] : strNewTable2D) {
					if (strsVals == null || strsVals.length == 0)
						break;
					StringsRecord record = new StringsRecord(toStringsArray(listFields), strsVals, strFieldID, strFieldShown);							
					strAppendMessage = strAppendMessage + appendRecord(record);
				}			 
			}
		}		

		if (strAppendMessage.length() > 0) {
			updateStringsTable2D();
			checkUniqueIDs();			
		}
		
		return listChanges;
	}
	
	public void cleanListChanges() {
		listChanges = new ArrayList<String>();
	}

	
	public List<String> getListChanges() {
		return listChanges;
	}
	
	private String appendRecord(StringsRecord record) {
		String Message = ""; //$NON-NLS-1$
		
		// Checking invalid records before update
		if (record.strErrorLog.length() > 0) {
			appendToErrorLog(record.strErrorLog);
			return Message;			
		}
				
		int intIndex = getIndexForRecord(record);
		if (intIndex < 0) {
			Message = messages.getString("StringsTable.NewRecordAdded")+record.getVal(strFieldShown,"")+"\n";			 //$NON-NLS-1$ //$NON-NLS-2$
			listRecords.add(record);
		} else {			
			StringsRecord oldRecord = listRecords.get(intIndex);
			Message = oldRecord.set(record);
			listRecords.set(intIndex,record);
			if (Message != null && Message.length() > 0)
				Message = messages.getString("StringsTable.record")+record.getVal(strFieldShown,"")+messages.getString("StringsTable.HasBeenModified")+"\t\t"+Message;			
		}
		
		// Append non empty messages
		if (Message.length() > 0)
			listChanges.add(Message);			
		
		return Message;
	}
	
	public void setStringsTable(String strTable2D[][]){
		this.strTable2D = strTable2D;

		// return on empty matrix or empty strFiledID
		if (strTable2D == null)
			return;

		// Get number of records
		numRecords = strTable2D.length - 1;
		
		if (strTable2D.length > 0) {			
			// Get Fields
			numFields = strTable2D[0].length;
			listFields = toArrayList(strTable2D[0]);
			
			// Check that Fields contain strFieldID and strFieldShown
			if (!listFields.contains(strFieldID)) {
				appendToErrorLog(messages.getString("StringsTable.FieldIDIsNotAvailable")); //$NON-NLS-1$
				return;
			}
			if (!listFields.contains(strFieldShown)) {
				appendToErrorLog(messages.getString("StringsTable.FieldShownIsNotAvailable")); //$NON-NLS-1$
				return;
			}

			listRecords = new ArrayList<StringsRecord>();			
			if (numRecords > 0 && numFields > 0) {
				// Remove first row with field names
				strTable2D = removeRowFromStringsTable (strTable2D, 0);

				// Populate Records				
				for (String strsVals[] : strTable2D) {
					StringsRecord record = new StringsRecord(toStringsArray(listFields), strsVals, strFieldID, strFieldShown);
							
					if (record.strErrorLog.length() > 0)
						appendToErrorLog(record.strErrorLog);

					listRecords.add(record); 
				}			 
			}
		}
		
		checkUniqueIDs();
	}
	
	public String[][] updateStringsTable2D() {

		numFields = listFields.size();
		numRecords = listRecords.size();
		
		strTable2D = new String[numRecords+1][numFields];
		strTable2D[0] = toStringsArray(listFields);
		
		int nRow = 1;
		for (StringsRecord record : listRecords) {
			strTable2D[nRow] = record.getVals();
			nRow++; 			
		}
		

		return strTable2D;
	}
	
	public boolean checkUniqueIDs() {
		boolean isUnique = false; 
		
		String strsIDs[] = getKeyColumn();
		String strNonUnique = isUniqueArray(strsIDs);
		
		if (strNonUnique != null && strNonUnique.length() == 0)
			isUnique = true;
		
		if (!isUnique)
			appendToErrorLog(messages.getString("StringsTable.NonUniqueIDs")+strNonUnique);		 //$NON-NLS-1$
		
		return isUnique;
	}

	public int getIndexForRecord(StringsRecord record) {
		int intIndex = -1;
		
		String strID = record.getVal(strFieldID, messages.getString("StringsTable.Unknown"));		 //$NON-NLS-1$
		intIndex = getIndexForRecord(strID, false);
		
		return intIndex;
	}

	public int getIndexForRecord(String strID) {
		return getIndexForRecord(strID, true);
	}
	
	public int getIndexForRecord(String strID, boolean REPORT_NOT_FOUND) {
		int intIndex = -1;
		
		String strsKeyColumn[] = getKeyColumn();
		Integer[] intIndexes = findStringInStringsArray(strsKeyColumn, strID);
		
		// Check result
		if (intIndexes == null || intIndexes.length == 0) {
			if (REPORT_NOT_FOUND)
				appendToErrorLog(messages.getString("StringsTable.RecordNotFound")+strID); //$NON-NLS-1$
			return intIndex;
		}
		if (intIndexes.length > 1) {
			appendToErrorLog(messages.getString("StringsTable.NotUniqueRecordFound")+strID); //$NON-NLS-1$
			return intIndex;
		}
		
		if (intIndexes.length == 1) {
			intIndex = intIndexes[0];
		}
		
		return intIndex;
	}
	
	public String[] getKeyColumn() {
		String[] strsColumn = null;
		
		strsColumn = getFieldColumn(strFieldID);		
		
		return strsColumn;
	}	

	public String[] getFieldColumn(String strField) {
		String[] strsColumn = null;
		
		if (getFieldIndex(strField) < 0) {
			appendToErrorLog("Unknown Field in Strings Table: "+strField);
			return strsColumn;
		}
		
		numRecords = listRecords.size();
		if (numRecords > 0) {
			strsColumn = new String[numRecords];
			
			int nRecord = 0;
			for (StringsRecord record : listRecords) {
				strsColumn[nRecord] = record.getVal(strField,messages.getString("StringsTable.Unknown")+strField);			 //$NON-NLS-1$
				nRecord++;
			}			
		}
		
		return strsColumn;
	}	
	
	public int getFieldIndex(final String strField) {

		// Check valid parameter
		if (strField == null || strField.length() == 0 || listFields == null)
			return -1;
		
		int index = listFields.indexOf(strField);		
		
		return index;
	}
	
	public static String[][] RemoveMatrixColumn(String matrixValues[][], String strRemovedColumnName) {
		String newmatrixValues[][] = matrixValues;
		                         
		int numRows = matrixValues.length;
		int numCols = matrixValues[0].length;
		
		if (numRows > 0 && numCols > 0) {			
			int indexKeyColumn = GetIndexInStringsArray(matrixValues[0], strRemovedColumnName);

			if (indexKeyColumn >= 0) {				
				if (numCols > 1) { // Remove column
					newmatrixValues = new String[numRows][numCols-1];
					for (int nRow = 0; nRow < numRows; nRow++) {
						int nNewCol = 0;
						for (int nCol = 0; nCol < numCols; nCol++) {
							if (nCol != indexKeyColumn) {
								newmatrixValues[nRow][nNewCol] = matrixValues[nRow][nCol];	
								nNewCol++;
							}
						}						
					}		    		
				}			
			}			
		}
		
		return newmatrixValues;
	}
	
	
	public static int GetIndexInStringsArray(String[] Strings,String Key) {
		int indexKeyColumn = -1;
		
		int numCols = Strings.length;
		for (int nCol = 0; nCol < numCols; nCol++)  {
			if (Strings[nCol].equals(Key))
				indexKeyColumn = nCol;
		}		
		
		return indexKeyColumn;
	}
	

	// Errors management
	public boolean isValid() {
		return Valid;
	}	

	public String getErrorLog() {
		return strErrorLog;
	}	
	
	private void appendToErrorLog(String str) {
		Valid = false;
		strErrorLog = strErrorLog+str+"\n"; //$NON-NLS-1$
	}	

	public void cleanErrorLog() {
		Valid = true;
		strErrorLog = ""; //$NON-NLS-1$
	}	
	
	
	// -------------------------- Strings Record -------------------------------------------
	public class StringsRecord {
		private ArrayList<String> listFields = null;
		private ArrayList<String> listVals = null;
		private String strFieldID = "";	 //$NON-NLS-1$
		private String strFieldShown = "";	 //$NON-NLS-1$		
		public String strErrorLog = ""; //$NON-NLS-1$
		private boolean Valid = true; 
		
		public StringsRecord(String strsFields[], String strsVals[], String strFieldID, String strFieldShown) {

			assignParameters(strsFields, strsVals, strFieldID, strFieldShown);			
		}
		
		public String set(StringsRecord newRecord) {
			String Message = "";  //$NON-NLS-1$
			if (isequal(newRecord)) {
				return Message;
			}
			
			Message = setVals(newRecord.getFields(), newRecord.getVals());
			
			return Message;
		}		
		
		public boolean isequal(StringsRecord record) {
			boolean isEqual = true;

			if (listFields.equals(record.listFields))
				isEqual = false;
			if (listVals.equals(record.listVals))
				isEqual = false;
			if (strFieldID.equals(record.strFieldID))
				isEqual = false;
			if (strFieldShown.equals(record.strFieldShown))
				isEqual = false;
			
			return isEqual;			
		}
		
		public String setVals(final String strsFields[], String strsVals[]) {
			String strLog = ""; //$NON-NLS-1$
			
	        if (checkFieldsAndVals(strsFields, strsVals).length() > 0)
	        	return null;
			
	        String strField;
	        String strVal;
			for (int nField = 0; nField < strsFields.length; nField++) {
				strField = strsFields[nField];
				strVal = strsVals[nField];
				
				strLog = strLog+setVal(strField,strVal);
			}
			
			return strLog;
		}

		public String[] getVals() {
			String strsDefaults[] = toStringsArray(listVals);
			String strsVals[] = getVals(getFields(), strsDefaults);
			
			return strsVals;
		}
		
		public String[] getVals(final String strsFields[], String strsDefaults[]) {
			
	        if (checkFieldsAndVals(strsFields, strsDefaults).length() > 0)
	        	return null;
			
	        String strField;
	        String strDefault;
			for (int nField = 0; nField < strsFields.length; nField++) {
				strField = strsFields[nField];
				strDefault = strsDefaults[nField];
				
				strsDefaults[nField] = getVal(strField,strDefault);
			}
			
			return strsDefaults;
		}
		
		public String getVal(final String strField, final String strDefault) {
			String strVal = strDefault;
					
			int index = getFieldIndex(strField);		
			if (index >= 0) {
				strVal = listVals.get(index);
			}
			
			return strVal;
		}

		public String setVal(final String strField, final String strVal) {
			String strLog = "";  //$NON-NLS-1$
			int index = getFieldIndex(strField);		
			if (index >= 0) {
				String strOldVal = listVals.get(index);
				if (!strOldVal.equalsIgnoreCase(strVal)){
					strLog = "\t"+strField+messages.getString("StringsTable.ChangedFrom")+strOldVal+messages.getString("StringsTable.to")+strVal+"\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					listVals.set(index, strVal);
				}
			}
			
			return strLog;
		}
		
		public boolean isField(String strField) {
			int index = getFieldIndex(strField);
			
			return index >= 0;
		}
		
		public String[] getFields() {
			return toStringsArray(this.listFields);
		}
		
		private void assignParameters(String strsFields[], String strsVals[], String strFieldID, String strFieldShown) {        
	        this.strFieldID = strFieldID;		
	        this.strFieldShown = strFieldShown;		
	        
	        if (checkFieldsAndVals(strsFields, strsVals).length() > 0)
	        	return;

	        listFields = toUniqueArrayList(strsFields);
	        listVals = toArrayList(strsVals);
	        
	        if (listFields.size() != listVals.size()) {
	        	appendToErrorLog(messages.getString("StringsTable.DifferentSize")+Integer.toString(listFields.size())+messages.getString("StringsTable.FieldsAnd")+Integer.toString(listVals.size())+messages.getString("StringsTable.Values"));        	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        }
	        
	        if (!listFields.contains(this.strFieldID)) {
	        	appendToErrorLog(strFieldID+messages.getString("StringsTable.NotFoundInFields")+toStrcat(listFields));        	 //$NON-NLS-1$
	        }

	        if (!listFields.contains(this.strFieldShown)) {
	        	appendToErrorLog(strFieldShown+messages.getString("StringsTable.NotFoundInFields")+toStrcat(listFields));        	 //$NON-NLS-1$
	        }
	        
	        return;		
		}
		
		private String checkFieldsAndVals(String strsFields[], String strsVals[]) {
			boolean areInvalidParameters = false;
			String strLog = ""; //$NON-NLS-1$
			
	        if (strsFields == null || strsFields.length == 0) {
	        	areInvalidParameters = true;
	        	strLog = strLog+messages.getString("StringsTable.EmptyFields"); //$NON-NLS-1$
	        }
	        	 
				
	        if (strsVals == null || strsVals.length == 0) {
	        	areInvalidParameters = true;
	        	strLog = strLog+messages.getString("StringsTable.EmptyValues"); //$NON-NLS-1$
	        }
	        
	        if (areInvalidParameters)
	        	return strLog; 
	        
	        if ( strsFields.length != strsVals.length) {
	        	strLog = strLog+messages.getString("StringsTable.DifferentSize")+Integer.toString(listFields.size())+messages.getString("StringsTable.FieldsAnd")+Integer.toString(listVals.size())+messages.getString("StringsTable.Values"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        	areInvalidParameters = true;
	        }
	        
	        return strLog;
		}

//		public int getIndexFieldID() {
//			int index = -1;
//			
//			for (String strField : listFields) {
//				
//			}
//			return index;
	//
//		}
				
		public boolean isValid() {
			
			return Valid;
		}	
	}
	
	// ****************************************************************
	public static String [][] removeColumnFromStringsTable (String strsTable[][], int indexRemovedCol) {
		
		if (strsTable != null && strsTable.length > 0 && strsTable[0].length > 0 && indexRemovedCol >= 0 && indexRemovedCol < strsTable[0].length) {
			strsTable = transposeStringsTable(strsTable);
			
			int numRows = strsTable[0].length;
			ArrayList<String []> list = new ArrayList<String []>(Arrays.asList(strsTable));
			list.remove(indexRemovedCol);
			
			String [][] strsNewTable = new String[list.size()][numRows];
			strsTable = list.toArray(strsNewTable);
			
			strsTable = transposeStringsTable(strsTable);			
		}
		
		return strsTable;
	}
	
	private static String [][] removeRowFromStringsTable (String strsTable[][], int indexRemovedRow) {
		
		if (strsTable != null && strsTable.length > 0 && strsTable[0].length > 0 && indexRemovedRow >= 0 && indexRemovedRow < strsTable.length) {
			int numCols = strsTable[0].length;
			ArrayList<String []> list = new ArrayList<String []>(Arrays.asList(strsTable));
			list.remove(indexRemovedRow);
			
			String [][] strsNewTable = new String[list.size()][numCols];
			strsTable = list.toArray(strsNewTable);		
		}
		
		return strsTable;
	}
	
	private static String [][] transposeStringsTable(String [][] m){
		int r = m.length;
		int c = m[0].length;
		String [][] t = new String[c][r];
		for(int i = 0; i < r; ++i){
			for(int j = 0; j < c; ++j){
				t[j][i] = m[i][j];
			}
		}
		return t;
	}
	
	
	
	public String[] createStringsArray(int numStrings, String strInitVal) {
		String strsStringsArray[] = null;
		
		if (numStrings > 0 && strInitVal != null) {
			strsStringsArray = new String[numStrings];
			
			for (int nString = 0; nString < numStrings; nString++) {
				strsStringsArray[nString] = strInitVal;				
			}
		}
			
		return strsStringsArray;
	}	
		
	private static ArrayList<String> toArrayList(String strs[]) {
		 ArrayList<String> strlist = new ArrayList<String>();

		 if (strs != null && strs.length > 0) {
			 for (String str : strs) {
				 strlist.add(str);
			 }			 
		 }

		 return strlist;
	}

	private static String isUniqueArray(String strs[]) {
		String strcatNonUniques = ""; //$NON-NLS-1$
		 
		 ArrayList<String> strlist = new ArrayList<String>();
		 if (strs != null && strs.length > 0) {
			 for (String str : strs) {
				 if (!strlist.contains(str)) {
					 strlist.add(str);					 
				 }
				 else {
					 strcatNonUniques = strcatNonUniques+";"+str; //$NON-NLS-1$
				 }
			 }			 
			 
			 if (strcatNonUniques.length() > 1)
				 strcatNonUniques = strcatNonUniques.substring(1);
		 }

		 return strcatNonUniques;
	}	
	
	private static ArrayList<String> toUniqueArrayList(String strs[]) {
		 ArrayList<String> strlist = new ArrayList<String>();

		 if (strs != null && strs.length > 0) {
			 for (String str : strs) {
				 if (!strlist.contains(str))
					 strlist.add(str);
			 }			 
		 }

		 return strlist;
	}

	private static String toStrcat(ArrayList<String> strlist) {
		String strs[] = toStringsArray(strlist);
		return toStrcat(strs);
	}
	
	private static String [] toStringsArray(ArrayList<String> strlist) {
	    String[] strs = new String[strlist.size()];
	    strs = strlist.toArray(strs);
	    
	    return strs;
	}
	
	private static String toStrcat(String strs[]) {
		 String strcat = ""; //$NON-NLS-1$

		 if (strs != null && strs.length > 0) {
			 for (String str : strs) {
				 strcat = strcat+";"+str; //$NON-NLS-1$
			 }			
			 
			 strcat = strcat.substring(1);
		 }

		 return strcat;
	}
	
	public static Integer[] findStringInStringsArray(final String strsVals[], final String strID) {
		Integer intIndexes[] = null;

		// Check valid parameter
		if (strsVals != null && strsVals.length != 0 && strID != null && strID.length() != 0) {
			ArrayList<Integer> intlist = new ArrayList<Integer>();
			
			for (Integer nVal = 0; nVal < strsVals.length; nVal++) {
				String strVal = strsVals[nVal];
				if (strVal.equalsIgnoreCase(strID)) {
					intlist.add(nVal);
				}
			}			
			
		    intIndexes = new Integer[intlist.size()];
		    intIndexes = intlist.toArray(intIndexes);
		}
			
		return intIndexes;
	}
}

