package server.network;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import model.Appointment;
import model.Employee;
import model.Room;

import server.database.Database;

public class Client implements Runnable {
	public static final String CLOSING_PROPERTY = "Closing";
	public static final String[] VALID_EVENTS = {"AddAppointment", "EditAppointment", "RemoveAppointment", 
		"GetAppointsments", "GetEmployeeList", "GetRoom", "GetAvailiableRooms", "Login", "AddMeeting", "EditMeeting", "RemoveMeeting", "GetMeetings", "GetMeeting", "AttendingReply"};
	public static final int SLEEP = 50;
	private Socket client;
	private boolean running = false;
	private long tick;
	private Employee auth = null;
	
	private BufferedReader in = null;
	private PrintWriter out = null;
	
	private PropertyChangeSupport pcs;
	
	public static enum EVENT_REPLY {
		OK, FAIL, NOTRECOGNIZED, XMLERROR, NOTLOGGEDIN;
	}
	
	public Client(Socket client) {
		this.client = client;
		pcs = new PropertyChangeSupport(this);
	}
	
	/**
	 * Closes the connection
	 */
	public void close() {
		try {
			running = false;
			client.close();
			in.close();
			out.close();
			
			if (pcs.getPropertyChangeListeners().length > 0) {
				pcs.firePropertyChange(Client.CLOSING_PROPERTY, true, false);
			}
		} catch (Exception e) { // IOException
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Thread starts, and socket starts listening
	 */
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
		} catch (Exception e) { // IOException
			System.out.println(e.getMessage());
		}
		
		running = true;
		while (running) {
			try {
				String line = in.readLine();
				if (line != null) {
					String message = "";
					// Leser flere linjer til meldingen er avsluttet med en tom linje
					while (line != null && !(line != null && line.equals(""))) {
						message += line;
						line = in.readLine();
					}
					System.out.println("Message is : " + message);
					String reply = message(message);
					System.out.println("Reply is " + reply);
					if (reply != null) {
						out.println(reply + "\n");
					}
				}
			} catch (Exception e) { // IOException
				System.out.println(e.getMessage());
			}
			
			// Waits a bit
			try {
        		Thread.sleep(SLEEP);
        	} catch (Exception e) { // InterruptedException
        		System.out.println(e.getMessage());
        	}
		}
	}
	
	/**
	 * Gets a message and returns a reply
	 * @param message Message from client
	 * @return Reply to message
	 */
	private String message(String message) {
		tick = System.currentTimeMillis();
		String event = getEventType(message);
		
		if (message.toLowerCase().equals("close")) {
			close();
			return null;
		} else if (Arrays.asList(VALID_EVENTS).contains(event)) {
			return executeEvent(event, message);
		} else {
			return getReplyEvent(EVENT_REPLY.NOTRECOGNIZED, "Event not recognized");
		}
	}
	
	/**
	 * @param message Message (XML)
	 * @return Event name in the message
	 */
	private String getEventType(String message) {
		// TODO: Get event type from XML in message
		return "";
	}
	
	/**
	 * @param eventType The associated reply event
	 * @param replyMessage The message that is included in the event
	 * @return XML-object for the given eventType
	 */
	public static String getReplyEvent(EVENT_REPLY eventType, String replyMessage) {
		return "<Event><Type>" + eventType + "</Type><Message>" + replyMessage + "</Message></Event>";
	}
	
	/**
	 * Executes the given event with the given values from message
	 * @param event Event to execute
	 * @param message Message with values for the event
	 * @return Reply message to Client
	 */
	@SuppressWarnings("deprecation")
	private String executeEvent(String event, String message) {
		if (event != null) {
			if (event.equals("Login")) {
				try {
					Employee employee = xml.XMLHandler.createEmployee(message);
					
					String sql = "SELECT * FROM Employees WHERE username = ? AND password = ?";
					String[] params = {employee.getUsername(), employee.getPassword()};
					List<HashMap<String, String>> res = Database.retrieve(sql, params);
					if (res.size() > 0) {
						// User was authorized via login
						auth = employee;
						return xml.XMLHandler.createXml(event, xml.XMLHandler.createXml(employee));
					} else {
						return getReplyEvent(EVENT_REPLY.FAIL, "Login failed");
					}
				} catch (Exception e) {
					System.out.println(e.getMessage()); // TODO: Remove on release
					return getReplyEvent(EVENT_REPLY.XMLERROR, "XML error");
				}
			} else if (auth == null) {
				// Client not logged in
				return getReplyEvent(EVENT_REPLY.NOTLOGGEDIN, "Not logged in");
			} else if (event.equals("AddAppointment")) {
				try {
					Appointment appointment = xml.XMLHandler.createAppointment(message);
					Room room = xml.XMLHandler.createRoom(message);
					
					// TODO: Possible bug: will this set addedDate to now() or not?
					String sql = "INSERT INTO Appointments (employee, addedDate, startDate, endDate, description, location) VALUES (?, ?, ?, ?, ?)";
					String[] params = {auth.getUsername(), appointment.getAddedDate().toString(), appointment.getStartDate().toString(), 
							appointment.getEndDate().toString(), appointment.getDescription(), appointment.getLocation()};
					int id = Database.modify(sql, params, Database.ReturnType.ID);
					int rows = 0;
					
					if (id != 0) {
						sql = "INSERT INTO Meetings (id, room) VALUES (?, ?)";
						String[] paramsMeeting = {String.valueOf(id), room.getName()};
						rows = Database.modify(sql, paramsMeeting, Database.ReturnType.ROWS);
					}
					
					if (id != 0 && rows != 0) {
						return xml.XMLHandler.createXml(event, xml.XMLHandler.createXml(appointment));
					} else {
						throw new Exception("Error: Nothing updated");
					}
				} catch (Exception e) {
					System.out.println(e.getMessage()); // TODO Remove on release
					return getReplyEvent(EVENT_REPLY.XMLERROR, "XML error");
				}
			} else if (event.equals("EditAppointment")) {
				try {
					// TODO: Currently any employee can edit any appointment (does not have to be owner)
					Appointment appointment = xml.XMLHandler.createAppointment(message);
					Room room = xml.XMLHandler.createRoom(message);
					
					String sqlAppointments = "UPDATE Appointments SET employee = ?, startDate = ?, endDate = ?, description = ?, location = ? WHERE id = ?";
					String[] paramsAppointments = {appointment.getAddedBy().getUsername(), appointment.getStartDate().toString(), 
							appointment.getEndDate().toString(), appointment.getDescription(), appointment.getLocation(), String.valueOf(appointment.getId())};
					int rowsAppointments = Database.modify(sqlAppointments, paramsAppointments, Database.ReturnType.ROWS);
					
					String sqlMeetings = "UPDATE Meetings SET room = ? WHERE id = ?";
					String[] paramsMeetings = {room.getName(), String.valueOf(appointment.getId())};
					int rowsMeetings = Database.modify(sqlMeetings, paramsMeetings, Database.ReturnType.ROWS);
					
					if (rowsAppointments != 0 || rowsMeetings != 0) {
						return xml.XMLHandler.createXml(event, xml.XMLHandler.createXml(appointment));
					} else {
						throw new Exception("Error: Nothing updated");
					}
					
				} catch (Exception e) {
					System.out.println(e.getMessage()); // TODO Remove on release
					return getReplyEvent(EVENT_REPLY.XMLERROR, "XML error");
				}
			} else if (event.equals("RemoveAppointment")) {
				try {
					// TODO: Currently anyone can delete any appointment
					Appointment appointment = xml.XMLHandler.createAppointment(message);
	
					String sqlAppointments = "DELETE FROM Appointments WHERE id = ?";
					String[] paramsAppointments = {String.valueOf(appointment.getId())};
					int rowsAppointments = Database.modify(sqlAppointments, paramsAppointments, Database.ReturnType.ROWS);
					
					String sqlMeetings = "DELETE FROM Meetings WHERE id = ?";
					String[] paramsMeetings = {String.valueOf(appointment.getId())};
					int rowsMeetings = Database.modify(sqlMeetings, paramsMeetings, Database.ReturnType.ROWS);
					
					if (rowsAppointments != 0 && rowsMeetings != 0) {
						return xml.XMLHandler.createXml(event, xml.XMLHandler.createXml(appointment));
					} else {
						throw new Exception("Error: Nothing updated");
					}
				} catch (Exception e ) {
					System.out.println(e.getMessage()); // TODO Remove on release
					return getReplyEvent(EVENT_REPLY.XMLERROR, "XML error");
				}
			} else if (event.equals("GetAppointsments")) {
				try {
					Employee employee = xml.XMLHandler.createEmployee(message);

					String sql = "SELECT * FROM Appointments WHERE employee = ?";
					String[] params = {employee.getUsername()};
					
					List<Appointment> appointments = new ArrayList<Appointment>();
					List<HashMap<String, String>> res = Database.retrieve(sql, params);
					for (HashMap<String, String> hm : res) {
						int id = Integer.parseInt(hm.get("id"));
						Employee addedBy = new Employee(hm.get("employee"));
						Date addedDate = new Date(hm.get("addedDate"));
						Date startDate = new Date(hm.get("startDate"));
						Date endDate = new Date(hm.get("endDate"));
						String description = hm.get("description");
						String location = hm.get("location");
						
						String sqlRoom = "SELECT * FROM rooms WHERE name = (SELECT room FROM Meetings WHERE id = ?)";
						String[] paramsRoom = {String.valueOf(id)};
						List<HashMap<String, String>> resRoom = Database.retrieve(sqlRoom, paramsRoom);
						Room room;
						
						if (resRoom.size() > 0) {
							String room_name = resRoom.get(1).get("name");
							int room_size = Integer.parseInt(resRoom.get(1).get("size"));
							String room_description = resRoom.get(1).get("description");
							room = new Room(room_name, room_size, room_description);
						} else {
							throw new Exception("Error: No such room");
						}
						
						Appointment appointment = new Appointment(id, addedBy, addedDate, startDate, endDate, description, location, room);
						appointments.add(appointment);
					}
					
					if (res.size() > 0 && appointments.size() > 0) {
						String data = "";
						for (int i = 0; i < appointments.size(); i++) {
							data += xml.XMLHandler.createXml(appointments.get(i));
						}
						
						return xml.XMLHandler.createXml(event, data);
					} else {
						throw new Exception("Error: Nothing found");
					}
				} catch (Exception e) {
					System.out.println(e.getMessage()); // TODO Remove on release
					return getReplyEvent(EVENT_REPLY.FAIL, "Could not get appointments");
				}
			} else if (event.equals("GetEmployeeList")) {
				String sql = "SELECT username, name FROM Employees";
				
				List<Employee> employees = new ArrayList<Employee>();
				List<HashMap<String, String>> res = Database.retrieve(sql);
				
				try {
					for (HashMap<String, String> hm : res) {
						employees.add(new Employee(hm.get("username"), hm.get("name")));
					}
				} catch (Exception e) {
					return getReplyEvent(EVENT_REPLY.FAIL, "Could not get employee list");
				}
				
				if (res.size() > 0 && employees.size() > 0) {
					String data = "";
					for (int i = 0; i < employees.size(); i++) {
						data += xml.XMLHandler.createXml(employees.get(i));
					}
					//TODO: (Critical) This event is not the event the Client will be looking for FIX everywhere!
					return xml.XMLHandler.createXml(event, data);
				} else {
					return getReplyEvent(EVENT_REPLY.FAIL, "No employees found");
				}
			} else if (event.equals("GetRoom")) {
				try {
					Room room = xml.XMLHandler.createRoom(message);
					
					String sql = "SELECT * FROM Room WHERE name = ?";
					String[] params = {room.getName()};
					
					List<HashMap<String, String>> res = Database.retrieve(sql, params);
					
					Room replyRoom;
					if (res.size() > 0) {
						String roomName = res.get(0).get("name");
						int roomSize = Integer.parseInt(res.get(0).get("size"));
						String roomDescription = res.get(0).get("description");
						replyRoom = new Room(roomName, roomSize, roomDescription);
					} else {
						throw new Exception("Error: No such room");
					}
					
					return xml.XMLHandler.createXml(event, xml.XMLHandler.createXml(replyRoom));
				} catch (Exception e) {
					System.out.println(e.getMessage()); // TODO Remove on release
					return getReplyEvent(EVENT_REPLY.XMLERROR, "XML error");
				}
				
			} else if (event.equals("GetAvailiableRooms")) {
				// TODO: Implement GetAvailiableRooms
			} else if (event.equals("AddMeeting")) {
				// TODO: Implement 
			} else if (event.equals("EditMeeting")) {
				// TODO: Implement 
			} else if (event.equals("RemoveMeeting")) {
				// TODO: Implement 
			} else if (event.equals("GetMeetings")) {
				// TODO: Implement 
			} else if (event.equals("GetMeeting")) {
				// TODO: Implement 
			} else if (event.equals("AttendingReply")) {
				// TODO: Implement 
			}
		}
		
		return getReplyEvent(EVENT_REPLY.NOTRECOGNIZED, "Event not recognized");
	}

	/**
	 * @return If Socket is open or not
	 */
	public boolean getRunning() {
		return running && client.isConnected();
	}
	
	/**
	 * @return Tick when last message was received
	 */
	public long getTick() {
		return tick;
	}
	
	/**
	 * Runs a pingpong to the client. If no response it closes the connection
	 */
	public void pingpong() {
		// TODO: (LOW PRIO) Implement
		/*
		BufferedReader in = null;
		PrintWriter out = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
		} catch (Exception e) { // IOException
			System.out.println(e.getMessage());
		}
		
		boolean waiting = true;
		long pingtick = System.currentTimeMillis();
		out.println("Ping");
		
		while (waiting) {
			try {
				String line = in.readLine();
				if (line != null) {
					tick = System.currentTimeMillis();
					break;
					System.out.println("Message: " + line);
				}
			} catch (Exception e) { // IOException
				System.out.println(e.getMessage());
			}
			
			// Closes connection if no reply was received
			if (tick)
			
			// Waits for message
			try {
	    		Thread.sleep(50);
	    	} catch (Exception e) { // InterruptedException
	    		System.out.println(e.getMessage());
	    	}
		}
		*/
	}
	
	/**
	 * Adds listener to events related to this Client
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	
	/**
	 * Releases all Socket resources
	 */
	protected void finalize() {
		close();
	}
}
