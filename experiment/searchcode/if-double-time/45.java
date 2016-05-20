package processSimulation.procSim;

import java.lang.Thread.UncaughtExceptionHandler;

//------------------------------------------------------------------
// Änderung (Christoph Behrends, Juli 2010):
// Verwendung der Klasse "BigMath" entfernt
//------------------------------------------------------------------

/**
 * scheduler class<br>
 * provides basic scheduling functions.<br>
 * 
 * Note: all methods and data in this class are static.
 * 
 * @author      Godmar Back - 
 * 					<a href="http://www.cs.utah.edu/~gback/process">
 *					http://www.cs.utah.edu/~gback/process</a>
 * @version     1.0
 * 
 * @see Process
 */
public class Scheduler {
    
    /** thrown exception in process thread */
    public static Throwable thrownException;
    
    /** number of processes */
    static int threadCount;
    
    /** process thread exceptionhandler */
    static UncaughtExceptionHandler exceptionHandler;
    
    /** global simulated time */
    private static double clock;
    
    /** something to wait on */
    private static Object mainMonitor = null;
    
    /** list of scheduled events */
    private static PrioQueue<Process> eventList = null;
    
    /** reference to current process */
    private static Process current = null;
    
    /** process states */ 
    public static enum State {
        /** blocked process - not scheduled if eventTime == -1 */
        IDLE,
        /** active process */
        RUNNING,
        /** terminated process */
        TERMINATED;
    };
    
    
    /**
     * initialize scheduler class.
     */
    static {
        mainMonitor = new Integer(0);
        reset();
    }
    
    
    private Scheduler() {}
    
    
    /**
     * current global simulation time.
     * 
     * @return simulation time
     */
    public static double getClock() {
    	return clock;
    }
    
    
    /**
     * like {@code activate(p, 0);}
     * 
     * @param p : process to be activated
     */
    public static void activate(Process p) {
        activate(p, 0);
    }
    
    
    /**
     * if {@code p} is not running or scheduled,
     * schedule it with delay time; 
     * otherwise, do nothing.
     * 
     * @param p    : process to be activated
     * @param time : time after which to schedule p
     */
    public static void activate(Process p, double time) {
        if (p.isActive()) {
            return;
        }
        if (p.isTerminated()) {
            throwError("Activate of terminated process. " + p);
        }
        schedule(p, time);  // p must be not active
    }
    
    
    /**
     * like {@code reactivate(p, 0);}
     * 
     * @param p : process to be activated
     */
    public static void reactivate(Process p) {
        reactivate(p, 0);
    }
    
    
    /**
     * if {@code p} is not running or scheduled,
     * schedule it with delay t; 
     * if {@code p} is running or scheduled,
     * reschedule it with delay t.
     * 
     * @param p : process to be activated
     * @param t : time after which to activate p
     */
    public static void reactivate(Process p, double t) {
        if (p.isTerminated()) {
            throwError("Reactivate of terminated process. " + p);
        }
        if (p.isScheduled()) {
            unschedule(p);
        }
        schedule(p, t);
    }
    
    
    /**
     * make current process non-active.
     */
    public static void passivate() {
        if (current.isTerminated()) {
            throwError("Passivate of terminated process. " + current);
        }
        if (current.isScheduled()) {
            unschedule(current);
        }
        current.setState(State.IDLE);
        current.setEventTime(Double.NaN);
        
        nextEvent(false);
    }
    
    
    /**
     * schedule for activation after {@code delay} time units,
     * and passivate.
     * 
     * @param delay : time for which to hold
     */
    public static void hold(double delay) {
        reactivate(current, delay);
        nextEvent(false);
    }
    
    
    /**
     * start simulation. (event queue must be non-empty)
     */
    public static void run() {
        nextEvent(false);
    }
    
    
    /**
     * reset simulation data.
     */
    public static void reset() {
        threadCount = 0;
        clock       = 0.;
        current          = null;
        thrownException  = null;
        eventList        = new PrioQueue<Process>();
        exceptionHandler = new UncaughtExceptionHandler() {
            
            public synchronized void uncaughtException(Thread t,
            		Throwable e) {
                thrownException = e;
                synchronized (mainMonitor) {
                    mainMonitor.notify();
                }
            }
        };
        synchronized (mainMonitor) {  // quit scheduler thread
            mainMonitor.notifyAll();
        }
    }
    
    
    /**
     * signal a fatal error and die.
     * 
     * @param message : error message
     */
    public static void throwError(String message) {
        throw new RuntimeException(
                "FATAL ERROR AT CLOCK: " + clock + " IN THREAD \""
                + Thread.currentThread() + "\"\n" + message
                + "\nScheduled Events: " + eventList);
    }
    
    
    /**
     * is {@code p} the active process?
     * 
     * @param p : process to be check
     * 
     * @return {@code true} if the active
     */
    public static boolean isCurrent(Process p) {
        return (current == null) ? false : current.equals(p);
    }
    
    
    /**
     * current active process.
     * 
     * @return active process
     */
    public static Process getCurrent() {
        return current;
    }
    
    
    /**
     * make next event happen.
     * 
     * @param die : caller dies if true
     */
    public static void nextEvent(boolean die) {
        // if run out of events, signal main thread to return
        if (eventList.isEmpty()) {
        	System.out.println("eventList ist leer");
            threadCount = 0;
            synchronized (mainMonitor) {
                mainMonitor.notify();
            }
            
            return;
        }
        // that shouldn't happen
        if (clock > eventList.front().getEventTime()) {
            throwError("Event list not time ordered.");
        }
        
        // get next event and advance clock
        Process next = eventList.dequeue();
        clock = next.getEventTime();
        next.setEventTime(Double.NaN);
        
        // schedule next thread
        passBaton(next, die);
    }

    
    /**
     * insert {@code p} in event list,
     * scheduled to execute at given time.
     * 
     * @param p    : process to be schedule
     * @param time : time after which to schedule p
     */
    private static void schedule(Process p, double time) {
    	double eventTime = time + clock;

        if (p.isTerminated() || p.isScheduled()) {
            throwError("Schedule of terminated or active process. "
            		+ p);
        }
        if(eventTime < clock){
            throwError("Event scheduled in past. eventTime:" + time
                    + " Process=" + p);
        }
        p.setEventTime(eventTime);
        eventList.enqueue(p, eventTime);
    }
    
    
    /**
     * remove event notice for process.
     * 
     * @param p : process to be unschedule
     */
    static void unschedule(Process p) {
        if (!p.isScheduled()) {
            throwError("Unschedule of unscheduled process. " + p);
        }
        if (!eventList.remove(p)) {
            throwError("Active process not found on event list. " + p);
        }
        p.setState(State.IDLE);
        p.setEventTime(Double.NaN);
    }
    
    
    /**
     * to support coroutines.
     * 
     * @param p : process to be resumed
     */
    static void passBaton(Process p) {
        passBaton(p, false);
    }
    
    
    /**
     * pass baton to next process.
     * 
     * @param p   : process to be resumed
     * @param die : caller dies if true
     */
    private static void passBaton(Process p, boolean die) {
        final Process caller = current;

        if (p.isTerminated()) {
            throwError("Trying to pass baton to terminated thread. "
            		+ p);
        }
        
        current = p;
        if (die) {
            // if a thread terminates, signal main thread
            caller.setState(State.TERMINATED);
            threadCount--;
            if (threadCount == 0) {
                synchronized (mainMonitor) {
                    mainMonitor.notify();
                }
            }
        }
        
        // don't do that if passing baton to same thread
        if (caller == null || !caller.equals(p)) {
            /* give next thread a go */
            synchronized (p) {
                p.setState(State.RUNNING);
                p.notify();
            }
            
            // called by main thread
            if (caller == null) {
                // wait till no more live threads
                synchronized (mainMonitor) {
                    try {
                        mainMonitor.wait();
                    } catch (InterruptedException e) {}
                }
                return;
            }
            
            // wait for someone to pass back the baton
            if (!die) {
                caller.setState(State.IDLE);
                caller.block();
            }
        }
    }
}

