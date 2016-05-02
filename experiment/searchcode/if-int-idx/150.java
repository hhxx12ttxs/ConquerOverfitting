package nl.hanze.db.io;
import nl.hanze.db.def.*;

import java.util.*;
import java.io.*;

/**
 * format of idx-record = #idx-record-nr#delete-flag#column-value#tbl-record-nr#null or nr-of-next-idx-record-in-list
 * delete-flag = 'D' or ' '; 'D' means this value is deleted; if an idx-record is deleted, it can later be reused
 *   examples :
 *   "900000  # #10000     #50000   #null    "
 *   "900000  # #10000     #50000   #null    "
 *   "900000  #D#10000     #50000   #null    "
 *   "900000  # #10000     #50000   #99999   "
 * note on LF : on linux PrintWriter.println appends LF=0x0a="\n", on windows println appends LF=0x0d0a="\r\n"
 * here alsways "\r\n" is used, since it works on linux as well (same as in TableDataIO.java)
 * so not using System.getProperty("line.separator")
 *
 * record_size = 8+3+column-size+1+8+1+8+2 (fixed part = 31)
 */

public class TableDataIO_Indexed extends TableDataIO {
	
    // size must be < 10.000.000
    private final int hashsize = 100;

    private final int record_size_fixed_part = 22;

    private class Positions {
	public int index;
	public int table;
	public Positions () {
	    index = 0;
	    table = 0;
	}
    }

    public TableDataIO_Indexed(TableDefinition def) throws Exception {
	super(def);
    }

    public int idxFixedRecordLength () {
	return this.record_size_fixed_part; 
    }

    @Override
	public long add(String[] record) throws Exception {
		long s = System.currentTimeMillis();
		// zelf implementeren
		long e = System.currentTimeMillis();
		return e - s;
	}
	
	@Override
	public long search(String colname, String value, ArrayList<String[]> result) {
		long s = System.currentTimeMillis();
		// zelf implementeren
		long e = System.currentTimeMillis();
		return e - s;
	}

	@Override
	public long update(String[] record) {
		long s = System.currentTimeMillis();
		// zelf implementeren
		long e = System.currentTimeMillis();
		return e - s;
	}

	@Override
	public long delete(String colname, String value) {
		long s = System.currentTimeMillis();
		// zelf implementeren
		long e = System.currentTimeMillis();
		return e - s;
	}

    /**
     * Create an index file for the specified column.
     * @param colname the column's name
     * @return the time it took to run this method.
     * @throws Exception
     */
    public long createIndexFile() throws Exception {
	long s = System.currentTimeMillis();
 	// fill idx file with hashsize empty records/lines (empty means filled with spaces)
 	PrintWriter pw = new PrintWriter(BaseIO.getInitDir() + File.separator + def.getTableName() + "_" + def.getPK() + ".idx");
 	int pkindex = def.getColPosition(def.getPK());
 	if (pkindex == -1) {
	    throw new Exception("Primary key does not exist");
 	}
 	Integer[] size = def.getSizes();
 	int recordSize = idxFixedRecordLength() + size[pkindex];

 	StringBuffer temp = new StringBuffer();
 	for (int i = 0; i < recordSize-2; i++) {
 	    temp.append(" ");
 	}

 	for(int i = 0;i < this.hashsize; i++) {
 	    pw.print(temp + "\r\n");
 	}
 	pw.close();

 	int table_pos = 0;
 	// create an index entry for each row present in the tbl-file (for column=colname)
 	BufferedReader buf = new BufferedReader(new FileReader(BaseIO.getInitDir() + File.separator + def.getTableName() + ".tbl")); 
 	String sLine = null;
 	while((sLine = buf.readLine()) != null) {
 	    // get column value (don't strip the spaces)
 	    String pkvalue = sLine.split("#")[pkindex];
 	    addIndexEntry(table_pos, pkvalue);
 	    table_pos++;
 	}
 	buf.close();
	long e = System.currentTimeMillis();
	return e-s;
    }
	
    /**
     * Obtain the index entry of the specified column.
     * @param column the column's name
     * @param value the value to obtain the index entry for
     * @throws Exception
     */
    private int hash(String key) throws Exception 
    {
	// return record number through hashing the column value
	// method is based on a simple hash function from Robert Sedgwicks book "Algorithms in C"
	// URL: http://www.partow.net/programming/hashfunctions/index.html
	int b = 378551;
	int a = 63689;
	long hash = 0;
	
	for(int i = 0; i < key.length(); i++) {
	    hash = hash * a + key.charAt(i);
	    a = a * b;
	};
	hash = hash % this.hashsize;
	// longs and ints are allways considered signed in Java, we want non-negative values only
	if (hash < 0) {
	    hash = -1*hash;
	}
	return (int)hash;
    }

    /**
     * Create an index entry in the idx file of the specified column.
     * @param tablePos position or line nr in table *.tbl
     * @param pkvalue the value of the primary key to create an index entry for
     * @throws Exception
     */
    protected void addIndexEntry(int tablePos, String pkvalue) throws Exception {
	System.out.println("" + tablePos + " " + pkvalue);
	if (!indexExists(def.getPK())) {
	    throw new Exception("No index created");
	}
	int pkindex = def.getColPosition(def.getPK());
	if (pkindex == -1) {
	    throw new Exception("Primary key does not exist");
	}
	Integer[] size = def.getSizes();
	if (pkvalue.length() > TableDefinition.INTEGER_SIZE) {
	    throw new Exception("supplied pkvalue too large");
	} else {
	    // make sure key value has appropriate length
	    StringBuffer temp = new StringBuffer();
	    temp.append(pkvalue);
	    for(int i = 0; i < TableDefinition.INTEGER_SIZE-pkvalue.length(); i++) {
		temp.append(' ');
	    }
	    pkvalue=temp.toString();
	}

	// calculate index = hash value
	String s_value = pkvalue.trim();
	int idxPos = hash(s_value);
	int recordSize = idxFixedRecordLength() + size[pkindex];
	
	String filename = BaseIO.getInitDir() + File.separator + def.getTableName() + "_" + def.getPK() + ".idx";
	File file=new File(filename);
	if (!file.exists()) {
	    throw new Exception("Index file does not exists, must be created before calling addIndexEntry()");
	};
	RandomAccessFile raf = new RandomAccessFile(BaseIO.getInitDir() + File.separator + def.getTableName() + "_" + def.getPK() + ".idx", "rw");
	raf.seek(idxPos*recordSize);
	String sLine = raf.readLine();
	System.out.println(sLine);
	if (sLine.substring(0, 1).equals(" ")) {
	    // empty record, reset file pointer and fill record
	    raf.seek(idxPos*recordSize);
 	    String temp1 = Integer.toString(idxPos);
 	    temp1 = String.format("%-5s", temp1);
 	    String temp2 = Integer.toString(tablePos);
 	    temp2 = String.format("%-5s", temp2);
 	    String rec = temp1+"# #"+pkvalue+"#"+temp2+"#"+"null "+"\r\n";
 	    // write record to idx-file	
 	    raf.write(rec.toString().getBytes());
 	} else {
 	    String[] parts = sLine.split("#");
 	    if (parts[1].equals("D")) {
 		// deleted record, reset file pointer, fill record but keep previous link !
		raf.seek(idxPos*recordSize);
		String temp1 = Integer.toString(idxPos);
		temp1 = String.format("%-5s", temp1);
		String temp2 = Integer.toString(tablePos);
		temp2 = String.format("%-5s", temp2);
 		String rec = temp1+"# #"+pkvalue+"#"+temp2;
 		// write record to idx-file	
 		raf.write(rec.toString().getBytes());
 	    } else {
 		// collision found ! a valid record is found, so add new record at eof
 		// calc new record number
 		int newidxPos = (int)(raf.length()/recordSize);

 		// reset file pointer and update the current record
 		raf.seek((idxPos*recordSize) + (recordSize-2-5));
		String temp1 = Integer.toString(newidxPos);
		temp1 = String.format("%-5s", temp1);
 		raf.write(temp1.getBytes());

 		// move file pointer to eof and append new record
 		raf.seek(raf.length());
 		// fill record and append to file
		String temp2 = Integer.toString(tablePos);
		temp2 = String.format("%-5s", temp2);
		String rec = temp1+"# #"+pkvalue+"#"+temp2+"#"+"null "+"\r\n";
		// write record to idx-file	
		raf.write(rec.toString().getBytes());
 	    }
 	}
	//raf.close();
    }

    /**
     * Search index file for a primary key value
     * @param pkvalue the primary key value to search for
     * @param position [0] = index entry, [1] = table row
     * @throws Exception
     */
    public boolean searchIndex(String pkvalue, Positions pos) throws Exception {
	boolean found = false;
	boolean end = false;
	if (!indexExists(def.getPK())) {
	    throw new Exception("No index created");
	}
	int pkindex = def.getColPosition(def.getPK());
	if (pkindex == -1) {
	    throw new Exception("Primary key does not exist");
	}

	// calculate index = hash value
	String s_value = pkvalue.trim();
	int idx = hash(s_value);

	Integer[] size = def.getSizes();
	int recordSize = idxFixedRecordLength() + size[pkindex];

	RandomAccessFile raf = new RandomAccessFile(BaseIO.getInitDir() + File.separator + def.getTableName() + "_" + def.getPK() + ".idx", "rw");
	raf.seek(idx*recordSize);
	String sLine = raf.readLine();
	System.out.println("sLine = " + sLine);
	if (sLine.substring(0, 1).equals(" ")) {
	    // empty record, end of search
	    found = false;
	    return found;
	}

	String[] parts = sLine.split("#");
	String s_part = parts[2].trim();
	if ((s_part.equals(pkvalue)) && !(parts[1].equals("D"))) {
	    found = true;
	    pos.index = Integer.parseInt(parts[0].trim());
	    pos.table = Integer.parseInt(parts[3].trim());
	}
	while ((!found) && (!end)) {
	    if (parts[4].substring(0,4).equals("null")) {
		// end of linked list
		end = true;
		found = false;
	    } else {
		idx = Integer.parseInt(parts[4].trim());
		raf.seek(idx*recordSize);
		sLine = raf.readLine();
		parts = sLine.split("#");
		s_part = parts[2].trim();
		if ((s_part.equals(pkvalue)) && !(parts[1].equals("D"))) {
		    found = true;
		    pos.index = Integer.parseInt(parts[0].trim());
		    pos.table = Integer.parseInt(parts[3].trim());
		}
	    }
	}
	return found;
    }

    /**
     * Delete an index entry in the idx file of the specified column.
     * @param colname the column's name
     * @param idxPos index entry (line nr) to be deleted
     * @throws Exception
     */
    protected long deleteIndexEntry(String colname, int idxPos) throws Exception {
	long s = System.currentTimeMillis();
	if (!indexExists(colname)) {
	    throw new Exception("No index created");
	}
	int pkindex = def.getColPosition(colname);
	if (pkindex == -1) {
	    throw new Exception("Column does not exist");
	}
	Integer[] size = def.getSizes();
	int recordSize = idxFixedRecordLength() + size[pkindex];

	RandomAccessFile raf = new RandomAccessFile(BaseIO.getInitDir() + File.separator + def.getTableName() + "_" + colname + ".idx", "rw");
	raf.seek(idxPos*recordSize);
	String sLine = raf.readLine();
	String[] parts = sLine.split("#");
	if (Integer.parseInt(parts[0].trim()) != idxPos) {
	    throw new Exception("Index not found in index file");
	} else {
	    raf.seek(idxPos*recordSize + 6);
	    String flag = "D";
	    raf.write(flag.toString().getBytes());
	}
	long e = System.currentTimeMillis();
	return e-s;
    }
    
    protected boolean indexExists(String colname) {
	return new File(BaseIO.getInitDir() + File.separator + def.getTableName() + "_" + colname + ".idx").exists();
    }
}

