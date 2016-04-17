package de.uni_passau.facultyinfo.client.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BusinessHoursFacility {
	public static final int TYPE_CAFETERIA = 1;
	public static final int TYPE_LIBRARY = 2;

	private String id;
	private String name;
	private int type;

	private List<BusinessHours> businessHours;

	@JsonCreator
	public BusinessHoursFacility(@JsonProperty("id") String id,
			@JsonProperty("name") String name, @JsonProperty("type") int type) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public BusinessHours getBusinessHours(int phase, int dayOfWeek) {
		BusinessHours businessHours = null;
		if (getBusinessHours() != null) {
			for (BusinessHours currentBusinessHours : getBusinessHours()) {
				if (currentBusinessHours.getPhase() == phase
						&& currentBusinessHours.getDayOfWeek() == dayOfWeek) {
					businessHours = currentBusinessHours;
					break;
				}
			}
		}
		return businessHours;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<BusinessHours> getBusinessHours() {
		return businessHours;
	}

	public void setBusinessHours(List<BusinessHours> businessHours) {
		this.businessHours = businessHours;
	}

}

