package com.orange.score.activity.entry;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PageableAdapter<T> extends BaseAdapter {

	public static final int DEFAULT_PAGE_SIZE = 400;
	public List<T> list = new ArrayList<T>();
	public Context context;
    
	public int startIndex = 0;
	public int endIndex = 0;
//    int totalIndexCount = 0; // not used yet
	public int pageSize = DEFAULT_PAGE_SIZE;

    public PageableAdapter(List<T> list, Context context) {
        super();
        this.list = list;
        this.context = context;
    }
    
	@Override
	public int getCount() {
		if (list.size() == 0)
			return 0;
		else
			return (endIndex - startIndex) + 1;
	}

	@Override
	public T getItem(int position) {
		if (position <= list.size() -1)
			return list.get(startIndex + position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		// this method is not used yet, below implementation is not verified
		if (position < list.size() -1)
			return startIndex + position;
		else
			return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

//	public int getTotalIndexCount() {
//		return totalIndexCount;
//	}
//
//	public void setTotalIndexCount(int totalIndexCount) {
//		this.totalIndexCount = totalIndexCount;
//	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public void updateList(List<T> newList){
		
		this.list = newList;		
		int size = list.size();
		
		if (size == 0){
			startIndex = 0;
			endIndex = 0;
		}
		else{
			startIndex = 0;
			endIndex = pageSize;
			if (this.endIndex > size-1 ){
				endIndex = size - 1;
			}
		}
				
		printListData();
	}

	protected void printListData() {
		Log.d("PageableAdapter", "List size = " + list.size() + ", start = " + startIndex +
				", end = " + endIndex + ", count = " + getCount());
	}

	// return true if there are more pages, else return false
	public boolean nextPage(){		
		int totalSize = list.size();
		int oldEndIndex = endIndex;
		if (totalSize == 0){
			return false;
		}
		else if (endIndex + pageSize >= totalSize){
			endIndex = totalSize - 1;
		}
		else{
			endIndex += pageSize;
		}
					
		printListData();
		
		return (oldEndIndex != endIndex) ? true : false;
	}
	
	public void onScroll(int firstVisibleItem, int visibleItemCount) {
		if ((firstVisibleItem + visibleItemCount) == getCount()){
			if (nextPage()){
				notifyDataSetChanged();
			}
		}
	}
	
}

