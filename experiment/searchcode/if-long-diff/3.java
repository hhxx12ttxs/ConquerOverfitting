/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.spatial.services.dto;

import java.sql.Timestamp;
import java.text.NumberFormat;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author ajay
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public class Session {
    private String sessionid;
    private String email;
    private String tasks;
    private int totaltime;
    private int layerCount;
    private int speciesCount;
    private int areaCount;
    private int toolCount;
    private int importCount;
    private int exportCount;
    private Timestamp startTime;
    private Timestamp endTime;
    private String displaytime;
    private String userip;

    public String getUserip() {
        return userip;
    }

    public void setUserip(String userip) {
        this.userip = userip;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTasks() {
        return tasks;
    }

    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

    public int getTotaltime() {
        return totaltime;
    }

    public void setTotaltime(int totaltime) {
        this.totaltime = totaltime;
        //displaytime = generateDisplayTime();
    }

    public int getAreaCount() {
        return areaCount;
    }

    public void setAreaCount(int areaCount) {
        this.areaCount = areaCount;
    }

    public int getLayerCount() {
        return layerCount;
    }

    public void setLayerCount(int layerCount) {
        this.layerCount = layerCount;
    }

    public int getSpeciesCount() {
        return speciesCount;
    }

    public void setSpeciesCount(int speciesCount) {
        this.speciesCount = speciesCount;
    }

    public int getToolCount() {
        return toolCount;
    }

    public void setToolCount(int toolCount) {
        this.toolCount = toolCount;
    }

    public int getExportCount() {
        return exportCount;
    }

    public void setExportCount(int exportCount) {
        this.exportCount = exportCount;
    }

    public int getImportCount() {
        return importCount;
    }

    public void setImportCount(int importCount) {
        this.importCount = importCount;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public String getDisplaytime() {
        generateDisplayTime();
        return displaytime;
    }

    public void setDisplaytime(String displaytime) {
        this.displaytime = displaytime;
    }

    public void incrementCount(String type) {
        if (type.equalsIgnoreCase("species")) {
            speciesCount++;
        } else if (type.equalsIgnoreCase("area")) {
            areaCount++;
        } else if (type.equalsIgnoreCase("layer")) {
            layerCount++;
        } else if (type.equalsIgnoreCase("tool")) {
            toolCount++;
        } else if (type.equalsIgnoreCase("import")) {
            importCount++;
        } else if (type.equalsIgnoreCase("export")) {
            exportCount++;
        }
    }

    private void generateDisplayTime() {
        long diff = totaltime;
        displaytime = "";

        long day = Math.round(Math.floor(diff / 60 / 60 / 24));
        diff -= day * 60 * 60 * 24;
        if (day > 0) {
            displaytime += day + ":";
        }

        long hour = Math.round(Math.floor(diff / 60 / 60));
        diff -= hour * 60 * 60;
        if (hour > 0) {
            displaytime += hour + ":";
        }

        long min = Math.round(Math.floor(diff / 60));
        diff -= min * 60;
        if (min > 0) {
            displaytime += min + ":";
        }

        long sec = Math.round(Math.floor(diff));
        if (sec > 0) {
            if (displaytime.trim().equals("")) {
                displaytime += "00:" + sec;
            } else {
                displaytime += sec;
            }

        }
        //System.out.println("Display time: " + displaytime + " for " + totaltime);
    }

    private void generateDisplayTimeAsText() {
        long diff = totaltime;
        displaytime = "";

        long day = Math.round(Math.floor(diff / 60 / 60 / 24));
        diff -= day * 60 * 60 * 24;
        if (day > 0 && day < 1) {
            displaytime += day + " day ";
        } else if (day > 1) {
            displaytime += day + " days ";
        }

        long hour = Math.round(Math.floor(diff / 60 / 60));
        diff -= hour * 60 * 60;
        if (hour > 0 && hour < 1) {
            displaytime += hour + " hour ";
        } else if (hour > 1) {
            displaytime += hour + " hours ";
        }

        long min = Math.round(Math.floor(diff / 60));
        diff -= min * 60;
        if (min > 0 && min < 1) {
            displaytime += min + " minute ";
        } else if (min > 1) {
            displaytime += min + " minutes ";
        }

        long sec = Math.round(Math.floor(diff));
        if (sec > 0 && sec < 1) {
            displaytime += sec + " second ";
        } else if (sec > 1) {
            displaytime += sec + " seconds ";
        }
    }

    public String toCSV() {
        double t = totaltime;
        t = t / 60;
        StringBuffer sb = new StringBuffer();
        sb
                .append("\"").append(email).append("\",")
                .append("\"").append(userip).append("\",")
                .append("\"").append(sessionid).append("\",")
                .append(speciesCount).append(",")
                .append(areaCount).append(",")
                .append(layerCount).append(",")
                .append(toolCount).append(",")
                .append(importCount).append(",")
                .append(exportCount).append(",")
                .append(startTime).append(",")
                .append(endTime).append(",")
                .append(t);

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Session{" + "sessionid=" + sessionid + "tasks=" + tasks + "totaltime=" + totaltime + "layerCount=" + layerCount + "speciesCount=" + speciesCount + "areaCount=" + areaCount + "toolCount=" + toolCount + '}';
    }

}

