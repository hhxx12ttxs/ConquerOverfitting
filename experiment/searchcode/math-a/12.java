package vivace.model;

import java.util.EventListener;

/***
 * An interface for listeners of slow model performance.
 * When the model haven't updated it's observers within the 
 * specified delay, the method slowTaskIsRunning() is invoked.
 * @author Ossen
 */
public interface SlowTaskListener extends EventListener {

	/**
	 * The time (in microseconds) to wait before invoking the slowTaskIsRunning() method 
	 * @return
	 */
	long getDelay();

	/***
	 * Method that will be called when the model's observers haven't finished
	 * their work within the delay. 
	 */
	void slowTaskStarted();

	/***
	 * If a call to slowTaskIsStarted was made, this method will be invoked when the model's
	 * observers are finished with their work. 
	 */
	void slowTaskFinished();
}


