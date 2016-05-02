package cornell.cs3300.nosql.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.Morphia;
import com.google.code.morphia.query.UpdateOperations;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

import cornell.cs3300.nosql.ReportingService;
import cornell.cs3300.nosql.ServerException;
import cornell.cs3300.nosql.ServerException.ErrorType;
import cornell.cs3300.nosql.pojos.Candy;
import cornell.cs3300.nosql.pojos.ClusterCentroid;
import cornell.cs3300.nosql.pojos.ClusterPoint;
import cornell.cs3300.nosql.pojos.Customer;

public class ReportingServiceImpl implements ReportingService {

	private static final Logger log = LoggerFactory.getLogger(ReportingServiceImpl.class);
	
	private Mongo mongo;
	private Morphia morphia;
	private Datastore ds;
	private DB db;
	/**
	 * Constructs the default service implementation, which connects to the
	 * database "candyStoreDB" at the hostname "localhost"
	 */
	public ReportingServiceImpl() {
		this("localhost", "candyStoreDB");
	}
	
	public ReportingServiceImpl(String dbHost, String dbName) {
		try {
			 mongo = new Mongo(dbHost);
			 morphia = new Morphia();
			 db = mongo.getDB(dbName);
		} catch (UnknownHostException e) {
			log.error("Unknown host");
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		}
		ds = morphia.createDatastore(mongo, dbName);
		ds.ensureIndexes();
		ds.ensureCaps();
		
		log.info("Connected to " + dbName + " on " + dbHost);
	}

	@Override
	public void rateCandy(Key<Customer> userId, Key<Candy> candyId, float rating) throws ServerException {
		if (ds.get(Customer.class, userId.getId()) == null) {
			log.error("Customer not found");
			throw new ServerException(ErrorType.CUSTOMER_NOT_FOUND, "Customer not found in database");
		}
		if (ds.get(Candy.class, candyId.getId()) == null) {
			log.error("Candy not found");
			throw new ServerException(ErrorType.CANDY_NOT_FOUND, "Candy not found in database");
		}
		if (rating < 0.0f || rating > 1.0f) {
			log.error("Rating invalid");
			throw new ServerException(ErrorType.INVALID_INPUT, "Rating must be between 0 and 1");
		}
		// First adds new rating to candy
		ds.update(candyId, ds.createUpdateOperations(Candy.class).add("rating", rating));
		
		// Then gets modified candy, calculates average rating, and updates
		Candy candyToAdd = ds.get(Candy.class, candyId.getId());
		ds.update(candyId, ds.createUpdateOperations(Candy.class).set("avgRating", candyToAdd.getAvgRating()));
		
		// Adds new cluster point
		ds.save(new ClusterPoint(candyToAdd.getSweetness(), candyToAdd.getViscosity(), candyToAdd.getSourness(), candyToAdd.getNuts(), candyToAdd.getTexture(), rating));
		
		log.info("Customer " + userId.getId() + " rated candy " + candyId.getId() + " with " + rating);
	}

	@Override
	public void purchaseCandy(Key<Customer> userId, Key<Candy> candyId) throws ServerException {
		Candy candy = ds.get(Candy.class, candyId.getId());
		Customer customer = ds.get(Customer.class, userId.getId());
		if (customer == null) {
			log.error("Customer not found");
			throw new ServerException(ErrorType.CUSTOMER_NOT_FOUND, "Customer not found in database");
		}
		if (candy == null) {
			log.error("Candy not found");
			throw new ServerException(ErrorType.CANDY_NOT_FOUND, "Candy not found in database");
		}
		UpdateOperations<Customer> addCandy = ds.createUpdateOperations(Customer.class).add("purchases", candy.getCandyName());
		ds.update(customer, addCandy);
		UpdateOperations<Customer> incCustomerPurchases = ds.createUpdateOperations(Customer.class).inc("purchaseCount");
		ds.update(customer, incCustomerPurchases);
		
		UpdateOperations<Candy> incCandyPurchases = ds.createUpdateOperations(Candy.class).inc("purchases");
		ds.update(candy, incCandyPurchases);
		
		log.info("Customer " + userId.getId() + " purchased candy " + candyId.getId());
	}

	@Override
	public String[] getCandyPurchased(Key<Customer> userId) throws ServerException {
		Customer customer = ds.get(Customer.class, userId.getId());
		if (customer == null) {
			log.error("Customer not found");
			throw new ServerException(ErrorType.CUSTOMER_NOT_FOUND, "Customer not found in database");
		}
		log.info("Getting candy purchased by customer " + userId.getId());
		return customer.getPurchases();
	}

	@Override
	public void runClustering(int numSeeds, float minimumRating, int iterations) throws ServerException {
		if (numSeeds <= 0) {
			log.error("Number invalid");
			throw new ServerException(ErrorType.INVALID_INPUT, "Number of seeds must be at least 1");
		}
		if (minimumRating < 0.0f || minimumRating > 1.0f) {
			log.error("Rating invalid");
			throw new ServerException(ErrorType.INVALID_INPUT, "Rating must be between 0 and 1");
		}
		if (iterations <= 0) {
			log.error("Iterations invalid");
			throw new ServerException(ErrorType.INVALID_INPUT, "Number of iterations must be at least 1");
		}
		
		log.info("Running " + iterations + " clustering iterations with " + numSeeds + " initial seeds, " +
				"filtering by minimum rating " + minimumRating);
		
		// Deletes ClusterCentroid collection to start fresh
		db.getCollection(ClusterCentroid.class.getSimpleName()).remove(new BasicDBObject());
		
		for (int it = 0; it < iterations; it++) {
			String params = "";
			
			if (ds.find(ClusterCentroid.class).asList().size() == 0) {	// First iteration, use 3 random cluster points
				List<ClusterPoint> clusters = ds.find(ClusterPoint.class).field("rating").greaterThan(minimumRating).asList();
				log.info("Found " + clusters.size() + " cluster points to use");
				
				Random rand = new Random();
				List<ClusterPoint> randClusters = new ArrayList<ClusterPoint>(numSeeds);
				for (int i = 0; i < numSeeds; i++) {
					randClusters.add(clusters.remove(rand.nextInt(clusters.size())));
				}
				for (ClusterPoint c : randClusters) {
					log.debug("Initial seed: " + c);
					params += String.format("{ sweetness: %f, viscosity: %f, sourness: %f, nuts: %f, texture: %f },\n",
						c.getSweetness(), c.getViscosity(), c.getSourness(), c.getNuts(), c.getTexture());
				}
			} else {	// Subsequent iterations, use resulting cluster centroids
				List<ClusterCentroid> clusters = ds.find(ClusterCentroid.class).limit(numSeeds).asList();
				for (ClusterCentroid cc : clusters) {
					Candy c = cc.getCandyCentroid();
					params += String.format("{ sweetness: %f, viscosity: %f, sourness: %f, nuts: %f, texture: %f },\n",
						c.getSweetness(), c.getViscosity(), c.getSourness(), c.getNuts(), c.getTexture());
				}
			}
		
			StringBuffer map = new StringBuffer("function m() { var centroids = [ ");
			map.append(params);
			map.append(" ];\n");
			map.append("var propStrs = [ \"sweetness\", \"viscosity\", \"sourness\", \"nuts\", \"texture\" ];\n");
			map.append("var bestIndex = -1, bestDist = Number.POSITIVE_INFINITY;\n");
			map.append("for (var i = 0; i < centroids.length; i++) {\n");
			map.append("  var dist = 0;\n");
			map.append("  for (var j = 0; j < propStrs.length; j++) {\n");
			map.append("    dist += Math.pow(centroids[i][propStrs[j]] - this[propStrs[j]], 2);\n");
			map.append("  }\n");
			map.append("  dist = Math.sqrt(dist);\n");
			map.append("  if (dist < bestDist) {\n");
			map.append("    bestDist = dist;\n");
			map.append("    bestIndex = i;\n");
			map.append("  }\n");
			map.append("}\n");
			map.append("emit(bestIndex, this); }");
			
			StringBuffer reduce = new StringBuffer("function r(clusterId, clusterPoints) {\n");
			reduce.append("var propStrs = [ \"sweetness\", \"viscosity\", \"sourness\", \"nuts\", \"texture\" ];\n");
			reduce.append("var kmean = {}, i = 0, j = 0;\n");
			reduce.append("for (i = 0; i < propStrs.length; i++) {\n");
			reduce.append("  kmean[propStrs[i]] = 0;\n");
			reduce.append("}\n");
			reduce.append("for (i = 0; i < clusterPoints.length; i++) {\n");
			reduce.append("  for (j in kmean) {\n");
			reduce.append("    kmean[j] += clusterPoints[i][j];\n");
			reduce.append("  }\n");
			reduce.append("}\n");
			reduce.append("for (i in kmean) {\n");
			reduce.append("  kmean[i] /= clusterPoints.length;\n");
			reduce.append("}\n");
			reduce.append("return kmean; }");
	
			DBCollection inputCollection = db.getCollection(ClusterPoint.class.getSimpleName());	// Use each cluster point as input
			OutputType type = MapReduceCommand.OutputType.INLINE;				// Don't write results to db
			DBObject query = new BasicDBObject("rating", new BasicDBObject("$gt", minimumRating));	
			MapReduceCommand mr = new MapReduceCommand(inputCollection, map.toString(), reduce.toString(), null, type, query);
			MapReduceOutput out = inputCollection.mapReduce(mr);
			db.getCollection("ClusterCentroid").remove(new BasicDBObject());
			
			// Add new cluster centroids to db
			for ( DBObject obj : out.results() ) {
				DBObject vals = (DBObject)obj.get("value");
			    ds.save(new ClusterCentroid(((Double)vals.get("sweetness")).floatValue(), ((Double)vals.get("viscosity")).floatValue(), 
			    		((Double)vals.get("sourness")).floatValue(), ((Double)vals.get("nuts")).floatValue(), ((Double)vals.get("texture")).floatValue()));
			}
			
			log.debug("Resulting centroids after iteration " + (it+1) + ":");
			for (ClusterCentroid c : ds.find(ClusterCentroid.class)) {
				log.debug(c.getCandyCentroid().toString());
			}
		}
	}

	@Override
	public ClusterCentroid[] getPreferenceClusters() throws ServerException {
		List<ClusterCentroid> clusters = ds.find(ClusterCentroid.class).asList();
		
		if (clusters.size() == 0) {
			log.error("No preference clusters found");
			throw new ServerException(ErrorType.NO_CLUSTERS, "No preference clusters found");
		}
		
		ClusterCentroid[] cluster = new ClusterCentroid[clusters.size()];
		int i = 0;
		for (ClusterCentroid c : clusters) {
			cluster[i] = c;
			i++;
		}
		log.info("Getting preference clusters");
		return cluster;
	}
}

