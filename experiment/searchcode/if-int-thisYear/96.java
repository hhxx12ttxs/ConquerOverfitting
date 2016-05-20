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

package com.aimluck.eip.common;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import com.aimluck.commons.field.ALDateTimeField;
import com.aimluck.commons.field.ALNumberField;
import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.cayenne.om.social.Activity;
import com.aimluck.eip.orm.Database;
import com.aimluck.eip.orm.query.ResultList;
import com.aimluck.eip.services.social.ALActivityService;
import com.aimluck.eip.services.social.model.ALActivityGetRequest;
import com.aimluck.eip.util.ALCommonUtils;

/**
 *
 */
public class ALActivity implements ALData, Serializable {

  private static final long serialVersionUID = 597816564175622540L;

  private int id;

  private ALStringField displayName;

  private ALStringField appId;

  private ALStringField loginname;

  private ALStringField title;

  private ALDateTimeField updateDate;

  private ALDateTimeField updateDateTime;

  private ALDateTimeField updateYear;

  private ALDateTimeField updateDateYear;

  private ALStringField externalId;

  private ALStringField portletParams;

  private ALStringField icon;

  private ALNumberField moduleId;

  private boolean isRead;

  public ALActivity() {
    initField();
  }

  /**
   *
   */
  @Override
  public void initField() {
    displayName = new ALStringField();
    displayName.setValue("_");
    appId = new ALStringField();
    loginname = new ALStringField();
    title = new ALStringField();
    externalId = new ALStringField();
    portletParams = new ALStringField();
    updateDate = new ALDateTimeField("M?d?");
    updateDateTime = new ALDateTimeField("H:mm");
    updateYear = new ALDateTimeField("yyyy?");
    updateDateYear = new ALDateTimeField("yyyy?M?d?");
    icon = new ALStringField();
    moduleId = new ALNumberField();
    isRead = true;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  /**
   * @return appId
   */
  public ALStringField getAppId() {
    return appId;
  }

  /**
   * @param appId
   *          ????? appId
   */
  public void setAppId(String appId) {
    this.appId.setValue(appId);
  }

  /**
   * @return loginname
   */
  public ALStringField getLoginName() {
    return loginname;
  }

  /**
   * @param loginname
   *          ????? loginname
   */
  public void setLoginName(String loginname) {
    this.loginname.setValue(loginname);
  }

  /**
   * @return title
   */
  public ALStringField getTitle() {
    return title;
  }

  /**
   * @param title
   *          ????? title
   */
  public void setTitle(String title) {
    this.title.setValue(title);
  }

  public void setUpdateDate(Date updateDate) {
    this.updateYear.setValue(updateDate);
    this.updateDateYear.setValue(updateDate);
    this.updateDate.setValue(updateDate);
    this.updateDateTime.setValue(updateDate);
  }

  public ALDateTimeField getUpdateDate() {
    ALDateTimeField today = new ALDateTimeField("M?d?");
    ALDateTimeField thisYear = new ALDateTimeField("yyyy?");
    today.setValue(new Date());
    thisYear.setValue(new Date());
    if (updateDate.toString().equals(today.toString())
      && updateYear.toString().equals(thisYear.toString())) {
      return updateDateTime;
    } else if (!updateDate.toString().equals(today.toString())
      && updateYear.toString().equals(thisYear.toString())) {
      return updateDate;
    } else {
      return updateDateYear;
    }
  }

  public void setDisplayName(String displayName) {
    this.displayName.setValue(displayName);
  }

  public ALStringField getDisplayName() {
    return displayName;
  }

  public ALStringField getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId.setValue(externalId);
  }

  public ALStringField getPortletParams() {
    return portletParams;
  }

  public void setPortletParams(String portletParams) {
    this.portletParams.setValue(portletParams);
  }

  public String getTitleText() {
    return ALCommonUtils.replaceToAutoCR(title.toString());
  }

  public void setModuleId(Integer moduleId) {
    this.moduleId.setValue(moduleId);
  }

  /**
   * @return moduleId
   */
  public ALNumberField getModuleId() {
    return moduleId;
  }

  public String getPopupUrl() {
    String portletParams = this.portletParams.getValue();
    String externalId = this.externalId.getValue();
    Long moduleId = this.moduleId.getValue();

    if (portletParams != null && portletParams.length() > 0) {
      StringBuilder b = new StringBuilder(portletParams);
      if (portletParams.indexOf("?") > -1) {
        b.append("&activityId=").append(id);
      } else {
        b.append("?activityId=").append(id);
      }
      return b.toString();
    } else {
      try {
        StringBuilder b =
          new StringBuilder("?template=GadgetsPopupScreen&view=popup&aid=")
            .append(URLEncoder.encode(appId.getValue(), "utf-8"))
            .append("&activityId=")
            .append(id);
        if (externalId != null && externalId.length() > 0) {
          b.append("&eid=").append(URLEncoder.encode(externalId, "utf-8"));
        }
        if (moduleId != null) {
          b.append("&mid=").append(moduleId);
        }
        return b.toString();
      } catch (UnsupportedEncodingException e) {
        //
      }
      return "";
    }
  }

  /**
   * @param isRead
   *          ????? isRead
   */
  public void setRead(boolean isRead) {
    this.isRead = isRead;
  }

  /**
   * @return isRead
   */
  public boolean isRead() {
    return isRead;
  }

  /**
   * @param icon
   *          ????? icon
   */
  public void setIcon(String icon) {
    this.icon.setValue(icon);
  }

  /**
   * @return icon
   */
  public ALStringField getIcon() {
    return icon;
  }

  /**
   * external_id???????????????????????null?
   * 
   * @param appid
   *          ex Schedule
   * @param external_id
   *          ???????????id
   * @return
   */
  public static ALActivity getRecentActivity(String appid, int external_id,
      float priority) {
    ResultList<ALActivity> list =
      ALActivityService.getList(new ALActivityGetRequest()
        .withAppId(appid)
        .withLimit(1)
        .withPriority(priority)
        .withExternalId(external_id));
    return list.size() > 0 ? list.get(0) : null;
  }

  /**
   * ?????Replace???
   * 
   * @param activity
   * @param loginname
   * @return
   */
  public boolean isReplace(String loginname) {
    return loginname.equals(this.getLoginName().getValue());

  }

  /**
   * ????????
   * 
   * @param activity
   * @param loginname
   * @return
   */
  public void Delete() {

    String sql = "delete from activity where id = " + this.getId();
    Database.sql(Activity.class, sql).execute();
  }
}

