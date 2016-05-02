/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.server.namenode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.apache.hadoop.hdfs.protocol.DatanodeID;
import org.apache.hadoop.hdfs.protocol.FSConstants.UpgradeAction;
import org.apache.hadoop.hdfs.server.common.JspHelper;
import org.apache.hadoop.hdfs.server.common.Storage;
import org.apache.hadoop.hdfs.server.common.UpgradeStatusReport;
import org.apache.hadoop.hdfs.server.common.Storage.StorageDirectory;
import org.apache.hadoop.util.ServletUtil;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.VersionInfo;

class NamenodeJspHelper {
  static String getSafeModeText(FSNamesystem fsn) {
    if (!fsn.isInSafeMode())
      return "";
    return "Safe mode is ON. <em>" + fsn.getSafeModeTip() + "</em><br>";
  }

  static String getInodeLimitText(FSNamesystem fsn) {
    long inodes = fsn.dir.totalInodes();
    long blocks = fsn.getBlocksTotal();
    long maxobjects = fsn.getMaxObjects();
    long totalMemory = Runtime.getRuntime().totalMemory();
    long maxMemory = Runtime.getRuntime().maxMemory();

    long used = (totalMemory * 100) / maxMemory;

    String str = inodes + " files and directories, " + blocks + " blocks = "
        + (inodes + blocks) + " total";
    if (maxobjects != 0) {
      long pct = ((inodes + blocks) * 100) / maxobjects;
      str += " / " + maxobjects + " (" + pct + "%)";
    }
    str += ".  Heap Size is " + StringUtils.byteDesc(totalMemory) + " / "
        + StringUtils.byteDesc(maxMemory) + " (" + used + "%) <br>";
    return str;
  }

  static String getUpgradeStatusText(FSNamesystem fsn) {
    String statusText = "";
    try {
      UpgradeStatusReport status = fsn
          .distributedUpgradeProgress(UpgradeAction.GET_STATUS);
      statusText = (status == null ? "There are no upgrades in progress."
          : status.getStatusText(false));
    } catch (IOException e) {
      statusText = "Upgrade status unknown.";
    }
    return statusText;
  }

  /** Return a table containing version information. */
  static String getVersionTable(FSNamesystem fsn) {
    return "<div id='dfstable'><table>"
        + "\n  <tr><td id='col1'>Started:</td><td>" + fsn.getStartTime()
        + "</td></tr>\n" + "\n  <tr><td id='col1'>Version:</td><td>"
        + VersionInfo.getVersion() + ", " + VersionInfo.getRevision()
        + "\n  <tr><td id='col1'>Compiled:</td><td>" + VersionInfo.getDate()
        + " by " + VersionInfo.getUser() + " from " + VersionInfo.getBranch()
        + "\n  <tr><td id='col1'>Upgrades:</td><td>"
        + getUpgradeStatusText(fsn) + "\n</table></div>";
  }

  static String getWarningText(FSNamesystem fsn) {
    // Ideally this should be displayed in RED
    long missingBlocks = fsn.getMissingBlocksCount();
    if (missingBlocks > 0) {
      return "<br> WARNING :" + " There are about " + missingBlocks
          + " missing blocks. Please check the log or run fsck. <br><br>";
    }
    return "";
  }

  static class HealthJsp {
    private int rowNum = 0;
    private int colNum = 0;
    private String sorterField = null;
    private String sorterOrder = null;

    private String rowTxt() {
      colNum = 0;
      return "<tr class=\"" + (((rowNum++) % 2 == 0) ? "rowNormal" : "rowAlt")
          + "\"> ";
    }

    private String colTxt() {
      return "<td id=\"col" + ++colNum + "\"> ";
    }

    private void counterReset() {
      colNum = 0;
      rowNum = 0;
    }

    void generateConfReport(JspWriter out, NameNode nn,
        HttpServletRequest request) throws IOException {
      FSNamesystem fsn = nn.getNamesystem();
      FSImage fsImage = fsn.getFSImage();
      List<Storage.StorageDirectory> removedStorageDirs = fsImage
          .getRemovedStorageDirs();

      // FS Image storage configuration
      out.print("<h3> " + nn.getRole() + " Storage: </h3>");
      out.print("<div id=\"dfstable\"> <table border=1 cellpadding=10 cellspacing=0 title=\"NameNode Storage\">\n"
              + "<thead><tr><td><b>Storage Directory</b></td><td><b>Type</b></td><td><b>State</b></td></tr></thead>");

      StorageDirectory st = null;
      for (Iterator<StorageDirectory> it = fsImage.dirIterator(); it.hasNext();) {
        st = it.next();
        String dir = "" + st.getRoot();
        String type = "" + st.getStorageDirType();
        out.print("<tr><td>" + dir + "</td><td>" + type
            + "</td><td>Active</td></tr>");
      }

      long storageDirsSize = removedStorageDirs.size();
      for (int i = 0; i < storageDirsSize; i++) {
        st = removedStorageDirs.get(i);
        String dir = "" + st.getRoot();
        String type = "" + st.getStorageDirType();
        out.print("<tr><td>" + dir + "</td><td>" + type
            + "</td><td><font color=red>Failed</font></td></tr>");
      }

      out.print("</table></div><br>\n");
    }

    void generateHealthReport(JspWriter out, NameNode nn,
        HttpServletRequest request) throws IOException {
      FSNamesystem fsn = nn.getNamesystem();
      ArrayList<DatanodeDescriptor> live = new ArrayList<DatanodeDescriptor>();
      ArrayList<DatanodeDescriptor> dead = new ArrayList<DatanodeDescriptor>();
      fsn.DFSNodesStatus(live, dead);

      sorterField = request.getParameter("sorter/field");
      sorterOrder = request.getParameter("sorter/order");
      if (sorterField == null)
        sorterField = "name";
      if (sorterOrder == null)
        sorterOrder = "ASC";

      // Find out common suffix. Should this be before or after the sort?
      String port_suffix = null;
      if (live.size() > 0) {
        String name = live.get(0).getName();
        int idx = name.indexOf(':');
        if (idx > 0) {
          port_suffix = name.substring(idx);
        }

        for (int i = 1; port_suffix != null && i < live.size(); i++) {
          if (live.get(i).getName().endsWith(port_suffix) == false) {
            port_suffix = null;
            break;
          }
        }
      }

      counterReset();
      long[] fsnStats = fsn.getStats();
      long total = fsnStats[0];
      long remaining = fsnStats[2];
      long used = fsnStats[1];
      long nonDFS = total - remaining - used;
      nonDFS = nonDFS < 0 ? 0 : nonDFS;
      float percentUsed = total <= 0 ? 0f : ((float) used * 100.0f)
          / (float) total;
      float percentRemaining = total <= 0 ? 100f : ((float) remaining * 100.0f)
          / (float) total;

      out.print("<div id=\"dfstable\"> <table>\n" + rowTxt() + colTxt()
          + "Configured Capacity" + colTxt() + ":" + colTxt()
          + StringUtils.byteDesc(total) + rowTxt() + colTxt() + "DFS Used"
          + colTxt() + ":" + colTxt() + StringUtils.byteDesc(used) + rowTxt()
          + colTxt() + "Non DFS Used" + colTxt() + ":" + colTxt()
          + StringUtils.byteDesc(nonDFS) + rowTxt() + colTxt()
          + "DFS Remaining" + colTxt() + ":" + colTxt()
          + StringUtils.byteDesc(remaining) + rowTxt() + colTxt() + "DFS Used%"
          + colTxt() + ":" + colTxt()
          + StringUtils.limitDecimalTo2(percentUsed) + " %" + rowTxt()
          + colTxt() + "DFS Remaining%" + colTxt() + ":" + colTxt()
          + StringUtils.limitDecimalTo2(percentRemaining) + " %" + rowTxt()
          + colTxt()
          + "<a href=\"dfsnodelist.jsp?whatNodes=LIVE\">Live Nodes</a> "
          + colTxt() + ":" + colTxt() + live.size() + rowTxt() + colTxt()
          + "<a href=\"dfsnodelist.jsp?whatNodes=DEAD\">Dead Nodes</a> "
          + colTxt() + ":" + colTxt() + dead.size() + "</table></div><br>\n");

      if (live.isEmpty() && dead.isEmpty()) {
        out.print("There are no datanodes in the cluster");
      }
    }
  }

  static void redirectToRandomDataNode(NameNode nn, HttpServletResponse resp)
      throws IOException {
    final DatanodeID datanode = nn.getNamesystem().getRandomDatanode();
    final String redirectLocation;
    final String nodeToRedirect;
    int redirectPort;
    if (datanode != null) {
      nodeToRedirect = datanode.getHost();
      redirectPort = datanode.getInfoPort();
    } else {
      nodeToRedirect = nn.getHttpAddress().getHostName();
      redirectPort = nn.getHttpAddress().getPort();
    }
    String fqdn = InetAddress.getByName(nodeToRedirect).getCanonicalHostName();
    redirectLocation = "http://" + fqdn + ":" + redirectPort
        + "/browseDirectory.jsp?namenodeInfoPort="
        + nn.getHttpAddress().getPort() + "&dir="
        + URLEncoder.encode("/", "UTF-8");
    resp.sendRedirect(redirectLocation);
  }

  static class NodeListJsp {
    private int rowNum = 0;

    private long diskBytes = 1024 * 1024 * 1024;
    private String diskByteStr = "GB";

    private String sorterField = null;
    private String sorterOrder = null;

    private String whatNodes = "LIVE";

    private String rowTxt() {
      return "<tr class=\"" + (((rowNum++) % 2 == 0) ? "rowNormal" : "rowAlt")
          + "\"> ";
    }

    private void counterReset() {
      rowNum = 0;
    }

    private String nodeHeaderStr(String name) {
      String ret = "class=header";
      String order = "ASC";
      if (name.equals(sorterField)) {
        ret += sorterOrder;
        if (sorterOrder.equals("ASC"))
          order = "DSC";
      }
      ret += " onClick=\"window.document.location="
          + "'/dfsnodelist.jsp?whatNodes=" + whatNodes + "&sorter/field="
          + name + "&sorter/order=" + order
          + "'\" title=\"sort on this column\"";

      return ret;
    }

    void generateNodeData(JspWriter out, DatanodeDescriptor d,
        String suffix, boolean alive, int nnHttpPort) throws IOException {
      /*
       * Say the datanode is dn1.hadoop.apache.org with ip 192.168.0.5 we use:
       * 1) d.getHostName():d.getPort() to display. Domain and port are stripped
       *    if they are common across the nodes. i.e. "dn1"
       * 2) d.getHost():d.Port() for "title". i.e. "192.168.0.5:50010"
       * 3) d.getHostName():d.getInfoPort() for url.
       *    i.e. "http://dn1.hadoop.apache.org:50075/..."
       * Note that "d.getHost():d.getPort()" is what DFS clients use to
       * interact with datanodes.
       */

      // from nn_browsedfscontent.jsp:
      String url = "http://" + d.getHostName() + ":" + d.getInfoPort()
          + "/browseDirectory.jsp?namenodeInfoPort=" + nnHttpPort + "&dir="
          + URLEncoder.encode("/", "UTF-8");

      String name = d.getHostName() + ":" + d.getPort();
      if (!name.matches("\\d+\\.\\d+.\\d+\\.\\d+.*"))
        name = name.replaceAll("\\.[^.:]*", "");
      int idx = (suffix != null && name.endsWith(suffix)) ? name
          .indexOf(suffix) : -1;

      out.print(rowTxt() + "<td class=\"name\"><a title=\"" + d.getHost() + ":"
          + d.getPort() + "\" href=\"" + url + "\">"
          + ((idx > 0) ? name.substring(0, idx) : name) + "</a>"
          + ((alive) ? "" : "\n"));
      if (!alive)
        return;

      long c = d.getCapacity();
      long u = d.getDfsUsed();
      long nu = d.getNonDfsUsed();
      long r = d.getRemaining();
      String percentUsed = StringUtils.limitDecimalTo2(d.getDfsUsedPercent());
      String percentRemaining = StringUtils.limitDecimalTo2(d
          .getRemainingPercent());

      String adminState = (d.isDecommissioned() ? "Decommissioned" : (d
          .isDecommissionInProgress() ? "Decommission In Progress"
          : "In Service"));

      long timestamp = d.getLastUpdate();
      long currentTime = System.currentTimeMillis();
      out.print("<td class=\"lastcontact\"> "
          + ((currentTime - timestamp) / 1000)
          + "<td class=\"adminstate\">"
          + adminState
          + "<td align=\"right\" class=\"capacity\">"
          + StringUtils.limitDecimalTo2(c * 1.0 / diskBytes)
          + "<td align=\"right\" class=\"used\">"
          + StringUtils.limitDecimalTo2(u * 1.0 / diskBytes)
          + "<td align=\"right\" class=\"nondfsused\">"
          + StringUtils.limitDecimalTo2(nu * 1.0 / diskBytes)
          + "<td align=\"right\" class=\"remaining\">"
          + StringUtils.limitDecimalTo2(r * 1.0 / diskBytes)
          + "<td align=\"right\" class=\"pcused\">"
          + percentUsed
          + "<td class=\"pcused\">"
          + ServletUtil.percentageGraph((int) Double.parseDouble(percentUsed),
              100) + "<td align=\"right\" class=\"pcremaining`\">"
          + percentRemaining + "<td title=" + "\"blocks scheduled : "
          + d.getBlocksScheduled() + "\" class=\"blocks\">" + d.numBlocks()
          + "\n");
    }

    void generateNodesList(JspWriter out, NameNode nn,
        HttpServletRequest request) throws IOException {
      ArrayList<DatanodeDescriptor> live = new ArrayList<DatanodeDescriptor>();
      ArrayList<DatanodeDescriptor> dead = new ArrayList<DatanodeDescriptor>();
      nn.getNamesystem().DFSNodesStatus(live, dead);

      whatNodes = request.getParameter("whatNodes"); // show only live or only
                                                     // dead nodes
      sorterField = request.getParameter("sorter/field");
      sorterOrder = request.getParameter("sorter/order");
      if (sorterField == null)
        sorterField = "name";
      if (sorterOrder == null)
        sorterOrder = "ASC";

      JspHelper.sortNodeList(live, sorterField, sorterOrder);
      JspHelper.sortNodeList(dead, "name", "ASC");

      // Find out common suffix. Should this be before or after the sort?
      String port_suffix = null;
      if (live.size() > 0) {
        String name = live.get(0).getName();
        int idx = name.indexOf(':');
        if (idx > 0) {
          port_suffix = name.substring(idx);
        }

        for (int i = 1; port_suffix != null && i < live.size(); i++) {
          if (live.get(i).getName().endsWith(port_suffix) == false) {
            port_suffix = null;
            break;
          }
        }
      }

      counterReset();

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }

      if (live.isEmpty() && dead.isEmpty()) {
        out.print("There are no datanodes in the cluster");
      } else {

        int nnHttpPort = nn.getHttpAddress().getPort();
        out.print("<div id=\"dfsnodetable\"> ");
        if (whatNodes.equals("LIVE")) {

          out.print("<a name=\"LiveNodes\" id=\"title\">" + "Live Datanodes : "
              + live.size() + "</a>"
              + "<br><br>\n<table border=1 cellspacing=0>\n");

          counterReset();

          if (live.size() > 0) {
            if (live.get(0).getCapacity() > 1024 * diskBytes) {
              diskBytes *= 1024;
              diskByteStr = "TB";
            }

            out.print("<tr class=\"headerRow\"> <th " + nodeHeaderStr("name")
                + "> Node <th " + nodeHeaderStr("lastcontact")
                + "> Last <br>Contact <th " + nodeHeaderStr("adminstate")
                + "> Admin State <th " + nodeHeaderStr("capacity")
                + "> Configured <br>Capacity (" + diskByteStr + ") <th "
                + nodeHeaderStr("used") + "> Used <br>(" + diskByteStr
                + ") <th " + nodeHeaderStr("nondfsused")
                + "> Non DFS <br>Used (" + diskByteStr + ") <th "
                + nodeHeaderStr("remaining") + "> Remaining <br>("
                + diskByteStr + ") <th " + nodeHeaderStr("pcused")
                + "> Used <br>(%) <th " + nodeHeaderStr("pcused")
                + "> Used <br>(%) <th " + nodeHeaderStr("pcremaining")
                + "> Remaining <br>(%) <th " + nodeHeaderStr("blocks")
                + "> Blocks\n");

            JspHelper.sortNodeList(live, sorterField, sorterOrder);
            for (int i = 0; i < live.size(); i++) {
              generateNodeData(out, live.get(i), port_suffix, true, nnHttpPort);
            }
          }
          out.print("</table>\n");
        } else {

          out.print("<br> <a name=\"DeadNodes\" id=\"title\"> "
              + " Dead Datanodes : " + dead.size() + "</a><br><br>\n");

          if (dead.size() > 0) {
            out.print("<table border=1 cellspacing=0> <tr id=\"row1\"> "
                + "<td> Node \n");

            JspHelper.sortNodeList(dead, "name", "ASC");
            for (int i = 0; i < dead.size(); i++) {
              generateNodeData(out, dead.get(i), port_suffix, false, nnHttpPort);
            }

            out.print("</table>\n");
          }
        }
        out.print("</div>");
      }
    }
  }
}
