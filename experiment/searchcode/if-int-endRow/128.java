/*
 * Aipo is a groupware program developed by Aimluck,Inc.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aimluck.eip.schedule;

/**
 * ??????????????????????
 * 
 */
public class ScheduleOnedayResultData extends ScheduleResultData {

  /** <code>startRow</code> ?? */
  private int startRow;

  /** <code>endRow</code> ?? */
  private int endRow;

  /** <code>index</code> ?????? */
  private int index;

  /** <code>dubliCount</code> ??????????? */
  private int dubliRow;

  /**
   * ???????????
   * 
   * @return endRow
   */
  public int getEndRow() {
    return endRow;
  }

  /**
   * ???????????
   * 
   * @param endRow
   */
  public void setEndRow(int endRow) {
    this.endRow = endRow;
  }

  /**
   * ???????????
   * 
   * @return startRow
   */
  public int getStartRow() {
    return startRow;
  }

  /**
   * ???????????
   * 
   * @param startRow
   */
  public void setStartRow(int startRow) {
    this.startRow = startRow;
  }

  /**
   * ?????????????
   * 
   * @return index
   */
  public int getIndex() {
    return index;
  }

  /**
   * ?????????????
   * 
   * @param index
   */
  public void setIndex(int index) {
    this.index = index;
  }

  /**
   * ??????????????????
   * 
   * @param index
   */
  public void setDubliRow(int dubliRow) {
    this.dubliRow = dubliRow;
  }

  /**
   * ??????????????????
   * 
   * @return index
   */
  public int getDubliRow() {
    return dubliRow;
  }
}

