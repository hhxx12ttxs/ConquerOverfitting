/**
 * 
 */
package org.dualr.litelog.entity;

/**
 * Page.java
 * 
 * @author DualR
 * @mail dualrs@gmail.com
 * @site http://mimaiji.appspot.com || http://www.mimaiji.com
 * @date 2009-8-3
 */
public class Page {
	private int count;
	private int pageSize;
	private int curPage;
	private int pageCout;

	public Page(int count, int pageSize) {
		this.count = count;
		this.pageSize = pageSize;
		int size = count / pageSize;
		int mod = count % pageSize;
		if (mod != 0)
			size++;
		this.pageCout = count == 0 ? 1 : size;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurPage() {
		return curPage;
	}

	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}

	public int getPageCout() {
		return pageCout;
	}

	public void setPageCout(int pageCout) {
		this.pageCout = pageCout;
	}

}

