/*
 *      Copyright 2009 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package com.google.code.sagetvaddons.sjq.client;

import com.google.code.gwtsrwc.client.BinaryRadioButton;
import com.google.code.gwtsrwc.client.ValidatedIntegerTextBox;
import com.google.code.gwtsrwc.client.ValidatedTextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

final class SettingsPanel extends VerticalPanel {
	static private SettingsPanel instance = null;
	static public SettingsPanel getInstance() {
		if(instance == null)
			instance = new SettingsPanel();
		return instance;
	}

	private VerticalPanel settingsBox;
	private ValidatedTextBox maxSleep, runDelay;
	private TextBox clntRestrictions, sageAlertUrl, sageAlertUser, sageAlertPwd;
	private BinaryRadioButton scanTv, scanMusic, scanVideos, scanDVDs, scanPics, debug, ignoreFailed, logTaskOutput;
	private ListBox runType, clearServerLogs, clearClientLogs, clearCompletedTaskLogs;
	private Button save;
	
	private SettingsPanel() {
		setSize("100%", "100%");
		setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		maxSleep = new ValidatedIntegerTextBox(2, 720);
		runDelay = new ValidatedIntegerTextBox(10, 1800);
		scanTv = new BinaryRadioButton("scanTv", "Yes", "No");
		scanMusic = new BinaryRadioButton("scanMusic", "Yes", "No");
		scanVideos = new BinaryRadioButton("scanVideos", "Yes", "No");
		scanDVDs = new BinaryRadioButton("scanDVDs", "Yes", "No");
		scanPics = new BinaryRadioButton("scanPics", "Yes", "No");
		debug = new BinaryRadioButton("debug", "Yes", "No");
		ignoreFailed = new BinaryRadioButton("ignoreFailed", "Yes", "No");
		logTaskOutput = new BinaryRadioButton("logTaskOutput", "Yes", "No");
		clearServerLogs = createLogSettings();
		clearClientLogs = createLogSettings();
		clearCompletedTaskLogs = createLogSettings();
		clntRestrictions = new TextBox();
		sageAlertUrl = new TextBox();
		sageAlertUser = new TextBox();
		sageAlertPwd = new PasswordTextBox();
		
		runType = new ListBox(true);
		runType.addItem("After end of each scheduled recording", "2");
		runType.addItem("After start of each scheduled recording", "1");
		runType.addItem("On an interval (every MaxSleep mins)", "0");
		
		settingsBox = new VerticalPanel();
		settingsBox.setSpacing(10);
		settingsBox.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		settingsBox.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		settingsBox.addStyleName("sjqLoginBox");
		Label lbl = new Label("SJQ Settings");
		lbl.addStyleName("sjqLoginBox-Label");
		settingsBox.add(lbl);
		
		settingsBox.add(createOptionPanel(maxSleep, "Maxiumum time MediaFileQueueLoader will sleep between runs (in minutes):"));
		settingsBox.add(createOptionPanel(runType, "The MediaFileQueueLoader thread should run:"));
		settingsBox.add(createOptionPanel(runDelay, "Amount of time the MediaFileQueueLoader will delay before each run (in seconds):"));
		settingsBox.add(createOptionPanel(ignoreFailed, "SJQ should ignore failed tasks (allows for automatic requeuing):"));
		settingsBox.add(createOptionPanel(scanTv, "Scan TV recordings:"));
		settingsBox.add(createOptionPanel(scanMusic, "Scan music files:"));
		settingsBox.add(createOptionPanel(scanVideos, "Scan imported videos:"));
		settingsBox.add(createOptionPanel(scanDVDs, "Scan DVDs:"));
		settingsBox.add(createOptionPanel(scanPics, "Scan pictures:"));
		settingsBox.add(createOptionPanel(logTaskOutput, "SJQ should log task output to the server:"));
		settingsBox.add(createOptionPanel(clearServerLogs, "Server logs should be purged:"));
		settingsBox.add(createOptionPanel(clearClientLogs, "Client logs should be purged:"));
		settingsBox.add(createOptionPanel(clearCompletedTaskLogs, "All task logs (completed and failed) should be purged:"));
		settingsBox.add(createOptionPanel(clntRestrictions, "Task client restrictions:"));
		settingsBox.add(createOptionPanel(sageAlertUrl, "Base URL for SageAlert (do not include trailing slash):"));
		settingsBox.add(createOptionPanel(sageAlertUser, "User name to connect to SageAlert:"));
		settingsBox.add(createOptionPanel(sageAlertPwd, "Password to connect to SageAlert:"));
		settingsBox.add(createOptionPanel(debug, "Run in debug mode:"));
		
		save = new Button("Save");
		save.setEnabled(false);
		save.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				saveValues();
			}			
		});
		settingsBox.add(save);
		
		loadValues();
		
		add(settingsBox);
	}
	
	private void saveValues() {
		final StatusPanel status = StatusPanel.getInstance();
		RequestBuilder req = new RequestBuilder(RequestBuilder.POST, AppState.getInstance().getCommandURL("writeSrvSettings"));
		req.setHeader("Content-Type", "application/x-www-form-urlencoded; UTF-8");
		try {
			req.sendRequest(encodeSettings(), new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					status.setMessage(exception.getLocalizedMessage(), StatusPanel.MessageType.ERROR);
				}

				public void onResponseReceived(Request request,	Response response) {
					save.setEnabled(false);
					status.setMessage("Settings saved to server!");
					loadValues();
				}
			});
		} catch(RequestException e) {
			status.setMessage(e.getLocalizedMessage(), StatusPanel.MessageType.ERROR);
		}
	}
	
	private void loadValues() {
		final StatusPanel status = StatusPanel.getInstance();
		RequestBuilder req = new RequestBuilder(RequestBuilder.GET, AppState.getInstance().getCommandURL("readSrvSettings"));
		try {
			req.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					status.setMessage(exception.getLocalizedMessage(), StatusPanel.MessageType.ERROR);
				}

				public void onResponseReceived(Request request,	Response response) {
					JSONObject o = JSONParser.parse(response.getText()).isObject();
					maxSleep.setText(o.get("MaxSleep").isString().stringValue());
					setMultiListBoxValue(runType, o.get("RunType").isString().stringValue());
					runDelay.setText(o.get("RunDelay").isString().stringValue());
					ignoreFailed.setState(Boolean.parseBoolean(o.get("IgnoreFailedTasks").isString().stringValue()));
					scanTv.setState(Boolean.parseBoolean(o.get("ScanTv").isString().stringValue()));
					scanMusic.setState(Boolean.parseBoolean(o.get("ScanMusic").isString().stringValue()));
					scanVideos.setState(Boolean.parseBoolean(o.get("ScanVideos").isString().stringValue()));
					scanDVDs.setState(Boolean.parseBoolean(o.get("ScanDVDs").isString().stringValue()));
					scanPics.setState(Boolean.parseBoolean(o.get("ScanPictures").isString().stringValue()));
					logTaskOutput.setState(!Boolean.parseBoolean(o.get("IgnoreTaskOutput").isString().stringValue()));
					setListBoxValue(clearServerLogs, o.get("ClearServerLogs").isString().stringValue());
					setListBoxValue(clearClientLogs, o.get("ClearClientLogs").isString().stringValue());
					setListBoxValue(clearCompletedTaskLogs, o.get("ClearCompletedTaskLogs").isString().stringValue());
					clntRestrictions.setText(o.get("ValidClients").isString().stringValue());
					debug.setState(Boolean.parseBoolean(o.get("Debug").isString().stringValue()));
					sageAlertUrl.setValue(o.get("SageAlertUrl").isString().stringValue());
					sageAlertUser.setValue(o.get("SageAlertUser").isString().stringValue());
					sageAlertPwd.setValue(o.get("SageAlertPwd").isString().stringValue());
					save.setEnabled(true);
				}
			});
		} catch(RequestException e) {
			status.setMessage(e.getLocalizedMessage(), StatusPanel.MessageType.ERROR);
		}
	}
	
	private String encodeSettings() {
		int runTypeVal = 0;
		for(int i = 0; i < runType.getItemCount(); ++i)
			if(runType.isItemSelected(i))
				runTypeVal += Integer.parseInt(runType.getValue(i));
		
		JSONObject o = new JSONObject();
		o.put("MaxSleep", new JSONString(maxSleep.getText()));
		o.put("RunType", new JSONString(Integer.toString(runTypeVal)));
		o.put("RunDelay", new JSONString(runDelay.getText()));
		o.put("IgnoreFailedTasks", new JSONString(Boolean.toString(ignoreFailed.isOn())));
		o.put("ScanTv", new JSONString(Boolean.toString(scanTv.isOn())));
		o.put("ScanMusic", new JSONString(Boolean.toString(scanMusic.isOn())));
		o.put("ScanVideos", new JSONString(Boolean.toString(scanVideos.isOn())));
		o.put("ScanDVDs", new JSONString(Boolean.toString(scanDVDs.isOn())));
		o.put("ScanPictures", new JSONString(Boolean.toString(scanPics.isOn())));
		o.put("IgnoreTaskOutput", new JSONString(Boolean.toString(!logTaskOutput.isOn())));
		o.put("Debug", new JSONString(Boolean.toString(debug.isOn())));
		o.put("ClearServerLogs", new JSONString(clearServerLogs.getValue(clearServerLogs.getSelectedIndex())));
		o.put("ClearClientLogs", new JSONString(clearClientLogs.getValue(clearClientLogs.getSelectedIndex())));
		o.put("ClearCompletedTaskLogs", new JSONString(clearCompletedTaskLogs.getValue(clearCompletedTaskLogs.getSelectedIndex())));
		o.put("ValidClients", new JSONString(clntRestrictions.getText()));
		o.put("SageAlertUrl", new JSONString(sageAlertUrl.getValue()));
		o.put("SageAlertUser", new JSONString(sageAlertUser.getValue()));
		o.put("SageAlertPwd", new JSONString(sageAlertPwd.getValue()));
		return "data=" + URL.encodeComponent(o.toString());
	}
	
	static private void setListBoxValue(ListBox b, String val) {
		for(int i = 0; i < b.getItemCount(); ++i) {
			if(b.getValue(i).equals(val)) {
				b.setSelectedIndex(i);
				break;
			}
		}
	}
	
	static private void setMultiListBoxValue(ListBox b, String val) {
		int iVal = Integer.parseInt(val);
		for(int i = 0; i < b.getItemCount(); ++i) {
			int item = Integer.parseInt(b.getValue(i));
			if((item & iVal) == item)
				b.setItemSelected(i, true);
		}
	}
	
	static private HorizontalPanel createOptionPanel(Widget w, String desc) {
		HorizontalPanel p = new HorizontalPanel();
		p.setSize("100%", "100%");
		p.setSpacing(8);
		
		Label lbl = new Label(desc);
		
		p.add(lbl);
		p.add(w);
		
		p.setCellWidth(lbl, "50%");
		p.setCellWidth(w, "50%");
		
		p.setCellHorizontalAlignment(lbl, HasHorizontalAlignment.ALIGN_RIGHT);
		
		return p;
	}
	
	static private ListBox createLogSettings() {
		ListBox b = new ListBox();
		b.addItem("Daily");
		b.addItem("Weekly");
		b.addItem("Monthly");
		return b;
	}
}

