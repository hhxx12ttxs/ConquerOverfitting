package lrg.common.abstractions.plugins.conformities;

import java.util.ArrayList;
import java.util.HashSet;

import lrg.common.abstractions.entities.AbstractEntityInterface;
import lrg.common.abstractions.entities.GroupEntity;
import lrg.common.abstractions.entities.ResultEntity;
import lrg.common.abstractions.plugins.Descriptor;
import lrg.common.abstractions.plugins.properties.PropertyComputer;
public class ConformityRule extends PropertyComputer implements StackItem {
	private static final long serialVersionUID = 6869073726875797577L;
	protected String propertyDescriptor;
	protected String conformityOperator;

	protected Object threshold;

	public ConformityRule(Descriptor theDescriptor) {
		super(theDescriptor.getName(), theDescriptor.getInfo(), theDescriptor.getAllEntityTypeNames(), "numeric");
	}

	public ConformityRule(String propertyName, String conformityOperator, String appliesToEntityTypeName) {
		super(appliesToEntityTypeName + ": " + propertyName + " " + conformityOperator, "", appliesToEntityTypeName, "numeric");
		initializeMembers(propertyName, conformityOperator, null);
	}

	public ConformityRule(String propertyName, String conformityOperator, String appliesToEntityTypeName, Object threshold) {
		super(appliesToEntityTypeName + ": " + propertyName + " " + conformityOperator + " " + threshold, "", appliesToEntityTypeName, "numeric");
		initializeMembers(propertyName, conformityOperator, threshold);
	}

	public ConformityRule(String propertyName, String conformityOperator, String appliesToEntityTypeName, double threshold) {
		super(appliesToEntityTypeName + ": " + propertyName + " " + conformityOperator + " " + threshold, "", appliesToEntityTypeName, "numeric");
		initializeMembers(propertyName, conformityOperator, new Double(threshold));
	}

	public String createNameForFilteredGroup(GroupEntity groupEntity) {
		return getDescriptorObject().getName() + " filter on (" + groupEntity.getName() + ")";
	}

	public ResultEntity compute(AbstractEntityInterface anEntity) {
		return new ResultEntity(applyConformity(anEntity));
	}

	public Double applyConformity(AbstractEntityInterface anEntity) {
		if (anEntity == null)
			return new Double(0);
		ResultEntity theResult = anEntity.getProperty(propertyDescriptor);
		if (theResult == null)
			return new Double(0);
		return theResult.applyConformity(conformityOperator, threshold) * weight;
	}

	public String toString() {
		if (this.getDescriptorObject().getName().indexOf("<html>") == -1)
			return this.getDescriptorObject().getName();
		else {
			return "<html>" + this.getDescriptorObject().getName();
		}
	}

	private void initializeMembers(String propertyName, String filteringOperator, Object threshold) {
		this.propertyDescriptor = propertyName;
		this.conformityOperator = filteringOperator;
		this.threshold = threshold;
	}
	
	private double weight = 1;
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public double getWeight() {
		return weight;
	}

	public String[] getIntersectionofEntityTypeNames(ConformityRule otherRule) {
		String[] thisETNames = this.getDescriptorObject().getAllEntityTypeNames();
		String[] otherETNames = this.getDescriptorObject().getAllEntityTypeNames();
		ArrayList<String> thisETNamesArray = new ArrayList<String>();
		ArrayList<String> otherETNamesArray = new ArrayList<String>();

		for (int i = 0; i < thisETNames.length; i++)
			thisETNamesArray.add(thisETNames[i]);
		for (int i = 0; i < otherETNames.length; i++)
			otherETNamesArray.add(otherETNames[i]);

		thisETNamesArray.retainAll(otherETNamesArray);

		if (thisETNamesArray.size() == 0)
			return new String[] { "" };
		return thisETNamesArray.toArray(new String[thisETNamesArray.size()]);
	}

	public String[] getUnionofEntityTypeNames(ConformityRule otherRule) {
		String[] thisETNames = this.getDescriptorObject().getAllEntityTypeNames();
		String[] otherETNames = this.getDescriptorObject().getAllEntityTypeNames();
		HashSet<String> allDistinctETNames = new HashSet<String>();

		for (int i = 0; i < thisETNames.length; i++)
			allDistinctETNames.add(thisETNames[i]);
		for (int i = 0; i < otherETNames.length; i++)
			allDistinctETNames.add(otherETNames[i]);

		if (allDistinctETNames.size() == 0)
			return new String[] { "" };
		return allDistinctETNames.toArray(new String[allDistinctETNames.size()]);
	}
}

