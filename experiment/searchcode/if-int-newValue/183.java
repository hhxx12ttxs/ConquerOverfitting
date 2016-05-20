/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.HashMap;

import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;
import uk.ac.lkl.common.util.expression.Expression;
import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.AnimationSettings;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;

import com.google.gwt.user.client.ui.Widget;
import uk.ac.lkl.com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import uk.ac.lkl.com.kiouri.sliderbar.client.event.BarValueChangedHandler;

/**
 * Implements a slider for unlocked tied numbers
 * 
 * @author Ken Kahn
 *
 */
public class TiedNumberSlider extends AdvancedSliderBar {
    
    private TiedNumberExpression<Number> number;
    private int increment;
    private int minimum;
    private int maximum;

    public TiedNumberSlider(final TiedNumberExpression<Number> number, final ExpresserCanvasPanel canvas) {
	super(); // 10, "160px", true);
	this.number = number;
	// then use settings from previous sessions (if any)
	HashMap<TiedNumberExpression<Number>, AnimationSettings> allSettings = 
		canvas.getModel().getAnimationSettings();
	AnimationSettings settings = allSettings.get(number);
	if (settings != null) {
	    useSettings(settings);
	} else {
	    settings = new AnimationSettings(number);
	    allSettings.put(number, settings);
	}
	// if number is out of the default range then change the settings minimally
	// to accommodate the current value
	int numberValue = number.getValue().intValue();
	if (numberValue < settings.getMinimum()) {
	    settings.setMinimum(numberValue);
	} else if (numberValue > settings.getMaximum()) {
	    settings.setMaximum(numberValue);
	}
	useSettings(settings);
	setSliderValue(numberValue);
	BarValueChangedHandler valueChangedHandler = new BarValueChangedHandler() {

	    @Override
	    public void onBarValueChanged(BarValueChangedEvent event) {
		int newValue = getSliderValue();
		int currentValue = number.getValue().intValue();
		if (currentValue != newValue) {
		    number.setValue(new Number(newValue));
		    EventManager eventManager = canvas.getEventManager();
		    if (eventManager != null) {
			eventManager.updateTiedNumber(number, true);
		    }
		    if (currentValue > maximum || currentValue < minimum) {
			String valueChangedBecauseOutOfRange = Expresser.messagesBundle.ValueChangedBecauseOutOfRange();
			valueChangedBecauseOutOfRange = valueChangedBecauseOutOfRange.replace("***name***", number.getName());
			ArrayList<Widget> targetWidgets = new ArrayList<Widget>();
			Widget parent = getParent();
			if (parent instanceof UnlockedTiedNumberGrid) {
			    UnlockedTiedNumberGrid unlockedTiedNumberGrid = (UnlockedTiedNumberGrid) parent;
			    int row = unlockedTiedNumberGrid.getRow(TiedNumberSlider.this);
			    if (row >= 0) {
				Widget settingsButton = unlockedTiedNumberGrid.getWidget(row, UnlockedTiedNumberGrid.SETTING_BUTTON_COLUMN);
				targetWidgets.add(settingsButton);
			    }
			}
			Expresser.instance().showInterventionFromExpresser(valueChangedBecauseOutOfRange, targetWidgets);
		    }
		    UIEventManager.processEvent(new TiedNumberSliderChangedEvent(number.getName(), newValue));
		}
	    }
	    
	};
	addBarValueChangedHandler(valueChangedHandler);
	UpdateListener<Expression<Number>> updateListener = new UpdateListener<Expression<Number>>() {

	    @Override
	    public void objectUpdated(UpdateEvent<Expression<Number>> e) {
		Expression<Number> tiedNumber = e.getSource();
		int newValue = tiedNumber.evaluate().intValue();
		int sliderValue = getSliderValue();
		if (newValue != sliderValue) {
		    setSliderValue(newValue);
		}
	    }
	    
	};
	number.addUpdateListener(updateListener);
//	sliderBarCalulator.clcValueByAbsPosition(10);
    }

    public TiedNumberExpression<Number> getNumber() {
        return number;
    }
    
    @Override
    public void setMaxValue(int maximum) {
	this.maximum = maximum;
	super.setMaxValue(getSliderMaxValue());
    }

    public void setMinValue(int minimum) {
	this.minimum = minimum;
    }

    public void setIncrement(int increment) {
	this.increment = increment;	
    }

    public void useSettings(AnimationSettings settings) {
	minimum = settings.getMinimum();
	setMinValue(minimum);
	increment = settings.getIncrement();
	setIncrement(increment);
	maximum = settings.getMaximum();
	setMaxValue(maximum);
    }
    
    public int getSliderValue() {
	int sliderValue = getValue();
	return sliderValue*increment+minimum;
    }
    
    public void setSliderValue(int numberValue) {
	setValue((numberValue-minimum)/increment);
    }
    
    private int getSliderMaxValue() {
	if (increment == 0) {
	    // still initialising
	    return getMaxValue();
	} else {
	    return (maximum-minimum)/increment;
	}
    }

}

