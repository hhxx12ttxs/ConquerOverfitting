/**
 * 
 */
package com.rainstars.common.util.tool;

import java.util.ArrayList;
import java.util.List;

import com.rainstars.common.BaseVo;

/**
 * @author Administrator
 *  
 */
public class PageObject extends BaseVo{
	//设置每页默认的的记录数
	public static int DEFAULT_PAGE_SIZE = 10;
	
	//每页的记录数
	private int pageSize = DEFAULT_PAGE_SIZE;
	
	//当前页在list中的位置,从0开始
	private int startIndex;
	
	//当前页存放的记录,存放在list中
	List items;
	
	//总记录数
	private int recordCount;
	
	private int newNo;
	
	private long diffTime;

	public int getNewNo() {
		return newNo;
	}

	public void setNewNo(int newNo) {
		this.newNo = newNo;
	}

	/***********************************************************************
	 * 构造方法,构造空页
	 */
	public PageObject() {
		this(new ArrayList(),0);
	}
	
	/***********************************************************************
	 * 构造方法
	 * @param items 本页包含的数据
	 * @param recordCount 数据库中的总记录条数
	 */
    public PageObject(List items,int recordCount) {
    	this.items = items;
    	this.recordCount = recordCount;
	}
    
    /***********************************************************************
     * 构造方法
     * @param items  本页包含的数据
     * @param recordCount  数据库中的总记录条数
     * @param startIndex  本页数据在数据库中的起始位置
     */
    public PageObject(List items,int recordCount, int startIndex)
    {
    	this.items = items;
    	this.recordCount = recordCount;
    	this.startIndex = startIndex;
    }
    
    /***********************************************************************
     * 构造方法
     * @param items  本页包含的数据
     * @param recordCount  数据库中的总记录条数
     * @param startIndex  本页数据在数据库中的起始位置
     * @param pageSize  本页的数据条数
     */
    public PageObject(List items,int recordCount,int startIndex,int pageSize)
    {
    	this.items = items;
    	this.recordCount = recordCount;
    	this.startIndex = startIndex;
    	this.pageSize = pageSize;
    }
    
    /***********************************************************************
     * 获取当前页码,从1开始
     * @return
     */
    public int getCurrentPageNo()
    {
    	return startIndex/pageSize + 1;
    }
    
    /**
     * 获取首页页码
     * @return
     */
    public int getFirstPage()
    {
    	return 1;
    }
    
    /**
     * 获取尾页页码
     * @return
     */
    public int getEndPage()
    {
    	return getPageCount();
    }
    
    /***********************************************************************
     * 获取总页数
     * @return
     */
    public int getPageCount()
    {
    	if (recordCount%pageSize == 0)
    	{
    		return recordCount/pageSize;
    	}
    	else{
    		return recordCount/pageSize + 1;
    	}
    }
    
    /***********************************************************************
     * 判断是否有下一页
     * @return
     */
    public boolean hasNextPage()
    {
    	if(getCurrentPageNo()<getPageCount())
    	{
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    /***********************************************************************
     * 判断是否有上一页
     * @return
     */
    public boolean hasPreviousPage()
    {
    	if (getCurrentPageNo() > 1)
    	{
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    /***********************************************************************
     * 判断是否有下一页
     * @return
     */
    public boolean getNextPage()
    {
    	return hasNextPage();
    }
    
    /***********************************************************************
     * 判断是否有上一页
     * @return
     */
    public boolean getPreviousPage()
    {
    	return hasPreviousPage();
    }
    
    /***********************************************************************
     * 获取任一页第一条数据的位置,startIndex从0开始
     * @param pageNo 指当前页码
     * @param pageSize 每页的记录条数
     * @return
     */
    public static int getPageIndex(int pageNo,int pageSize)
    {
		return (pageNo-1)*pageSize;
    }
    
    /***********************************************************************
     * 获取任一页第一条数据的位置,每页的条数使用默认值
     * @param pageNo 指当前页码
     * @return
     */
    public static int getPageIndex(int pageNo)
    {
    	return (pageNo - 1)*DEFAULT_PAGE_SIZE;
    }

	public List getItems() {
		return items;
	}

	public void setItems(List items) {
		this.items = items;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public long getDiffTime() {
		return diffTime;
	}

	public void setDiffTime(long diffTime) {
		this.diffTime = diffTime;
	}
}

