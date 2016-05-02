/** 
 * CopyRright (c)1985-2009:ada.young <br />                             
 * Project: Sun_PS<br />                                           
 * Module ID:    <br />   
 * Comments:            <br />                                  
 * JDK version used:<JDK1.6><br />                                 
 * Namespace:com.gete.sunps.page<br />                             
 * Author?ada.young <br />                  
 * Create Date?  2009-10-27<br />   
 * Modified By?ada.young          <br />                                
 * Modified Date:2009-10-27        <br />                               
 * Why & What is modified <br />   
 * Version: 1.01         <br />       
 */
package com.ada.dao.help;

import java.util.logging.Logger;

public class PageBean {
	private Logger logger = Logger.getLogger("ArticleDaoImpl");
	private int totalRows;// ?????

	private int pageSize;// ?????????
	private int currentPage;// ????
	private int totalPages;// ???
	private int endRow;
	private int startRow;// ?????????
	private int startPage;// ?????????
	private int endPage;// ?????????

	public PageBean(int size, int curpage, int rows) {
		totalRows = rows;
		pageSize = size;
		if (curpage < 1) {
			currentPage = 1;
		} else {
			currentPage = curpage;
		}
		int temp = 0;
		temp = rows / pageSize;
		if (rows % pageSize > 0) {
			temp++;
		}
		totalPages = temp;
		if (currentPage > totalPages) {
			currentPage = totalPages;
		}

	}

	public int getTotalRows() {
		return totalRows;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public int getStartRow() {
		startRow = (currentPage - 1) * pageSize;
		if(startRow<0){
			startRow=0;
		}
		return startRow;
	}

	public int getStartPage() {
		if (currentPage < 4) {
			startPage = 1;
		} else {
			startPage = currentPage - 3;
		}

		return startPage;
	}

	public int getEndPage() {
		if (currentPage > totalPages) {
			endPage = totalPages;
		} else if ((currentPage + 3) > totalPages) {
			endPage = totalPages;
		} else {
			endPage = currentPage + 3;
		}
		return endPage;
	}

	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}

	public int getEndRow() {
		endRow = (currentPage) * pageSize - 1;
		if (endRow > totalRows) {
			endRow = totalRows;
		}
		return endRow;
	}
}

