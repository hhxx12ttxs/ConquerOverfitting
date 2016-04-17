package com.artezio.arttime.web;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;

import com.artezio.arttime.datamodel.Employee;
import com.artezio.arttime.datamodel.HourType;
import com.artezio.arttime.datamodel.Hours;
import com.artezio.arttime.datamodel.Project;

public class HoursIndexedBundle {
	private List<Hours> hours;
	private HoursBundle typeEmployeeDateBundle;
	private HoursBundle typeBundle;
	private HoursBundle typeDateBundle;
	private HoursBundle typeEmployeeBundle;
	private HoursBundle projectTypeDateBundle;
	private HoursBundle projectTypeEmployeeDateBundle;
	private HoursBundle projectTypeEmployeeBundle;
	private HoursBundle dateBundle;
	private HoursBundle projectBundle;
	private HoursBundle employeeBundle;
	private HoursBundle projectEmployeeBundle;
	private HoursBundle projectTypeBundle;

	public HoursIndexedBundle(List<Hours> hours) {
		this.hours = hours;
		createTypeEmployeeDateBundle();
		createTypeBundle();
		createTypeDateBundle();
		createTypeEmployeeBundle();
		createProjectTypeDateBundle();
		createProjectTypeEmployeeDateBundle();
		createProjectTypeEmployeeBundle();
		createDateBundle();
		createProjectBundle();
		createEmployeeBundle();
		createProjectEmployeeBundle();
		createProjectTypeBundle();
	}

	public void add(Hours hour) {
		addToTypeEmployeeDateBundle(hour);
		addToTypeBundle(hour);
		addToTypeDateBundle(hour);
		addToTypeEmployeeBundle(hour);
		addToProjectTypeDateBundle(hour);
		addToProjectTypeEmployeeDateBundle(hour);
		addToProjectTypeEmployeeBundle(hour);
		addToDateBundle(hour);
		addToProjectBundle(hour);
		addToEmployeeBundle(hour);
		addToProjectEmployeeBundle(hour);
		addToProjectTypeBundle(hour);		
		hours.add(hour);
	}

	public Set<Employee> getUsedEmployees() {
		Stream<HoursKey> hoursKeys = employeeBundle.keySet().parallelStream();
		return hoursKeys
				.map(hoursKey -> hoursKey.getEmployee())
				.collect(Collectors.toSet());
	}

	public Set<Employee> getUsedEmployees(Project project) {
		List<Hours> hours = getHours(project);
		return getEmployees(hours);
	}

	public Set<HourType> getUsedHourTypes() {
		Stream<HoursKey> hoursKeys = typeBundle.keySet().parallelStream();
		return hoursKeys
				.map(hoursKey -> hoursKey.getHourType())
				.collect(Collectors.toSet());
	}

	public Set<HourType> getUsedHourTypes(Project project) {
		List<Hours> hours = getHours(project);
		return getHourTypes(hours);
	}

	public Set<HourType> getUsedHourTypes(Employee employee) {
		List<Hours> hours = getHours(employee);
		return getHourTypes(hours);		
	}

	public Set<Project> getUsedProjects() {
		List<Hours> hours = getHours();
		return getProjects(hours);		
	}

	public Set<Project> getUsedProjects(Employee employee) {
		List<Hours> hours = getHours(employee);
		return getProjects(hours);
	}

	public Set<HourType> getUsedHourTypes(Employee employee, Project project) {
		List<Hours> hours = getHours(project, employee);
		return getHourTypes(hours);
	}

	private Set<HourType> getHourTypes(List<Hours> hours) {
		return (hours == null)
				? new HashSet<HourType>()
				: hours
						.parallelStream()
						.map(hour -> hour.getType())
						.collect(Collectors.toSet());
	}
	
	private Set<Project> getProjects(List<Hours> hours) {
		if (hours == null) {
			return new HashSet<Project>();
		}
		return hours
				.parallelStream()
				.map(hour -> hour.getProject())
				.collect(Collectors.toSet());
	}
	
	private Set<Employee> getEmployees(List<Hours> hours) {
		return (hours == null)
				? new HashSet<Employee>()
				: hours
						.parallelStream()
						.map(hour -> hour.getEmployee())
						.collect(Collectors.toSet());
	}

	private void addToProjectTypeBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getProject(), hour.getType());
		projectTypeBundle.add(hoursKey, hour);
	}

	private void addToProjectEmployeeBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getProject(), hour.getEmployee());
		projectEmployeeBundle.add(hoursKey, hour);
	}

	private void addToEmployeeBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getEmployee());
		employeeBundle.add(hoursKey, hour);
	}

	private void addToProjectBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getProject());
		projectBundle.add(hoursKey, hour);
	}

	private void addToDateBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getDate());
		dateBundle.add(hoursKey, hour);
	}

	private void addToProjectTypeEmployeeBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getProject(), hour.getType(),
				hour.getEmployee());
		projectTypeEmployeeBundle.add(hoursKey, hour);
	}

	private void addToProjectTypeEmployeeDateBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getProject(), hour.getType(),
				hour.getEmployee(), hour.getDate());
		projectTypeEmployeeDateBundle.add(hoursKey, hour);
	}

	private void addToProjectTypeDateBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getProject(), hour.getType(),
				hour.getDate());
		projectTypeDateBundle.add(hoursKey, hour);
	}

	private void addToTypeEmployeeBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getType(), hour.getEmployee());
		typeEmployeeBundle.add(hoursKey, hour);
	}

	private void addToTypeDateBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getType(), hour.getDate());
		typeDateBundle.add(hoursKey, hour);
	}

	private void addToTypeBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getType());
		typeBundle.add(hoursKey, hour);
	}

	private void addToTypeEmployeeDateBundle(Hours hour) {
		HoursKey hoursKey = new HoursKey(hour.getType(), hour.getEmployee(),
				hour.getDate());
		typeEmployeeDateBundle.add(hoursKey, hour);
	}

	private HoursBundle createTypeEmployeeDateBundle() {
		typeEmployeeDateBundle = new HoursBundle();
		hours.forEach(hour -> addToTypeEmployeeDateBundle(hour));				
		return typeEmployeeDateBundle;
	}

	private HoursBundle createTypeBundle() {
		typeBundle = new HoursBundle();
		hours.forEach(hour -> addToTypeBundle(hour));
		return typeBundle;
	}

	private HoursBundle createTypeDateBundle() {
		typeDateBundle = new HoursBundle();
		hours.forEach(hour -> addToTypeDateBundle(hour));
		return typeDateBundle;
	}

	private HoursBundle createTypeEmployeeBundle() {
		typeEmployeeBundle = new HoursBundle();
		hours.forEach(hour -> addToTypeEmployeeBundle(hour));		
		return typeEmployeeBundle;
	}

	private HoursBundle createProjectTypeDateBundle() {
		projectTypeDateBundle = new HoursBundle();
		hours.forEach(hour -> addToProjectTypeDateBundle(hour));		
		return projectTypeDateBundle;
	}

	private HoursBundle createProjectTypeBundle() {
		projectTypeBundle = new HoursBundle();
		hours.forEach(hour -> addToProjectTypeBundle(hour));		
		return projectTypeBundle;
	}

	private HoursBundle createProjectTypeEmployeeDateBundle() {
		projectTypeEmployeeDateBundle = new HoursBundle();
		hours.forEach(hour -> addToProjectTypeEmployeeDateBundle(hour));		
		return projectTypeEmployeeDateBundle;
	}

	private HoursBundle createProjectTypeEmployeeBundle() {
		projectTypeEmployeeBundle = new HoursBundle();
		hours.forEach(hour -> addToProjectTypeEmployeeBundle(hour));		
		return projectTypeEmployeeBundle;
	}

	private HoursBundle createDateBundle() {
		dateBundle = new HoursBundle();
		hours.forEach(hour -> addToDateBundle(hour));
		return dateBundle;
	}

	private HoursBundle createProjectBundle() {
		projectBundle = new HoursBundle();
		hours.forEach(hour -> addToProjectBundle(hour));
		return projectBundle;
	}

	private HoursBundle createEmployeeBundle() {
		employeeBundle = new HoursBundle();
		hours.forEach(hour -> addToEmployeeBundle(hour));
		return employeeBundle;
	}

	private HoursBundle createProjectEmployeeBundle() {
		projectEmployeeBundle = new HoursBundle();
		hours.forEach(hour -> addToProjectEmployeeBundle(hour));
		return projectEmployeeBundle;
	}

	public List<Hours> getHours() {
		return hours;
	}

	public List<Hours> getHours(HourType type, Employee employee, Date date) {
		HoursKey hoursKey = new HoursKey(type, employee, date);
		return typeEmployeeDateBundle.get(hoursKey);
	}

	public List<Hours> getHours(HourType type) {
		HoursKey hoursKey = new HoursKey(type);
		return typeBundle.get(hoursKey);
	}

	public List<Hours> getHours(HourType type, Date date) {
		HoursKey hoursKey = new HoursKey(type, date);
		return typeDateBundle.get(hoursKey);
	}

	public List<Hours> getHours(HourType type, Employee employee) {
		HoursKey hoursKey = new HoursKey(type, employee);
		return typeEmployeeBundle.get(hoursKey);
	}

	public List<Hours> getHours(Project project, HourType type, Date date) {
		HoursKey hoursKey = new HoursKey(project, type, date);
		return projectTypeDateBundle.get(hoursKey);
	}

	public List<Hours> getHours(Project project, HourType type) {
		HoursKey hoursKey = new HoursKey(project, type);
		return projectTypeBundle.get(hoursKey);
	}

	public List<Hours> getHours(Project project, HourType type,
			Employee employee, Date date) {
		HoursKey hoursKey = new HoursKey(project, type, employee, date);
		return projectTypeEmployeeDateBundle.get(hoursKey);
	}

	public List<Hours> getHours(Project project, HourType type,
			Employee employee) {
		HoursKey hoursKey = new HoursKey(project, type, employee);
		return projectTypeEmployeeBundle.get(hoursKey);
	}

	public List<Hours> getHours(Date date) {
		HoursKey hoursKey = new HoursKey(date);
		return dateBundle.get(hoursKey);
	}

	public List<Hours> getHours(Project project) {
		HoursKey hoursKey = new HoursKey(project);
		return projectBundle.get(hoursKey);
	}

	public List<Hours> getHours(Employee employee) {
		HoursKey hoursKey = new HoursKey(employee);
		return employeeBundle.get(hoursKey);
	}

	public List<Hours> getHours(Project project, Employee employee) {
		HoursKey hoursKey = new HoursKey(project, employee);
		return projectEmployeeBundle.get(hoursKey);
	}

	class HoursBundle {
		private Map<HoursKey, List<Hours>> hoursBundle;

		public HoursBundle() {
			hoursBundle = new ConcurrentHashMap<HoursKey, List<Hours>>();
		}

		public void add(HoursKey key, Hours hours) {
			List<Hours> hoursList;
			if (hoursBundle.containsKey(key)) {
				hoursList = hoursBundle.get(key);
			} else {
				hoursList = new LinkedList<Hours>();
				hoursBundle.put(key, hoursList);
			}
			hoursList.add(hours);
		}

		public List<Hours> get(HoursKey key) {
			return hoursBundle.containsKey(key) ? hoursBundle.get(key) : null;
		}

		public Set<HoursKey> keySet() {
			return hoursBundle.keySet();
		}
	}

	class HoursKey {
		private Project project;
		private HourType type;
		private Employee employee;
		private Date date;

		public HoursKey() {
		}

		public HoursKey(HourType type, Employee employee, Date date) {
			this.type = type;
			this.employee = employee;
			this.date = date;
		}

		public HoursKey(HourType type) {
			this.type = type;
		}

		public HoursKey(HourType type, Date date) {
			this.type = type;
			this.date = date;
		}

		public HoursKey(HourType type, Employee employee) {
			this.type = type;
			this.employee = employee;
		}

		public HoursKey(Project project, HourType type, Date date) {
			this.project = project;
			this.type = type;
			this.date = date;
		}

		public HoursKey(Project project, HourType type) {
			this.project = project;
			this.type = type;
		}

		public HoursKey(Project project, HourType type, Employee employee,
				Date date) {
			this.project = project;
			this.type = type;
			this.employee = employee;
			this.date = date;
		}

		public HoursKey(Employee employee, Project project, HourType type) {
			this.project = project;
			this.type = type;
			this.employee = employee;
		}

		public HoursKey(Project project, HourType type, Employee employee) {
			this.project = project;
			this.type = type;
			this.employee = employee;
		}

		public HoursKey(Date date) {
			this.date = date;
		}

		public HoursKey(Project project) {
			this.project = project;
		}

		public HoursKey(Employee employee) {
			this.employee = employee;
		}

		public HoursKey(Project project, Employee employee) {
			this.project = project;
			this.employee = employee;
		}

		public Project getProject() {
			return project;
		}

		public void setProject(Project project) {
			this.project = project;
		}
		
		public HourType getHourType() {
			return type;
		}

		public void setHourType(HourType type) {
			this.type = type;
		}

		public Employee getEmployee() {
			return employee;
		}

		public void setEmployee(Employee employee) {
			this.employee = employee;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result
					+ ((employee == null) ? 0 : employee.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result
					+ ((project == null) ? 0 : project.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			HoursKey other = (HoursKey) obj;
			if (date == null) {
				if (other.date != null)
					return false;
			} else if (!date.equals(other.date))
				return false;
			if (employee == null) {
				if (other.employee != null)
					return false;
			} else if (!employee.equals(other.employee))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			if (project == null) {
				if (other.project != null)
					return false;
			} else if (!project.equals(other.project))
				return false;			
			return true;
		}
	}

	public Hours getSingleHours(Project project, HourType hourType, Employee employee, Date date) {
		List<Hours> hours = getHours(project, hourType, employee, date);
		return (CollectionUtils.isEmpty(hours))
				? null
				: hours.get(0);
	}
	
	public boolean isHoursExists(Project project, HourType hourType, Employee employee, Date date) {
		List<Hours> hours = getHours(project, hourType, employee, date);
		return (hours != null);
	}

}

