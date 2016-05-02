package org.opencomment.server.entities;

import java.util.ArrayList;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.opencomment.server.core.AbstractEntity;
import org.opencomment.server.core.AbstractFactory;

public class Domain extends AbstractEntity {
	public static final String DOMAIN = "domain";
	
	public static enum RelTypes implements RelationshipType {
		DOMAIN,
	}
	
	public Domain(Node node) {
		super(node);
	}
	
	public String getDomain() {
		return (String)underlyingNode.getProperty(DOMAIN);
	}
	
	public static class Query {
		
		public static ArrayList<Domain> getDomains(TopLevelDomain tld) {
			ArrayList<Domain> domains = new ArrayList<Domain>();
			Iterable<Relationship> relationships = tld.getUnderlyingNode().getRelationships(Direction.OUTGOING, RelTypes.DOMAIN);
			for (Relationship relationship : relationships) {
				domains.add(new Domain(relationship.getEndNode()));
			}
			return domains;
		}
		
		public static Domain getDomain(TopLevelDomain tld, String domain) {
			Iterable<Relationship> domains = tld.getUnderlyingNode().getRelationships(Direction.OUTGOING, RelTypes.DOMAIN);
			for (Relationship rel : domains) {
				Node endNode = rel.getEndNode();
				if (endNode.hasProperty(DOMAIN) && endNode.getProperty(DOMAIN).equals(domain)) {
					return new Domain(endNode);
				}
			}
			
			return null;
		}
		
		public static Domain getOrCreateDomain(TopLevelDomain tld, String domain) {
			Domain domainNode = getDomain(tld, domain);
			
			if (domainNode==null) {
				domainNode = new Factory(tld.getUnderlyingNode().getGraphDatabase()).attachToTld(tld.getUnderlyingNode()).setDomain(domain).create();
			}
			
			return domainNode;
		}
	}
	
	public static class Factory extends AbstractFactory {
		private TopLevelDomain tld;
		
		public Factory(GraphDatabaseService graphDb) {
			super(graphDb);
		}
		
		public Domain create() {
			super.create();
			Domain domain = new Domain(underlyingNode);
			return domain;
		}
		
		public Factory attachToTld(Node node) {
			tld = new TopLevelDomain(node);
			node.createRelationshipTo(underlyingNode, RelTypes.DOMAIN);
			return this;
		}
		
		public Factory setDomain(String domain) {
			if (tld==null) {
				failure();
				throw new IllegalArgumentException("topleveldomain has not been set");
			}
			if (Query.getDomain(tld, domain)!=null) {
				failure();
				throw new IllegalArgumentException("domain is already indexed");
			}
			underlyingNode.setProperty(DOMAIN, domain);
			return this;
		}
	}
}

