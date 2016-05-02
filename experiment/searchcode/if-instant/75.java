package org.dftproject.genesis.ui.pages.circlediagram;

import org.dftproject.genesis.data.genealogy.GenealogyConstants;
import org.dftproject.genesis.data.genealogy.GenealogyUtils;
import org.dftproject.genesis.data.genealogy.IInstant;
import org.dftproject.genesis.data.genealogy.IName;
import org.dftproject.genesis.data.genealogy.IPerson;
import org.dftproject.genesis.data.genealogy.IRole;
import org.dftproject.genesis.data.genealogy.Sex;

public class CircleDiagramNode {

	private final IPerson person;

	private final String name;
	private final String summary;
	private Sex sex;

	private CircleDiagramNode left;
	private CircleDiagramNode right;
	private boolean leftSet;
	private boolean rightSet;

	public CircleDiagramNode(IPerson person) {
		this(person, getName(person), getSex(person), getSummary(person));
	}
	
	public CircleDiagramNode(IPerson person, String name, Sex sex, String summary) {
		this.person = person;
		this.name = name;
		this.sex = sex;
		this.summary = summary;
	}

	public IPerson getPerson() {
		return person;
	}

	public String getName() {
		return name;
	}

	public String getSummary() {
		return summary;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public CircleDiagramNode getLeft() {
		return left;
	}

	public void setLeft(CircleDiagramNode left) {
		this.left = left;
		leftSet = true;
	}
	
	public boolean isLeftSet() {
		return leftSet;
	}

	public CircleDiagramNode getRight() {
		return right;
	}

	public void setRight(CircleDiagramNode right) {
		this.right = right;
		rightSet = true;
	}
	
	public boolean isRightSet() {
		return rightSet;
	}

	public boolean hasMore() {
		return left != null || right != null;
	}

	/**
	 * Returns the standard name of a person.
	 * <p>
	 * This method may be called on a worker thread.
	 * @param person a person
	 * @return the name of the person
	 */
	public static String getName(IPerson person) {
		IName name = GenealogyUtils.getName(person);
		return (name != null ? GenealogyUtils.stringFromName(name) : "(unknown)");
	}

	/**
	 * Returns the standard sex of a person.
	 * <p>
	 * This method may be called on a worker thread.
	 * @param person a person
	 * @return the sex of the person
	 */
	public static Sex getSex(IPerson person) {
		return GenealogyUtils.getSex(person);
	}

	/**
	 * Returns the standard summary for a person.
	 * <p>
	 * This method may be called on a worker thread.
	 * @param person a person
	 * @return the summary for the person
	 */
	public static String getSummary(IPerson person) {
		StringBuilder sb = new StringBuilder();
		for (IRole role : person.getRoles(GenealogyConstants.Child)) {
			IInstant instant = GenealogyUtils.getDate(role.getEvent());
			if (instant != null) {
				String birthDate = GenealogyUtils.stringFromInstant(instant);
				if (birthDate != null) {
					if (sb.length() > 0)
						sb.append("\n");
					sb.append("b. ").append(birthDate);
				}
				break;
			}
		}

		for (IRole role : person.getRoles(GenealogyConstants.Deceased)) {
			IInstant instant = GenealogyUtils.getDate(role.getEvent());
			if (instant != null) {
				String deathDate = GenealogyUtils.stringFromInstant(instant);
				if (deathDate != null) {
					if (sb.length() > 0)
						sb.append("\n");
					sb.append("d. ").append(deathDate);
				}
				break;
			}
		}

		return sb.toString();
	}

}
