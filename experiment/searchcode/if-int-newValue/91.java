/**
 * 
 */
package uk.ac.lkl.migen.system.expresser.model.tiednumber;

import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.util.gwt.FactoryRepository;
import uk.ac.lkl.migen.system.util.gwt.Timer;

/**
 * The timer that causes task variables to repeatedly change values
 * 
 * @author Ken Kahn
 *
 */
public class TiedNumberTimer {
    
    // if Integer.MIN_VALUE then a small random amount
    final static public int RANDOM_STEP = Integer.MIN_VALUE;
    
    protected int stepAmount = RANDOM_STEP;

    private TiedNumberExpression<Number> tiedNumber;

    private Timer timer = null;

    private AnimationSettings settings;
    
//    private boolean postponed = false;
       
    public TiedNumberTimer(TiedNumberExpression<Number> number) {
	this.tiedNumber = number;
	settings = new AnimationSettings(number);
    }
    
    public void step() {
	if (tiedNumber == null || tiedNumber.isLocked()) {
	    return;
	}
	// TODO: revisit this since won't work in the stand-alone version
//	if (Expresser.instance().isCanvasBeingUpdated()) {
//	    postponed = true;
////	    System.out.println("timer postponed");
//	    return;
//	}
	int currentValue = tiedNumber.getValue().intValue();
	int delta;
	int maximum = settings.getMaximum();
	int minimum = settings.getMinimum();
	int increment = settings.getIncrement();
	if (increment == 0) {
	    return;
	}
//	if (settings.isAnySettings()) {
	    if (settings.isRandomOrder()) {
		int count = (1+maximum-minimum)/increment;
		int newValue = minimum + (int) ((Math.random()*count)*increment);
		while (newValue == currentValue && count > 1) {
		    newValue = minimum + (int) ((Math.random()*count)*increment);
		}
		tiedNumber.setValue(new Number(newValue));
		return;
	    } else if (stepAmount == RANDOM_STEP) {
		stepAmount = increment;
	    }
//	} 
	if (stepAmount == RANDOM_STEP) {
	    // use old scheme
	    final int deltas[] = {-3, -2, -1, 1, 2, 3};
	    double random_double_less_than_6 = Math.random()*6;
	    delta = deltas[(int) random_double_less_than_6];
	    if (currentValue + delta > maximum) {
		delta = -delta;
	    } else if (currentValue + delta < minimum) {
		delta = -delta;
	    }
	} else {
	    if (currentValue + stepAmount > maximum) {
		stepAmount = -stepAmount;
	    } else if (currentValue + stepAmount < minimum) {
		stepAmount = -stepAmount;
	    }
	    delta = stepAmount;
	}
	tiedNumber.setValue(new Number(currentValue + delta));
    }

    public void start() {
	if (timer != null) {
	    // already running
	    return;
	}
	Runnable runnable = new Runnable() {

	    @Override
	    public void run() {
		step();		
	    }
	    
	};
	timer = FactoryRepository.getTimerFactory().getTimer(runnable);
	timer.schedule(settings.getDelay());
    }
    
    public int getDelay() {
	return settings.getDelay();
    }
    
    public void stop() {
	if (timer != null) {
	    timer.cancel();
	    timer = null;
	}
    }

    public int getStepAmount() {
        return stepAmount;
    }

    public void setStepAmount(int stepAmount) {
        this.stepAmount = stepAmount;
    }

    public void scheduleRepeating(int periodInMilliseconds) {
	timer.schedule(periodInMilliseconds);
    }

    public void setAnimationSettings(AnimationSettings animationSettings) {
	if (animationSettings == null) {
	    // empty AnimationSettings contains default values while null causes exceptions
	    animationSettings = new AnimationSettings(tiedNumber);
	}
	this.settings = animationSettings;	
    }

//    public void resumeIfPostponed() {
//	if (postponed) {
//	    postponed = false;
//	    System.out.println("timer resumed");
//	    step();
//	}	
//    }
    
}

