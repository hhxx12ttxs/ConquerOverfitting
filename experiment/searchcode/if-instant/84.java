package org.dftproject.lineagelinkage.core;

import genealogy.deprecated.datamodel.pedigree.Date;
import genealogy.deprecated.datamodel.pedigree.EventType;
import genealogy.deprecated.datamodel.pedigree.Gender;
import genealogy.deprecated.datamodel.pedigree.Individual;
import genealogy.deprecated.datamodel.pedigree.LifeEvent;
import genealogy.deprecated.datamodel.pedigree.Location;
import genealogy.deprecated.datamodel.pedigree.PedigreeValidationError;

import java.util.Set;

import org.dftproject.genesis.data.genealogy.Events;
import org.dftproject.genesis.data.genealogy.GenealogyConstants;
import org.dftproject.genesis.data.genealogy.GenealogyUtils;
import org.dftproject.genesis.data.genealogy.IInstant;
import org.dftproject.genesis.data.genealogy.IEvent;
import org.dftproject.genesis.data.genealogy.IName;
import org.dftproject.genesis.data.genealogy.IPerson;
import org.dftproject.genesis.data.genealogy.IPlace;
import org.dftproject.genesis.data.genealogy.IRole;
import org.dftproject.genesis.data.genealogy.Sex;

public class PedigreeUtils {
	
	public static Individual pedigreeFromPerson(IPerson person) {
		Individual pedigree = new Individual(getName(person));
		populatePedigree(pedigree, person, 1);
		return pedigree;
	}
	
	protected static void populatePedigree(Individual pedigree, IPerson person, int degrees) {
		Sex sex = GenealogyUtils.getSex(person);
		if (sex == Sex.Male)
			pedigree.setGender(Gender.MALE);
		else if (sex == Sex.Female)
			pedigree.setGender(Gender.FEMALE);
		
		Set<IRole> roles = person.getRoles(new String[] { GenealogyConstants.Child, GenealogyConstants.Deceased });
		for (IRole role : roles) {
			String type = role.getType();
			if (GenealogyConstants.Child.equals(type))
				populateEvent(pedigree, role.getEvent(), "Birth", true);
			else if (GenealogyConstants.Deceased.equals(type))
				populateEvent(pedigree, role.getEvent(), "Death", true);
		}
		
		if (degrees-- == 0)
			return;
		
		IPerson relation = GenealogyUtils.getFather(person);
		if (relation != null)
			populatePedigree(pedigree.addFather(getName(relation)), relation, degrees);
		
		relation = GenealogyUtils.getMother(person);
		if (relation != null)
			populatePedigree(pedigree.addMother(getName(relation)), relation, degrees);
		
		Set<IPerson> relations = GenealogyUtils.getSiblings(person);
		if (relations != null) {
			for (IPerson sibling : relations)
				populatePedigree(pedigree.addSibling(getName(sibling)), sibling, degrees);
		}
		
		relations = GenealogyUtils.getSpouses(person);
		if (relations != null) {
			for (IPerson spouse : relations)
				populatePedigree(pedigree.addSpouse(getName(spouse)), spouse, degrees);
		}
		
		relations = GenealogyUtils.getChildren(person);
		if (relations != null) {
			for (IPerson child : relations)
				populatePedigree(pedigree.addChild(getName(child)), child, degrees);
		}
	}
	
	protected static String getName(IPerson person) {
		IName name = GenealogyUtils.getName(person);
		if (name == null)
			return "(unknown)";
		return GenealogyUtils.stringFromName(name);
	}
	
	protected static void populateEvent(Individual pedigree, IEvent event, String key, boolean biological) {
		if (event == null)
			return;
		
		IInstant instant = (IInstant) GenealogyUtils.getValue(event, Events.instant, IInstant.class);
		IPlace place = (IPlace) GenealogyUtils.getValue(event, Events.place, IPlace.class);
		
		if (instant == null && place == null)
			return;
		
		LifeEvent birth = new LifeEvent();
		birth.setEventType(biological ? EventType.BIOLOGICAL : EventType.DEFAULT);
		
		if (instant != null)
			birth.setDate(new Date(GenealogyUtils.stringFromInstant(instant)));
		
		if (place != null)
			birth.setLocation(new Location(place.toString()));
		
		try {
			pedigree.addEvent(key, birth);
		} catch (PedigreeValidationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		}
	}

}

