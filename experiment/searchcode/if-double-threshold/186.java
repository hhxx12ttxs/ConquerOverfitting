package lrg.common.abstractions.plugins.conformities;

import lrg.common.abstractions.entities.AbstractEntityInterface;
import lrg.common.abstractions.entities.ResultEntity;

public class ValueBasedConformityRule extends ConformityRule {
	private double value;
	public ValueBasedConformityRule(double propertyValue, String conformityOperator, String appliesToEntityTypeName, Object threshold) {
		super(propertyValue + "", conformityOperator, appliesToEntityTypeName, threshold);
		value = propertyValue;
	}

	public ValueBasedConformityRule(double propertyValue, String conformityOperator, String appliesToEntityTypeName, double threshold) {
		super(propertyValue + "", conformityOperator, appliesToEntityTypeName, threshold);
		value = propertyValue;
	}

	public Double applyConformity(AbstractEntityInterface anEntity) {
		if (anEntity == null)
			return new Double(0);
		ResultEntity theResult = new ResultEntity(value);
		if (theResult == null)
			return new Double(0);
		return theResult.applyConformity(conformityOperator, threshold) * getWeight();
	}
}
