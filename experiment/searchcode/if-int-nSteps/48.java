/**
 * File: FileHelper.java
 * Created by: mhaimel
 * Created on: 20 Jul 2009
 * CVS:  $Id: FileHelper.java,v 1.3 2009/12/07 10:07:50 mhaimel Exp $
 */
package uk.ac.ebi.curtain.util;

import java.io.File;
import java.util.LinkedList;
import java.util.zip.CRC32;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.curtain.utils.CurtainUncheckedException;

/**
 * @author mhaimel
 *
 */
public class FileHelper {
	private static final String CURR_DIR_DOT = ".";
	// 79fc37bd
	// 00000000
	private static String FILL_STRING = "00000000"; 
	private static Integer N_SIZE = 2;
	public static Integer N_STEPS = FILL_STRING.length()/N_SIZE;
	
	private String filler;
	private Integer nSteps;
	private Integer nSize;
	private CRC32 crc = new CRC32();
	
	public FileHelper() {
		this(FILL_STRING,N_STEPS,N_SIZE);
	}

	public FileHelper(String filler, Integer steps, Integer size) {
		this.filler = filler;
		this.nSteps = steps;
		this.nSize = size;
	}
	
	public static File getSubDirectory(File base, Integer id){
		return new FileHelper().buildSubdir(base, id);
	}	
	
	public static File getSubDirectory(File base, String id){
		return new FileHelper().buildSubdir(base, id);
	}
	
	public static String getCrcidFromSubDirectory(File subDir){
		return new FileHelper().crcidFromSubDirectory(subDir);
	}
	
	public static Integer getIdFromSubDirectory(File subDir){
		return new FileHelper().idFromSubDirectory(subDir);
	}	
	
	public String crcidFromSubDirectory(File subDir) {
		LinkedList<String> list = new LinkedList<String>();
		for(int i = 0; i < nSteps; ++i){
			String tmpName = subDir.getName();
			if(tmpName.equals(CURR_DIR_DOT)){
				subDir = subDir.getParentFile();
				tmpName = subDir.getName();
			}
			if(!nSize.equals(tmpName.length())){
				throw new CurtainUncheckedException("Unexpected name length: expected length of " + nSize+ " for name " + tmpName);
			}
			list.addFirst(tmpName);
			subDir = subDir.getParentFile();
		}
		String crc = StringUtils.join(list, "");
		return crc;
	}
	
	public Integer idFromSubDirectory(File subDir){
		return Integer.valueOf(crcidFromSubDirectory(subDir), 16);
	}

	public File buildSubdir(File base, String id) {
		return _calculateBasedir(base, identifierString(id));
	}

	public File buildSubdir(File base, Integer id){
		return _calculateBasedir(base, identifierString(id));
	}
	
	private String identifierString(Integer id){
		return Integer.toHexString(id);
	}
	
	private String identifierString(String id){
		crc.reset();
		crc.update(id.getBytes());
		return Long.toHexString(crc.getValue());
	}
	
	private File _calculateBasedir(File base, String idString) {
		File superDir = base;
		String dirStr = _id2String(idString);
		Integer nSteps = getNSteps();
		Integer nSize = getNSize();
		int totLen = getFiller().length();
		int nPrefix = dirStr.length() - totLen;
		if(nPrefix < 0){
			nPrefix = 0;
		}
		
		for(int i = 0; i < nSteps; ++i){
			int iCurr = (i*nSize+(i==0?0:nPrefix));
			int iNext = Math.min((i*nSize+nSize+nPrefix), dirStr.length());
			String subDir = dirStr.substring(iCurr, iNext);
			superDir = new File(superDir,subDir);
		}
		return superDir;
	}
	
	public String id2String(String id){
		return _id2String(identifierString(id));
	}
	
	public String id2String(Integer id){
		return _id2String(identifierString(id));
	}
	
	private String _id2String(String idStr) {
		// fill string
		String repString = getFiller();
		if(idStr.length() < repString.length()){
			int i = repString.length() - idStr.length();
			idStr = StringUtils.left(repString, i)+idStr;
		}
		return idStr;
	}	

	private String getFiller() {
		return filler;
	}

	private Integer getNSteps() {
		return nSteps;
	}

	public Integer getNSize() {
		return nSize;
	}
	
}

