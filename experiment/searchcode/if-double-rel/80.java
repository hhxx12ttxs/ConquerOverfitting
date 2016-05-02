package models.relationships;

import java.util.ArrayList;

import models.nodes.Estudante;
import models.nodes.Pesquisa;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import exceptions.InvalidPropertyException;
import exceptions.RelationshipTypeException;

public class Participa extends RelationshipModel {
	protected static enum RelTypes implements RelationshipType {
		PARTICIPA
	}
	
	public static final RelationshipType REL_TYPE = RelTypes.PARTICIPA;
	
	static final String ESTUDANTE = "estudante";
	static final String PESQUISA = "pesquisa";
	static final String BOLSA = "bolsa";
	
	private Estudante estudante;
	private Pesquisa pesquisa;
	private double bolsa;
	
	private Participa(Relationship rel) {
		if ( ! rel.isType(REL_TYPE)) {
			throw new RelationshipTypeException();
		}
		
		setInnerRel(rel);
		initFields();
	}
	
	private Participa(Estudante estudante, Pesquisa pesquisa, double bolsa) throws Exception {
		Transaction tx = GRAPH.beginTx();
		Relationship rel = null;
		try {
			rel = super.createRelationship(estudante.getInnerNode(), pesquisa.getInnerNode(), REL_TYPE);
			
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
		
		this.estudante = estudante;
		this.pesquisa = pesquisa;
		this.bolsa = bolsa;
	}
	
	@Override
	protected void initFields() {
		Relationship rel = getInnerRel();
		this.estudante = Estudante.get(rel.getStartNode());
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
	
	public Estudante getEstudante() {
		return this.estudante;
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
	public Estudante getStartNode() {
		return getEstudante();
	}

	@Override
	public Pesquisa getEndNode() {
		return getPesquisa();
	}

	public static Participa relate(Estudante estudante, Pesquisa pesquisa, double bolsa) {
		ArrayList<Relationship> relList = relsBetween(estudante.getInnerNode(), pesquisa.getInnerNode(), REL_TYPE, Direction.OUTGOING);
		
		// there can be only one
		if (relList.size() > 0) {
			return new Participa(relList.get(0));
		}
		
		try {
			return new Participa(estudante, pesquisa, bolsa);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Participa get(Relationship  rel) {
		if (rel.isType(REL_TYPE)) {
			return new Participa(rel);
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
		return o instanceof Participa
				&& getInnerRel().equals( ( (Participa)o ).getInnerRel() );
	}
	
	@Override
	public String toString() {
		return estudante.getNome() + " participa da pesquisa " + pesquisa.getTitulo();
	}

}

