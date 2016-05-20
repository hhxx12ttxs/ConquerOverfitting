package datastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {
	private List<ProjectProperty> projectProperties;
	// A somewhat convoluted way to solve searching for fetching properties by
	// name. The best I got at the moment though.
	private Map<String, Integer> searchMap;
	private boolean databaseProject;

	/**
	 * 
	 * @param databaseProject
	 *            true if it's a project for the database. false if it's the
	 *            project that is going to be examined.
	 */
	public Project(boolean databaseProject) {
		projectProperties = new ArrayList<ProjectProperty>();
		searchMap = new HashMap<String, Integer>();
		this.databaseProject = databaseProject;
	}

	public void addProperty(String name, Object property) {
		projectProperties.add(new ProjectProperty(name, property, databaseProject));
		searchMap.put(name, projectProperties.size() - 1);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (ProjectProperty p : projectProperties) {
			sb.append(p.toString());
			sb.append(" | ");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Returns the property with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public ProjectProperty getProperty(String name) {
		return projectProperties.get(searchMap.get(name));
	}

	/**
	 * Returns all properties.
	 * 
	 * @param name
	 * @return
	 */
	public List<ProjectProperty> getProperties() {
		return projectProperties;
	}

	/**
	 * Gets the amount of effort (person-hours) this project required.
	 * 
	 * @return
	 */
	public double getEffort() {
		return projectProperties.get(searchMap.get("effort")).getDoubleValue();
	}

	/**
	 * Returns the similarity of this Project to another Project by using the
	 * Euclidean Distance formula given in the lecture notes.
	 * 
	 * @param p
	 *            The other project.
	 * @return A double (0.0 - 1.0) determining the projects similarity.
	 */
	public double similarityTo(Project p) {
		double totalDistance = 0;
		int n = 0;
		for (ProjectProperty pp : this.projectProperties) {
			// Uses the searchMap to find the property in the other project that
			// has the same name.
			totalDistance += pp.getEuclideanDistance(p.projectProperties.get((p.searchMap.get(pp
					.getName()))));
			n++;
		}
		return 1 - Math.sqrt(totalDistance / n);
	}

	// (double) ProjectDatabase.getInstance().getNumberOfProjects()

	/**
	 * Calculates effort based one project and seven factors.
	 * 
	 * @param compareProject
	 *            Project to compare with
	 * @return the effort (in ph)
	 */

	public double examineWithAllFactors(Project compareProject) {
		double factor = 0;
		int n = 0;
		for (ProjectProperty pp : this.projectProperties) {
			if (pp.getPropertyType().equals(Double.class) && !pp.getName().equals("effort")) {
				if (compareProject.getProperty(pp.getName()).getDoubleValue() != 0) {
					factor += this.getProperty(pp.getName()).getDoubleValue()
							/ compareProject.getProperty(pp.getName()).getDoubleValue();
					n++;
				}
				// System.out.println(this.getProperty(pp.getName()).getDoubleValue());
				// System.out.println(compareProject.getProperty(pp.getName()).getDoubleValue());
				// System.out.println(factor);
			}
		}
		System.out.println(factor);
		System.out.println(n);
		System.out.println(compareProject.getProperty("effort").getDoubleValue());
		return (factor / n) * compareProject.getProperty("effort").getDoubleValue();
	}
}

