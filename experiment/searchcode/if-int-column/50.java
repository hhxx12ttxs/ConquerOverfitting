package androidlab.exercise6_1.db;

/* 
 * Copyright (c) 2012 Leander Sabel
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class stores information on a single column of a SQLite database.
 * 
 * @author Leander
 * 
 */
public class Column {

  private String key;
  private int column;
  private String type;

  /**
   * @param key
   * @param column
   * @param type
   */
  public Column(String key, int column, String type) {
	super();
	this.key = key;
	this.column = column;
	this.type = type;
  }

  /**
   * @return the key
   */
  public String getKey() {
	return key;
  }

  /**
   * @param key
   *          the key to set
   */
  public void setKey(String key) {
	this.key = key;
  }

  /**
   * @return the column
   */
  public int getColumn() {
	return column;
  }

  /**
   * @param column
   *          the column to set
   */
  public void setColumn(int column) {
	this.column = column;
  }

  /**
   * @return the type
   */
  public String getType() {
	return type;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setType(String type) {
	this.type = type;
  }

}

