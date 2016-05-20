package lrg.common.abstractions.plugins.conformities.composed;

import java.util.LinkedList;
import java.util.List;

import lrg.common.abstractions.entities.AbstractEntityInterface;
import lrg.common.abstractions.entities.GroupEntity;
import lrg.common.abstractions.entities.ResultEntity;
import lrg.common.abstractions.plugins.conformities.ConformityRule;
import lrg.common.abstractions.plugins.conformities.StackItem;

public class ComposedMetricConformityRule extends ConformityRule implements StackItem {
	private static final long serialVersionUID = -5259114155504416172L;
	protected String[] properties;
//	protected String conformityOperator;

//	Object threshold;

	public ComposedMetricConformityRule(String[] metrics, String operator, String appliesToEntityTypeName, double threshold) {
		super(appliesToEntityTypeName + ": " + metrics.toString() + " "
				+ operator + " " + threshold, "",
				appliesToEntityTypeName, "numeric");

		initializeMembers(metrics, operator, threshold);
	}

	public String createNameForFilteredGroup(GroupEntity groupEntity) {

		return getDescriptorObject().getName() + " filter on ("
				+ groupEntity.getName() + ")";

	}

	public ResultEntity compute(AbstractEntityInterface anEntity) {
		return new ResultEntity(applyConformity(anEntity));
	}

	public Double applyConformity(AbstractEntityInterface anEntity) {
		if (anEntity == null)
			return new Double(0);
		List<ResultEntity> partialResults = new LinkedList<ResultEntity>();
		for (String property : properties)
			partialResults.add(anEntity.getProperty(property));
		double result = 0;
		for (ResultEntity r : partialResults)
			result += (Double)r.getValue();
		
		ResultEntity theResult = new ResultEntity(result);
		if (theResult == null)
			return new Double(0);
		return theResult.applyConformity(conformityOperator, threshold) * getWeight();
	}

	public String toString() {
		if (this.getDescriptorObject().getName().indexOf("<html>") == -1)
			return this.getDescriptorObject().getName();
		else {
			return "<html>" + this.getDescriptorObject().getName();
		}
	}

	private void initializeMembers(String[] propertyName,
			String filteringOperator, Object threshold) {

		this.properties = propertyName;

		this.conformityOperator = filteringOperator;

		this.threshold = threshold;

	}
}

