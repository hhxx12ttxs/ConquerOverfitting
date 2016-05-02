package org.dftproject.genesis.data.genealogy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.dftproject.genesis.data.IAttribute;
import org.dftproject.genesis.data.IEntity;

public class GenealogyUtils {

	/**
	 * Returns the name of a person.
	 * @param person a person
	 * @return the name
	 */
	public static IName getName(IPerson person) {
		return (IName) getValue(person, GenealogyConstants.name, IName.class);
	}

	/**
	 * Returns the sex of a person.
	 * @param person a person
	 * @return the sex
	 */
	public static Sex getSex(IPerson person) {
		return (Sex) getValue(person, GenealogyConstants.sex, Sex.class);
	}

	/**
	 * Returns the father of a person.
	 * @param person a person
	 * @return the father
	 */
	public static IPerson getFather(IPerson person) {
		if (person == null)
			return null;
		Set<IRole> roles = person.getRoles(GenealogyConstants.Child);
		for (IRole role : roles) {
			IEvent birth = role.getEvent();
			if (birth != null) {
				Set<IRole> fatherRoles = birth.getRoles(GenealogyConstants.Father);
				for (IRole fatherRole : fatherRoles) {
					IPerson father = fatherRole.getPerson();
					if (father != null)
						return father;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the mother of a person.
	 * @param person a person
	 * @return the mother
	 */
	public static IPerson getMother(IPerson person) {
		if (person == null)
			return null;
		Set<IRole> roles = person.getRoles(GenealogyConstants.Child);
		for (IRole role : roles) {
			IEvent birth = role.getEvent();
			if (birth != null) {
				Set<IRole> motherRoles = birth.getRoles(GenealogyConstants.Mother);
				for (IRole motherRole : motherRoles) {
					IPerson mother = motherRole.getPerson();
					if (mother != null)
						return mother;
				}
			}
		}
		return null;
	}

	/**
	 * Returns all the children of a person.
	 * @param person a person
	 * @return all the children
	 */
	public static Set<IPerson> getChildren(IPerson person) {
		Set<IPerson> children = new HashSet<IPerson>();

		Set<IRole> roles = person.getRoles(new String[] { GenealogyConstants.Parent, GenealogyConstants.Father, GenealogyConstants.Mother });
		for (IRole role : roles) {
			IEvent event = role.getEvent();
			if (event != null) {
				Set<IRole> childRoles = event.getRoles(GenealogyConstants.Child);
				for (IRole childRole : childRoles) {
					IPerson child = childRole.getPerson();
					if (child != null)
						children.add(child);
				}
			}
		}

		return Collections.unmodifiableSet(children);
	}

	/**
	 * Returns all the spouses of a person.
	 * @param person a person
	 * @return all the spouses
	 */
	public static Set<IPerson> getSpouses(IPerson person) {
		Set<IPerson> spouses = new HashSet<IPerson>();

		// Find all marriage events

		Set<IRole> roles = person.getRoles(GenealogyConstants.Spouse);
		for (IRole role : roles) {
			IEvent event = role.getEvent();
			if (event != null) {
				// Find all spouses for this marriage

				Set<IRole> marriageRoles = event.getRoles(GenealogyConstants.Spouse);
				for (IRole marriageRole : marriageRoles) {
					// Add any spouse that isn't this person

					IPerson spouse = marriageRole.getPerson();
					if (!person.sameAs(spouse))
						spouses.add(spouse);
				}
			}
		}

		return Collections.unmodifiableSet(spouses);
	}

	/**
	 * Returns all the siblings of a person.
	 * @param person a person
	 * @return all the siblings
	 */
	public static Set<IPerson> getSiblings(IPerson person) {
		Set<IPerson> siblings = new HashSet<IPerson>();

		// Add all of father's children

		IPerson father = getFather(person);
		if (father != null)
			siblings.addAll(getChildren(father));

		// Add all of mother's children

		IPerson mother = getMother(person);
		if (mother != null)
			siblings.addAll(getChildren(mother));

		// Remove self

		siblings.remove(person);

		return Collections.unmodifiableSet(siblings);
	}

	/**
	 * Returns the date of an event.
	 * @param event an event
	 * @return the date
	 */
	public static IInstant getDate(IEvent event) {
		return (IInstant) getValue(event, Events.instant, IInstant.class);
	}

	/**
	 * Returns the place of an event.
	 * @param event an event
	 * @return the place
	 */
	public static IPlace getPlace(IEvent event) {
		return (IPlace) getValue(event, Events.place, IPlace.class);
	}

	/**
	 * Returns the first value of a given type for an attribute of an entity.
	 * @param entity an entity
	 * @param name the attribute name
	 * @param type the value class or superclass
	 * @return the first qualifying value
	 */
	public static Object getValue(IEntity entity, String name, Class<?> type) {
		List<Object> values = getValues(entity, name, type);
		if (values.isEmpty())
			return null;
		return values.get(0);
	}

	/**
	 * Returns all values of a given type for an attribute of an entity.
	 * @param entity an entity
	 * @param name the attribute name
	 * @param type the value class or superclass
	 * @return all qualifying values
	 */
	public static List<Object> getValues(IEntity entity, String name, Class<?> type) {
		List<Object> matchingValues = new LinkedList<Object>();

		Set<IAttribute> attributes = entity.getAttributes(name);
		for (IAttribute attribute : attributes) {
			Object value = attribute.getValue();
			if (type.isAssignableFrom(value.getClass()))
				matchingValues.add(value);
		}

		return Collections.unmodifiableList(matchingValues);
	}

	public static String stringFromInstant(IInstant instant) {
		if (instant == null)
			return null;
		return instant.toString(Locale.getDefault());
	}

	public static String stringFromName(IName name) {
		return stringFromNameParts(name.getNameParts());
	}
	
	public static String stringFromNameParts(List<INamePart> nameParts) {
		StringBuilder sb = new StringBuilder();
		
		Iterator<INamePart> it = nameParts.iterator();
		while (it.hasNext()) {
			sb.append(it.next().getValue());
			if (it.hasNext())
				sb.append(" ");
		}
		
		return sb.toString();
	}
	
	public static List<INamePart> getNameParts(IName name, String type) {
		List<INamePart> parts = new LinkedList<INamePart>();
		
		for (INamePart part : name.getNameParts()) {
			if (type.equals(part.getType()))
				parts.add(part);
		}
		
		return parts;
	}

}

