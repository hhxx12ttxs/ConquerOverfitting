package performance;
import java.util.*;
import com.objectwave.persist.*;
import com.objectwave.persist.file.FileBroker;
import com.objectwave.persist.setup.Configurator;
import com.objectwave.persist.query.SQLQuery;
import com.objectwave.transactionalSupport.*;
import com.objectwave.persist.broker.*;
/**
 * Create, query, and update a bunch of objects.
 * @author David Hoag
 * @version $Date: 2005/04/12 22:26:43 $ $Revision: 2.9 $
 */
public class PerformanceTest
{
	long countOfInserts = 0;
    Random rnd = new Random();
    int queryBuildCount = 0;
    int deleteCount;
    int updateCount;
    static boolean noUpdate = System.getProperty("update") == null;
    static boolean realDatabase = System.getProperty("live") != null;
    static boolean realPool = System.getProperty("realPool") != null;
    static boolean painfull = System.getProperty("longQuery") != null;
	static boolean local = System.getProperty("local") != null;
	static int numberOfThreads = 1;
	static int numberOfOrders = 1;
	static int numberOfTransactions = 1;
    static double updateLimit = .49;
	/**
	 */
	public Collection query() throws Exception
	{
        TestEntity search = new TestEntity();
        ObjectQuery oq = new SQLQuery(search);
        search.setPhone("ptes%");
        oq.setAsLike(true);
        return oq.find();
	}
	/** */
	public void query(ArrayList v)
	{
		Iterator e= v.iterator();
		while(e.hasNext())
	    try
	    {
	    	TestEntity orig = (TestEntity)e.next();
            TestEntity search = new TestEntity();
            ObjectQuery oq = new SQLQuery(search);
            search.setPrimaryKeyField(orig.getPrimaryKeyField());
            TestEntity t = (TestEntity)oq.findUnique();
	    }
	    catch (Exception ex) { ex.printStackTrace(); } 
	}
	/**
	 * For each record in the argumnet v do a findUnique to build the objects
	 * one at a time.
	 * 
	 * @param test
	 * @param v Each record in the database is in this list
	 */
	protected static void doQueryLoad(PerformanceTest test, ArrayList v)
	{
		System.out.println("Doing long query with " + numberOfThreads + " number of threads.");
        int size = v.size();
        size = (size / numberOfThreads);
        v.ensureCapacity(size);
	    Thread [] threads = new Thread[ numberOfThreads ];
	    for(int i = 0; i < numberOfThreads ; ++i)
	    {
		    threads[i] = new Thread(test.getPerformanceQueryTest(v));
	    }
	    for(int i = 0; i < numberOfThreads ; ++i)
        {
		    threads[i].start();
        }
	    for(int i = numberOfThreads - 1; i >  -1; --i)
		    try
		    {
			    threads[i].join();
		    } catch (InterruptedException ex) {}

	}
    /**
     * @param  broker
     * @exception  Exception
     */
    private final static String SQL_SEQUENCE = "CREATE TABLE sequence( nextval int PRIMARY KEY)";
    private final static String SQL_SEQUENCE_INIT = "INSERT INTO sequence(nextval) VALUES(1)";
    protected static void buildDatabase(RDBBroker broker) 
    {
    	try{
        broker.getConnection().execSql(createTestEntity);
        broker.getConnection().execSql(createTestPerson);
        broker.getConnection().execSql(SQL_SEQUENCE);
        broker.getConnection().execSql(SQL_SEQUENCE_INIT);
    	}catch(Exception ex){
    		System.out.println("Skipping db init " + ex);
    	}
    }
	static String createTestEntity = "create table TestEntity (" +
    	"databaseIdentifier int (20) not null," +
		"primaryContact int(20) ," +
		"phone varchar (15) ," +
		"annualIncome int (8) ," +
		"entityType char (1) );";
	static String createTestPerson = "create table TestPerson ( " +
    	"databaseIdentifier int (20) not null," +
    	"phone varchar (15) ," +
		"name varchar (35))";
	
    /**
     */
    protected static void dumpSql()
    {
        System.out.println("create table TestEntity (" );
        System.out.println("databaseIdentifier number (20) not null,");
        System.out.println("primaryContact number (20) ,");
        System.out.println("phone varchar (15) ,");
        System.out.println("annualIncome number (8) ,");
        System.out.println("entityType char (1) );");
        System.out.println("");
        System.out.println("create table TestPerson ( ");
        System.out.println("databaseIdentifier number (20) not null,");
        System.out.println("phone varchar (15) ,");
        System.out.println("name varchar (35));");
    }
    /**
     * This will default to
     * - ObjectPool execution only. 
     * - No updating of values.
     * Use -Dupdate to cause random updates (and exceptions)
     * Use -Dlive to go to a database.
     * Use -DrealPool to go to a database and use the object pool.
     */
    public static void main(String [] args)
    {
    	int argsIdx = 0;
    	//Prevent debug messages
		System.getProperties().put( "thresholdList", "com:info,com.objectwave.logging:info,com.objectwave.configuration:info,com.objectwave.utility:info" );
    	if(args.length == 0 )
    	{
    		System.out.println("must pass a property file as the first argument");
    		return;
    	}
        if(args.length == 1)
        {
            if(args[0].equals("dumpSql") )
            {
                dumpSql();
                return;
            }
        }
        try
        {
	        numberOfThreads = new Integer(args[argsIdx++]).intValue();
	        numberOfOrders = new Integer(args[argsIdx++]).intValue();
	        numberOfTransactions = new Integer(args[argsIdx++]).intValue();
        } catch (Exception e) {}
        System.out.println("local= " + (System.getProperty("local") != null));
		System.out.println("realPool= " + realPool);
		System.out.println("live= " + realDatabase);
		System.out.println("update=" + ! noUpdate);
        if(! noUpdate)
        try
        {
            updateLimit = .49;
            updateLimit = Double.parseDouble( System.getProperty("update"));
            System.out.println("Update percentage " + updateLimit + "%");
            updateLimit = updateLimit / 100;
        }
        catch(NumberFormatException ex)
        {
        }
        System.out.println("longQuery=" + painfull);
//        try { 
//            System.out.println("Press enter to begin");
//        new java.io.DataInputStream(System.in).readLine();        
//        } catch (Throwable t) {}


        initializeDatabase();
        
        try {
        	if( BrokerFactory.getDefaultBroker() instanceof RDBBroker){
                buildDatabase( (RDBBroker)BrokerFactory.getDefaultBroker() );
        	}

    		System.out.println("Using " + numberOfThreads + " thread, " + numberOfTransactions + " transactions/thread "+ numberOfOrders + " orders/transaction, "); 
            PerformanceTest test = new PerformanceTest();
	        long start = runTests(test);

	        long totalTime = System.currentTimeMillis() - start;
	        int startCount = test.queryBuildCount ;
            start = System.currentTimeMillis();
            ArrayList v = (ArrayList)test.query();
            if(!painfull)
            {
				test.increment(v.size());
			}
			else
			{
	            start = System.currentTimeMillis();
				doQueryLoad(test, v);
			}

            displayResults(test, start, startCount, totalTime);
            Configurator.getDatabaseBroker().close();
            if(System.getProperty("pause") != null)
            {
            	new java.io.DataInputStream(System.in).readLine();
            }
        }
        catch (Exception e) { e.printStackTrace(); }
        //System.exit(0);
    }
    /**
	 * @param test
	 * @return
	 */
	private static long runTests(PerformanceTest test)
	{
		Thread [] threads = new Thread[ numberOfThreads ];
		for(int i = 0; i < numberOfThreads ; ++i)
		{
		    threads[i] = new Thread(test.getPerformanceTest());
		}
		long start = new java.util.Date().getTime();
		for(int i = 0; i < numberOfThreads ; ++i)
		    threads[i].start();
		for(int i = numberOfThreads - 1; i >  -1; --i)
		    try
		    {
		        threads[i].join();
		    } catch (InterruptedException ex) {}
		return start;
	}
	/**
	 * @param test
	 * @param start
	 * @param startCount
	 * @throws QueryException
	 */
	private static void displayResults(PerformanceTest test, long start, int startCount, long insertUpdateTime) throws QueryException
	{
		long totalTime = System.currentTimeMillis() - start;
		double calc1 = test.countOfInserts / (insertUpdateTime/ 1000.0);
        System.out.println("Insert + update time: " + insertUpdateTime + "ms " + test.countOfInserts + " transactions. " + (int)calc1 + " trans/sec");
		double calc = (test.queryBuildCount - startCount) / (totalTime / 1000.0);
		System.out.println("Query time: " + totalTime + "ms for " + (test.queryBuildCount - startCount) + " objects. " + (int)calc + " objs/sec");
		start = new java.util.Date().getTime();
		test.deleteTestTestEntitys();
		totalTime = new java.util.Date().getTime() - start;
		System.out.println("Delete time: " + totalTime + "ms");
		System.out.println("UpdateCount : " + test.updateCount);
		System.out.println("QueryCount : " + test.queryBuildCount);
		System.out.println("Total Objects : " + test.deleteCount);
	}
	/**
	 * 
	 */
	protected static void initializeDatabase()
	{
        if(local)
        {
			FileBroker fb = new FileBroker();
			BrokerFactory.setDefaultBroker( fb );
			SQLQuery.setDefaultBroker( fb );
        }
		else
        if(realPool)
        {
            Configurator.useDatabase(true , new ObjectPoolImpl(), "/performance/jgrinder.properties");
        }
        else
        if(realDatabase)
		{
            Configurator.useDatabase( "/performance/jgrinder.properties");
		}
        else
            Configurator.useObjectPoolBroker(new ObjectPoolImpl());
	}
	/**
     */
    public synchronized void increment(final int inc)
    {
        queryBuildCount += inc;
    }
    /**
     */
    public Runnable getPerformanceQueryTest(final ArrayList v)
    {
        return new Runnable()
        {
            public void run()
            {
                try
                { 
                    query(v);
                    increment(v.size());
                }
                catch (Exception e) { e.printStackTrace(); } 
            }
        };
    }
    /**
     */
    public Runnable getPerformanceTest()
    {
        return new Runnable()
        {
            public void run()
            {
                try
                { 
                    saveTestEntitys(numberOfOrders);
                }
                catch (Exception e) { e.printStackTrace(); } 
            }
        };
    }
    /**
     */
    public void saveTestEntitys(int count) throws UpdateException, QueryException
    {
        TestPerson tp = new TestPerson();
        tp.setRetrievedFromDatabase(true);
        tp.setAsTransient(true);
        tp.setObjectIdentifier(new Integer(10201));
        tp.setAsTransient(false);
        String transactionType = "RDB";
        if( ! realDatabase ){
        	transactionType = "TransientTransLog";
        }
        Session session = Session.getCurrent();
        for(int j = 0; j < numberOfTransactions; ++j)
        {
        	TestEntity last = null;
            session.startTransaction( transactionType );
            for(int i = 0; i < count; ++i)
            {
                TestEntity orderId = new TestEntity();
                orderId.setPhone("ptest");
                orderId.setAnnualIncome((short)10021);
                orderId.setPrimaryContact(tp);
                last = orderId;
            } 
            session.commit();
            if(! realDatabase || (! noUpdate && (rnd.nextFloat() < updateLimit)))
            {
            	if( last != null)
                updateRandomAmounts( last, transactionType );
            }
        }
        if( !realDatabase )
        	//Add the update transactions
        	updateCounts( numberOfTransactions );
        updateCounts( numberOfTransactions );
    }
    protected synchronized void updateCounts( int insertCounts ){
    	countOfInserts += insertCounts;
    }
    /**
     */
    public void deleteTestTestEntitys() throws QueryException
    {
        TestEntity search = new TestEntity();
        ObjectQuery oq = new SQLQuery(search);
        search.setPhone("ptes%");
        
        oq.setAsLike(true);
        // The original way
        /*
        Vector v = oq.find();
        deleteCount = v.size();
        System.out.println("About to delete " + deleteCount);
        Enumeration e = v.elements();
        while(e.hasMoreElements())
        {
            TestEntity obj = (TestEntity)e.nextElement();
            obj.delete();
        } 
        */
        try
        {
        	oq.deleteAll();
        }
        catch(Exception t){ t.printStackTrace(); }
    }
    /**
     */
    public void updateRandomAmounts( TestEntity obj, String transactionType ) throws QueryException, UpdateException
    {
        Session session = Session.getCurrent();
        session.startTransaction( transactionType );
        obj.setAnnualIncome( (short)230);
        obj.setAnnualIncome( (short)233);
        obj.setAnnualIncome( (short)234);
        obj.setPhone("ptest30303033");
        obj.setPhone("ptestnew Value");
        obj.setPhone("ptest30303033");
        obj.setPhone("ptestnew Value");
        session.commit();
    }
}

