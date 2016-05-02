package com.foreveryoung.models.parameters;

import java.util.Date;

import com.foreveryoung.Constants.ParameterType;
import com.foreveryoung.models.Patient;
import com.foreveryoung.Constants;
import com.foreveryoung.SessionData;
import com.foreveryoung.R;

public class Weight extends HealthParameter {

	// Constants

	double weight;
	double bmi;
	
	/**
	 * @param weight
	 */
	public Weight(double weight, Date timestamp) {
		super(timestamp);
		this.weight = weight;
		this.type = ParameterType.WEIGHT;
		setState();
		}
	
	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	
	public double getWeightKG() {
		return weight/Constants.CONVERSION_LBS_KG;
	}
	
	public double getBMI() {
		return getWeightKG()/
			   Math.sqrt((((Patient) SessionData.getInstance().getUser()).getHeight()/Constants.CONVERSION_CM_M));
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public int getIconResID() {
		// TODO Auto-generated method stub
		
		switch (getState()) {
			case GOOD:
				return R.drawable.weight_gr;
			case MEDIOCRE:
				return R.drawable.weight_ye;
			case BAD:
				return R.drawable.weight_red;
		}
		return 0;

	}

	@Override
	public void setState() {
		// TODO Auto-generated method stub
		bmi = getBMI();
		
		if (bmi < Constants.BMI_VERY_LOW) {
			state = HealthState.BAD;
		} else if (bmi < Constants.BMI_LOW) {
			state = HealthState.MEDIOCRE;
		} else if (bmi < Constants.BMI_GOOD) {
			state = HealthState.GOOD;
		} else if (bmi < Constants.BMI_HIGH) {
			state = HealthState.MEDIOCRE;
		} else {
			state = HealthState.BAD;
		}
		
	}
	
	@Override
	public String toString() {
		return "BMI: "+(int)getBMI();
	}
	
}

