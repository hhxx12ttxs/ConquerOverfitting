package dbPhase.hypeerweb;

import java.io.Serializable;

/**
 * The height class represents the height of the HyPeerWeb
 * 
 * @author james
 * 
 */
public class Height implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4050694606928690522L;
	private int height;
	private boolean isDynamic = true;

	/**
	 * Create a height object from a given height
	 * 
	 * @param inital_height
	 */
	public Height(int initialHeight) {
		height = initialHeight;
	}

	/**
	 * Decrements the height. Guarentees that the value will not drop below 0
	 * 
	 * @return the new height of the HyPeerWeb
	 */
	public int dec() {
		if (height > 0 && isDynamic) {
			--height;
		}
		return height;
	}

	/**
	 * Increments the Height.
	 * 
	 * @return the new height of the HyPeerWeb
	 */
	public int inc() {
		if (isDynamic) {
			++height;
		}
		return height;
	}

	/**
	 * The current height
	 * 
	 * @return returns the current height
	 */
	public int get() {
		return height;
	}

	public void set(int h) {
		height = h;
	}

	public void setDynamic(boolean isAuto) {
		isDynamic = isAuto;
	}

	public boolean isDynamic() {
		return isDynamic;
	}
}

