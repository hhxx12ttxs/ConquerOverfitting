package mobserv.eurecom.androommates.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

import android.app.PendingIntent;

import mobserv.eurecom.androommates.exceptions.NetworkOccupiedException;
import mobserv.eurecom.androommates.models.Message;

public class MessagingController extends Controller implements Observer {
	public final static int MESSAGE_LOAD_STEP = 20;
	
	private ArrayList<Message> list;
	private Status status = Status.INIT;

	public MessagingController(MainEngine engine) {
		super(engine);
	}
	
	//Events
	public void onDisplay() {
		if(!areMessagesLoaded()) {
			status = Status.LOADING_MESS;
			notifyObservers();
			
			//Load the first 20
			engine.netcomManager.setObserver(this);
			try {
				engine.netcomManager.request(RequestType.INBOX_INIT);
			} catch (NetworkOccupiedException e) {
				status = Status.LOAD_ERR;
				notifyObservers();
				
			}
			return;
		}
		
		status = Status.LOADED_OK;
		notifyObservers();
	}
	/**
	 * This method should ONLY be called when the onDisplay method already has been called
	 * at least once before.
	 */
	public void onReloadMessages() {
		//Reload only the head.
		engine.netcomManager.setObserver(this);
		try {
			engine.netcomManager.request(RequestType.INBOX_INIT);
		} catch (NetworkOccupiedException e) {
			status = Status.LOAD_ERR;
			notifyObservers();
			return;
		}
	}
	/**
	 * Only applicable if hasMoreMessages is true!
	 */
	public void onLoadMoreMessages() {
		status = Status.LOADING_MESS;
		engine.netcomManager.setObserver(this);
		engine.netcomManager.putData(Integer.valueOf(list.size() + MESSAGE_LOAD_STEP));
		engine.netcomManager.putData(Integer.valueOf(list.size()));
		try {
			engine.netcomManager.request(RequestType.INBOX_C);
		} catch (NetworkOccupiedException e) {
			status = Status.LOAD_ERR;
			e.printStackTrace();
		}
	}
	public void onMessagePost(String message) {
		status = Status.SENDING;
		notifyObservers();
		
		engine.netcomManager.setObserver(this);
		engine.netcomManager.putData(message);
		try {
			engine.netcomManager.request(RequestType.INBOX_POST);
		} catch (NetworkOccupiedException e) {
			status = Status.SEND_ERR;
			e.printStackTrace();
			notifyObservers();
		}
	}
	
	//Updates

	@Override
	public void update(Observable observable, Object data) {
		System.out.println("Got netcom update");
		System.out.println("For request " + (RequestType)data + " and status " + engine.netcomManager.getRequestResult());
		
		//netcom speaking
		//Several request types possible
		switch((RequestType)data) {
		case INBOX_INIT:
			switch(engine.netcomManager.getRequestResult()) {
			case OK:
				try {
					System.out.println("Parsing messages data: " + (String)engine.netcomManager.getResponseData());
					ArrayList<Message> messagesReceived = parseJSONData((String)engine.netcomManager.getResponseData());
//					if(status ==  Status.RELOADING_MESS) {
//						reloadHead(messagesReceived); 
//					} else
//						list = messagesReceived;
					//TODO make work
					list = messagesReceived;
					status = Status.LOADED_OK;
				} catch (Exception e) {
					System.out.println("Parsing: failed... because: ");
					e.printStackTrace();
					status = Status.LOAD_ERR;
					break;
				}
				
				break;
			case TIMEOUT:
			case WRONG_REQUEST:
				status = Status.LOAD_ERR;
				break;
			}
			break;
		case INBOX_C:
			switch(engine.netcomManager.getRequestResult()) {
			case OK:
				//TODO parse these as well
				//Add them to the current array
				status = Status.LOADED_OK;
				break;
			case TIMEOUT:
			case WRONG_REQUEST:
				status = Status.LOAD_ERR;
				break;
			}
			break;
		case INBOX_POST:
			switch(engine.netcomManager.getRequestResult()) {
			case OK:			
				
				//Reload the head
				status = Status.SEND_OK;
				notifyObservers();
				status = Status.RELOADING_MESS;
				notifyObservers();
				onReloadMessages();
				break;
			case TIMEOUT:
			case WRONG_REQUEST:
				status = Status.SEND_ERR;
			}
			break;
		}
		
		notifyObservers();
	}
	
	//Getters
	public List<Message> getMessageList() {
		return list;
	}
	public boolean areMessagesLoaded() {
		return list != null;
	}
	public boolean hasMoreMessages() {
		return list.size() % MESSAGE_LOAD_STEP == 0;
	}
	public Status getStatus() {
		return status;
	}
	
	//Internal
	/**
	 * This is called when a reload was successfully made
	 * CHecks whether arraylist is up-to-date. Supposes a reload is never made 
	 * later than the usual message load step.
	 * 
	 */
	private void reloadHead(ArrayList<Message> newVersion) {
		if(list.isEmpty()) {
			list = newVersion;
			return;
		}
		
		//TODO this will fail if our most recent message
		// cannot be found in the newer version
		Message initialMessage = list.get(0);
		int initialIndex = newVersion.indexOf(initialMessage);
		
		//TODO this will fail if initialIndex >= newVersion.size()
		for(int index = 0 ; index < initialIndex; index++) {
			list.add(newVersion.get(index));
		}
	}
	private ArrayList<Message> parseJSONData(String jsonString) throws Exception {
		JSONObject o = new JSONObject(jsonString);
		
		//Return code
		int code = o.getInt("return_code");
		if(code != 200)
			throw new Exception("Request failed");
		
		//Messages node
		JSONObject messages = o.getJSONObject("messages");
		return ResponseDataParseFactory.parseMessagesData(messages);
	}
	
	
	
	//nested
	public static enum Status {
		INIT, LOADING_MESS, LOADED_OK, SENDING, SEND_OK, LOAD_ERR, SEND_ERR, RELOADING_MESS
	}

}

