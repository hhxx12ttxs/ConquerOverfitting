package org.gvsig.hyperlink.config;

import com.iver.utiles.IPersistence;
import com.iver.utiles.XMLEntity;

public class LinkConfig implements IPersistence {
	private String fieldName;
	private String extension = "";
	private String actionCode;

	protected LinkConfig() {
	}

	public LinkConfig(String actionCode, String fieldName) {
		this.actionCode = actionCode;
		this.fieldName = fieldName;
	}

	public LinkConfig(String actionCode, String fieldName, String extension) {
		this.actionCode = actionCode;
		this.fieldName = fieldName;
		this.extension = extension;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public String getClassName() {
		return this.getClass().getName();
	}

	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", this.getClassName());
		xml.putProperty("actionCode", getActionCode());
		xml.putProperty("fieldName", getFieldName());
		xml.putProperty("extension", getExtension());
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
		if (xml.contains("actionCode")) {
			setActionCode(xml.getStringProperty("actionCode"));
		}
		else {
			setActionCode("");
		}
		if (xml.contains("fieldName")) {
			setFieldName(xml.getStringProperty("fieldName"));
		}
		else {
			setFieldName("");
		}
		if (xml.contains("extension")) {
			setExtension(xml.getStringProperty("extension"));
		}
	}

	public static LinkConfig createFromXMLEntity(XMLEntity xml) {
		LinkConfig link = new LinkConfig();
		link.setXMLEntity(xml);
		return link;
	}
}

