package models.relationships;

import java.util.ArrayList;

import models.nodes.Pesquisa;
import models.nodes.Professor;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import exceptions.InvalidPropertyException;
import exceptions.RelationshipTypeException;

public class Coordena extends RelationshipModel {
	protected static enum RelTypes implements RelationshipType {
		COORDENA
	}
	
	static final RelationshipType REL_TYPE = RelTypes.COORDENA;
	
	static final String PROFESSOR = "professor";
	static final String PESQUISA = "pesquisa";
	static final String BOLSA = "bolsa";
	
	private Professor professor;
	private Pesquisa pesquisa;
	private double bolsa;
	
	private Coordena(Relationship rel) {
		if ( ! rel.isType(REL_TYPE)) {
			throw new RelationshipTypeException();
		}
		
		setInnerRel(rel);
		initFields();
	}
	
	private Coordena(Professor professor, Pesquisa pesquisa, double bolsa) throws Exception {
		Transaction tx = GRAPH.beginTx();
		Relationship rel = null;
		try {
			rel = super.createRelationship(professor.getInnerNode(), pesquisa.getInnerNode(), REL_TYPE);
			
			rel.setProperty(BOLSA, bolsa);

			tx.success();
		}
		catch (Exception e) {
			tx.failure();
			throw e;
		}
		finally {
			tx.finish();
		}
		
		this.professor = professor;
		this.pesquisa = pesquisa;
		this.bolsa = bolsa;
	}
	
	@Override
	protected void initFields() {
		Relationship rel = getInnerRel();
		this.professor = Professor.get(rel.getStartNode());
		this.pesquisa = Pesquisa.get(rel.getEndNode());
	}
	
	@Override
	protected void setProperty(String propertyName, Object propertyValue) {
		Transaction tx = GRAPH.beginTx();
		try {
			switch(propertyName) {
			case BOLSA:
				getInnerRel().setProperty(BOLSA, (Double)propertyValue);
				this.bolsa = (Double)propertyValue;
				break;
			default:
				throw new InvalidPropertyException();
			}
			
			tx.success();
		}
		catch(Exception e) {
			tx.failure();
		}
		finally {
			tx.finish();
		}
	}
	
	public Professor getProfessor() {
		return this.professor;
	}
	
	public Pesquisa getPesquisa() {
		return this.pesquisa;
	}
	
	public double getBolsa() {
		return this.bolsa;
	}
	
	public void setBolsa(double bolsa) {
		setProperty(BOLSA, bolsa);
	}
	
	@Override
	public Professor getStartNode() {
		return getProfessor();
	}

	@Override
	public Pesquisa getEndNode() {
		return getPesquisa();
	}

	public static Coordena relate(Professor professor, Pesquisa pesquisa, double bolsa) {
		ArrayList<Relationship> relList = relsBetween(professor.getInnerNode(), pesquisa.getInnerNode(), REL_TYPE, Direction.OUTGOING);
		
		// there can be only one
		if (relList.size() > 0) {
			return new Coordena(relList.get(0));
		}
		
		try {
			return new Coordena(professor, pesquisa, bolsa);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Coordena get(Relationship  rel) {
		if (rel.isType(REL_TYPE)) {
			return new Coordena(rel);
		}
		else {
			return null;
		}
	}
	
	@Override
	public int hashCode() {
		return getInnerRel().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Coordena
				&& getInnerRel().equals( ( (Coordena)o ).getInnerRel() );
	}
	
	@Override
	public String toString() {
		return professor.getNome() + " coordena a pesquisa " + pesquisa.getTitulo();
	}

}

