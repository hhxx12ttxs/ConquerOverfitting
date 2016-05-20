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

package com.aimluck.eip.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.aimluck.commons.field.ALDateTimeField;
import com.aimluck.commons.field.ALNumberField;
import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.common.ALData;
import com.aimluck.eip.fileupload.beans.FileuploadBean;
import com.aimluck.eip.util.ALCommonUtils;
import com.aimluck.eip.workflow.util.WorkflowUtils;

/**
 * ???????ResultData??? <BR>
 * 
 */
public class WorkflowResultData implements ALData {

  /** Request ID */
  protected ALNumberField request_id;

  /** Request? */
  protected ALStringField request_name;

  /** ????ID */
  protected ALNumberField category_id;

  /** ????? */
  protected ALStringField category_name;

  /** ??? */
  protected ALNumberField priority;

  /** ?????? */
  protected ALStringField priority_image;

  /** ???????? */
  protected ALStringField priority_string;

  /** ??????? */
  protected ALStringField state_string;

  /** ?? */
  protected ALStringField progress;

  /** ?? */
  protected ALNumberField price;

  /** ??? */
  protected ALStringField create_date;

  /** ??? */
  protected ALDateTimeField createYear;

  /** ??? */
  protected ALDateTimeField createDateYear;

  /** ??? */
  protected ALDateTimeField createDate;

  /** ??? */
  protected ALDateTimeField createDateTime;

  /** ?????? */
  protected ALStringField last_update_user;

  /** ???? */
  protected ALStringField client_name;

  /** ????????? */
  private List<FileuploadBean> attachmentFileList = null;

  /** ???? */
  private ALDateTimeField updateDate;

  /** ???? */
  private ALDateTimeField updateDateTime;

  /** ??????????ID */
  protected ALNumberField activityId;

  /**
   *
   *
   */
  @Override
  public void initField() {
    request_id = new ALNumberField();
    request_name = new ALStringField();
    category_id = new ALNumberField();
    category_name = new ALStringField();
    priority = new ALNumberField();
    priority_image = new ALStringField();
    priority_string = new ALStringField();
    state_string = new ALStringField();
    progress = new ALStringField();
    price = new ALNumberField();
    create_date = new ALStringField();

    last_update_user = new ALStringField();
    client_name = new ALStringField();
    attachmentFileList = new ArrayList<FileuploadBean>();

    updateDate = new ALDateTimeField("M?d?");
    updateDateTime = new ALDateTimeField("H:mm");

    createYear = new ALDateTimeField("yyyy?");
    createDateYear = new ALDateTimeField("yyyy?M?d?");
    createDate = new ALDateTimeField("M?d?");
    createDateTime = new ALDateTimeField("H:mm");

    activityId = new ALNumberField();
  }

  /**
   * @return
   */
  public ALNumberField getRequestId() {
    return request_id;
  }

  /**
   * @return
   */
  public String getRequestName() {
    return ALCommonUtils.replaceToAutoCR(request_name.toString());
  }

  /**
   * @return
   */
  public ALNumberField getCategoryId() {
    return category_id;
  }

  /**
   * @return
   */
  public String getCategoryName() {
    return ALCommonUtils.replaceToAutoCR(category_name.toString());
  }

  /**
   * @return
   */
  public ALStringField getStateString() {
    return state_string;
  }

  /**
   * @return
   */
  public ALStringField getPriorityString() {
    return priority_string;
  }

  /**
   * @return
   */
  public ALStringField getPriorityImage() {
    return priority_image;
  }

  /**
   * @return
   */
  public ALStringField getProgress() {
    return progress;
  }

  /**
   * @return
   */
  public ALNumberField getPrice() {
    return price;
  }

  public String getPriceStr() {
    return WorkflowUtils.translateMoneyStr(price.toString());
  }

  /**
   * @return
   */
  public ALStringField getLastUpdateUser() {
    return last_update_user;
  }

  /**
   * @param i
   */
  public void setRequestId(long i) {
    request_id.setValue(i);
  }

  /**
   * @param string
   */
  public void setRequestName(String string) {
    request_name.setValue(string);
  }

  /**
   * @param i
   */
  public void setCategoryId(long i) {
    category_id.setValue(i);
  }

  /**
   * @param i
   */
  public void setCategoryName(String string) {
    category_name.setValue(string);
  }

  /**
   * @param i
   */
  public void setPriority(int i) {
    priority.setValue(i);
  }

  /**
   * @param string
   */
  public void setPriorityImage(String string) {
    priority_image.setValue(string);
  }

  /**
   * @param string
   */
  public void setPriorityString(String string) {
    priority_string.setValue(string);
  }

  /**
   * @param string
   */
  public void setStateString(String string) {
    state_string.setValue(string);
  }

  /**
   * @param string
   */
  public void setProgress(String string) {
    progress.setValue(string);
  }

  /**
   * @param string
   */
  public void setPrice(long i) {
    price.setValue(i);
  }

  /**
   * @return
   */
  public ALStringField getCreateDate() {
    return create_date;
  }

  /**
   * @param string
   */
  public void setCreateDate(String string) {
    create_date.setValue(string);
  }

  /**
   * @param string
   */
  public void setLastUpdateUser(String string) {
    last_update_user.setValue(string);
  }

  /**
   * @return list
   */
  public List<FileuploadBean> getAttachmentFileList() {
    return attachmentFileList;
  }

  /**
   * @param list
   */
  public void setAttachmentFiles(List<FileuploadBean> list) {
    attachmentFileList = list;
  }

  /**
   * @return
   */
  public ALStringField getClientName() {
    return client_name;
  }

  /**
   * @param string
   */
  public void setClientName(String string) {
    client_name.setValue(string);
  }

  /**
   * @return
   */
  public ALNumberField getActivityId() {
    return activityId;
  }

  /**
   * @param string
   */
  public void setActivityId(int string) {
    activityId.setValue(string);
  }

  /**
   * @param field
   */
  public void setUpdateDateTime(Date date) {
    if (date == null) {
      return;
    }
    this.updateDate.setValue(date);
    this.updateDateTime.setValue(date);
  }

  public ALDateTimeField getUpdateDateTime() {
    ALDateTimeField today = new ALDateTimeField("M?d?");
    today.setValue(new Date());
    if (updateDate.toString().equals(today.toString())) {
      return updateDateTime;
    } else {
      return updateDate;
    }
  }

  public void setCreateDateTime(Date date) {
    if (date == null) {
      return;
    }
    this.createYear.setValue(date);
    this.createDateYear.setValue(date);
    this.createDate.setValue(date);
    this.createDateTime.setValue(date);
  }

  public ALDateTimeField getCreateDateTime() {
    ALDateTimeField today = new ALDateTimeField("M?d?");
    ALDateTimeField thisYear = new ALDateTimeField("yyyy?");
    today.setValue(new Date());
    thisYear.setValue(new Date());
    if (createDate.toString().equals(today.toString())
      && createYear.toString().equals(thisYear.toString())) {
      return createDateTime;
    } else if (!createDate.toString().equals(today.toString())
      && createYear.toString().equals(thisYear.toString())) {
      return createDate;
    } else {
      return createDateYear;
    }
  }
}

