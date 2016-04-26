/*
COPYRIGHT AND DISCLAIMER NOTICE
=========================================

The following copyright and disclaimer notice applies to all files 
included in this application

Objectivity, Inc. grants you a nonexclusive copyright license to use all
programming code examples from which you can generate similar function
tailored to your own specific needs.

All sample code is provided by Objectivity, Inc. for illustrative 
purposes only. These examples have not been thoroughly tested under all 
conditions. Objectivity, Inc., therefore, cannot guarantee or imply 
reliability, serviceability, or function of these programs.

All programs contained herein are provided to you "AS IS" without any
warranties or indemnities of any kind. The implied warranties of 
non-infringement, merchantability and fitness for a particular purpose 
are expressly disclaimed.
 */

package com.infinitegraph.samples.ighunter.ws;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PreDestroy;
import javax.jws.WebMethod;
import javax.jws.WebService;

import com.infinitegraph.AccessMode;
import com.infinitegraph.Edge;
import com.infinitegraph.GraphDatabase;
import com.infinitegraph.Transaction;
import com.infinitegraph.Vertex;
import com.infinitegraph.VertexHandle;
import com.infinitegraph.indexing.IndexException;
import com.infinitegraph.navigation.GraphView;
import com.infinitegraph.navigation.Guide;
import com.infinitegraph.navigation.Hop;
import com.infinitegraph.navigation.NavigationResultHandler;
import com.infinitegraph.navigation.Navigator;
import com.infinitegraph.navigation.Path;
import com.infinitegraph.navigation.Qualifier;
import com.infinitegraph.navigation.policies.MaximumPathDepthPolicy;
import com.infinitegraph.navigation.policies.MaximumResultCountPolicy;
import com.infinitegraph.navigation.qualifiers.VertexIdentifier;
import com.infinitegraph.navigation.qualifiers.VertexTypes;
import com.infinitegraph.policies.PolicyChain;
import com.infinitegraph.samples.ighunter.model.Owns;
import com.infinitegraph.samples.ighunter.model.PhoneCall;
import com.infinitegraph.samples.ighunter.model.PhoneRecord;
import com.infinitegraph.samples.ighunter.model.ServiceEndpoint;
import com.infinitegraph.samples.ighunter.model.ServiceOwner;
import com.infinitegraph.samples.ighunter.model.Stats;
import com.infinitegraph.samples.ighunter.query.PhoneCallQualifier;
import com.infinitegraph.samples.ighunter.util.AddressGenerator;
import com.infinitegraph.samples.ighunter.util.Country;
import com.infinitegraph.samples.ighunter.util.GraphUtil;
import com.infinitegraph.samples.ighunter.util.HunterSettings;
import com.infinitegraph.samples.ighunter.util.PhoneNumberProvider;
import com.infinitegraph.samples.ighunter.util.PhoneNumberProviderFactory;
import com.infinitegraph.samples.ighunter.util.Range;
import com.infinitegraph.samples.ighunter.util.Stopwatch;
import com.infinitegraph.samples.ighunter.util.TargetGender;
import com.infinitegraph.samples.ighunter.util.UserIndex;

@WebService(endpointInterface = "com.infinitegraph.samples.ighunter.ws.HunterService", name = "HunterService", portName = "BasicHttpBinding_IHunterService", serviceName = "HunterService")
public class HunterServiceImpl implements HunterService
{

	private static String SUCCESS = "OK";
	private GraphDatabase graphDB = null;
	private BlockingQueue<ResultRelationship> resultsQueue;
	private AddressGenerator addressGenerator = null;
	private Random numberGen = new Random();
	private HunterThread hunter;
	private Stopwatch timer = new Stopwatch();
	private boolean debug = false;

	public HunterServiceImpl()
	{
	}

	public HunterServiceImpl(GraphDatabase graphDB)
	{
		this.graphDB = graphDB;

		try
		{
			GraphUtil.init(graphDB);

			addressGenerator = new AddressGenerator(
					HunterSettings.INSTANCE.getStreetNamesFileName(),
					HunterSettings.INSTANCE.getZipCodesFileName());

			resultsQueue = new LinkedBlockingQueue<ResultRelationship>();

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public String getTimeAsString()
	{
		return new Date().toString();
	}

	@Override
	public String Hunt(String start, String end, int depth)
	{
		return doHunt(start, end, depth, null, null, null);
	}

	@Override
	public String HuntEx(String start, String end, int depth,
			Range<Integer> callDurationRange, Range<Long> callDateRange,
			String targetGender)
	{
		return doHunt(start, end, depth, callDurationRange, callDateRange,
				targetGender);
	}

	@Override
	public HuntResult CancelHunt()
	{
		if (debug)
			System.out.println("Called CancelHunt() Method");

		HuntResult result = null;

		if (hunter != null)
		{
			result = new HuntResult();
			result.pathsFound = hunter.getPathFoundCount();
			result.elapsedMilliseconds = timer.getElapsedTime();
			timer.reset();
			hunter.cancel();
		}

		synchronized (resultsQueue)
		{
			resultsQueue.clear();
		}

		return result;
	}

	@Override
	public HuntResult GetHuntResult()
	{
		if (debug)
			System.out.println("Called getHuntResult() Method");

		HuntResult result = new HuntResult();

		List<ResultRelationship> relationships = new ArrayList<ResultRelationship>();

		// If there are no queued tasks just wait
		// otherwise go and get one
		// ResultRelationship rr;

		if (resultsQueue.peek() != null)
		{
			synchronized (resultsQueue)
			{
				int count = resultsQueue.drainTo(relationships);
				if (debug)
					System.out.println("retrieved " + count
							+ " items from resultsQueue");
			}

			result.relationship = new ArrayList<ResultRelationship>();
			for (ResultRelationship hunterRel : relationships)
			{
				if (hunterRel.from != null)
				{
					ResultRelationship detail = new ResultRelationship(
							hunterRel);

					generateOwnerDetail(detail.from);

					generateOwnerDetail(detail.to);

					result.relationship.add(detail);
				} else
				{
					result.relationship.add(new ResultRelationship());
					if (debug)
						System.out
								.println("*** Search Complete: No more paths in queue ***");
				}
			}
		}

		result.elapsedMilliseconds = timer.getElapsedTime();
		result.percentComplete = 0;
		result.pathsFound = (hunter != null) ? hunter.getPathFoundCount() : 0;
		int resultSize = (result.relationship != null) ? result.relationship
				.size() : 0;

		if (debug)
			System.out.println("GetHuntResult() - Returning result length "
					+ resultSize);

		return result;
	}

	@Override
	public ResultNode GetOwnerInformation(String number)
	{
		if (debug)
			System.out.println("Called GetOwnerInformation(" + number + ")");

		ResultNode result = new ResultNode();

		Transaction tx = null;

		UserIndex seNumberIndex;
		try
		{
			ServiceOwner so;

			tx = graphDB.beginTransaction(AccessMode.READ);

			seNumberIndex = new UserIndex(false, graphDB);
			ServiceEndpoint endpoint = seNumberIndex.get(number);
			result.number = number;
			if (endpoint != null
					&& (so = GraphUtil.getServiceOwnerFromEndpoint(endpoint)) != null)
			{
				result.firstName = so.getFirstName();
				result.middleName = so.getMiddleName();
				result.surname = so.getSurname();
				result.isMale = so.isMale();
				generateOwnerDetail(result);
			} else
			{
				setResultNodeError(result);
			}
		} catch (IndexException e)
		{
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (tx != null)
				tx.commit();
		}

		return result;
	}

	@Override
	public SearchResult PerformOwnerSearchByName(String searchKey)
	{
		if (debug)
			System.out.println("Called PerformOwnerSearchByName(" + searchKey
					+ ")");
		return null;

	}

	@Override
	public SearchResult PerformOwnerSearchByNumber(String searchKey)
	{
		if (debug)
			System.out.println("Called PerformOwnerSearchByNumber(" + searchKey
					+ ")");

		SearchResult sr = new SearchResult();
		sr.results = new ArrayList<SearchResultItem>();

		Transaction tx = null;

		try
		{
			if (searchKey.length() >= 4)
			{
				tx = graphDB.beginTransaction(AccessMode.READ);

				// Get all the numbers with the supplied prefix
				UserIndex seNumberIndex = new UserIndex(false, graphDB);
				if (seNumberIndex != null)
				{
					PhoneNumberProvider endpointProvider = PhoneNumberProviderFactory
							.getProvider(Country.US);
					String[] numRange = endpointProvider
							.getNumberRange(searchKey);
					
					Iterator<ServiceEndpoint> elemItr = 
					seNumberIndex.getRange(numRange[0].toString(), numRange[1].toString());
					
					while ( elemItr.hasNext())
					{
						ServiceEndpoint se = elemItr.next();
						ServiceOwner so;
						if (se != null
								&& (so = GraphUtil
										.getServiceOwnerFromEndpoint(se)) != null)
						{
							sr.results.add(new SearchResultItem(se.getNumber(),
									so.toString()));
						}
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			if (tx != null)
			{
				tx.commit();
			}
		}

		if (debug)
			System.out.println("Returning " + sr.results.size()
					+ " matching numbers");

		return sr;

	}

	@Override
	public SearchResult GetRelationshipsForService(String number)
	{
		if (debug)
			System.out.println("Called GetRelationshipsForService(" + number
					+ ")");

		SearchResult results = new SearchResult();
		results.results = new ArrayList<SearchResultItem>();

		Transaction tx = null;
		UserIndex seNumberIndex;

		try
		{
			tx = graphDB.beginTransaction(AccessMode.READ);

			seNumberIndex = new UserIndex(false, graphDB);
			ServiceEndpoint endpoint = seNumberIndex.get(number);

			if (endpoint != null)
			{
				ServiceOwner so = null;
				ServiceEndpoint se = null;

				for (VertexHandle v : endpoint.getNeighbors(new VertexTypes(
						GraphUtil.serviceEndpointTypeId)))
				{
					se = (ServiceEndpoint) v.getVertex();
					so = GraphUtil.getServiceOwnerFromEndpoint(se);

					SearchResultItem item = new SearchResultItem(se.getNumber()
							.substring(0, 10), null);

					if (so != null)
					{
						item.fullName = so.getFullName();
					} else
					{
						item.fullName = GraphUtil.UNKNOWN_OWNER_KEY;
					}

					results.results.add(item);
				}
			}

		} catch (Exception e)
		{
			System.out.println("   Exception - " + e.getMessage());
		} finally
		{
			if (tx != null)
			{
				tx.commit();
			}
		}
		return results;
	}

	@WebMethod
	public DBStats GetDBStatistics()
	{
		DBStats stats = new DBStats();

		Transaction tx = null;
		try
		{
			tx = graphDB.beginTransaction(AccessMode.READ);
			Stats s = (Stats) graphDB.getNamedVertex("stats");
			stats.numCalls = s.getNumCalls();
			stats.numEndpoints = s.getNumEndpoints();
			stats.numOwners = s.getNumOwners();
		} catch (Exception e)
		{
			System.out.println("   Exception - " + e.getMessage());
		} finally
		{
			if (tx != null)
			{
				tx.commit();
			}
		}
		return stats;
	}

	@WebMethod(exclude = true)
	@PreDestroy
	public void cleanup()
	{
		if (graphDB != null)
		{
			graphDB.close();
			graphDB = null;
		}
	}

	@WebMethod(exclude = true)
	private void generateOwnerDetail(ResultNode detail)
	{
		try
		{
			// Generate address
			detail.addressStreet = String.format("%d %s",
					addressGenerator.generateStreetNumber(),
					addressGenerator.generateStreet());
			AddressGenerator.CityName cityDetails = addressGenerator
					.generateCity();
			detail.addressCity = cityDetails.city;
			detail.addressState = cityDetails.state;
			detail.addressZip = cityDetails.zip;
			detail.addressLat = cityDetails.geoLat;
			detail.addressLong = cityDetails.geoLong;

			if (GraphUtil.isUnknownPerson(detail))
			{
				return;
			}

			// Generate vitals
			detail.height = (short) (60 + numberGen.nextInt(17));
			detail.weight = (short) (100 + numberGen.nextInt(120));

			Calendar cal = Calendar.getInstance();

			cal.set(1961, 0, 0);
			long val1 = cal.getTimeInMillis();

			cal.set(1995, 0, 0);
			long val2 = cal.getTimeInMillis();
			long randomTS = (long) (numberGen.nextDouble() * (val2 - val1))
					+ val1;
			detail.DOB = randomTS;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@WebMethod(exclude = true)
	private void setResultNodeError(ResultNode detail)
	{
		detail.firstName = GraphUtil.UNKNOWN_OWNER_KEY;
	}

	private String doHunt(String start, String end, int depth,
			Range<Integer> callDurationRange, Range<Long> callDateRange,
			String targetGender)
	{
		if (debug)
			System.out.println("Called hunt() Method");

		String retVal = SUCCESS;

		timer.reset();

		if (hunter != null)
		{
			hunter.cancel();
		}

		synchronized (resultsQueue)
		{
			resultsQueue.clear();
		}

		ServiceEndpoint startEndpoint = null;
		ServiceEndpoint targetEndpoint = null;
		Transaction tx = null;
		try
		{
			tx = graphDB.beginTransaction(AccessMode.READ);

			UserIndex seNumberIndex = new UserIndex(false, graphDB);

			startEndpoint = seNumberIndex.get(start);
			targetEndpoint = seNumberIndex.get(end);

			if (debug && startEndpoint != null && targetEndpoint != null)
			{
				System.out.println("Start Endpoint id: "
						+ startEndpoint.getId());
				System.out.println("Target Endpoint id: "
						+ targetEndpoint.getId());
			}

		} catch (IndexException ge)
		{
			ge.printStackTrace();
			retVal = ge.getMessage();
		} catch (Exception e)
		{
			e.printStackTrace();
			retVal = e.getMessage();
		} finally
		{
			if (tx != null)
				tx.commit();
		}

		if (startEndpoint != null && targetEndpoint != null)
		{
			timer.start();

			hunter = new HunterThread(graphDB, resultsQueue, startEndpoint,
					targetEndpoint);
			hunter.setDepth(depth);
			hunter.setDurationRange(callDurationRange);
			hunter.setDateRange(callDateRange);
			hunter.setTargetGender(TargetGender.valueOf(targetGender));
			hunter.setDebug(debug);
			hunter.start();
			try {
			hunter.waiting();
			} catch ( InterruptedException ie ) {
				ie.printStackTrace();
			}
		} else
		{
			retVal = "Start and/or end number is invalid";
		}

		return retVal;
	}

	@Override
	public String GetRandomEndpointNumber(String number)
	{
		String retVal = null;
		Transaction tx = null;

		try
		{
			tx = graphDB.beginTransaction(AccessMode.READ);
			UserIndex seNumberIndex = new UserIndex(false, graphDB);

			ServiceEndpoint se = seNumberIndex.get(number);

			List<ServiceEndpoint> endpoints = new ArrayList<ServiceEndpoint>();

			// First get all the neighboring endpoints
			Qualifier seTypesFilter = new VertexTypes(
					GraphUtil.serviceEndpointTypeId);
			for (VertexHandle v : se.getNeighbors(seTypesFilter))
			{
				endpoints.add((ServiceEndpoint) v.getVertex());
			}

			// then pick a random one from the list and return it.
			if (endpoints.size() > 0)
			{
				retVal = endpoints.get(numberGen.nextInt(endpoints.size()))
						.getNumber();
			} else
			{
				retVal = "";
			}
		} catch (IndexException ge)
		{
			ge.printStackTrace();
			retVal = ge.getMessage();
		} catch (Exception e)
		{
			e.printStackTrace();
			retVal = e.getMessage();
		} finally
		{
			if (tx != null)
				tx.commit();
		}

		return retVal;
	}
}

class HunterThread implements Runnable
{
	private GraphDatabase graph;
	private Transaction tx;
	private ServiceEndpoint startEndpoint;
	private ServiceEndpoint targetEndpoint;
	private int depth;
	private Range<Integer> callDurationRange;
	private Range<Long> callDateRange;
	private TargetGender targetGender;

	private final BlockingQueue<ResultRelationship> resultQueue;
	private PhoneCallQualifier pathQualifier;
	private ResultPathHandler resultPathHandler;

	private Navigator finder = null;
	private Thread runner;

	private boolean debug = true;

	public HunterThread(GraphDatabase graph,
			BlockingQueue<ResultRelationship> resultQueue,
			ServiceEndpoint start, ServiceEndpoint end)
	{
		this.graph = graph;
		this.resultQueue = resultQueue;
		this.startEndpoint = start;
		this.targetEndpoint = end;
		this.callDurationRange = null;
		this.callDateRange = null;
		this.targetGender = TargetGender.BOTH;
		runner = new Thread(this);
	}

	public void setDepth(int depth)
	{
		this.depth = depth;
	}

	public void setDurationRange(Range<Integer> r)
	{
		this.callDurationRange = r;
	}

	public void setDateRange(Range<Long> r)
	{
		this.callDateRange = r;
	}

	public void setTargetGender(TargetGender tg)
	{
		this.targetGender = tg;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	@Override
	public void run()
	{
		try
		{
			PolicyChain resultLimitPolicies = new PolicyChain();
			resultLimitPolicies.addPolicy(new MaximumResultCountPolicy(50));
			resultLimitPolicies.addPolicy(new MaximumPathDepthPolicy(depth));
			tx = graph.beginTransaction(AccessMode.READ);

			resultPathHandler = new ResultPathHandler();
			pathQualifier = new PhoneCallQualifier();
			pathQualifier.setPathDepth(depth);
			pathQualifier.setCallDurationRange(callDurationRange);
			pathQualifier.setCallDateRange(callDateRange);
			pathQualifier.setTargetGender(targetGender);
			GraphView gv = null;

			gv = new GraphView();
            gv.excludeClass(graph.getTypeId(ServiceOwner.class.getName()));
           	gv.excludeClass(graph.getTypeId(Owns.class.getName()));
			VertexIdentifier vId = new VertexIdentifier(targetEndpoint);
			finder = startEndpoint.navigate(gv,Guide.SIMPLE_DEPTH_FIRST, null, vId, resultLimitPolicies, resultPathHandler);

			finder.start();

			ResultRelationship rr = new ResultRelationship();
			rr.from = null;
			rr.to = null;
			resultQueue.add(rr);
			if (debug)
			{
				System.out.println("Total found paths - "
						+ resultPathHandler.getPathCount());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			tx.commit();
			finder = null;
		}


	}

	public void start()
	{
		if (!runner.isAlive())
		{
			runner.start();
		}
	}

	public void waiting() throws InterruptedException
	{
		if (runner.isAlive())
		{
			runner.join();
		}
	}


	public void cancel()
	{
		if (finder != null)
		{
			finder.stop();
		}
	}

	public long getPathSearchCount()
	{
		return (pathQualifier != null) ? pathQualifier.getPathCount() : 0;
	}

	public long getPathFoundCount()
	{
		return (resultPathHandler != null) ? resultPathHandler.getPathCount()
				: 0;
	}

	class ResultPathHandler implements NavigationResultHandler
	{
		private int pathCount;

		public ResultPathHandler()
		{
			pathCount = 0;
		}

		@Override
		public void handleResultPath(Path path, Navigator navigator)
		{

			if (path.isEmpty()) {
				System.out.println(" Empty path !!!");
			}
			
			Vertex v1 = path.get(0).getVertex();

			for (Hop h : path)
			{
				if (h.hasEdge())
				{
					// display edge info..
					
					Vertex v2 = h.getVertex();

					Edge e = h.getEdge();
					long edgeType = e.getTypeId();
					ResultRelationship rr = new ResultRelationship();

					ServiceEndpoint se1 = (ServiceEndpoint) v1;
					ServiceEndpoint se2 = (ServiceEndpoint) v2;

				try{
					ServiceOwner so1 = GraphUtil
							.getServiceOwnerFromEndpoint(se1);
					if (so1 != null)
					{
						rr.from = new ResultNode(se1.getNumber(),
								so1.getFirstName(), so1.getMiddleName(),
								so1.getSurname(), so1.isMale());
					} else
					{
						rr.from = new ResultNode(se1.getNumber(),
								GraphUtil.UNKNOWN_OWNER_KEY,
								GraphUtil.UNKNOWN_OWNER_KEY,
								GraphUtil.UNKNOWN_OWNER_KEY, true);
					}
				}
				catch (java.lang.NullPointerException npe){
						System.out.println("SERVICE OWNER 1 null pointer");
						npe.printStackTrace();
				}

				try {
					ServiceOwner so2 = GraphUtil
							.getServiceOwnerFromEndpoint(se2);
					if (so2 != null)
					{
						rr.to = new ResultNode(se2.getNumber(),
								so2.getFirstName(), so2.getMiddleName(),
								so2.getSurname(), so2.isMale());
					} else
					{


						rr.to = new ResultNode(se2.getNumber(),
								GraphUtil.UNKNOWN_OWNER_KEY,
								GraphUtil.UNKNOWN_OWNER_KEY,
								GraphUtil.UNKNOWN_OWNER_KEY, true);
					}
				}
				catch (java.lang.NullPointerException npe){
						System.out.println("SERVICE OWNER 2 null pointer");
						npe.printStackTrace();
				}

					rr.strength = se1.getEdgeCount();

					resultQueue.add(rr);

					if (debug)
					{
						if (edgeType == GraphUtil.phoneCallTypeId)
						{
							System.out.println("Phone Call: " + v1.toString()
									+ "<- Duration:"
									+ ((PhoneCall) e).getDuration() + "->"
									+ v2.toString());
						} else
						{
							System.out.println("Phone Call: " + v1.toString()
									+ "<- Calls:"
									+ ((PhoneRecord) e).getNumCalls() + "->"
									+ v2.toString());
						}
					}
					v1 = v2;
				}
			}
			pathCount++;
		}

		public int getPathCount()
		{
			return pathCount;
		}

		@Override
		public void handleNavigatorFinished(Navigator arg0) {
			// TODO Auto-generated method stub
			
		}

	}

}

