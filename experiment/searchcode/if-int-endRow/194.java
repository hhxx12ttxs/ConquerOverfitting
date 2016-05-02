package com.swinarta.sunflower.server.model;

import java.util.Map;

public class SgwtRequest {

	private String dataSource;
	private int startRow;
	private int endRow;
	private String[] sortBy;
	private String textMatchStyle;
	private String componentId;
	private Map<String, Object> data;
	private Map<String, Object> oldValues;
	private OPERATION_TYPE operationType;
	
	public enum OPERATION_TYPE {
	    FETCH, ADD, UPDATE, REMOVE 
	}
	
	public String getDataSource() {
		return dataSource;
	}
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}
	public OPERATION_TYPE getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationTypeStr) {
		if("fetch".equalsIgnoreCase(operationTypeStr)){
			this.operationType = OPERATION_TYPE.FETCH;
		}else if("update".equalsIgnoreCase(operationTypeStr)){
			this.operationType = OPERATION_TYPE.UPDATE;
		}else if("add".equalsIgnoreCase(operationTypeStr)){
			this.operationType = OPERATION_TYPE.ADD;
		}else if("remove".equalsIgnoreCase(operationTypeStr)){
			this.operationType = OPERATION_TYPE.REMOVE;
		}
	}
	public int getStartRow() {
		return startRow;
	}
	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}
	public int getEndRow() {
		return endRow;
	}
	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}
	public String[] getSortBy() {
		return sortBy;
	}
	public void setSortBy(String[] sortBy) {
		this.sortBy = sortBy;
	}
	public String getTextMatchStyle() {
		return textMatchStyle;
	}
	public void setTextMatchStyle(String textMatchStyle) {
		this.textMatchStyle = textMatchStyle;
	}
	public String getComponentId() {
		return componentId;
	}
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	public Map<String, Object> getData() {
		return data;
	}
	public void setData(Map<String, Object> data) {
		this.data = data;
	}
	public Map<String, Object> getOldValues() {
		return oldValues;
	}
	public void setOldValues(Map<String, Object> oldValues) {
		this.oldValues = oldValues;
	}
	
}

