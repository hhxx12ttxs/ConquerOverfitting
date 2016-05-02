package uniLearn.data;

import java.math.BigDecimal;

public class MarkingScheme {
	//define
	//public static final int ID_MAX_LENGTH = 10;
	public static final int NAME_MAX_LENGTH = 50;
	
	//variable
	private int acId;
	private int order;
	private String name;
	private String criteria;
	private BigDecimal mark;
	
	//constructor
	public MarkingScheme(int acID, int order, String name) {
		this.acId = acID;
		this.order = order;
		this.name = name;
		this.mark = BigDecimal.ZERO;
	}

	//get method
	/**
	 * @return the acId
	 */
	public int getAcId() {
		return acId;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the criteria
	 */
	public String getCriteria() {
		return criteria;
	}

	/**
	 * @return the mark
	 */
	public BigDecimal getMark() {
		return mark;
	}

	//set method
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if(name == null || name.length() > NAME_MAX_LENGTH)
			this.name = "";
		else
			this.name = name;
	}

	/**
	 * @param criteria the criteria to set
	 */
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	/**
	 * @param mark the mark to set
	 */
	public void setMark(BigDecimal mark) {
		this.mark = mark == null ? BigDecimal.ZERO : mark;
	}

	//override
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + acId;
		result = prime * result
				+ ((criteria == null) ? 0 : criteria.hashCode());
		result = prime * result + ((mark == null) ? 0 : mark.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + order;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarkingScheme other = (MarkingScheme) obj;
		if (acId != other.acId)
			return false;
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria))
			return false;
		if (mark == null) {
			if (other.mark != null)
				return false;
		} else if (!mark.equals(other.mark))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (order != other.order)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MarkingScheme [acId=");
		builder.append(acId);
		builder.append(", order=");
		builder.append(order);
		builder.append(", name=");
		builder.append(name);
		builder.append(", criteria=");
		builder.append(criteria);
		builder.append(", mark=");
		builder.append(mark);
		builder.append("]");
		return builder.toString();
	}

	
}

