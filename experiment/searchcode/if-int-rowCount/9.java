package org.jackysoft.util;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope("prototype")
public class PageManager {

	private int rowCount;
	private int rowsInPage;
	private int pageIndex;
	private int pageCount;
	private int startIndex;
    private int toIndex;
    private int currentRowInpage;
	
	public PageManager(){rowsInPage=10;}
	
	/**
	 * @param int rowCount 总行数用来计算总页数
	 * @param int rowsInPage 每页要显示的记录数
	 * */
	public PageManager(int rowCount , int rowsInPage) {
		this();
		this.rowsInPage = rowsInPage;
		this.rowCount = rowCount;		
	}
	
	
	/**
	 * 
	 */
	
	public void setPageIndex(int pageIndex){
		this.pageIndex = pageIndex;
	}
	
	/**
	 * @return int 获取当前页码
	 * 
	 * */
	public int getPageIndex() {
		int pc = getPageCount();
		if(this.pageIndex>pc) 
			this.pageIndex = pc;
		else if(this.pageIndex<=0)
			this.pageIndex = 1;
		if(pc==0) this.pageIndex=1;
		return this.pageIndex;
	}

	/**
	 * @return 返回总行数记录总数
	 * */
	public int getRowCount() {
		return this.rowCount;
	}

	/**
	 * @return 返回每页显示的行数
	 * */
	public int getRowInPage() {
		return this.rowsInPage;
	}

	/**
	 * @return 返回当翻到第pageIndex 页时，记录从第几行开始获取
	 * @param int pageIndex 设置页码
	 * 
	 * */
	public int getStartIndexOfPage(int pageIndex) {
		if ((pageIndex <= 0) || (pageIndex >= getRowCount()))
			return 0;        
		return ((pageIndex - 1) *  getRowInPage());
	}

	public int getCurrentStartIndex(int pageIndex,int rowCount,int rowsInPage) {
		setRowCount(rowCount);
		setRowsInPage(rowsInPage);
		if ((pageIndex <= 0) || (pageIndex >= getRowCount()))
			return 0;        
		return ((pageIndex - 1) * getRowInPage());
	}

	public PageInfo getPageInfo(int pIndex,int rowCount,int rowsInPage){
		PageInfo page      = new PageInfo();
		int start 	   = getCurrentStartIndex(pIndex,rowCount,rowsInPage);
		int pCount     = getCurrentPageCount(rowCount,rowsInPage);
		int crowInPage = getCurrentRowInPage(pIndex,rowCount,rowsInPage);
		page.setRowsInPage(crowInPage);
		page.setStartIndex(start);
		page.setPageCount(pCount);
		return page;
	}
	
	
	/**
	 * 
	 * */
	
	public int getStartIndex(){	
		startIndex = getStartIndexOfPage(getPageIndex());
        return startIndex;
	}
	
	public int getLastPageIndex(){
		return getPageCount();
	}
	
	/**
	 * 下页页码
	 * */
	public int getNextPageIndex(){
		return isLast()?getLastPageIndex():getPageIndex()+1;
	}
	
	/**
	 * 上页页码
	 * 
	 * */
	public int getPrevPageIndex(){
		return isFirst()?1:getPageIndex()-1;
	}
	/**
	 * @return 总页数
	 * */
	public int getPageCount() {
		if(this.rowCount==0 ||this.rowsInPage==0)return 0;
		int base = this.rowCount / this.rowsInPage;
		if (this.rowCount % this.rowsInPage == 0)
			this.pageCount = base;
		else
			this.pageCount = (base + 1);
		return this.pageCount;
	}
	
	
	public int getCurrentPageCount(int rowCount,int rowsInPage) {
		setRowCount(rowCount);
		setRowsInPage(rowsInPage);
		int base = getRowCount() / getRowInPage();
		if (getRowCount() % getRowInPage() == 0)
			this.pageCount = base;
		else
			this.pageCount = (base + 1);
		return this.pageCount;
	}
	
	public int getCurrentRowInPage(int pageIndex,int rowCount,int rowsInPage){
		setRowCount(rowCount);
		setRowsInPage(rowsInPage);
		int pCount = getPageCount();
		if(pageIndex<pCount){
			return rowsInPage;
		}else{
			return getRowCount()%rowsInPage;
		}
	}

	public int getCurrentRowInpage(){
		int pCount = getPageCount();
		if(this.pageIndex<pCount){
			return currentRowInpage= rowsInPage;
		}else{
			return currentRowInpage=getRowCount()%rowsInPage;
		}
	}
	
	public boolean hasNext() {
		return (getPageIndex() < getPageCount());
	}
	
	public boolean isLast(){
		return !hasNext();
	}

	public boolean hasPrevious() {
		return (getPageIndex() > 1);
	}	
	
	public boolean isFirst(){
		return !hasPrevious();
	}
	
	public void setRowCount(int rowCount){
        this.rowCount = rowCount;
        getPageCount();
        getStartIndex();
        getToIndex();
        getCurrentRowInpage();
	}
	
	public void setRowsInPage(int rowsInPage){
		this.rowsInPage = rowsInPage;
		getPageCount();
		getStartIndex();
        getToIndex();
        getCurrentRowInpage();
	}

	public int getToIndex() {
		toIndex = getStartIndex()+getCurrentRowInpage();
		return toIndex;
	}
}
