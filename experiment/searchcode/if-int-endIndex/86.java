package com.to211.to211admin.domain.base;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;


public class BaseQuery {
	@JSONField(serialize=false)
	private Map<String, String> searchData = new HashMap<String, String>();
	//数据查询范围的开始索引位置
	@JSONField(serialize=false)
	private int startIndex;
	//数据查询范围的结束索引位置
	@JSONField(serialize=false)
	private int endIndex;
	//数据查询范围大小：等于结束索引 - 开始索引
	@JSONField(serialize=false)
	private int indexRange;
	//数据库满足查询条件的记录数
	@JSONField(serialize=false)
	private int totalCount;
	//查询关联的page对象
	@JSONField(serialize=false)
	private Page<?> page;
	
	/**
	 * 保存搜索条件，在页面的使用通常为：searchData['name']=xiaoY 或 searchData['age']=20
	 * @return
	 */
	@JSONField(serialize=false)
	public Map<String, String> getSearchData() {
		return searchData;
	}
	
	/**
	 * 保存搜索条件，在页面的使用通常为：searchData['name']=xiaoY 或 searchData['age']=20
	 * @param searchData
	 */
	public void setSearchData(Map<String, String> searchData) {
		this.searchData = searchData;
	}
	
	/**
	 * 获取数据查询范围的开始索引位置
	 * @return
	 */
	@JSONField(serialize=false)
	public int getStartIndex() {
		if(startIndex > this.endIndex)
		{
			startIndex = this.endIndex;
		}
		return startIndex;
	}
	/**
	 * 设置数据查询范围的开始索引位置
	 * @param startIndex
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
	/**
	 * 获取数据查询范围的结束索引位置
	 * @return
	 */
	@JSONField(serialize=false)
	public int getEndIndex() {
		if(endIndex < this.startIndex){
			endIndex = this.startIndex;
		}
		return endIndex;
	}
	/**
	 * 设置数据查询范围的结束索引位置
	 * @param endIndex
	 */
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
	/**
	 * 获取数据查询范围大小：等于结束索引 - 开始索引(endIndex - startIndex)
	 * @return
	 */
	@JSONField(serialize=false)
	public int getIndexRange() {
		if(indexRange <= 0){
			indexRange = endIndex - startIndex;
		}
		return indexRange;
	}
	/**
	 * 设置数据查询范围大小：等于结束索引 - 开始索引(endIndex - startIndex)
	 * @param indexRange
	 */
	public void setIndexRange(int indexRange) {
		this.indexRange = indexRange;
	}

	@JSONField(serialize=false)
	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	@JSONField(serialize=false)
	public Page<?> getPage() {
		return page;
	}

	public void setPage(Page<?> page) {
		this.page = page;
	}
}

