package net.tortuga.input;


import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.porcupine.coord.Coord;
import com.porcupine.math.Calc;


/**
 * Thing for testing input states and events.
 * 
 * @author Ondřej Hruška (MightyPork)
 */
public class InputTrigger {

	/** Trigger event */
	public EInput type = EInput.BTN_PRESS;
	/** mark that this trigger is static */
	public boolean isStatic = false;
	//KEY_DOWN, KEY_HOLD, KEY_UP, SCROLL, MOVE, BTN_DOWN, BTN_UP, BTN_HOLD;
	private int keyIndex = -1;
	private int scrollDir = -2;
	private int buttonIndex = -1;
	private int attrib_raw = -1;


	/**
	 * Pack to bundle
	 * 
	 * @return bundle
	 */
	public TriggerBundle toBundle()
	{
		return new TriggerBundle(type, attrib_raw);
	}


	/**
	 * Create trigger from bundle
	 * 
	 * @param bundle bundle
	 */
	public InputTrigger(TriggerBundle bundle) {
		this(bundle.event, bundle.attrib);
	}


	/**
	 * Set trigger attribute
	 * 
	 * @param attrib attribute number (Key→index, Mouse→button,
	 *            Scroll→direction)
	 */
	public void setAttrib(int attrib)
	{
		attrib_raw = attrib;
		switch (type) {
			case BTN_PRESS:
			case BTN_DOWN:
			case BTN_UP:
			case BTN_RELEASE:
				buttonIndex = attrib;
				break;

			case KEY_PRESS:
			case KEY_DOWN:
			case KEY_UP:
			case KEY_RELEASE:
				keyIndex = attrib;
				break;

			case SCROLL:
				scrollDir = attrib;
				break;
		}

		if (type == EInput.BTN_DOWN || type == EInput.KEY_DOWN || type == EInput.BTN_UP || type == EInput.KEY_UP) isStatic = true;
	}


	/**
	 * New trigger - event tester
	 * 
	 * @param type event type
	 * @param attrib (Key→index, Mouse→button, Scroll→direction)
	 */
	public InputTrigger(EInput type, int attrib) {
		this.type = type;
		setAttrib(attrib);
	}


	/**
	 * Try to trigger by mouse event.
	 * 
	 * @param button button which caused this event
	 * @param down true = down, false = up
	 * @param wheelDelta number of steps the wheel turned since last event
	 * @param pos mouse position
	 * @param deltaPos delta mouse position
	 * @return was triggered
	 */
	public boolean onMouseButton(int button, boolean down, int wheelDelta, Coord pos, Coord deltaPos)
	{
		if (type == EInput.BTN_PRESS) {
			return down && buttonIndex == button;
		}

		if (type == EInput.BTN_RELEASE) {
			return !down && buttonIndex == button;
		}

		if (type == EInput.SCROLL) {
			return Calc.sgn(wheelDelta) == Calc.sgn(scrollDir);
		}
		return false;
	}


	/**
	 * Try to trigger by keyboard event.
	 * 
	 * @param key key index, constant Keyboard.KEY_???
	 * @param c character typed, if any
	 * @param down true = down, false = up
	 * @return was triggered
	 */
	public boolean onKey(int key, char c, boolean down)
	{
		if (type == EInput.KEY_PRESS) {
			return down && keyIndex == key;
		}

		if (type == EInput.KEY_RELEASE) {
			return !down && keyIndex == key;
		}
		return false;
	}


	/**
	 * Try to trigger by static inputs (held key/button)
	 * 
	 * @return was triggered
	 */
	public boolean handleStaticInputs()
	{
		if (type == EInput.BTN_DOWN) {
			return Mouse.isButtonDown(buttonIndex);
		}

		if (type == EInput.KEY_DOWN) {
			return Keyboard.isKeyDown(keyIndex);
		}

		if (type == EInput.BTN_UP) {
			return !Mouse.isButtonDown(buttonIndex);
		}

		if (type == EInput.KEY_UP) {
			return !Keyboard.isKeyDown(keyIndex);
		}

		return false;
	}

}

