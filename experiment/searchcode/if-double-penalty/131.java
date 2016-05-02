package net.rfactor.racecontrol.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import net.rfactor.chat.server.ChatService;
import net.rfactor.livescoring.client.endurance.BackupService;
import net.rfactor.livescoring.client.endurance.EnduranceScoring;
import net.rfactor.livescoring.client.endurance.Penalty;
import net.rfactor.livescoring.client.endurance.Penalty.Type;
import net.rfactor.livescoring.client.endurance.Track;
import net.rfactor.livescoring.client.endurance.Track.RaceMode;
import net.rfactor.livescoring.client.endurance.Vehicle;
import net.rfactor.serverlog.ServerLog;
import net.rfactor.trackview.TrackViewerConstants;
import net.rfactor.util.TimeUtil;

import org.apache.felix.dm.Component;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogService;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.Action;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class VaadinClient extends com.vaadin.Application {
    private static final int LAST_POSITION = 256;

    private static final long serialVersionUID = 1L;

    private volatile DependencyManager m_manager;
    private volatile BundleContext m_context;
    private volatile LogService m_log;
    private volatile EnduranceScoring m_enduranceScoring;
    private volatile ChatService m_chat;
    private volatile ConfigurationAdmin m_configAdmin;
    private volatile BackupService m_backupService;
    private volatile ServerLog m_serverLog;

    private static final String m_User_Admin = "admin";
    private static final String m_Pass_Admin = "24legend";
    private static final String m_User = "rc";
    private static final String m_Pass = "legend";
    private boolean m_isAdmin = false;
    
    private Window m_mainWindow;
	private final AtomicBoolean m_dependenciesResolved = new AtomicBoolean();

	private final Object COL_TIME = "Time";
	private final Object COL_DESC = "Description";
	private final Object COL_CAR = "Car";
	private final Object COL_CLASS = "Class";
	private final Object COL_UNTIL = "To resolve until";
	private final Object COL_REASON = "Reason";
	private final Object COL_RESOLVED = "Resolved";
	private final Object COL_RC = "RC Info";
	private final Object COL_PENALTY = "Penalty";
	private final Object COL_VEHICLE = "Vehicle";
	
	static final Action ACTION_NONE = new Action("No action");
	static final Action ACTION_CLEAR = new Action("Clear penalty");
	
	static final Action[] ACTIONS_CLEAR = new Action[] { ACTION_CLEAR };
	static final Action[] ACTIONS_NONE = new Action[] { };

	private MenuItem m_eventMenu;
	
    public void setupDependencies(Component component) {
    }
    
    public void start() {
//        System.out.println("Starting");
        m_dependenciesResolved.set(true);
    }
    
    public void stop() {
        m_dependenciesResolved.set(false);
    }
    
    public void destroyDependencies() {
    }
    
    public void init() {
        setTheme("rc");
        if (!m_dependenciesResolved.get()) {
            final Window message = new Window("Race Control");
            setMainWindow(message);
            message.getContent().setSizeFull();
            Label richText = new Label(
                "<h1>Race Control User Interface</h1>" +
                "<p>Due to missing component dependencies on the server, probably due to misconfiguration, " +
                "the user interface cannot be properly started. Please contact your server administrator. " +
                "You can retry accessing the user interface by <a href=\"?restartApplication\">following this link</a>.</p>"
            );
            richText.setContentMode(Label.CONTENT_XHTML);
            message.addComponent(richText);
            return;
        }
        
        m_mainWindow = new Window("Race Control");
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
		m_mainWindow.setContent(layout);
        setMainWindow(m_mainWindow);
        MenuBar menuBar = new MenuBar();
        menuBar.setSizeUndefined();
        menuBar.setWidth("100%");
        MenuItem systemItem = menuBar.addItem("System", null);
        
        MenuItem linksItem = systemItem.addItem("Links & Utils", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
		        // Create the window
		        final Window subwindow = new Window("Links & Utils");
		        subwindow.setWidth("30em");
		        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
		        layout.setMargin(true);
		        layout.setSpacing(true);
		        layout.addComponent(new Label("Links to external utilities"));
		        layout.addComponent(new Label(""));
		        
		        Link link = new Link("Liveticker", new ExternalResource("/score/ticker.html"));
		        link.setTargetName("_blank");
		        layout.addComponent(link);
		        
		        link = new Link("Broadcast scoring", new ExternalResource("/score/overlay"));
		        link.setTargetName("_blank");
		        layout.addComponent(link);
		        
		        Button btnClose = new Button("Close", new Button.ClickListener() {
		            public void buttonClick(ClickEvent event) {
	            		// all was well, we close the window
	            		(subwindow.getParent()).removeWindow(subwindow);
		            }
		        });
		        		        
		        layout.addComponent(btnClose);
		        layout.setComponentAlignment(btnClose, Alignment.MIDDLE_LEFT);
		        
		        getMainWindow().addWindow(subwindow);
		        subwindow.center();
			}
		});
        
        MenuItem aboutItem = systemItem.addItem("About", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
		        // Create the window
		        final Window subwindow = new Window("About");
		        subwindow.setWidth("40em");
		        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
		        layout.setMargin(true);
		        layout.setSpacing(true);
		        Label message = new Label("<h1>Race Control Software</h1><p>Still under development, so be gentle!</p>");
		        message.setContentMode(Label.CONTENT_XHTML);
		        subwindow.addComponent(message);
		        Button close = new Button("Close", new Button.ClickListener() {
		            public void buttonClick(ClickEvent event) {
		                (subwindow.getParent()).removeWindow(subwindow);
		            }
		        });
		        layout.addComponent(close);
		        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
		        getMainWindow().addWindow(subwindow);
		        subwindow.center();
			}});
        
        MenuItem logoutItem = systemItem.addItem("Logout", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
				close();
			}});
        
	        m_eventMenu = menuBar.addItem("Event", null);
	        
	        MenuItem rfactorDirItem = m_eventMenu.addItem("rFactor Folder", new Command() {
	            @Override
	            public void menuSelected(MenuItem selectedItem) {
	                // Create the window
	                final Window subwindow = new Window("rFactor Folder");
	                subwindow.setWidth("40em");
	                final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
	                layout.setMargin(true);
	                layout.setSpacing(true);
	
	                Dictionary tvconfig = getConfig(TrackViewerConstants.TRACKVIEWER_PID);
	                String oldFolder = (tvconfig == null) ? null : (String) tvconfig.get(TrackViewerConstants.RFACTORFOLDER_KEY);
	                final TextField rFactorFolderField = new TextField("Folder where rFactor is installed", (oldFolder == null ? "" : oldFolder));
	                rFactorFolderField.setWidth("30em");
	                layout.addComponent(rFactorFolderField);
	                
	                Button close = new Button("Confirm", new Button.ClickListener() {
	                    public void buttonClick(ClickEvent event) {
	                        String folder = (String) rFactorFolderField.getValue();
	                        if (folder != null && (new File(folder)).isDirectory() && (new File(folder, "rFactor.exe")).isFile()) {
	                            Dictionary tvconfig = new Properties();
	                            tvconfig.put(TrackViewerConstants.RFACTORFOLDER_KEY, folder);
	                            try {
	                                setConfig(TrackViewerConstants.TRACKVIEWER_PID, tvconfig);
	                            }
	                            catch (IOException e) {
	                                e.printStackTrace();
	                            }
	                            (subwindow.getParent()).removeWindow(subwindow);
	                        }
	                        else {
	                            m_mainWindow.showNotification(
	                                "Invalid folder<br>",
	                                "The folder you entered, is not a folder containing an<br>" +
	                                "rFactor.exe file. Please select a valid folder.",
	                                Notification.TYPE_ERROR_MESSAGE);
	                        }
	                    }
	                });
	                layout.addComponent(close);
	                layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
	                getMainWindow().addWindow(subwindow);
	                subwindow.center();
	            }
	        });        
	        
	        MenuItem configureItem = m_eventMenu.addItem("Configure", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Configure Event");
			        subwindow.setWidth("40em");
			        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        
			        Dictionary chatConfig = getConfig(ChatService.CHATSERVICE_PID);
			        Dictionary scoringConfig = getConfig(EnduranceScoring.SCORINGSERVICE_PID);
			        
			        Object serverNameValue = chatConfig == null ? "" : chatConfig.get(ChatService.SERVERNAME_KEY);
			        
			        Object eventLaps = scoringConfig == null ? "9" : scoringConfig.get(EnduranceScoring.EVENTLAPS_KEY);
			        Object eventTime = scoringConfig == null ? "0" : scoringConfig.get(EnduranceScoring.EVENTTIME_KEY);
			        Object pitSpeed = scoringConfig == null ? "100" : scoringConfig.get(EnduranceScoring.PITSPEED_KEY);
			        Object internalScoring = scoringConfig == null ? "true" : scoringConfig.get(EnduranceScoring.INTERNALSCORING_KEY);
			        
			        RaceMode raceMode = Track.RaceMode.LAPS;
			        Integer laps = 0;
			        Double minutes = 0.0;
			        
			        Track track = m_enduranceScoring.getTrack();
			        if (track != null) {
				        raceMode = track.getRaceMode();
				        laps = m_enduranceScoring.getLaps();
				        minutes = m_enduranceScoring.getTime() / 60.0;
			        }
			        
			        if (eventLaps instanceof String) {
			            if (eventTime instanceof String) {
			                raceMode = RaceMode.LAPS_TIME;
			                minutes = Double.parseDouble((String) eventTime) / 60.0; 
			            }
			            else {
			                raceMode = RaceMode.LAPS;
			            }
	                    laps = Integer.parseInt((String) eventLaps); 
			        }
			        else {
			            raceMode = RaceMode.TIME;
	                    minutes = Double.parseDouble((String) eventTime) / 60.0; 
			        }
			        
			        List<String> types = Arrays.asList(new String[] {"Laps", "Minutes", "Laps/Minutes"});
			        final OptionGroup eventType = new OptionGroup("Select event type", types);
			        eventType.setValue((raceMode == RaceMode.LAPS ? "Laps" : (raceMode == RaceMode.LAPS_TIME ? "Laps/Minutes" : "Minutes")));
			        eventType.setMultiSelect(false);
			        subwindow.addComponent(eventType);
			        
			        final TextField amountLaps = new TextField("Laps:", (raceMode != RaceMode.TIME ? laps.toString() : ""));
			        subwindow.addComponent(amountLaps);
			        
			        final TextField amountMinutes = new TextField("Minutes:", (raceMode != RaceMode.LAPS ? minutes.toString() : ""));
			        subwindow.addComponent(amountMinutes);
			        
			        final TextField amountPitlane = new TextField("Pitlane speed [km/h]:", (pitSpeed instanceof String ? (String) pitSpeed : ""));
			        subwindow.addComponent(amountPitlane);
			        
			        final TextField serverName = new TextField("Server:", (serverNameValue instanceof String) ? (String) serverNameValue : "");
			        subwindow.addComponent(serverName);
			        
			        final CheckBox internalScoringCheckBox = new CheckBox("Use internal scoring?", (internalScoring instanceof String) ? Boolean.parseBoolean(internalScoring.toString()) : true);
			        subwindow.addComponent(internalScoringCheckBox);
			        
			        Button close = new Button("Confirm", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			            	String selectedEventType = (String) eventType.getValue();
			            	Integer laps = 0;
		            		Double seconds = 0.0;
		            		Integer pitSpeed = 0;
		            		String server = "";
		            		RaceMode raceMode = RaceMode.LAPS;
		            		String internalScoring = "true";
		            		
			            	try {
			            		laps = Integer.parseInt((String) "0" + amountLaps.getValue());
			            	}
			            	catch (Exception e) {}
			            	try {
			            		seconds = Double.parseDouble((String) "0" + amountMinutes.getValue()) * 60.0;
			            	}
			            	catch (Exception e) {}
			            	try {
			            		pitSpeed = Integer.parseInt((String) "0" + amountPitlane.getValue());
			            	}
			            	catch(Exception e) {}
			            	
			            	server = (String) serverName.getValue();
			            	server = server.trim();
			            	
			            	internalScoring = ((Boolean) internalScoringCheckBox.getValue()).toString();
			            	
		            		if (pitSpeed < 30 || pitSpeed > 200) {
								m_mainWindow.showNotification(
				                        "Configure event<br>",
				                        "You must enter a number into the pitlane speed field<br>" +
				                        "which is greater than 30km/h and smaller than 200km/h!<br>" +
				                        "Do NOT append any unit (like km/h or so)!",
				                        Notification.TYPE_ERROR_MESSAGE);
								return;
		            		}
		            		//System.out.println("Pitspeed: " + pitSpeed);
		            		
			            	
		            		if (server.isEmpty()) {
								m_mainWindow.showNotification(
				                        "Configure event<br>",
				                        "The server name is missing.<br>" +
				                        "Make sure that you use the correct name,<br>" +
				                        "Otherwise, the chat messages will not receive the drivers!",
				                        Notification.TYPE_ERROR_MESSAGE);
								return;
		            		}
		            		//System.out.println("Server: " + server);
	
		            		if ("Laps".equals(selectedEventType)) {
			            		if (laps < 1) {
									m_mainWindow.showNotification(
					                        "Configure event<br>",
					                        "You must enter a number into the lap field which is greater than 0!",
					                        Notification.TYPE_ERROR_MESSAGE);
									return;
			            		}
			            		raceMode = RaceMode.LAPS;
			            	}
			            	else if ("Minutes".equals(selectedEventType)) {
			            		if (seconds < 60.0) {
									m_mainWindow.showNotification(
					                        "Configure event<br>",
					                        "You must enter a number into the minutes field which is greater or equal to 1!",
					                        Notification.TYPE_ERROR_MESSAGE);
									return;
			            		}
			            		raceMode = RaceMode.TIME;
			            	}
			            	else if ("Laps/Minutes".equals(selectedEventType)) {
			            		if (seconds < 60.0) {
									m_mainWindow.showNotification(
					                        "Configure event<br>",
					                        "You must enter a number into the minutes field which is greater or equal to 1!",
					                        Notification.TYPE_ERROR_MESSAGE);
									return;
			            		}
			            		if (laps < 1) {
									m_mainWindow.showNotification(
					                        "Configure event<br>",
					                        "You must enter a number into the lap field which is greater than 0!",
					                        Notification.TYPE_ERROR_MESSAGE);
									return;
			            		}
			            		raceMode = RaceMode.LAPS_TIME;
			            	}
			            	Dictionary chatConfig = new Properties();
			            	chatConfig.put(ChatService.SERVERNAME_KEY, server);
			            	try {
	                            setConfig(ChatService.CHATSERVICE_PID, chatConfig);
	                        }
	                        catch (IOException e) {
	                            e.printStackTrace();
	                        }
			            	
	                        Dictionary oldScoringConfig = getConfig(EnduranceScoring.SCORINGSERVICE_PID);
	                        Dictionary scoringConfig = new Properties();
	                        switch (raceMode) {
	                            case LAPS:
	                                scoringConfig.put(EnduranceScoring.EVENTLAPS_KEY, "" + laps);
	                                break;
	                            case TIME:
	                                scoringConfig.put(EnduranceScoring.EVENTTIME_KEY, "" + seconds);
	                                break;
	                            case LAPS_TIME:
	                                scoringConfig.put(EnduranceScoring.EVENTLAPS_KEY, "" + laps);
	                                scoringConfig.put(EnduranceScoring.EVENTTIME_KEY, "" + seconds);
	                                break;
	                        }
	                        scoringConfig.put(EnduranceScoring.PITSPEED_KEY, "" + pitSpeed);
	                        scoringConfig.put(EnduranceScoring.INTERNALSCORING_KEY, internalScoring);
	                        if (oldScoringConfig != null) {
	                            String classes = (String) oldScoringConfig.get(EnduranceScoring.CLASSES_KEY);
	                            String drivers = (String) oldScoringConfig.get(EnduranceScoring.DRIVERS_KEY);
	                            scoringConfig.put(EnduranceScoring.CLASSES_KEY, classes == null ? "" : classes);
	                            scoringConfig.put(EnduranceScoring.DRIVERS_KEY, drivers == null ? "" : drivers);
	                        }
	                        try {
	                            setConfig(EnduranceScoring.SCORINGSERVICE_PID, scoringConfig);
	                        }
	                        catch (IOException e) {
	                            e.printStackTrace();
	                        }
			                (subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        layout.addComponent(close);
			        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
				}
	
			});
	        
	        MenuItem chatItem = m_eventMenu.addItem("Chat enable/disable", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Chat enable/disable");
			        subwindow.setWidth("40em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
	
			        
			        List<String> types = Arrays.asList(new String[] {"No", "Yes"});
			        final OptionGroup chatPause = new OptionGroup("Enable chat", types);
			        chatPause.setValue((m_chat.getPause()? "No" : "Yes"));
			        chatPause.setMultiSelect(false);
			        layout.addComponent(chatPause);
			        layout.addComponent(new Label("Disabling the chatsystem will prevent sending any messages to the drivers"));
			        layout.addComponent(new Label("Do not forget to re-enable the system!!!"));
			        
			        Button close = new Button("Confirm", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			            	boolean selectedChatPause = "No".equals((String)chatPause.getValue());
			            	m_chat.setPause(selectedChatPause);
			            	
							m_mainWindow.showNotification(
			                        "Chat enable/disable<br>",
			                        "Chat "+(selectedChatPause ? "disabled" : "enabled"),
			                        Notification.POSITION_BOTTOM_RIGHT);
//		            		System.out.println("Chat "+(selectedChatPause ? "disabled" : "enabled"));
			                (subwindow.getParent()).removeWindow(subwindow);
			            }
			        }
			        );
			        layout.addComponent(close);
			        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
				}
			});        
	        
	        MenuItem vehicleClassesItem = m_eventMenu.addItem("Vehicle classes", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Setup vehicle classes");
			        subwindow.setWidth("40em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
	
			        String classesString = "";
			        for (String s : m_enduranceScoring.getParticipatingClasses()){ 
			        	classesString += s + "\r\n";
			        }
		        	final TextArea classesArea = new TextArea("Participating vehicle classes:", classesString);
		        	classesArea.setRows(6);
		        	classesArea.setWidth("35em");
		        	layout.addComponent(classesArea);
		        	
			        layout.addComponent(new Label("Enter the participating vehicle classes one per row."));
			        layout.addComponent(new Label("Only classes listed here will be recognized in the scoring tables."));
			        layout.addComponent(new Label("Leave empty to accept all classes on the server."));
			        
			        Button btnClose = new Button("Confirm", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			            	List<String> classesList = new ArrayList<String>();
			            	StringBuffer classes = new StringBuffer();
			            	
			            	// now try to parse the textarea
			            	String data = (String) classesArea.getValue();
			            	StringTokenizer st = new StringTokenizer(data, "\n");
			            	while (st.hasMoreTokens()) {
			            		String line = st.nextToken();
			            		line = line.trim().toUpperCase(); // strip leading and trailing whitespace
			            		if (line != "" && !classesList.contains(line)) {
		            				classesList.add(line);
		            				if (classes.length() > 0) {
		            				    classes.append(";");
		            				}
		            				classes.append(line);
			            		}
			            	}
			            	// Display a message
			            	if (classesList.size() > 0) {
					        	m_mainWindow.showNotification(
				                        "Notification<br>",
				                        "Set " + classesList.size() + " participating class(es)",
				                        Notification.POSITION_CENTERED);
			            	}
			            	else if (classesList.size() == 0) {
					        	m_mainWindow.showNotification(
				                        "Notification<br>",
				                        "Cleared participating classes",
				                        Notification.POSITION_CENTERED);
			            	}
			            	
	                        Dictionary scoringConfig = getConfig(EnduranceScoring.SCORINGSERVICE_PID);
	                        if (scoringConfig == null) {
	                            scoringConfig = new Properties();
	                        }
	                        scoringConfig.put(EnduranceScoring.CLASSES_KEY, classes.toString());
	                        try {
	                            setConfig(EnduranceScoring.SCORINGSERVICE_PID, scoringConfig);
	                        }
	                        catch (IOException e) {
	                            e.printStackTrace();
	                        }
			            	
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        
			        layout.addComponent(btnClose);
			        layout.setComponentAlignment(btnClose, Alignment.TOP_RIGHT);
	
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
				}
			});
	        
	        MenuItem driversItem = m_eventMenu.addItem("Drivers", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Setup participating drivers");
			        subwindow.setWidth("40em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
	
			        String driversString = "";
			        for (String s : m_enduranceScoring.getParticipatingDrivers()){ 
			        	driversString += s + "\r\n";
			        }
		        	final TextArea driversArea = new TextArea("Participating drivers:", driversString);
		        	driversArea.setRows(6);
		        	driversArea.setWidth("35em");
		        	layout.addComponent(driversArea);
		        	
			        layout.addComponent(new Label("Enter the participating drivers one per row."));
			        layout.addComponent(new Label("Only drivers listed here will be allowed to join the server. Others will be automatically kicked or if they keep trying to connect they will get banned!"));
			        layout.addComponent(new Label("Leave empty to accept all drivers on the server."));
			        
			        Button btnClose = new Button("Confirm", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			            	List<String> driversList = new ArrayList<String>(); 
			            	StringBuffer drivers = new StringBuffer();
			            	
			            	// now try to parse the textarea
			            	String data = (String) driversArea.getValue();
			            	StringTokenizer st = new StringTokenizer(data, "\n");
			            	while (st.hasMoreTokens()) {
			            		String line = st.nextToken();
			            		line = line.trim().toUpperCase(); // strip leading and trailing whitespace
			            		if (line != "" && !driversList.contains(line)) {
			            			driversList.add(line);
	                                if (drivers.length() > 0) {
	                                    drivers.append(";");
	                                }
	                                drivers.append(line);
			            		}
			            	}
			            	// Display a message
			            	if (driversList.size() > 0) {
					        	m_mainWindow.showNotification(
				                        "Notification<br>",
				                        "Set " + driversList.size() + " participating drivers",
				                        Notification.POSITION_CENTERED);
			            	}
			            	else if (driversList.size() == 0) {
					        	m_mainWindow.showNotification(
				                        "Notification<br>",
				                        "Cleared participating drivers",
				                        Notification.POSITION_CENTERED);
			            	}
			            	
	                        Dictionary scoringConfig = getConfig(EnduranceScoring.SCORINGSERVICE_PID);
	                        if (scoringConfig == null) {
	                            scoringConfig = new Properties();
	                        }
	                        scoringConfig.put(EnduranceScoring.DRIVERS_KEY, drivers.toString());
	                        try {
	                            setConfig(EnduranceScoring.SCORINGSERVICE_PID, scoringConfig);
	                        }
	                        catch (IOException e) {
	                            e.printStackTrace();
	                        }
			            	
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        
			        layout.addComponent(btnClose);
			        layout.setComponentAlignment(btnClose, Alignment.TOP_RIGHT);
	
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
				}
			});
	             
	        MenuItem gridItem = m_eventMenu.addItem("Grid", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Set Starting Grid");
			        subwindow.setWidth("40em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        
			        final SortedSet<Vehicle> rankedVehicles = m_enduranceScoring.getRankedVehicles();
			        
			        if (rankedVehicles != null) {
				        for (Vehicle v : rankedVehicles) {
				        	TextField vehiclePosition = new TextField(v.getVehicleName());
				        	vehiclePosition.setValue("" + v.getQualifyPosition());
				        	layout.addComponent(vehiclePosition);
				        }
				        
				        Button close = new Button("Confirm", new Button.ClickListener() {
				            public void buttonClick(ClickEvent event) {
				            	int index = 0;
				            	for (Vehicle v : rankedVehicles) {
				            		TextField vehiclePosition = (TextField) layout.getComponent(index++);
				            		v.setQualifyPosition(Integer.parseInt((String) vehiclePosition.getValue()));
				            	}
				                (subwindow.getParent()).removeWindow(subwindow);
				            }
				        });
				        layout.addComponent(close);
				        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
				        getMainWindow().addWindow(subwindow);
				        subwindow.center();
			        }
			        else {
			        	m_mainWindow.showNotification(
	                        "Notification<br>",
	                        "No cars on track, cannot set starting grid",
	                        Notification.TYPE_TRAY_NOTIFICATION);
			        }
				}
			});
	        
	        MenuItem gridBatchItem = m_eventMenu.addItem("Grid Batch", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Set Starting Grid");
			        subwindow.setWidth("40em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        
			        final SortedSet<Vehicle> rankedVehicles = m_enduranceScoring.getRankedVehicles();
			        
			        
			        if (rankedVehicles != null) {
			        	final TextArea batchArea = new TextArea("Paste the contents of the qualifying batch file here:");
			        	batchArea.setWidth("35em");
			        	batchArea.setHeight("10em");
			        	layout.addComponent(batchArea);
			        	final TextArea responseArea = new TextArea("Log");
			        	responseArea.setWidth("35em");
			        	responseArea.setHeight("20em");
			        	layout.addComponent(responseArea);
				        
				        Button close = new Button("Save", new Button.ClickListener() {
				            public void buttonClick(ClickEvent event) {
				            	StringBuffer response = new StringBuffer();
				            	StringBuffer response2 = new StringBuffer();
				            	// now try to parse the textarea
				            	String data = (String) batchArea.getValue();
				            	StringTokenizer st = new StringTokenizer(data, "\n");
				            	int pos = 0;
				            	// first set all positions to the end
				            	for (Vehicle v : rankedVehicles) {
				            		v.setQualifyPosition(LAST_POSITION);
				            	}
				            	// now parse the submitted batch file 
				            	while (st.hasMoreTokens()) {
				            		String line = st.nextToken();
				            		line = line.trim(); // strip leading and trailing whitespace
				            		if (line.startsWith("/editgrid")) {
				            			int sp1 = line.indexOf(' '); // index of first space
				            			int sp2 = line.indexOf(' ', sp1 + 1); // index of second space
				            			//int pos = Integer.parseInt(line.substring(sp1 + 1, sp2));
				            			String car = line.substring(sp2 + 1).trim().toUpperCase();
				            			boolean foundCar = false;
						            	for (Vehicle v : rankedVehicles) {
						            		String veh = v.getDriver().toUpperCase();
						            		if (veh.equals(car)) {
						            			pos++;
						            			v.setQualifyPosition(pos);
						            			foundCar = true;
							            		response2.append("P" + pos + "  " + v.getDriver() + " (" + v.getVehicleName() + ")\n");
						            			break;
						            		}
						            	}
						            	if (!foundCar) {
						            		response.append("Not found: " + car + "\n");
						            	}
				            		}
				            	}
				            	// if the response is not empty, there were problems parsing the file
				            	if (response.length() > 0) {
				            		response.delete(0, response.length());
				            		if (response2.length() > 0) response = response2;
				            		response.append("--- Moving unknown drivers to the end of the field --- \n");
					            	// now update qualifying position of the 
				            		// "unknown" cars to the end of the list
					            	for (Vehicle v : rankedVehicles) {
					            		if (v.getQualifyPosition() == LAST_POSITION) {
					            			pos++;
					            			v.setQualifyPosition(pos);
					            			response.append("P" + pos + "  " + v.getDriver() + " (" + v.getVehicleName() + ")\n");
					            		}
					            	}
				            		response.append("--- Grid setup complete --- \n");
				            		response.append("--- You can close this window now! --- \n");
				            		// and we show those problems and keep the window open so the user
				            		// can try again
				            		responseArea.setValue(response.toString());
			                        m_serverLog.log(-1, "GridBatchLoaded", 
			                            "problems", "true"
		                            );
				            	}
				            	else if (pos == 0){
				            		response.append("The pasted contents seam not to be \n");
				            		response.append("in the rFactor batchfile format! \n");
				            		response.append("Grid NOT set! \n");
				            		response.append(" \nPlease check ... \n");
				            		responseArea.setValue(response.toString());
				            	}
				            	else {
				            		// all was well, we close the window
				            		(subwindow.getParent()).removeWindow(subwindow);
                                    m_serverLog.log(-1, "GridBatchLoaded", 
                                        "problems", "false"
                                    );
				            	}
				            }
				        });
				        layout.addComponent(close);
				        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
				        getMainWindow().addWindow(subwindow);
				        subwindow.center();
			        }
			        else {
			        	m_mainWindow.showNotification(
	                        "Notification<br>",
	                        "No cars on track, cannot set starting grid",
	                        Notification.TYPE_TRAY_NOTIFICATION);
			        }
				}
			});
	        
	        MenuItem scoringBatchItem = m_eventMenu.addItem("Scoring Batch", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Set scoring");
			        subwindow.setWidth("40em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        
			        final SortedSet<Vehicle> rankedVehicles = m_enduranceScoring.getRankedVehicles();
			        
			        
			        if (rankedVehicles != null) {
			        	final TextArea batchArea = new TextArea("Paste the contents of the scoring backup file here:");
			        	batchArea.setWidth("35em");
			        	batchArea.setHeight("10em");
			        	layout.addComponent(batchArea);
			        	final TextArea responseArea = new TextArea("Log");
			        	responseArea.setWidth("35em");
			        	responseArea.setHeight("20em");
			        	layout.addComponent(responseArea);
				        
				        Button close = new Button("Save", new Button.ClickListener() {
				            public void buttonClick(ClickEvent event) {
				            	StringBuffer response = new StringBuffer();
				            	StringBuffer response2 = new StringBuffer();
				            	// now try to parse the textarea
				            	String data = (String) batchArea.getValue();
				            	StringTokenizer st = new StringTokenizer(data, "\n");
				            	int pos = 0;
				            	// first set all positions to the end
				            	for (Vehicle v : rankedVehicles) {
				            		v.setQualifyPosition(LAST_POSITION);
				            	}
				            	// now parse the submitted batch file 
				            	while (st.hasMoreTokens()) {
				            		String line = st.nextToken();
				            		line = line.trim(); // strip leading and trailing whitespace
				            		if (!line.startsWith("Pos;ClassPos")) {
				            			//Pos;ClassPos;Vehicle;LapsCompleted;TimeBehind;LapPos
				            			String[] items = line.split(";");
				            			//int posTotal = Integer.parseInt(items[0]);
				            			//int posClass = Integer.parseInt(items[1]);
				            			String veh = items[2].trim();
				            			int laps = Integer.parseInt(items[3]);
				            			int lappos = Integer.parseInt(items[5]);
				            			
				            			boolean foundCar = false;
						            	for (Vehicle v : rankedVehicles) {
						            		String veh2 = v.getVehicleName();
						            		if (veh2.equals(veh)) {
						            			pos++;
						            			//v.setRank(posTotal);
						            			v.setQualifyPosition(lappos);
						            			v.setLapsCompleted(laps);
						            			foundCar = true;
							            		response2.append("P" + lappos + " " + v.getVehicleName() + " (Laps: " + v.getLapsCompleted() + ")\n");
						            			break;
						            		}
						            	}
						            	if (!foundCar) {
                                            pos++;
						            	    Vehicle v = m_enduranceScoring.createVehicle(veh);
						            	    v.setQualifyPosition(lappos);
						            	    v.setLapsCompleted(laps);
                                            response.append("P" + lappos + " " + v.getVehicleName() + " (Laps: " + v.getLapsCompleted() + ") Not on server now!\n");
						            	}
				            		}
				            	}
				            	// if the response is not empty, there were problems parsing the file
				            	if (response.length() > 0) {
				            		response.delete(0, response.length());
				            		if (response2.length() > 0) response = response2;
				            		response.append("--- Moving unknown vehicles to the end of the field --- \n");
					            	// now update qualifying position of the 
				            		// "unknown" cars to the end of the list
					            	for (Vehicle v : rankedVehicles) {
					            		if (v.getQualifyPosition() == LAST_POSITION) {
					            			pos++;
					            			v.setQualifyPosition(pos);
					            			response.append("P" + pos + "  " + v.getVehicleName() + " (Laps: " + v.getLapsCompleted() + ") Unknown vehicle!\n");
					            		}
					            	}
				            		response.append("--- Scoring setup complete --- \n");
				            		response.append("--- You can close this window now! --- \n");
				            		// and we show those problems and keep the window open so the user
				            		// can try again
				            		responseArea.setValue(response.toString());
				            	}
				            	else if (pos == 0) {
				            		response.append("The pasted contents is not in\n");
				            		response.append("the correct scoring backup format!\n");
				            		response.append("Scoring NOT set!\n");
				            		response.append("\nPlease check...\n");
				            		responseArea.setValue(response.toString());
				            	}
				            	else {
				            		// all was well, we close the window
				            		(subwindow.getParent()).removeWindow(subwindow);
				            	}
				            }
				        });
				        layout.addComponent(close);
				        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
				        getMainWindow().addWindow(subwindow);
				        subwindow.center();
			        }
			        else {
			        	m_mainWindow.showNotification(
	                        "Notification<br>",
	                        "No cars on track, cannot set starting grid",
	                        Notification.TYPE_TRAY_NOTIFICATION);
			        }
				}
			});
	        
	        MenuItem resetItem = m_eventMenu.addItem("Reset", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Reset?");
			        subwindow.setWidth("20em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        layout.addComponent(new Label("By clicking on Reset, all scoring data will be reset!"));
			        layout.addComponent(new Label(""));
			        layout.addComponent(new Label("Do you really want to reset?"));
			        
			        HorizontalLayout buttons = new HorizontalLayout();
			        //buttons.setMargin(true);
			        buttons.setSpacing(true);
			        
			        Button btnCancel = new Button("Cancel", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        
			        Button btnReset = new Button("Reset", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
							m_enduranceScoring.reset();
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        		        
			        buttons.addComponent(btnCancel);
			        buttons.setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT);
			        
			        buttons.addComponent(btnReset);
			        buttons.setComponentAlignment(btnReset, Alignment.MIDDLE_RIGHT);
			        
			        layout.addComponent(buttons);
			        layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
			        
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
					
				}
			});
	        
	        MenuItem goingGreenItem = m_eventMenu.addItem("Going green", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Going green?");
			        subwindow.setWidth("30em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        layout.addComponent(new Label("By clicking on 'GO GREEN', you confirm that the cars are running in the correct starting order!"));
			        layout.addComponent(new Label(""));
			        layout.addComponent(new Label("Do you really want to switch to race session?"));
			        
			        HorizontalLayout buttons = new HorizontalLayout();
			        //buttons.setMargin(true);
			        buttons.setSpacing(true);
			        
			        Button btnCancel = new Button("Cancel", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        
			        Button btnDo = new Button("GO GREEN", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
							m_enduranceScoring.goingGreen();
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        		        
			        buttons.addComponent(btnCancel);
			        buttons.setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT);
			        
			        buttons.addComponent(btnDo);
			        buttons.setComponentAlignment(btnDo, Alignment.MIDDLE_RIGHT);
			        
			        layout.addComponent(buttons);
			        layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
			        
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
				}
			});
	                
	        
	        MenuItem checkeredFlagItem = m_eventMenu.addItem("Checkered Flag", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Wave checkered flag?");
			        subwindow.setWidth("30em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        layout.addComponent(new Label("By clicking on 'CHECKERED FLAG', you confirm that the race is over and the cars will get shown the checkered flag!"));
			        layout.addComponent(new Label(""));
			        layout.addComponent(new Label("Do you really want to end the race?"));
			        
			        HorizontalLayout buttons = new HorizontalLayout();
			        //buttons.setMargin(true);
			        buttons.setSpacing(true);
			        
			        Button btnCancel = new Button("Cancel", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        
			        Button btnDo = new Button("CHECKERED FLAG", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			            	m_enduranceScoring.waveCheckeredFlag();
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        		        
			        buttons.addComponent(btnCancel);
			        buttons.setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT);
			        
			        buttons.addComponent(btnDo);
			        buttons.setComponentAlignment(btnDo, Alignment.MIDDLE_RIGHT);
			        
			        layout.addComponent(buttons);
			        layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
			        
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
				}
			});
	                
	        
	        MenuItem saveResultsItem = m_eventMenu.addItem("Save results", new Command() {
				@Override
				public void menuSelected(MenuItem selectedItem) {
			        // Create the window
			        final Window subwindow = new Window("Save results to disk?");
			        subwindow.setWidth("30em");
			        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
			        layout.setMargin(true);
			        layout.setSpacing(true);
			        layout.addComponent(new Label("By clicking on 'SAVE' the current scoring will be saved to disk!"));
			        
			        HorizontalLayout buttons = new HorizontalLayout();
			        //buttons.setMargin(true);
			        buttons.setSpacing(true);
			        
			        Button btnCancel = new Button("Cancel", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        
			        Button btnDo = new Button("Save", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			        		double eventTime = m_enduranceScoring.getEventTime();
							m_backupService.backupStandings(m_enduranceScoring.getRankedVehicles(), eventTime, m_enduranceScoring.useInternalScoring());
			        		
		            		// all was well, we close the window
		            		(subwindow.getParent()).removeWindow(subwindow);
			            }
			        });
			        		        
			        buttons.addComponent(btnCancel);
			        buttons.setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT);
			        
			        buttons.addComponent(btnDo);
			        buttons.setComponentAlignment(btnDo, Alignment.MIDDLE_RIGHT);
			        
			        layout.addComponent(buttons);
			        layout.setComponentAlignment(buttons, Alignment.MIDDLE_RIGHT);
			        
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
				}
			});
	                

	    MenuItem penaltyMenuItem = menuBar.addItem("Penalties", null);
        MenuItem penaltyItem = penaltyMenuItem.addItem("Hand out", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
		        // Create the window
		        final Window subwindow = new Window("Hand out penalty");
		        subwindow.setWidth("40em");
		        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
		        layout.setMargin(true);
		        layout.setSpacing(true);
		        
		        final SortedSet<Vehicle> rankedVehicles = m_enduranceScoring.getRankedVehicles();

		        if (rankedVehicles != null) {
		        	final ComboBox vehicleCombo = new ComboBox("Vehicle", rankedVehicles);
		        	final ComboBox penaltyTypeCombo = new ComboBox("Penalty Type", Arrays.asList(Penalty.Type.values()));
		        	//final TextField descriptionField = new TextField("Description");
		        	final TextField reasonField = new TextField("Reason");
			        Button close = new Button("Confirm", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			            	double eventTime = m_enduranceScoring.getEventTime();
			            	Vehicle selectedVehicle = (Vehicle) vehicleCombo.getValue();
			            	Penalty.Type selectedType = (Type) penaltyTypeCombo.getValue();
			            	//String description = (String) descriptionField.getValue();
							String reason = (String) reasonField.getValue();
							if (selectedType == Penalty.Type.LAP_DOWN_PENALTY){
								Penalty p = m_enduranceScoring.createPenalty(selectedVehicle, selectedType, "1 Lap down", reason, eventTime, eventTime + Penalty.DEFAULT_TIME_TO_RESOLVE_PENALTY);
								p.clearPenalty("RC", eventTime, "RC");
								selectedVehicle.setLapsCompleted(selectedVehicle.getLapsCompleted() - 1);
							}else if (selectedType == Penalty.Type.LAP_UP_PENALTY){
								Penalty p = m_enduranceScoring.createPenalty(selectedVehicle, selectedType, "1 Lap up", reason, eventTime, eventTime + Penalty.DEFAULT_TIME_TO_RESOLVE_PENALTY);
								p.clearPenalty("RC", eventTime, "RC");
								selectedVehicle.setLapsCompleted(selectedVehicle.getLapsCompleted() + 1);
							}else{
								m_enduranceScoring.createPenalty(selectedVehicle, selectedType, "RC Penalty", reason, eventTime, eventTime + Penalty.DEFAULT_TIME_TO_RESOLVE_PENALTY);
							}
			                (subwindow.getParent()).removeWindow(subwindow);
							String message = "" + selectedType + " - " + reason;
							selectedVehicle.setMessage(m_enduranceScoring.getLastEt(), message);
							// TODO
//			                m_serverLog.log(m_enduranceScoring.getEventTime(), selectedVehicle.getDriver() + ": " + message);
			            }
			        });
			        layout.addComponent(vehicleCombo);
			        layout.addComponent(penaltyTypeCombo);
			        //layout.addComponent(descriptionField);
			        layout.addComponent(reasonField);
			        layout.addComponent(close);
			        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
		        }
		        else {
		        	m_mainWindow.showNotification(
                        "Notification<br>",
                        "No cars on track, cannot hand out a penalty",
                        Notification.TYPE_TRAY_NOTIFICATION);
		        }
			}
		});

        MenuItem penaltyClearItem = penaltyMenuItem.addItem("Clear", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
		        // Create the window
		        final Window subwindow = new Window("Clear penalty");
		        subwindow.setWidth("40em");
		        final VerticalLayout layout = (VerticalLayout) subwindow.getContent();
		        layout.setMargin(true);
		        layout.setSpacing(true);
		        
		        final ListSelect penaltiesList = new ListSelect("Penalties:");
		        penaltiesList.setWidth("36em");
		        penaltiesList.setRequired(true);
		        
	        	final TextField reasonField = new TextField("Reason:");
	        	reasonField.setRequired(true);
	        	reasonField.setWidth("36em");
	        	
		        final SortedSet<Vehicle> rankedVehicles = m_enduranceScoring.getRankedVehicles();
	        	
	        	if (rankedVehicles != null) {
		        	final ComboBox vehicleCombo = new ComboBox("Vehicle", rankedVehicles);
		        	vehicleCombo.setWidth("20em");
		        	vehicleCombo.setRequired(true);
		        	vehicleCombo.setFilteringMode(Filtering.FILTERINGMODE_STARTSWITH);
		        	vehicleCombo.setImmediate(true);

		        	// Create listener to process the chosen Vehicle
		        	vehicleCombo.addListener(new ComboBox.ValueChangeListener(){
	        			public void valueChange(ValueChangeEvent event){
	        				penaltiesList.removeAllItems();
	        				String vehName = vehicleCombo.getValue().toString();
	        				Vehicle vehicle = null;
	        				for (Vehicle v : rankedVehicles){
//	        					System.out.println(v.getVehicleName()+"\r\n");
	        					if (v.getVehicleName().equals(vehName)){
	        						vehicle = v;
	        						break;
	        					}
	        				}
	        				
	        				if (vehicle != null){
		        				for (Penalty p : vehicle.getPenalties()) {
		        					if (!p.isResolved()){
		        						//penaltiesCombo.addItem(p);
		        						penaltiesList.addItem(p);
		        					}
		        				}
		        				
	        				}
	        			}
	        		});
			        Button clearButton = new Button("Clear", new Button.ClickListener() {
			            public void buttonClick(ClickEvent event) {
			            	double eventTime = m_enduranceScoring.getEventTime();
			            	Vehicle selectedVehicle = (Vehicle) vehicleCombo.getValue();
			            	Penalty selectedpenalty = (Penalty) penaltiesList.getValue();
							String reason = (String) reasonField.getValue();

							if (selectedpenalty == null){
								m_mainWindow.showNotification(
				                        "Notification<br>",
				                        "No penalty selected!<br>You MUST select a penalty to clear before pressing the 'Clear' button!",
				                        Notification.TYPE_TRAY_NOTIFICATION);								
							}else if (reason.trim() == ""){
									m_mainWindow.showNotification(
					                        "Notification<br>",
					                        "You need to enter a reason for clearing before pressing the 'Clear' button!",
					                        Notification.TYPE_TRAY_NOTIFICATION);								
							}else{
								selectedpenalty.clearPenalty(reason, eventTime, "RaceControl");
								
				                (subwindow.getParent()).removeWindow(subwindow);
								String message = "" + selectedpenalty.getType() + " - CLEARED - " + reason;
								selectedVehicle.setMessage(m_enduranceScoring.getLastEt(), message);
								// TODO
//								m_serverLog.log(eventTime, selectedVehicle.getDriver() + ": " + message + " - " + reason);
							}
			            }
			        });
			        layout.addComponent(vehicleCombo);
			        layout.addComponent(penaltiesList);
			        layout.addComponent(reasonField);
			        layout.addComponent(clearButton);
			        layout.setComponentAlignment(clearButton, Alignment.TOP_RIGHT);
			        getMainWindow().addWindow(subwindow);
			        subwindow.center();
		        }
		        else {
		        	m_mainWindow.showNotification(
                        "Notification<br>",
                        "No cars on track, cannot clear a penalty",
                        Notification.TYPE_TRAY_NOTIFICATION);
		        }
			}
		});

        MenuItem penaltiesListItem = penaltyMenuItem.addItem("List", new Command() {
			@Override
			public void menuSelected(MenuItem selectedItem) {
		        // Create the window
		        final Window subwindow = new Window("Penalties");
		        subwindow.setWidth("1000px");
		        subwindow.setHeight("550px");
		        VerticalLayout layout = (VerticalLayout) subwindow.getContent();
		        layout.setMargin(true);
		        layout.setSpacing(true);
		        
		        final Table table = new Table("Penalties");

		        layout.addComponent(table);

		        // set a style name, so we can style rows and cells
		        //table.setStyleName("iso3166");

		        // size
		        table.setWidth("100%");
		        table.setHeight("420px");

		        // selectable
		        table.setSelectable(true);
		        table.setMultiSelect(true);
		        table.setImmediate(true); // react at once when something is selected

		        // connect data source
		        table.setContainerDataSource(getPenaltiesContainer(m_enduranceScoring.getVehicles(), m_enduranceScoring.getLastEt()));

		        // turn on column reordering and collapsing
		        table.setColumnReorderingAllowed(true);
		        table.setColumnCollapsingAllowed(true);

		        // set column headers
		        table.setColumnHeaders(new String[] { "Time", "Description", "Car", "Class", "To resolve until", "Reason", "Resolved","RC info", "P", "V" });
		        
		        table.setVisibleColumns(new Object[] {COL_TIME, COL_DESC, COL_CAR, COL_CLASS, COL_UNTIL, COL_REASON, COL_RESOLVED, COL_RC });
		        
		        // Column alignment
		        table.setColumnAlignment(COL_TIME, Table.ALIGN_RIGHT);
		        table.setColumnWidth(COL_PENALTY, 0);
		        table.setColumnWidth(COL_VEHICLE, 0);
				
		        // Actions (a.k.a context menu)
		        table.addActionHandler(new Action.Handler() {
 		            public Action[] getActions(Object target, Object sender) {
 		            	return ACTIONS_CLEAR;
 		            	/*
		                if (markedRows.contains(target)) {
		                    return ACTIONS_MARKED;
		                } else {
		                    return ACTIONS_UNMARKED;
		                }
		                */
		            }
		            public void handleAction(Action action, Object sender, Object target) {
		                if (ACTION_CLEAR == action) {
			            	double eventTime = m_enduranceScoring.getEventTime();
			            	Item item = table.getItem(target);

			            	Vehicle selectedVehicle = (Vehicle) item.getItemProperty(COL_VEHICLE).getValue();
			            	Penalty selectedpenalty = (Penalty) item.getItemProperty(COL_PENALTY).getValue();
							String reason = "RC decision";

							if (selectedpenalty == null){
								m_mainWindow.showNotification(
				                        "Notification<br>",
				                        "Penaly already cleared!",
				                        Notification.TYPE_TRAY_NOTIFICATION);								
							}else if (selectedVehicle == null){
									m_mainWindow.showNotification(
					                        "Notification<br>",
					                        "No vehicle selected!<br>You MUST select a vehicle to clear before pressing the 'Clear' button!",
					                        Notification.TYPE_TRAY_NOTIFICATION);								
							}else{
								selectedpenalty.clearPenalty(reason, eventTime, "RaceControl");
								
								item.getItemProperty(COL_UNTIL).setValue("Resolved");
								item.getItemProperty(COL_RESOLVED).setValue("Yes");
								item.getItemProperty(COL_RC).setValue("RaceControl @ " + TimeUtil.toLapTime(selectedpenalty.getClearTime()) + " >> RC decision");

								String message = "" + selectedpenalty.getType() + " - CLEARED - " + reason;
								selectedVehicle.setMessage(m_enduranceScoring.getLastEt(), message);
								// TODO
//				                m_serverLog.log(eventTime, selectedVehicle.getDriver() + ": " + message + " - " + reason);
							}
		                } else if (ACTION_NONE == action) {
		                	// do nothing
		                }

		            }

		        });		        
		        
		        Button reload = new Button("Reload", new Button.ClickListener() {
		            public void buttonClick(ClickEvent event) {
		            	table.setContainerDataSource(getPenaltiesContainer(m_enduranceScoring.getVehicles(), m_enduranceScoring.getLastEt()));
		                //(subwindow.getParent()).removeWindow(subwindow);
		            }
		        });
		        layout.addComponent(reload);
		        layout.setComponentAlignment(reload, Alignment.TOP_RIGHT);
		        getMainWindow().addWindow(subwindow);
		        subwindow.center();
			}});
        
        
        Embedded htmlView = new Embedded("Standings View", new ExternalResource("/score"));
        htmlView.setType(Embedded.TYPE_BROWSER);
        htmlView.setSizeFull();

        Embedded htmlView2 = new Embedded("Track View", new ExternalResource("/track/track.html"));
        htmlView2.setType(Embedded.TYPE_BROWSER);
        htmlView2.setSizeFull();

        Embedded htmlView3 = new Embedded("Hotlap View", new ExternalResource("/hotlaps/html"));
        htmlView3.setType(Embedded.TYPE_BROWSER);
        htmlView3.setSizeFull();
        
        Embedded htmlView4 = new Embedded("Log View", new ExternalResource("/log"));
        htmlView4.setType(Embedded.TYPE_BROWSER);
        htmlView4.setSizeFull();

        Embedded htmlView5 = new Embedded("Qualification", new ExternalResource("/score/quali"));
        htmlView5.setType(Embedded.TYPE_BROWSER);
        htmlView5.setSizeFull();
        
        Embedded htmlView6 = new Embedded("Penalties", new ExternalResource("/score/penalties"));
        htmlView6.setType(Embedded.TYPE_BROWSER);
        htmlView6.setSizeFull();
        
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addTab(htmlView5, "Qualification", null);
        tabs.addTab(htmlView, "Standings", null);
        tabs.addTab(htmlView3, "Hotlaps", null);
        tabs.addTab(htmlView4, "Log", null);
        tabs.addTab(htmlView2, "Track", null);
        tabs.addTab(htmlView6, "Penalties", null);
        
        layout.addComponent(menuBar);
        layout.setExpandRatio(menuBar, 0);
        layout.addComponent(tabs);
        layout.setExpandRatio(tabs, 1);
        
        LoginWindow loginWindow = new LoginWindow();
        m_mainWindow.getWindow().addWindow(loginWindow);
        loginWindow.center();
    }

    public class LoginWindow extends Window {
	    private TextField m_name;
	    private PasswordField m_password;
	    private Button m_loginButton;
	    
	    public LoginWindow() {
	        super("Race Control Login");
	        setResizable(false);
	        setModal(true);
	        setWidth("15em");
	        
	        LoginPanel p = new LoginPanel();
	        setContent(p);
	    }
	    
	    public void closeWindow() {
	        getParent().removeWindow(this);
	    }
	    
	    public class LoginPanel extends VerticalLayout {
	        public LoginPanel() {
	            setSpacing(true);
	            setMargin(true);
	            setClosable(false);
	            setSizeFull();
	            m_name = new TextField("Name", "");
	            m_password = new PasswordField("Password", "");
	            m_loginButton = new Button("Login");
	            addComponent(m_name);
	            addComponent(m_password);
	            addComponent(m_loginButton);
	            setComponentAlignment(m_loginButton, Alignment.BOTTOM_CENTER);
	            m_name.focus();
	            m_name.selectAll();
	            m_loginButton.addListener(new Button.ClickListener() {
	                public void buttonClick(ClickEvent event) {
	                    if (login((String) m_name.getValue(), (String) m_password.getValue())) {
	                        closeWindow();
	                        m_eventMenu.setEnabled(m_isAdmin);
	                    }
	                    else {
	                        // TODO provide some feedback, login failed, for now don't close the login window
	                        m_loginButton.setComponentError(new UserError("Invalid username or password."));
	                    }
	                }
	            });
	        }
	    }
	}

    // TODO AE/Need some sort of table with usernames and passwords
	private boolean login(String username, String password) {
		m_isAdmin = false;
		if (m_User.equals(username.toLowerCase()) && m_Pass.equals(password)){
			// "normal" user
			return true;
		}
		if (m_User_Admin.equals(username.toLowerCase()) && m_Pass_Admin.equals(password)){
			// Admin user
			m_isAdmin = true;
			return true;
		}
		return false;
		//return true;
//		return "admin".equals(username) && "admin".equals(password);
	}
	
	private IndexedContainer getPenaltiesContainer(Map<String, Vehicle> vehicles, double lastEt){

		
		
		IndexedContainer container = new IndexedContainer();
		SortedSet<Penalty> penalties = new TreeSet<Penalty>(new VaadinPenaltyTimeComparator());
		for(Vehicle v : vehicles.values()){
	        for (Penalty p : v.getPenalties()) {
	        	penalties.add(p);
	        }
		}
		// "Time", "Description", "Car", "Class", "To resolve until", "Reason", "Resolved","RC info"
		container.addContainerProperty(COL_TIME, String.class, null);
		container.addContainerProperty(COL_DESC, String.class, null);
		container.addContainerProperty(COL_CAR, String.class, null);
		container.addContainerProperty(COL_CLASS, String.class, null);
		container.addContainerProperty(COL_UNTIL, String.class, null);
		container.addContainerProperty(COL_REASON, String.class, null);
		container.addContainerProperty(COL_RESOLVED, String.class, null);
		container.addContainerProperty(COL_RC, String.class, null);
		container.addContainerProperty(COL_PENALTY, Penalty.class, null);
		container.addContainerProperty(COL_VEHICLE, Vehicle.class, null);

				
		Vehicle v = null;
		int id = 0;
		for(Penalty p : penalties){
			id++;
			// Get vehicle reference only if it isn't already set or has changed
			if (v == null || v.getVehicleName() != p.getVehicleName()){
				v = vehicles.get(p.getVehicleName());
			}
			double timeToResolve = p.getTimeout() - lastEt;
			
			Item item = container.addItem(id);
			item.getItemProperty(COL_TIME).setValue(TimeUtil.toLapTime(p.getTime()));
			item.getItemProperty(COL_DESC).setValue(p.getDescription());
			item.getItemProperty(COL_CAR).setValue(v.getVehicleName());
			item.getItemProperty(COL_CLASS).setValue(v.getVehicleClass());
			item.getItemProperty(COL_UNTIL).setValue((!p.isResolved() 
    				? ((p.getTimeout() > 0) 
            				? TimeUtil.toLapTime(p.getTimeout()) +" ("+(timeToResolve >= 0 ? TimeUtil.toLapTime(timeToResolve) : "<span style='color:red;'>Exceeded</span>") + ")"
            				: " ")
            			: "Resolved"));
			item.getItemProperty(COL_REASON).setValue(p.getReason());
			item.getItemProperty(COL_RESOLVED).setValue((p.isResolved() ? "Yes" : "No"));
			item.getItemProperty(COL_RC).setValue((p.getClearTime() > 0.0 
    				? p.getClearUser() +
					  " @ " + TimeUtil.toLapTime(p.getClearTime()) +
					  (p.getClearReason() != "" ? " >> " + p.getClearReason() : " ")
					: " "));
			item.getItemProperty(COL_PENALTY).setValue((p.isResolved() ? null : p));
			//item.getItemProperty(COL_PENALTY).setReadOnly(true);
			item.getItemProperty(COL_VEHICLE).setValue(v);
			//item.getItemProperty(COL_VEHICLE).setReadOnly(true);

					
			
		}
		return container;
	}

	private Dictionary getConfig(String pid) {
        Configuration config;
        try {
            config = m_configAdmin.getConfiguration(pid, null);
            return config.getProperties();
        }
        catch (IOException ioe) {
            return null;
        }
    }
    private void setConfig(String pid, Dictionary props) throws IOException {
        Configuration config;
        config = m_configAdmin.getConfiguration(pid, null);
        config.update(props);
    }
}

