/************************************************************************
*				
*    
***********************************************************************/

/**
 * $Log: RMSModel.java,v $
 */


package com.gmail.semiwolf.rms;

import org.javawing.mobile.mvc.Model;

/**
 *
 @brief
 *   <p><b>model</b></p>
 * 
 * 
 *  <p><center>COPYRIGHT (C) 2009,RiseTek Systems Inc.All Rights Reserved.</center></p>
 *  @author TangJian
 *  @version cigarette
 *  @see 
 *  @since 2009-9-2
 */

public class RMSModel2 extends Model{
	private String addDataTime = "0";
	private String searchDataTime = "0";
	private String searchResult = "????";
	private RMSUtils rmsUtils = null; 

	/**
	 * @brief
	 *
	 */
	public RMSModel2() {
		rmsUtils = new RMSUtils(null);
	}
	
	public void addData(int amount, String data){
		String _data = data;
		if("".equals(_data)){
			_data = "[(??????)-(6001001188)-(22.00)-(500)]"; 
		}
		
		
		long start = System.currentTimeMillis();
		String[] dataArray = new String[amount];
		for (int i = 0; i < amount; i++) {
			dataArray[i] = _data + i;
		}
		rmsUtils.writeUTFData(dataArray);
		
		long end = System.currentTimeMillis();
		addDataTime = String.valueOf(end - start) + "ms";
		System.out.println("add data over " + addDataTime);

//		closeRecStore(); // Close record store

		updateViews();
	}
	
	public void searchData(String condition) {
		searchResult = rmsUtils.searchUTFData(condition);
		updateViews();
	}
	
	public void deleteRecStore() {
		rmsUtils.deleteRecStore();
	}

	private String record = "";
	public void setRecord(int id){
		long start = System.currentTimeMillis();

		record = rmsUtils.getRecord(id, "[");
		updateViews();
		long end = System.currentTimeMillis();
		searchDataTime = String.valueOf(end - start) + "ms";

	}
	
	public void updateRecord(int id, byte[] updateData){
		rmsUtils.updateRecord(id, updateData);
	}
	
	public String getSearchResult(){
		return searchResult;
	}
	
	public String getRecord(){
		return record;
	}
	
	public String getAddDataTime() {
		return addDataTime;
	}
	
	public String getSearchDataTime(){
		return searchDataTime;
	}
	
	private void log(String str) {
		System.err.println("Msg: " + str);
	}
	
}

