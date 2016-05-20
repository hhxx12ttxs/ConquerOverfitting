/**
 * Copyright (c) 2006-2008 MiniMe. Code released under The MIT/X Window System
 * License. Full license text can be found in license.txt
 */
package minime.core;

/**
 * The Event class is responsible for creating a event, this event has specified
 * type, and has two parameters and an object parameter for event, also an time
 * parameter for executing the event.
 * 
 * @author yishu
 * 
 */
public class Event {
	/** event mask */
	public static final int EVENT_MASK = 0xF000;

	/** The application event */
	public static final int APP_EVENT = 0x1000;

	/** The pointer event type */
	public static final int POINTER_EVENT = 0x2000;

	/** The key event, paramB is game action, paramA is keyCode */
	public static final int KEY_EVENT = 0x3000;

	/** SoftBar event, paramA is SoftBar action, paramB is SoftBar keyCode */
	public static final int SB_EVENT = 0x4000;

	/** indicating a a pointer press */
	public static final int POINTER_PRESSED = 0x2001; // pen
	/** indicating a a pointer release */
	public static final int POINTER_RELEASED = 0x2002; // pen
	/** indicating a a pointer drag */
	public static final int POINTER_DRAGGED = 0x2003; // pen

	/** The key down event, paramA is game action. */
	public static final int KEY_DOWN_EVENT = 0x3001;
	/** The key up event, paramA is game action. */
	public static final int KEY_UP_EVENT = 0x3002;

	/** The circleMenu event to notify which item is selected */
	public static final int CIRCLEMENU_EVENT = 0x1006;

	/** The menu event to notify which item is selected */
	public static final int MENU_EVENT = 0x1007;

	/** The menu event to notify when selection is made */
	public static final int MENU_SELECTION_EVENT = 0x1008;

	/**
	 * The timer off event, when a pointed time task finishes, it can touch off
	 * this event.
	 */
	public static final int TIMER_OFF_EVENT = 0x1009;

	/** The menu event to notify when selection is made */
	public static final int POPUP_EVENT = 0x100A;

	public static final int POPUP_MENU_EVENT = 0x100B;
	/**
	 * Event fire from ChoiceGroup when a radio button is selected.
	 */
	public static final int RADIO_SELECTED_EVENT = 0x100C;

	/**
	 * Event fire from ChoiceGroup when a check box is marked.
	 */
	public static final int BOX_MARKED_EVENT = 0x100D;

	/**
	 * Event fire from ChoiceGroup when a check box is unmarked.
	 */
	public static final int BOX_UNMARKED_EVENT = 0x100E;
	public static final int NET_EVENT = 0x200A;

	/** The card event, paramA is action status */
	public static final int CARD_OPERATION_SUCCESSFUL = 0x3000;
	public static final int CARD_OPERATION_ERROR = 0x3001;
	public static final int CARD_SCRIPT_EVENT = 0x3002;

	/** The event parameter for application event */
	public static final int APP_START = 0x0100;
	public static final int APP_STOP = 0x0101;
	public static final int APP_PAUSE = 0x0102;
	public static final int APP_RESUME = 0x0103;
	public static final int APP_DESTROY = 0x0104;

	/** The event parameter for network event */
	public static final int RELOAD_RESULT_OK = 0x0206;
	public static final int RELOAD_RESULT_ERROR = 0x0207;
	public static final int RELOAD_PROGRESS = 0x0208;
	public static final int RELOAD_CANCELED = 0x0209;

	public static final int NET_STATUS_OPERATION_COMPLETED = 0x020A;
	public static final int NET_STATUS_ERROR = 0x020B;
	public static final int NET_STATUS_TIMEOUT = 0x020C;
	public static final int NET_STATUS_CONNECTING_TO_SEVER = 0x020D;
	public static final int NET_STATUS_SEVER_CONNECTED = 0x020E;
	public static final int NET_STATUS_CHARGING = 0x020F;

	// three statements of response process of charge
	public static final int NET_STATUS_CHARGE_GETLINE = 0X0210;
	public static final int NET_STATUS_CHARGE_GETHEAD = 0X0211;
	public static final int NET_STATUS_CHARGE_GETBODY = 0X0212;
	// event of construct socket link
	public static final int NET_OPENCONNECTION = 0X0213;

	public static final int STARTUP_EVENT = 0x030D;
	public static final int CARD_EVENT = 0x030E;
	public static final int PASSWORD_LEFT_TIMES_EVENT = 0x030F;

	// timer event
	public static final int TIMER_EVENT = 0x0310;
	public static final int TIMER_FINISH = 0x0311;

	/** The event type */
	public int type;
	/** the time for event is to be executed. */
	public long when;

	public int paramA;
	public int paramB;
	public Object payload;

	/**
	 * Constructs an event instance with given event type, event paramters and
	 * time the event is to be executed.
	 * 
	 * @param type
	 *            the event type
	 * @param paramA
	 *            the first parameter for event
	 * @param paramB
	 *            the second parameter for event
	 * @param payload
	 *            an object parameter for event.
	 * @param when
	 *            the time for event is to be executed.
	 */
	public Event(int type, int paramA, int paramB, Object payload, long when) {
		this.type = type;
		this.paramA = paramA;
		this.paramB = paramB;
		this.payload = payload;
		this.when = when;
	}

	/**
	 * Creates a new event instance with given specified type, two int
	 * parameters and an object.
	 * 
	 * @param type
	 *            the event type
	 * @param paramA
	 *            the first parameter for event
	 * @param paramB
	 *            the second parameter for event
	 * @param payload
	 *            an object parameter for event.
	 * @return the created event.
	 */
	public static Event createEvent(int type, int paramA, int paramB,
			Object payload) {
		return createEvent(type, paramA, paramB, payload, 0);
	}

	/**
	 * Creates a new event instance with given specified type and two int
	 * parameters.
	 * 
	 * @param type
	 *            the event type
	 * @param paramA
	 *            the first parameter for event
	 * @param paramB
	 *            the second parameter for event
	 * @return the created event.
	 */
	public static Event createEvent(int type, int paramA, int paramB) {
		return createEvent(type, paramA, paramB, null, 0);
	}

	/**
	 * Creates a new event instance with given specified type, two int
	 * parameters ,an object parameter and the delay time for executing the
	 * event after that specified delay.
	 * 
	 * @param type
	 *            the event type
	 * @param paramA
	 *            the first parameter for event
	 * @param paramB
	 *            the second parameter for event
	 * @param payload
	 *            an object parameter for event.
	 * @param delay
	 *            delay in milliseconds before task is to be executed.
	 * @return the created event.
	 */
	public static Event createEvent(int type, int paramA, int paramB,
			Object payload, long delay) {
		return new Event(type, paramA, paramB, payload, delay
				+ System.currentTimeMillis());
	}

	/**
	 * 
	 * @param type
	 * @return the event type
	 */
	public static int getEventType(int type) {
		return type & EVENT_MASK;
	}

	private static String getEventTypeString(int type) {
		String typeString = null;
		switch (type) {
		case KEY_EVENT:
			typeString = "KEY_EVENT";
			break;
		case KEY_DOWN_EVENT:
			typeString = "KEY_DOWN_EVENT";
			break;
		case KEY_UP_EVENT:
			typeString = "KEY_UP_EVENT";
			break;
		case POINTER_PRESSED:
			typeString = "POINTER_PRESSED";
			break;
		case POINTER_RELEASED:
			typeString = "POINTER_RELEASED";
			break;
		case POINTER_DRAGGED:
			typeString = "POINTER_DRAGGED";
			break;
		case SB_EVENT:
			typeString = "SB_EVENT";
			break;
		case MENU_EVENT:
			typeString = "MENU_EVENT";
			break;
		case MENU_SELECTION_EVENT:
			typeString = "MENU_SELECTION_EVENT";
			break;
		case TIMER_EVENT:
			typeString = "TIMER_EVENT";
			break;
		case APP_EVENT:
			typeString = "APP_EVENT";
			break;
		}
		return typeString;
	}
	
	public boolean isKeyEvent() {
		return (getEventType(type) == KEY_EVENT);
	}
	
	public boolean isPointerEvent() {
		return (getEventType(type) == POINTER_EVENT);
	}
	
	public boolean isSoftBarEvent() {
		return (type == SB_EVENT);
	}
	
	public boolean isMenuEvent() {
		return (type == MENU_EVENT);
	}

	public boolean isMenuSelectionEvent() {
		return (type == MENU_SELECTION_EVENT);
	}
	
	public boolean isKeyPressedEvent() {
		return (type == KEY_DOWN_EVENT);
	}
	
	public boolean isKeyReleasedEvent() {
		return (type == KEY_UP_EVENT);
	}
	
	public boolean isPointerPressedEvent() {
		return (type == POINTER_PRESSED);
	}
	
	public boolean isPointerReleasedEvent() {
		return (type == POINTER_RELEASED);
	}
	
	public boolean isPointerDraggedEvent() {
		return (type == POINTER_DRAGGED);
	}
	
	public boolean isKeyPressed(int keyValue) {
		if (this.type != KEY_DOWN_EVENT) {
			return false;
		}
		
		return (this.paramA == keyValue);
	}
	
	public boolean isKeyReleased(int keyValue) {
		if (this.type != KEY_UP_EVENT) {
			return false;
		}
		
		return (this.paramA == keyValue);
	}

	/**
	 * Returns the event information as a string.
	 */
	public String toString() {
		String print = null;
		print = getEventTypeString(type) + ":" + Integer.toHexString(paramA)
				+ "," + Integer.toHexString(paramB);
		return print;
	}
}

