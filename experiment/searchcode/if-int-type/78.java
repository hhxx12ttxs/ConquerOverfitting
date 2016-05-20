/*
 * Copyright (c) 2008-2011 Simon Ritchie.
 * All rights reserved. 
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program.  If not, see http://www.gnu.org/licenses/>.
 */
package org.rimudb;

import java.io.*;

/**
 * This class represents the meta data that describes a column in a table.
 * 
 * @author Simon Ritchie
 *
 */
public class ColumnMetaData implements Serializable {
	private static final long serialVersionUID = -919397174121629814L;
	private int columnNbr = -1;
	private String propertyName = null;
	private String columnName = null;
	private int columnSize = 0;
	private int decimalDigits = 0;
	private int primaryKeyNbr = 0;
	private boolean rightJustify = false;
	private int columnType = 0;
	private boolean nullCapable = false;
	private boolean autoIncrement = false;
	private boolean version = false;
	private String sequenceName = "";
	
	public ColumnMetaData() {
	}
	
	public ColumnMetaData(int columnNbr, String propertyName, String columnName, int length, int decimals, int keyNbr, boolean rightJustify, int type, boolean nullCapable, boolean autoIncrement, boolean version) {
		setColumnNbr(columnNbr);
		setPropertyName(propertyName);
		setColumnName(columnName);
		setColumnSize(length);
		setDecimalDigits(decimals);
		setPrimaryKeyNbr(keyNbr);
		setRightJustify(rightJustify);
		setColumnType(type);
		setNullCapable(nullCapable);
		setAutoIncrement(autoIncrement);
		setVersion(version);
	}
	
	public ColumnMetaData(int columnNbr, String propertyName, String columnName, int length, int decimals, int keyNbr, boolean rightJustify, int type, boolean nullCapable, boolean autoIncrement, boolean version, String sequenceName) {
		setColumnNbr(columnNbr);
		setPropertyName(propertyName);
		setColumnName(columnName);
		setColumnSize(length);
		setDecimalDigits(decimals);
		setPrimaryKeyNbr(keyNbr);
		setRightJustify(rightJustify);
		setColumnType(type);
		setNullCapable(nullCapable);
		setAutoIncrement(autoIncrement);
		setVersion(version);
		setSequenceName(sequenceName);
	}
	
	public int getColumnNbr() {
		return columnNbr;
	}
	public void setColumnNbr(int columnNbr) {
		this.columnNbr = columnNbr;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public int getColumnSize() {
		return columnSize;
	}
	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}
	public int getDecimalDigits() {
		return decimalDigits;
	}
	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	public int getPrimaryKeyNbr() {
		return primaryKeyNbr;
	}
	public void setPrimaryKeyNbr(int primaryKeyNbr) {
		this.primaryKeyNbr = primaryKeyNbr;
	}
	public boolean isRightJustify() {
		return rightJustify;
	}
	public void setRightJustify(boolean rightJustify) {
		this.rightJustify = rightJustify;
	}
	
	/** 
	 * Return the SQL type of the column. See java.sql.Types
	 * @return int
	 */
	public int getColumnType() {
		return columnType;
	}
	
	/**
	 * Set the SQL type of the column. See java.sql.Types
	 * @param columnType int
	 */
	public void setColumnType(int columnType) {
		this.columnType = columnType;
	}
	public boolean isNullCapable() {
		return nullCapable;
	}
	public void setNullCapable(boolean nullCapable) {
		this.nullCapable = nullCapable;
	}
	public boolean isAutoIncrement() {
		return autoIncrement;
	}
	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(boolean version) {
		this.version = version;
	}

	/**
	 * @return the version
	 */
	public boolean isVersion() {
		return version;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	/**
	 * Make a deep copy of the ColumnMetaData
	 * 
	 * @return ColumnMetaData
	 */
	public ColumnMetaData copy() {
		ColumnMetaData cmd = new ColumnMetaData();
		cmd.setColumnNbr(getColumnNbr());
		cmd.setPropertyName(getPropertyName());
		cmd.setColumnName(getColumnName());
		cmd.setColumnSize(getColumnSize());
		cmd.setDecimalDigits(getDecimalDigits());
		cmd.setPrimaryKeyNbr(getPrimaryKeyNbr());
		cmd.setRightJustify(isRightJustify());
		cmd.setColumnType(getColumnType());
		cmd.setNullCapable(isNullCapable());
		cmd.setAutoIncrement(isAutoIncrement());
		cmd.setVersion(isVersion());
		cmd.setSequenceName(getSequenceName());
		return cmd;
	}

}

