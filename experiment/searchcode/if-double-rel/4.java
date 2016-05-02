package models.relationships;

import java.util.ArrayList;

import models.nodes.Disciplina;
import models.nodes.Estudante;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import exceptions.InvalidPropertyException;
import exceptions.RelationshipTypeException;

public class Cursa extends RelationshipModel {
	protected static enum RelTypes implements RelationshipType {
		CURSA
	}
	
	static final RelationshipType REL_TYPE = RelTypes.CURSA;
	
	static final String ESTUDANTE = "professor";
	static final String DISCIPLINA = "disciplina";
	static final String ANO = "ano";
	static final String SEMESTRE = "semestre";
	static final String MEDIA = "media";
	
	private Estudante estudante;
	private Disciplina disciplina;
	private int ano;
	private int semestre;
	private Double media = null;
	
	private Cursa(Relationship rel) {
		if ( ! rel.isType(REL_TYPE)) {
			throw new RelationshipTypeException();
		}
		
		setInnerRel(rel);
		initFields();
	}
	
	private Cursa(Estudante estudante, Disciplina disciplina, int ano, int semestre) throws Exception {
		Transaction tx = GRAPH.beginTx();
		Relationship rel = null;
		try {
			rel = super.createRelationship(estudante.getInnerNode(), disciplina.getInnerNode(), REL_TYPE);
			
			rel.setProperty(ANO, ano);
			rel.setProperty(SEMESTRE, semestre);
			
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
		this.disciplina = disciplina;
		this.ano = ano;
		this.semestre = semestre;
	}
	
	@Override
	protected void initFields() {
		Relationship rel = getInnerRel();
		this.estudante = Estudante.get(rel.getStartNode());
		this.disciplina = Disciplina.get(rel.getEndNode());
		this.ano = (Integer)rel.getProperty(ANO);
		this.semestre = (Integer)rel.getProperty(SEMESTRE);
		this.media = (Double)rel.getProperty(MEDIA, null);
	}
	
	@Override
	protected void setProperty(String propertyName, Object propertyValue) {
		Transaction tx = GRAPH.beginTx();
		try {
			switch(propertyName) {
			case ANO:
				getInnerRel().setProperty(ANO, (Integer)propertyValue);
				this.ano = (Integer)propertyValue;
				break;
			case SEMESTRE:
				getInnerRel().setProperty(SEMESTRE, (Integer)propertyValue);
				this.semestre = (Integer)propertyValue;
				break;
			case MEDIA:
				getInnerRel().setProperty(MEDIA, (Double)propertyValue);
				this.media = (Double)propertyValue;
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
	
	public Disciplina getDisciplina() {
		return this.disciplina;
	}
	
	public int getAno() {
		return this.ano;
	}
	
	public void setAno(int ano) {
		setProperty(ANO, ano);
	}
	
	public int getSemestre() {
		return this.semestre;
	}
	
	public void setSemestre(int semestre) {
		setProperty(SEMESTRE, semestre);
	}
	
	public double getMedia() {
		return this.media;
	}
	
	public void setMedia(double media) {
		setProperty(MEDIA, media);
	}
	
	@Override
	public Estudante getStartNode() {
		return getEstudante();
	}

	@Override
	public Disciplina getEndNode() {
		return getDisciplina();
	}

	public static Cursa relate(Estudante estudante, Disciplina disciplina, int ano, int semestre) {
		ArrayList<Relationship> relList = relsBetween(estudante.getInnerNode(), disciplina.getInnerNode(), REL_TYPE, Direction.OUTGOING);
		
		if (relList.size() > 0) {
			for (Relationship r : relList) {
				if (r.getProperty(ANO).equals(ano) && r.getProperty(SEMESTRE).equals(semestre)) {
					return new Cursa(r);
				}
			}
		}
		
		try {
			return new Cursa(estudante, disciplina, ano, semestre);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Cursa get(Relationship  rel) {
		if (rel.isType(REL_TYPE)) {
			return new Cursa(rel);
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
		return o instanceof Cursa
				&& getInnerRel().equals( ( (Cursa)o ).getInnerRel() );
	}
	
	@Override
	public String toString() {
		return estudante.getNome()
				+ " cursa a disciplina "
				+ disciplina.getNome()
				+ (media != null ? " com média " + media.toString() : "");
	}

}

