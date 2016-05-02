package uniLearn.data;

public class AssignmentMaterial {
	//define
	//public static final int ID_MAX_LENGTH = 10;
	public static final int NAME_MAX_LENGTH = 50;
	public static final int FILENAME_MAX_LENGTH = 50;
	
	private int order;
	private String name;
	private int acId;
	private String fileName;
	private String info;
	
	//constructor
	public AssignmentMaterial(int acId, int order, String name) {
		this.name = name;
		this.acId = acId;
		this.order = order;
		this.fileName = "";
		this.info = "";
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		if(fileName == null)
			this.fileName = "";
		else if(fileName.length() > FILENAME_MAX_LENGTH)
			this.fileName = fileName.substring(0, FILENAME_MAX_LENGTH);
		this.fileName = fileName;
	}

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * @return the acId
	 */
	public int getAcId() {
		return acId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + acId;
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((info == null) ? 0 : info.hashCode());
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
		AssignmentMaterial other = (AssignmentMaterial) obj;
		if (acId != other.acId)
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
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
		builder.append("AssignmentMaterial [order=");
		builder.append(order);
		builder.append(", name=");
		builder.append(name);
		builder.append(", acId=");
		builder.append(acId);
		builder.append(", fileName=");
		builder.append(fileName);
		builder.append(", info=");
		builder.append(info);
		builder.append("]");
		return builder.toString();
	}

	
}

