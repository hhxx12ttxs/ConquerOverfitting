package com.helper;

public class PageBeans
{
	private int totalRows;
	private int pageSize;
	private int currentPage;
	private int totalPages;
	private int startRow;
    private String query;

	public String getQuery() {
		return query;
	}


	public void setQuery(String query) {
		this.query = query;
	}


	
	public PageBeans(int totalRows,int pageSize)
	{
		this.pageSize=pageSize;
		this.totalRows = totalRows;
		totalPages = totalRows / pageSize;
		if (totalRows % pageSize > 0)
		{
			totalPages++;
		}
		currentPage = 1;
		startRow = 0;
	}
	
    public PageBeans(){
		
    }
    public void config(){
    	totalPages = totalRows / pageSize;
		if (totalRows % pageSize > 0)
		{
			totalPages++;
		}
    	if (currentPage < 1)
		{
    		currentPage=1;
		}
    	if (currentPage>totalPages)
		{
			currentPage=totalPages;
		}
		startRow = (currentPage - 1) * pageSize;
    }
	
	public void first()
	{
		currentPage = 1;
		startRow = 0;
	}

	
	public void previous()
	{
		if (currentPage == 1)
		{
			startRow = 0;
			return;
		}
		currentPage--;
		startRow = (currentPage - 1) * pageSize;
	}

	
	public void next()
	{
		if (currentPage < totalPages)
		{
			currentPage++;
		}
		startRow = (currentPage - 1) * pageSize;
	}

	
	public void last()
	{
		currentPage = totalPages;
		startRow = (currentPage - 1) * pageSize;
	}
	
	
	
	public void refresh(int _currentPage) {
		currentPage = _currentPage;
		if (currentPage > totalPages) {
			last();
		}
		if(currentPage<1)
		{
			first();
		}
	}
	/**
	 * setter & getter methods
	 * 
	 * @return
	 */
	public int getCurrentPage()
	{
		return currentPage;
	}

	public void setCurrentPage(int currentPage)
	{
		this.currentPage = currentPage;
	}


	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	public int getStartRow()
	{
		return startRow;
	}

	public void setStartRow(int startRow)
	{
		this.startRow = startRow;
	}

	public int getTotalPages()
	{
		return totalPages;
	}

	public void setTotalPages(int totalPages)
	{
		this.totalPages = totalPages;
	}

	public int getTotalRows()
	{
		return totalRows;
	}

	public void setTotalRows(int totalRows)
	{
		this.totalRows = totalRows;
	}

}

