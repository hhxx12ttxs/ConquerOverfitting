package com.google.appengine.api.datastore.dev;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DataTypeTranslator;
import com.google.appengine.repackaged.com.google.common.base.Preconditions;
import com.google.appengine.repackaged.com.google.io.protocol.ProtocolMessage;
import com.google.appengine.tools.development.LocalRpcService;
import com.google.appengine.tools.development.LocalServiceContext;
import com.google.appengine.tools.development.ServiceProvider;
import com.google.apphosting.api.ApiBasePb;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.DatastorePb;
import com.google.apphosting.api.DatastorePb.Query;
import com.google.apphosting.api.DatastorePb.QueryResult;
import com.google.storage.onestore.PropertyType;
import com.google.storage.onestore.v3.OnestoreEntity;

@ServiceProvider(LocalRpcService.class)
public class LocalDatastoreService
implements LocalRpcService
{
private static final Logger logger;
static final int DEFAULT_BATCH_SIZE = 20;
public static final String MAX_QUERY_LIFETIME_PROPERTY = "datastore.max_query_lifetime";
private static final int DEFAULT_MAX_QUERY_LIFETIME = 30000;
public static final String MAX_TRANSACTION_LIFETIME_PROPERTY = "datastore.max_txn_lifetime";
private static final int DEFAULT_MAX_TRANSACTION_LIFETIME = 30000;
public static final String STORE_DELAY_PROPERTY = "datastore.store_delay";
static final int DEFAULT_STORE_DELAY_MS = 30000;
public static final String BACKING_STORE_PROPERTY = "datastore.backing_store";
public static final String NO_STORAGE_PROPERTY = "datastore.no_storage";
static final String ENTITY_GROUP_MESSAGE = "can't operate on multiple entity groups in a single transaction.";
static final String CONTENTION_MESSAGE = "too much contention on these datastore entities. please try again.";
static final String HANDLE_NOT_FOUND_MESSAGE_FORMAT = "handle %s not found";
static final String GET_SCHEMA_START_PAST_END = "start_kind must be <= end_kind";
/*  159 */   private final AtomicLong entityId = new AtomicLong(1L);
/*      */ 
/*  161 */   private final AtomicLong queryId = new AtomicLong(0L);
private String backingStore;
/*  171 */   private Map<String, Profile> profiles = Collections.synchronizedMap(new HashMap());
private static final long MAX_BATCH_GET_KEYS = 1000000000L;
/*  188 */   private final Map<Long, LiveQuery> liveQueries = Collections.synchronizedMap(new HashMap());
/*      */ 
/*  195 */   private final Map<Long, LiveTxn> liveTxns = Collections.synchronizedMap(new HashMap());
private int maxQueryLifetimeMs;
private int maxTransactionLifetimeMs;
/*  210 */   private final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(2);
/*      */ 
/*  212 */   private final RemoveStaleQueries removeStaleQueriesTask = new RemoveStaleQueries();
/*      */ 
/*  214 */   private final RemoveStaleTransactions removeStaleTransactionsTask = new RemoveStaleTransactions();
/*      */ 
/*  216 */   private final PersistDatastore persistDatastoreTask = new PersistDatastore();
/*      */ 
/*  221 */   private final AtomicInteger transactionHandleProvider = new AtomicInteger(0);
private int storeDelayMs;
private boolean dirty;
/*  239 */ //  private final ReadWriteLock globalLock = new ReentrantReadWriteLock();
private boolean noStorage;
private Thread shutdownHook;
private static final Comparator<Comparable<Object>> MULTI_TYPE_COMPARATOR;
			private static boolean debug = false;
/*      */ 
			/////////////////////////////////////////////////////////////////////
			private static final NativeSSL proxy = NativeSSL.getProxy();			//change-new

			public InputStream outputToSocket(String appId, String method, ProtocolMessage msg){
				if (debug)
					System.out.println("sending "+method);
				return proxy.doPost(appId, method, msg);
//				System.out.println("sending "+ method+ " complete");
			}
			/////////////////////////////////////////////////////////////////////



public void clearProfiles()
{
/*  181 */     this.profiles.clear();
}
/*      */ 
public LocalDatastoreService()
{
/*  246 */     setMaxQueryLifetime(30000);
/*  247 */     setMaxTransactionLifetime(30000);
/*  248 */     setStoreDelay(30000);
}
/*      */ 
public void init(LocalServiceContext context, Map<String, String> properties) {
/*  252 */    // String storeFile = (String)properties.get("datastore.backing_store");
/*  253 */    // if (storeFile == null) {
/*  254 */    //   File dir = GenerationDirectory.getGenerationDirectory(context.getAppDir());
/*  255 */    //   dir.mkdirs();
/*  256 */    //   storeFile = dir.getAbsolutePath() + File.separator + "local_db.bin";
 // }
/*  258 */    // setBackingStore(storeFile);
/*      */ 
/*  260 */   //  String noStorageProp = (String)properties.get("datastore.no_storage");
/*  261 */   //  if (noStorageProp != null) {
/*  262 */    //   this.noStorage = Boolean.valueOf(noStorageProp).booleanValue();
 // }
/*      */ 		if (System.getProperty("DEBUG") != null)
					this.debug = true;
/*  265 */     String storeDelayTime = (String)properties.get("datastore.store_delay");
/*  266 */     this.storeDelayMs = parseInt(storeDelayTime, this.storeDelayMs, "datastore.store_delay");
/*      */ 
/*  268 */     String maxQueryLifetime = (String)properties.get("datastore.max_query_lifetime");
/*  269 */     this.maxQueryLifetimeMs = parseInt(maxQueryLifetime, this.maxQueryLifetimeMs, "datastore.max_query_lifetime");
/*      */ 
/*  272 */     String maxTxnLifetime = (String)properties.get("datastore.max_txn_lifetime");
/*  273 */     this.maxTransactionLifetimeMs = parseInt(maxTxnLifetime, this.maxTransactionLifetimeMs, "datastore.max_txn_lifetime");
/*      */ 
/*  276 */     LocalCompositeIndexManager.getInstance().setAppDir(context.getAppDir());
}
/*      */ 		/**
 				*	use 	
 				*/
private static int parseInt(String valStr, int defaultVal, String propName) {
/*  280 */     if (valStr != null) {
    try {
/*  282 */         return Integer.parseInt(valStr);
    } catch (NumberFormatException e) {
/*  284 */         logger.log(Level.WARNING, "Expected a numeric value for property " + propName + "but received, " + valStr + ". Resetting property to the default.");
    }
  }
/*      */ 
/*  288 */     return defaultVal;
}
/*      */ 	/**
			 * 	use	
			 */
public void start() {
/*  292 */     AccessController.doPrivileged(new PrivilegedAction() {
    public Object run() {
/*  294 */         LocalDatastoreService.this.start_();
/*  295 */         return null;
    }
  });
}
/*      */ 
private void start_() {
/*  301 */   //  if (!(this.noStorage)) {
/*  302 */    //   load();
 // }
/*  304 */     this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
/*  305 */     this.scheduler.scheduleWithFixedDelay(this.removeStaleQueriesTask, this.maxQueryLifetimeMs * 5, this.maxQueryLifetimeMs * 5, TimeUnit.MILLISECONDS);
/*      */ 
/*  307 */     this.scheduler.scheduleWithFixedDelay(this.removeStaleTransactionsTask, this.maxTransactionLifetimeMs * 5, this.maxTransactionLifetimeMs * 5, TimeUnit.MILLISECONDS);
/*      */ 
/*  309 */   //  if (!(this.noStorage)) {
/*  310 */   //    this.scheduler.scheduleWithFixedDelay(this.persistDatastoreTask, this.storeDelayMs, this.storeDelayMs, TimeUnit.MILLISECONDS);
//  }
/*      */ 
/*  315 */     this.shutdownHook = new Thread()
  {
    public void run() {
/*  318 */         LocalDatastoreService.this.stop();
    }
  };
/*  321 */     Runtime.getRuntime().addShutdownHook(this.shutdownHook);
}
/*      */ 
public void stop() {
/*  325 */     this.scheduler.shutdown();
/*  326 */   //  if (!(this.noStorage)) {
/*  327 */   //    this.persistDatastoreTask.run();
//  }
/*  329 */     this.liveQueries.clear();
/*  330 */     this.liveTxns.clear();
  try
  {
/*  334 */       Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
  }
  catch (IllegalStateException ex)
  {
  }
}
/*      */ /**
 			*	use		 
 			*/
public void setMaxQueryLifetime(int milliseconds) {
/*  342 */     this.maxQueryLifetimeMs = milliseconds;
}
/*      */ 	/**
 			*	use 
 			*/
public void setMaxTransactionLifetime(int milliseconds) {
/*  346 */     this.maxTransactionLifetimeMs = milliseconds;
}
/*      */ 	/**
 			* 	no use	
 			*/
public void setBackingStore(String backingStore)
{
/*  351 */     this.backingStore = backingStore;
}
/*      */ 	/**
 			*	use  		
 			*/
public void setStoreDelay(int delayMs) {
/*  355 */     this.storeDelayMs = delayMs;
}
/*      */ 	/**
 			*	no use 	
 			*/	
/*      */ //  public void setNoStorage(boolean noStorage) {
/*  359 */  //   this.noStorage = noStorage;
/*      */ //  }
/*      */ 
public String getPackage() {
/*  363 */     return "datastore_v3";
}
/*      */ 
public DatastorePb.GetResponse get(LocalRpcService.Status status, DatastorePb.GetRequest request) {
/*  367 */     DatastorePb.GetResponse response = new DatastorePb.GetResponse();
/*  368 */     LiveTxn liveTxn = null;

////////////////////////////////////////////////////////////////////////////////////////// change-new
				//for every key in request, add an entity
			int counter = 0;
			if (debug)
			{
				System.out.println("********get request starts here**********");
			
				System.out.println("the flat string of get request is: "+ request.toFlatString());
				System.out.println("sise of the keys: "+ request.keySize());
				System.out.println("size of the key list: "+request.keys().size());
			}
//////////////////////////////////////////////////////////////////////////////////////////
/*  369 */ //    for (OnestoreEntity.Reference key : request.keys()) {
//////////////////////////////////////////////////////////////////////////////////////////
				if (debug)
					System.out.println("---- No."+counter+" key:");
				counter++;
//////////////////////////////////////////////////////////////////////////////////////////
/*  370 */       String app = request.getKey(0).getApp();
//////////////////////////////////////////////////////////////////////////////////////////
if (debug)
	System.out.println("app's id: "+ app);
//////////////////////////////////////////////////////////////////////////////////////////
/*  371 */  //     OnestoreEntity.Path groupPath = getGroup(key);//a copy of the first path in key, a path with the only first element in the key----is it group path?
/*  372 */  //     OnestoreEntity.Path.Element lastPath = (OnestoreEntity.Path.Element)getLast(key.getPath().elements());
/////////////////////////////////////////////////////////////////////////////////////////

			//	System.out.println("flat string of group path: "+ groupPath.toFlatString());
/////////////////////////////////////////////////////////////////////////////////////////
/*  373 */  //     DatastorePb.GetResponse.Entity group = response.addEntity();
				//there is only one profile for an app, the profiles are stored in file
/*  374 */ //      Profile profile = getOrCreateProfile(app);
/*  375 */  //     synchronized (profile) {
/*  376 */  //       Profile.EntityGroup eg = profile.getGroup(groupPath);
/*  377 */  //       if (request.hasTransaction()) {
/*  378 */  //         if (liveTxn == null) {
/*  379 */  //           liveTxn = (LiveTxn)safeGetFromExpiringMap(this.liveTxns, request.getTransaction().getHandle());
/*      */  //         }
/*      */ 
/*  383 */ //          eg.addTransaction(liveTxn);
/*      */  //       }
/*      */ 			//use the entire key to get the data,in fact only use the last element
/*  386 */  //       OnestoreEntity.EntityProto entity = eg.get(liveTxn, key);
/*  387 */  //       if (entity != null) {
/*  388 */  //         group.getMutableEntity().copyFrom(entity);
/*      */  //       }
/*      */  //     }
/*      */  //   }
/*      */ 
//////////////////////////////////////////////////////////////////////////////////////
if (debug)
{
	System.out.println("the flat string of get response is: "+response.toFlatString());
	System.out.println("********get request ends here**********");
}
	DatastorePb.GetResponse response2 = new DatastorePb.GetResponse();
	response2.parseFrom(inputStreamToArray(this.outputToSocket(request.getKey(0).getApp(),"GetRequest", request)));
if (debug)
{
	System.out.println("the flat string of get response is: "+response2.toFlatString());
}
//////////////////////////////////////////////////////////////////////////////////////



/*  393 */     return response2;				//modified-return response
}
/*      */ 
public DatastorePb.PutResponse put(LocalRpcService.Status status, DatastorePb.PutRequest request) {
  try {
/*  398 */   //    this.globalLock.readLock().lock();
/*  399 */       DatastorePb.PutResponse localPutResponse = putImpl(status, request);
/*      */ 
/*  401 */       return localPutResponse; } finally {// this.globalLock.readLock().unlock();
  }
}
/*      */ 
public DatastorePb.PutResponse putImpl(LocalRpcService.Status status, DatastorePb.PutRequest request)
{
	//////////////////////////////////////////////////////////////////////////
	if (debug)
	{
		System.out.println("********put request starts here**********");
		System.out.println("the flat string of put request is: "+ request.toFlatString());
		System.out.println("sise of the entites: "+ request.entitySize());
		System.out.println("size of the entity list: "+request.entitys().size());
	}
	/////////////////////////////////////////////////////////////////////////
	
	
	
	
//  Iterator i$;
//  OnestoreEntity.EntityProto clone;
//  OnestoreEntity.Reference key;
///*  407 */     DatastorePb.PutResponse response = new DatastorePb.PutResponse();
///*  408 */     List clones = new ArrayList();
///*  409 */     String app = null;
///*  410 */     Profile profile = null;
///*  411 */     LiveTxn liveTxn = null;
//			//every entity is an entityProto
///*  412 */     for (OnestoreEntity.EntityProto entity : request.entitys()) {
///*  413 */       clone = (OnestoreEntity.EntityProto)entity.clone();
///*  414 */       clones.add(clone);
///*  415 */       assert (clone.hasKey());
///*  416 */       key = clone.getKey();
///*  417 */       assert (key.getPath().elementSize() > 0);
///*      */ 
///*  419 */       if (app == null) {
///*  420 */         app = key.getApp();
///*  421 */         profile = getOrCreateProfile(app);
//    }
///*      */ 
///*  424 */       clone.getMutableKey().setApp(app);
///*      */ 
///*  426 */       OnestoreEntity.Path.Element lastPath = (OnestoreEntity.Path.Element)getLast(key.getPath().elements());
///*      */ 
///*  428 */       if ((!(lastPath.hasId())) && (!(lastPath.hasName()))) {
///*  429 */         lastPath.setId(this.entityId.getAndIncrement());
//    }
///*      */ 
///*  432 */       if (clone.getEntityGroup().elementSize() == 0)
//    {
///*  434 */         OnestoreEntity.Path group = clone.getMutableEntityGroup();
///*  435 */         OnestoreEntity.Path.Element root = (OnestoreEntity.Path.Element)key.getPath().elements().get(0);
///*  436 */         OnestoreEntity.Path.Element pathElement = group.addElement();
///*  437 */         pathElement.setType(root.getType());
///*  438 */         if (root.hasName())
///*  439 */           pathElement.setName(root.getName());
//      else
///*  441 */           pathElement.setId(root.getId());
//    }
//    else
//    {
///*  445 */         assert ((clone.hasEntityGroup()) && (clone.getEntityGroup().elementSize() > 0));
//    }
//  }
///*      */ 
///*  449 */     synchronized (profile) {
///*  450 */       for (i$ = clones.iterator(); i$.hasNext(); ) { clone = (OnestoreEntity.EntityProto)i$.next();
///*  451 */         key = clone.getKey();
///*  452 */         String kind = ((OnestoreEntity.Path.Element)getLast(key.getPath().elements())).getType();
///*  453 */         Extent extent = getOrCreateExtent(profile, kind);
///*  454 */         Profile.EntityGroup eg = profile.getGroup(clone.getEntityGroup());
///*  455 */         if (request.hasTransaction())
//      {
///*  458 */           if (liveTxn == null) {
///*  459 */             liveTxn = (LiveTxn)safeGetFromExpiringMap(this.liveTxns, request.getTransaction().getHandle());
//        }
///*      */ 
///*  463 */           eg.addTransaction(liveTxn);
///*  464 */           liveTxn.addWrittenEntity(clone);
//      } else {
///*  466 */           eg.incrementVersion();
///*  467 */           extent.getEntities().put(key, clone);
///*  468 */           this.dirty = true;
//      }
///*  470 */         response.mutableKeys().add(clone.getKey());
//    }
//  }
			if (debug)
/*      */ 		System.out.println("********put request ends here**********");
			String app = request.getEntity(0).getKey().getApp();
			DatastorePb.PutResponse response2 = new DatastorePb.PutResponse();
			
			byte[] bytes = inputStreamToArray(this.outputToSocket(app, "PutRequest", request));
			
			
			response2.parseFrom(bytes);
			if (debug)
			{
				System.out.println("len of the response string: "+ response2.toFlatString().length());
				System.out.println("the flat string of put response2 is: "+response2.toFlatString());
			}
			return response2; //modified -return response
/*  474 */   //  return response;
}
private byte[] inputStreamToArray(InputStream in) {
//	BufferedReader reader = new BufferedReader(new InputStreamReader(in));
// //   instram.re
////    DataInputStream dia = new DataInputStream(in);
// //   dia.re
//	reader.re
	int len;
    int size = 10240;
    byte[] buf = null;
    try {
    if (in instanceof ByteArrayInputStream) {
     
    	if (debug)	
    		System.out.println("is a bytearray");
		size = in.available();
	
      buf = new byte[size];
      len = in.read(buf, 0, size);
    } else {
    	if (debug)
    		System.out.println("normal");
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      buf = new byte[size];
      long t1 = System.nanoTime();
      while ((len = in.read(buf, 0, size)) != -1){
    	  if (debug)
    		  System.out.println("get data len: "+len);
    	  bos.write(buf, 0, len);
      }
      long t2 = System.nanoTime();
      if (debug)
    	  System.out.println("wait reading time: "+ (t2-t1)/1000000.0);
      buf = bos.toByteArray();
     // System.out.println(new String(buf));
      
    }
   // String buf_s = new String(buf,"UTF8");
  //  int tempEnd = buf_s.indexOf("Content-type");
  //  buf_s = buf_s.substring(tempEnd);
  //  int end = buf_s.indexOf("\r\n\r\n");
 //   buf_s = buf_s.substring(end);
  //  end = buf_s.indexOf("\r\n"); 
  //  if (end != -1)
  //  	buf = buf_s.substring(end+4).getBytes();
  //  else
   // 	buf = buf_s.substring(0).getBytes();
    buf = removeHeaders(buf);
   // System.out.println(new String(buf));
    in.close();
    } catch (IOException e) {
		e.printStackTrace();
	}
 //   System.out.println(new String(buf));
    return buf;
}
private byte[] removeHeaders(byte[] buf) {
	int len = buf.length;
	int i = 0;
	for (; i < len; i++){
		if (buf[i] == '\r' ){
			if ((i + 1)< len && buf[i+1] == '\n'){
				if ((i + 2)< len && buf[i+2] == '\r'){
					if ((i + 3)< len && buf[i+3] == '\n'){
						break;
					}
				}
			}
		}
	}
	if (i == len){
		//not found
		return buf;
	}
	
	byte[] newbuf = new byte[len-i];
	int j = 0; 
	i +=4;
	while(i < len){
			newbuf[j++] = buf[i++];
		}	
	return newbuf;
}



/*      */ 
public DatastorePb.DeleteResponse delete(LocalRpcService.Status status, DatastorePb.DeleteRequest request) {
  try {
/*  479 */  //     this.globalLock.readLock().lock();
/*  480 */       DatastorePb.DeleteResponse localDeleteResponse = deleteImpl(status, request);
/*      */ 
/*  482 */       return localDeleteResponse; } finally { //this.globalLock.readLock().unlock();
  }
}
/*      */ 
private OnestoreEntity.Path getGroup(OnestoreEntity.Reference key)
{
/*  491 */     OnestoreEntity.Path path = key.getPath();
/*  492 */     OnestoreEntity.Path group = new OnestoreEntity.Path();
/*  493 */     group.addElement(path.getElement(0));
/*  494 */     return group;
}
/*      */ 
public DatastorePb.DeleteResponse deleteImpl(LocalRpcService.Status status, DatastorePb.DeleteRequest request)
{
/*  499 */     LiveTxn liveTxn = null;
////////////////////////////////////////////////
if (debug)
{
	System.out.println("********delete request starts here**********");
	System.out.println("the flat string of get request is: "+ request.toFlatString());
	System.out.println("sise of the keys: "+ request.keySize());
	System.out.println("size of the key list: "+request.keys().size());
}
///////////////////////////////////////////////
///*  500 */     for (OnestoreEntity.Reference key : request.keys()) {
///*  501 */       String app = key.getApp();
///*  502 */       OnestoreEntity.Path group = getGroup(key);
///*  503 */       Profile profile = getOrCreateProfile(app);
///*  504 */       if (request.hasTransaction()) {
///*  505 */         if (liveTxn == null) {
///*  506 */           liveTxn = (LiveTxn)safeGetFromExpiringMap(this.liveTxns, request.getTransaction().getHandle());
//      }
///*  508 */         synchronized (profile) {
///*  509 */           Profile.EntityGroup eg = profile.getGroup(group);
///*      */ 
///*  512 */           eg.addTransaction(liveTxn);
///*  513 */           liveTxn.addDeletedEntity(key);
//      }
///*      */ 
//    }
///*      */ 
///*  521 */       OnestoreEntity.Path.Element lastPath = (OnestoreEntity.Path.Element)getLast(key.getPath().elements());
///*  522 */       String kind = lastPath.getType();
///*  523 */       Map extents = profile.getExtents();
///*  524 */       Extent extent = (Extent)extents.get(kind);
///*  525 */       if (extent == null) {
//      continue;
//    }
///*  528 */       synchronized (profile) {
///*  529 */         Profile.EntityGroup eg = profile.getGroup(group);
///*  530 */         if (extent.getEntities().containsKey(key)) {
///*  531 */           eg.incrementVersion();
///*  532 */           extent.getEntities().remove(key);
///*  533 */           this.dirty = true;
//      }
//    }
//  }
			if (debug)
			System.out.println("********delete request ends here, returning an empty delete response**********");
/*  538 */   	inputStreamToArray(this.outputToSocket(request.getKey(0).getApp(),"DeleteRequest",request));
/*  538 */     return new DatastorePb.DeleteResponse();
}
/*      */ 
public DatastorePb.QueryResult runQuery(LocalRpcService.Status status, DatastorePb.Query query)
{
				return runQuery_New(status, query);
///*  546 */     LocalCompositeIndexManager.ValidatedQuery validatedQuery = new LocalCompositeIndexManager.ValidatedQuery(query);
///*      */ 
///*  548 */     query = validatedQuery.getQuery();
///*      */ 
///*  550 */     String app = query.getApp();
///*  551 */     Profile profile = getOrCreateProfile(app);
///*      */ 
///*  559 */     synchronized (profile) {
///*  560 */       if (query.hasTransaction())
//    {
///*  562 */         OnestoreEntity.Path groupPath = getGroup(query.getAncestor());
///*  563 */         Profile.EntityGroup eg = profile.getGroup(groupPath);
///*  564 */         LiveTxn liveTxn = (LiveTxn)safeGetFromExpiringMap(this.liveTxns, query.getTransaction().getHandle());
///*      */ 
///*  567 */         eg.addTransaction(liveTxn);
//    }
//  }
///*  570 */     Iterable queryEntities = null;
///*  571 */     Map extents = profile.getExtents();
///*  572 */     Extent extent = (Extent)extents.get(query.getKind());
///*  573 */     if (extent != null) {
///*  574 */       synchronized (extent)
//    {
///*  576 */         queryEntities = new ArrayList(extent.getEntities().values());
//    }
//  }
///*      */ 
///*  580 */     if (queryEntities == null) {
///*  581 */       queryEntities = Collections.emptyList();
//  }
///*      */ 
///*  585 */     if (query.hasAncestor()) {
///*  586 */       List ancestorPath = query.getAncestor().getPath().elements();
///*  587 */       queryEntities = Iterables.filter(queryEntities, new Predicate(ancestorPath)
//    {
//      public boolean apply(OnestoreEntity.EntityProto entity) {
///*  590 */           List path = entity.getKey().getPath().elements();
///*  591 */           return ((path.size() >= this.val$ancestorPath.size()) && (path.subList(0, this.val$ancestorPath.size()).equals(this.val$ancestorPath)));
//      }
///*      */ 
//    });
//  }
///*      */ 
///*  599 */     List filteredResults = new ArrayList();
///*      */ 
///*  601 */     for (OnestoreEntity.EntityProto queryEntity : queryEntities) {
///*  602 */       for (DatastorePb.Query.Filter filter : query.filters()) {
///*  603 */         OnestoreEntity.Property filterProperty = filter.getProperty(0);
///*  604 */         Comparable filterValue = DataTypeTranslator.getComparablePropertyValue(filterProperty);
///*      */ 
///*  608 */         assert (filter.getOpEnum() != DatastorePb.Query.Filter.Operator.IN);
///*      */ 
///*  610 */         Collection entityProps = DataTypeTranslator.findIndexedPropertiesOnPb(queryEntity, filterProperty.getName());
///*      */ 
///*  612 */         if (!(entityProps.isEmpty()));
///*  618 */         boolean atLeastOneValueMatches = false;
///*  619 */         for (OnestoreEntity.Property entityProp : entityProps) {
///*  620 */           Comparable singleValue = DataTypeTranslator.getComparablePropertyValue(entityProp);
///*      */ 
///*  622 */           if (matches(singleValue, filterValue, filter.getOpEnum())) {
///*  623 */             atLeastOneValueMatches = true;
///*  624 */             break;
//        }
///*      */ 
//      }
///*      */ 
///*  628 */         if (!(atLeastOneValueMatches));
//    }
///*      */ 
///*  634 */       filteredResults.add(queryEntity);
//  }
///*      */ 
///*  639 */     Object orderProperties = new HashSet();
///*      */ 
///*  641 */     for (DatastorePb.Query.Order order : query.orders()) {
///*  642 */       ((Set)orderProperties).add(order.getProperty());
//  }
///*      */ 
///*  645 */     for (Iterator protoIt = filteredResults.iterator(); protoIt.hasNext(); ) {
///*  646 */       OnestoreEntity.EntityProto proto = (OnestoreEntity.EntityProto)protoIt.next();
///*  647 */       Map entityProperties = new HashMap();
///*  648 */       DataTypeTranslator.extractIndexedPropertiesFromPb(proto, entityProperties);
///*      */ 
///*  650 */       DataTypeTranslator.extractImplicitPropertiesFromPb(proto, entityProperties);
///*  651 */       if (!(entityProperties.keySet().containsAll((Collection)orderProperties))) {
///*  652 */         protoIt.remove();
//    }
///*      */ 
//  }
///*      */ 
///*  657 */     Object sortComparator = new Comparator(validatedQuery)
//  {
//    public int compare(OnestoreEntity.EntityProto protoA, OnestoreEntity.EntityProto protoB)
//    {
///*  663 */         for (Iterator i$ = this.val$validatedQuery.getQuery().orders().iterator(); i$.hasNext(); )
//      {
//        int result;
///*  663 */           DatastorePb.Query.Order order = (DatastorePb.Query.Order)i$.next();
///*  664 */           String property = order.getProperty();
//        try
//        {
///*  667 */             Collection aValues = LocalDatastoreService.getComparablePropertyValues(protoA, property);
///*  668 */             Collection bValues = LocalDatastoreService.getComparablePropertyValues(protoB, property);
///*      */ 
///*  671 */             Comparable minA = LocalDatastoreService.multiTypeMin(aValues);
///*  672 */             Comparable minB = LocalDatastoreService.multiTypeMin(bValues);
///*      */ 
///*  675 */             result = LocalDatastoreService.MULTI_TYPE_COMPARATOR.compare(minA, minB);
//        }
//        catch (LocalDatastoreService.NonExistentPropertyException e)
//        {
///*  679 */             throw new IllegalStateException("Trying to sort on a non-existent property.");
//        }
///*      */ 
///*  682 */           if (result != 0) {
///*  683 */             if (order.getDirectionEnum() == DatastorePb.Query.Order.Direction.DESCENDING) {
///*  684 */               result = -result;
//          }
///*  686 */             return result;
//        }
///*      */ 
//      }
///*      */ 
///*  691 */         return 0;
//    }
//  };
///*  695 */     Collections.sort(filteredResults, (Comparator)sortComparator);
///*      */ 
///*  698 */     if (query.hasOffset()) {
///*  699 */       int offset = Math.min(filteredResults.size(), query.getOffset());
///*  700 */       filteredResults = new ArrayList(filteredResults.subList(offset, filteredResults.size()));
//  }
///*      */ 
///*  705 */     if (query.hasLimit())
//  {
///*  708 */       int limit = Math.min(filteredResults.size(), query.getLimit());
///*  709 */       filteredResults = new ArrayList(filteredResults.subList(0, limit));
//  }
///*      */ 
///*  713 */     long cursor = this.queryId.getAndIncrement();
///*  714 */     this.liveQueries.put(Long.valueOf(cursor), new LiveQuery(filteredResults, System.currentTimeMillis(), query.isKeysOnly()));
///*      */ 
///*  719 */     AccessController.doPrivileged(new PrivilegedAction(validatedQuery) {
//    public Object run() {
///*  721 */         LocalCompositeIndexManager.getInstance().processQuery(this.val$validatedQuery.getQuery());
///*  722 */         return null;
//    }
//  });
///*  727 */     int count = 0;
///*  728 */     if (query.hasCount())
///*  729 */       count = query.getCount();
///*  730 */     else if (query.hasLimit())
///*  731 */       count = query.getLimit();
//  else {
///*  733 */       count = 20;
//  }
///*      */ 
///*  737 */     DatastorePb.NextRequest nextReq = new DatastorePb.NextRequest();
///*  738 */     nextReq.getMutableCursor().setCursor(cursor);
///*  739 */     nextReq.setCount(count);
///*  740 */     return ((DatastorePb.QueryResult)(DatastorePb.QueryResult)next(status, nextReq));
}
/////////////////////////////////////////////////////////////////////////////////////////////change-new
private QueryResult runQuery_New(Status status, Query query) {
	if (debug)
		System.out.println("the flat string of the query is: "+query.toFlatString());
	DatastorePb.QueryResult queryResult = new DatastorePb.QueryResult();
	String appID = query.getApp();
	queryResult.parseFrom(inputStreamToArray(this.outputToSocket(appID, "Query", query)));
	if (debug)
		System.out.println("query result before set cursor"+queryResult.toFlatString());
	//not sure about this...
	long cursor = this.queryId.getAndIncrement();
	
	//System.out.println("the current cursor is: "+ cursor);
	int count = 0;
	if (query.hasCount())
	   count = query.getCount();
   else if (query.hasLimit())
    count = query.getLimit();
	   else {
    count = 20;
	   }
	
	
//	System.out.println("current count is: "+count);
	
//	 DatastorePb.NextRequest nextReq = new DatastorePb.NextRequest();
//	 nextReq.getMutableCursor().setCursor(cursor);
//	 nextReq.setCount(count);
	
	

	 /*  813 */     int end = Math.min(count, queryResult.resultSize());
	 List subList = queryResult.results().subList(0, end);
	 /*  816 */     List nextResults = new ArrayList(subList);
	 /*  817 */  //   subList.clear();
	
//	System.out.println("the flat string of next req is: "+ nextReq.toFlatString());

	if (debug)
		System.out.println("the flat string of new query result is: "+ queryResult.toFlatString());
	
	 DatastorePb.QueryResult final_oresult = new DatastorePb.QueryResult();
	 
	 for (Object proto : nextResults) {
			if (!(nextResults instanceof OnestoreEntity.EntityProto)){
				if (debug)
					System.out.println("nextResults wrong type!! but continue to cast");
			}
			else{
				if (debug)
					System.out.println("nextResults correct type!!");
			}
	       final_oresult.addResult((OnestoreEntity.EntityProto )proto);
	 }
	 
	 final_oresult.getMutableCursor().setCursor(cursor);
	 final_oresult.setKeysOnly(query.isKeysOnly());
	 final_oresult.setMoreResults((queryResult.results().size() - end)>0);
	 if (debug)
		System.out.println("the flat string of new final result is: "+ final_oresult.toFlatString());

	//copy the implementation from py version
//	Iterable queryEntities = null;
//	
//	if (query.hasAncestor()) {runQuery_New
//		System.out.println("this query has an ancestor: "+ query.getAncestor().getPath());
//		
///*  585 */       final List ancestorPath = query.getAncestor().getPath().elements();
///*  586 */       queryEntities = Iterables.filter(queryEntities, new Predicate()
//    {
//      public boolean apply(OnestoreEntity.EntityProto entity) {
///*  589 */           List path = entity.getKey().getPath().elements();
///*  590 */           return ((path.size() >= ancestorPath.size()) && (path.subList(0, ancestorPath.size()).equals(ancestorPath)));
//      }
///*      */ 
///*      */
//
//		@Override
//		public boolean apply(Object arg0) {
//			return apply((OnestoreEntity.EntityProto)arg0);
//		}      
//		});
//  }	
	return final_oresult;
}
//////////////////////////////////////////////////////////////////////////////////////////////////
/*      */ 
static Comparable<Object> multiTypeMin(Collection<Comparable<Object>> comparables)
{
/*  747 */     Comparable smallest = null;
/*  748 */     for (Comparable comp : comparables) {
/*  749 */       if (smallest == null)
/*  750 */         smallest = comp;
/*  751 */       else if (MULTI_TYPE_COMPARATOR.compare(comp, smallest) < 0) {
/*  752 */         smallest = comp;
    }
  }
/*  755 */     return smallest;
}
/*      */ 
static List<Comparable<Object>> getComparablePropertyValues(OnestoreEntity.EntityProto entityProto, String propertyName)
  throws LocalDatastoreService.NonExistentPropertyException
{
/*  777 */     Collection entityProperties = DataTypeTranslator.findIndexedPropertiesOnPb(entityProto, propertyName);
/*      */ 
/*  779 */     if (entityProperties.isEmpty()) {
/*  780 */       throw new LocalDatastoreService.NonExistentPropertyException();
  }
/*  782 */     List values = new ArrayList(entityProperties.size());
/*  783 */     for (Object prop : entityProperties) {
/*  784 */       values.add(DataTypeTranslator.getComparablePropertyValue((OnestoreEntity.Property)prop));
  }
/*  786 */     return values;
}
/*      */ 
private static <T> T safeGetFromExpiringMap(Map<Long, T> map, long key)
{
/*  795 */     T value = map.get(Long.valueOf(key));
/*  796 */     if (value == null) {
/*  797 */       throw new ApiProxy.ApplicationException(DatastorePb.Error.ErrorCode.INTERNAL_ERROR.getValue(), String.format("handle %s not found", new Object[] { Long.valueOf(key) }));
  }
/*      */ 
/*  800 */     return value;
}
/*      */ 
public DatastorePb.QueryResult next(LocalRpcService.Status status, DatastorePb.NextRequest request) {
/*  804 */     
/////////////////////////////////////////////////////////////////////////////////////
if (debug)
{
	System.out.println("********next request starts here**********");
	System.out.println("the flat string of next request is: "+ request.toFlatString());
}
/////////////////////////////////////////////////////////////////////////////////////
//				DatastorePb.QueryResult result = new DatastorePb.QueryResult();
///*      */ 
///*  806 */     long cursorId = request.getCursor().getCursor();
///*  807 */     LiveQuery liveQuery = (LiveQuery)safeGetFromExpiringMap(this.liveQueries, cursorId);
///*  808 */     List queryContents = liveQuery.getEntities();
///*      */ 
///*  810 */     int count = 20;
///*  811 */     if (request.hasCount()) {
///*  812 */       count = request.getCount();
//  }
///*  814 */     int end = Math.min(count, queryContents.size());
///*      */ 
///*  816 */     List subList = queryContents.subList(0, end);
///*  817 */     List nextResults = new ArrayList(subList);
///*  818 */     subList.clear();
///*  819 */     for (Object proto : nextResults) {
///*  820 */       result.addResult((OnestoreEntity.EntityProto )proto);
//  }
///*  822 */     result.getMutableCursor().setCursor(cursorId);
///*  823 */     result.setMoreResults(queryContents.size() > 0);
///*  824 */     result.setKeysOnly(liveQuery.isKeysOnly());
/*      */ 
/////////////////////////////////////////////////////////////
//System.out.println("the flat string of quey result is: "+result.toFlatString());
if (debug)
	System.out.println("***********next ends here****************");
DatastorePb.QueryResult result2 = new DatastorePb.QueryResult();
result2.parseFrom(inputStreamToArray(this.outputToSocket("sudoId_for_next", "Next", request)));
if (debug)
	System.out.println("the result of new queryResult: "+ result2);
return result2;//modified return result;
/////////////////////////////////////////////////////////////////////////////

/*  826 */   //  return result;
}
/*      */ 
public ApiBasePb.Integer64Proto count(LocalRpcService.Status status, DatastorePb.Query request) {
	System.out.println("------count begins--------");
/*  830 */     LocalRpcService.Status queryStatus = new LocalRpcService.Status();
/*  831 */     DatastorePb.QueryResult queryResult = runQuery(queryStatus, request);
/*  832 */     long cursor = queryResult.getCursor().getCursor();
/*      */ 
String appID = request.getApp();
/*  837 */     List queryData = ((LiveQuery)safeGetFromExpiringMap(this.liveQueries, cursor)).getEntities();
/*      */ 
/*  839 */     this.liveQueries.remove(Long.valueOf(cursor));
/*  840 */     ApiBasePb.Integer64Proto results = new ApiBasePb.Integer64Proto();
/*  841 */     results.setValue(queryData.size() + queryResult.resultSize());
System.out.println("the flat string of Integer64Proto result is: "+ results.toFlatString());
System.out.println("----------count ends here----------------");
inputStreamToArray(this.outputToSocket(appID, "Count", request));
/*  842 */     return results;
}
/*      */ 
public ApiBasePb.VoidProto deleteCursor(LocalRpcService.Status status, DatastorePb.Cursor request) {
/*  846 */     this.liveQueries.remove(Long.valueOf(request.getCursor()));
/*  847 */     return new ApiBasePb.VoidProto();
}
/*      */ 
public DatastorePb.QueryExplanation explain(LocalRpcService.Status status, DatastorePb.Query req)
{
/*  854 */     throw new UnsupportedOperationException("Not yet implemented.");
}
/*      */ 
public DatastorePb.Transaction beginTransaction(LocalRpcService.Status status, ApiBasePb.VoidProto req)
{
	///////////////////////////////////////////
	if (debug)
		System.out.println("------begin transaction begins--------");
	//////////////////////////////////////////
/*  860 */     DatastorePb.Transaction txn = new DatastorePb.Transaction().setHandle(this.transactionHandleProvider.getAndIncrement());
/*  861 */     this.liveTxns.put(Long.valueOf(txn.getHandle()), new LiveTxn(System.currentTimeMillis()));
/*  862 */     
///////////////////////////////
/*  861 */  if (debug)   	
			System.out.println("handle is: "+ txn.getHandle());
String appID="sudoId_for_begin_transaction";
if (debug)
System.out.println("------begin transactions ends here--------");
inputStreamToArray(this.outputToSocket(appID, "BeginTransaction", txn));
///////////////////////////////
		return txn;
}
/*      */ 
private void removeLiveTxn(long handle) {
/*  866 */     LiveTxn txn = (LiveTxn)safeGetFromExpiringMap(this.liveTxns, handle);
/*  867 */     txn.close();
/*  868 */     this.liveTxns.remove(Long.valueOf(handle));
}
/*      */ 
public DatastorePb.CommitResponse commit(LocalRpcService.Status status, DatastorePb.Transaction req) {
/*  872 */     LiveTxn liveTxn = (LiveTxn)safeGetFromExpiringMap(this.liveTxns, req.getHandle());
/*      */ 
/*  874 */     synchronized (liveTxn) {
/*  875 */       removeLiveTxn(req.getHandle());
/*  876 */       if (liveTxn.isDirty()) {
      try {
/*  878 */       //    this.globalLock.readLock().lock();
/*  879 */           commitImpl(liveTxn);
      } finally {
/*  881 */       //    this.globalLock.readLock().unlock();
      }
    }
  }
DatastorePb.CommitResponse res = new DatastorePb.CommitResponse();
//just give a handle
inputStreamToArray(this.outputToSocket(liveTxn.getApp(), "Commit", res));
/*  884 */     return res;
}
/*      */ 
private void commitImpl(LiveTxn liveTxn) {
	if (debug)
	{	
		System.out.println("------commit liveTxn begins--------");
		System.out.println("the app id is: "+ liveTxn.getApp());
	}
		/*  889 */     Profile profile = (Profile)this.profiles.get(liveTxn.getApp());
/*  890 */     assert (profile != null);
/*  891 */     Profile.EntityGroup eg = liveTxn.getEntityGroup();
/*  892 */     synchronized (profile)
  {
    OnestoreEntity.Path.Element lastPath;
    String kind;
    Map extents;
    Extent extent;
/*  895 */       liveTxn.checkEntityGroupVersion();
/*  896 */       eg.incrementVersion();
/*  897 */       for (OnestoreEntity.EntityProto entity : liveTxn.getWrittenEntities()) {
/*  898 */         lastPath = (OnestoreEntity.Path.Element)getLast(entity.getKey().getPath().elements());
/*  899 */         kind = lastPath.getType();
/*  900 */         extents = profile.getExtents();
/*  901 */         extent = getOrCreateExtent(profile, kind);
/*  902 */         extent.getEntities().put(entity.getKey(), entity);
    }
/*  904 */       for (OnestoreEntity.Reference key : liveTxn.getDeletedKeys()) {
/*  905 */         lastPath = (OnestoreEntity.Path.Element)getLast(key.getPath().elements());
/*  906 */         kind = lastPath.getType();
/*  907 */         extents = profile.getExtents();
/*  908 */         extent = (Extent)extents.get(kind);
/*  909 */         if (extent != null) {
/*  910 */           extent.getEntities().remove(key);
      }
/*      */ 
    }
/*      */ 
/*  915 */       this.dirty = true;
  }
if (debug)
System.out.println("------commit liveTxn ends--------");
}
/*      */ 
public ApiBasePb.VoidProto rollback(LocalRpcService.Status status, DatastorePb.Transaction req) {
/*  920 */     
	////////////////////////////////
	System.out.println("------rollback liveTxn begins--------");
	LiveTxn liveTxn = (LiveTxn)safeGetFromExpiringMap(this.liveTxns, req.getHandle());
/*      */ 
	////////////////////////////
	System.out.println("the handle to remove is: "+ req.getHandle());
/*  922 */     synchronized (liveTxn) {
/*  923 */       removeLiveTxn(req.getHandle());
  }
System.out.println("------rollback liveTxn ends, returnning a voidProto--------");
System.out.println("rollback is not implemented in appscale GAE/J");
/*  925 */     return new ApiBasePb.VoidProto();
}
/*      */ 
public DatastorePb.Schema getSchema(LocalRpcService.Status status, DatastorePb.GetSchemaRequest req)
{
  
	///////////////////////////////////
	System.out.println("------getSchema request begins--------");
	System.out.println("the flat string of the request is: "+req.toFlatString());
	OnestoreEntity.EntityProto allPropsProto;
  Map allProps;
/*  931 */     if ((req.hasStartKind()) && (req.hasEndKind())) {
/*  932 */       Preconditions.checkArgument(req.getStartKind().compareTo(req.getEndKind()) <= 0, "start_kind must be <= end_kind");
  }
/*      */ 
/*  935 */     DatastorePb.Schema schema = new DatastorePb.Schema();
/*  936 */     Profile profile = getOrCreateProfile(req.getApp());
/*  937 */     Map extents = profile.getExtents();
/*  938 */     synchronized (extents)
  {
/*  942 */       for (Object entry : extents.entrySet()) {
/*  943 */       //  String kind = (String)entry.getKey();
/*  944 */            String kind = (String)((Map.Entry )entry).getKey();
					if ((req.hasStartKind()) && (kind.compareTo(req.getStartKind()) < 0)) continue; if ((req.hasEndKind()) && (kind.compareTo(req.getEndKind()) > 0)) {
        continue;
      }
/*  947 */         if (((Extent)((Map.Entry )entry).getValue()).getEntities().isEmpty())
      {
        continue;
      }
/*      */ 
/*  952 */         allPropsProto = new OnestoreEntity.EntityProto();
/*  953 */         schema.addKind(allPropsProto);
/*  954 */         OnestoreEntity.Path path = new OnestoreEntity.Path();
/*  955 */         path.addElement().setType(kind);
/*  956 */         allPropsProto.setKey(new OnestoreEntity.Reference().setApp(req.getApp()).setPath(path));
/*      */ 
/*  959 */         allPropsProto.getMutableEntityGroup();
/*      */ 
/*  961 */         if (req.isProperties())
      {
/*  963 */           allProps = new HashMap();
/*  964 */           for (OnestoreEntity.EntityProto entity : ((Extent)((Map.Entry )entry).getValue()).getEntities().values()) {
/*  965 */             for (OnestoreEntity.Property prop : entity.propertys())
          {
/*  967 */               OnestoreEntity.Property schemaProp = (OnestoreEntity.Property)allProps.get(prop.getName());
/*  968 */               if (schemaProp == null) {
/*  969 */                 schemaProp = allPropsProto.addProperty().setName(prop.getName()).setMultiple(false);
/*      */ 
/*  971 */                 allProps.put(prop.getName(), schemaProp);
            }
/*      */ 
/*  975 */               PropertyType type = PropertyType.getType(prop.getValue());
/*  976 */               schemaProp.getMutableValue().mergeFrom(type.placeholderValue);
          }
        }
      }
    }
  }
/*      */ 
/*  983 */     schema.setMoreResults(false);
System.out.println("the flat string of the schema is: "+ schema.toFlatString());
System.out.println("------getSchema ends--------");
/*  984 */     return schema;
}
/*      */ 
public ApiBasePb.Integer64Proto createIndex(LocalRpcService.Status status, OnestoreEntity.CompositeIndex req)
{
/*  990 */     throw new UnsupportedOperationException("Not yet implemented.");
}
/*      */ 
public ApiBasePb.VoidProto updateIndex(LocalRpcService.Status status, OnestoreEntity.CompositeIndex req) {
/*  994 */     throw new UnsupportedOperationException("Not yet implemented.");
}
/*      */ 
public DatastorePb.CompositeIndices getIndices(LocalRpcService.Status status, ApiBasePb.StringProto req) {
/*  998 */     throw new UnsupportedOperationException("Not yet implemented.");
}
/*      */ 
public ApiBasePb.VoidProto deleteIndex(LocalRpcService.Status status, OnestoreEntity.CompositeIndex req) {
/* 1002 */     throw new UnsupportedOperationException("Not yet implemented.");
}
/*      */ 
public DatastorePb.AllocateIdsResponse allocateIds(LocalRpcService.Status status, DatastorePb.AllocateIdsRequest req) {
  try {
/* 1007 */    //   this.globalLock.readLock().lock();
/* 1008 */       DatastorePb.AllocateIdsResponse localAllocateIdsResponse = allocateIdsImpl(req);
/*      */ 
/* 1010 */       return localAllocateIdsResponse; } finally {// this.globalLock.readLock().unlock();
  }
}
/*      */ 
private DatastorePb.AllocateIdsResponse allocateIdsImpl(DatastorePb.AllocateIdsRequest req) {
/* 1015 */       System.out.println("------------allocateId request begins--------------");
System.out.println("the size of req is: "+ req.getSize()); 
	if (req.getSize() > 1000000000L) {
/* 1016 */       throw new ApiProxy.ApplicationException(DatastorePb.Error.ErrorCode.BAD_REQUEST.getValue(), "cannot get more than 1000000000 keys in a single call");
  }
/*      */ 
/* 1026 */     long start = this.entityId.getAndAdd(req.getSize());
/* 1027 */     return new DatastorePb.AllocateIdsResponse().setStart(start).setEnd(start + req.getSize() - 1L);
}
/*      */ 
private Profile getOrCreateProfile(String app)
{
/* 1033 */     synchronized (this.profiles) {
/* 1034 */       Profile profile = (Profile)this.profiles.get(app);
/* 1035 */       if (profile == null) {
/* 1036 */         profile = new Profile();
/* 1037 */         this.profiles.put(app, profile);
    }
/* 1039 */       return profile;
  }
}
/*      */ 
private Extent getOrCreateExtent(Profile profile, String kind) {
/* 1044 */     Map extents = profile.getExtents();
/* 1045 */     synchronized (extents) {
/* 1046 */       Extent e = (Extent)extents.get(kind);
/* 1047 */       if (e == null) {
/* 1048 */         e = new Extent();
/* 1049 */         extents.put(kind, e);
    }
/* 1051 */       return e;
  }
}
/*      */ 
private boolean matches(Comparable<Object> value1, Comparable<Object> value2, DatastorePb.Query.Filter.Operator op)
{
/* 1058 */     switch (op.ordinal()+1)
  {
  case 1:
/* 1060 */       return (MULTI_TYPE_COMPARATOR.compare(value1, value2) == 0);
  case 2:
/* 1063 */       return (MULTI_TYPE_COMPARATOR.compare(value1, value2) > 0);
  case 3:
/* 1066 */       return (MULTI_TYPE_COMPARATOR.compare(value1, value2) >= 0);
  case 4:
/* 1069 */       return (MULTI_TYPE_COMPARATOR.compare(value1, value2) < 0);
  case 5:
/* 1072 */       return (MULTI_TYPE_COMPARATOR.compare(value1, value2) <= 0);
  }
/*      */ 
/* 1075 */     throw new ApiProxy.ApplicationException(DatastorePb.Error.ErrorCode.INTERNAL_ERROR.getValue(), "Unable to perform filter using operator " + op);
}
/*      */ 
private void load()
{
/* 1082 */     File backingStoreFile = new File(this.backingStore);
/* 1083 */     String path = backingStoreFile.getAbsolutePath();
/* 1084 */     if (!(backingStoreFile.exists())) {
/* 1085 */       logger.log(Level.INFO, "The backing store, " + path + ", does not exist. " + "It will be created.");
/*      */ 
/* 1087 */       return;
  }
  try
  {
/* 1091 */       long start = System.currentTimeMillis();
/* 1092 */       ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(this.backingStore)));
/*      */ 
/* 1095 */       this.entityId.set(objectIn.readLong());
/*      */ 
/* 1097 */       Map profilesOnDisk = (Map)objectIn.readObject();
/* 1098 */       this.profiles = profilesOnDisk;
/*      */ 
/* 1100 */       objectIn.close();
/* 1101 */       long end = System.currentTimeMillis();
/*      */ 
/* 1103 */       logger.log(Level.INFO, "Time to load datastore: " + (end - start) + " ms");
  }
  catch (FileNotFoundException e) {
/* 1106 */       logger.log(Level.SEVERE, "Failed to find the backing store, " + path);
  } catch (IOException e) {
/* 1108 */       logger.log(Level.INFO, "Failed to load from the backing store, " + path, e);
  } catch (ClassNotFoundException e) {
/* 1110 */       logger.log(Level.INFO, "Failed to load from the backing store, " + path, e);
  }
}
/*      */ 
private static <T> T getLast(List<T> list)
{
/* 1139 */     return list.get(list.size() - 1);
}
/*      */ 
static void pruneHasCreationTimeMap(long now, int maxLifetimeMs, Map<Long, ? extends HasCreationTime> hasCreationTimeMap)
{
/* 1500 */     long deadline = now - maxLifetimeMs;
/* 1501 */     Iterator queryIt = hasCreationTimeMap.entrySet().iterator();
/* 1502 */     while (queryIt.hasNext()) {
/* 1503 */       Map.Entry entry = (Map.Entry)queryIt.next();
/* 1504 */       HasCreationTime query = (HasCreationTime)entry.getValue();
/* 1505 */       if (query.getCreationTime() < deadline)
/* 1506 */         queryIt.remove();
  }
}
/*      */ 
void removeStaleQueriesNow()
{
/* 1567 */     this.removeStaleQueriesTask.run();
}
/*      */ 
void removeStaleTxnsNow()
{
/* 1572 */     this.removeStaleTransactionsTask.run();
}
/*      */ 
static
{
/*  105 */     logger = Logger.getLogger(LocalDatastoreService.class.getName());
/*      */ 
/* 1114 */     MULTI_TYPE_COMPARATOR = new Comparator()
  {
    public int compare(Comparable<Object> o1, Comparable<Object> o2) {
/* 1117 */         if (o1 == null) {
/* 1118 */           if (o2 == null) {
/* 1119 */             return 0;
        }
/*      */ 
/* 1122 */           return -1; }
/* 1123 */         if (o2 == null)
      {
/* 1125 */           return 1;
      }
/* 1127 */         Integer comp1TypeRank = Integer.valueOf(DataTypeTranslator.getTypeRank(o1.getClass()));
/* 1128 */         Integer comp2TypeRank = Integer.valueOf(DataTypeTranslator.getTypeRank(o2.getClass()));
/* 1129 */         if (comp1TypeRank.equals(comp2TypeRank))
      {
/* 1131 */           return o1.compareTo(o2);
      }
/* 1133 */         return comp1TypeRank.compareTo(comp2TypeRank);
    }
/*      */

@Override
public int compare(Object o1, Object o2) {
	return compare((Comparable<Object>)o1, (Comparable<Object>)o2);
}     };
}
/*      */ 
private class PersistDatastore
  implements Runnable
{
  public void run()
  {
//    try
//    {
///* 1519 */    //     LocalDatastoreService.this.globalLock.writeLock().lock();
///* 1520 */         privilegedPersist();
//    } catch (IOException e) {
///* 1522 */         LocalDatastoreService.logger.log(Level.SEVERE, "Unable to save the datastore", e);
//    } finally {
///* 1524 */ //        LocalDatastoreService.this.globalLock.writeLock().unlock();
//    }
  }
/*      */ 
//  private void privilegedPersist() throws IOException {
//    try {
///* 1530 */         AccessController.doPrivileged(new PrivilegedExceptionAction() {
//        public Object run() throws IOException {
///* 1532 */             LocalDatastoreService.PersistDatastore.this.persist();
///* 1533 */             return null;
//        } } );
//    }
//    catch (PrivilegedActionException e) {
///* 1537 */         Throwable t = e.getCause();
///* 1538 */         if (t instanceof IOException) {
///* 1539 */           throw ((IOException)t);
//      }
///* 1541 */         throw new RuntimeException(t);
//    }
//  }
///*      */ 
//  private void persist() throws IOException {
///* 1546 */       if (!(LocalDatastoreService.this.dirty)) {
///* 1547 */         return;
//    }
///*      */ 
///* 1550 */       long start = System.currentTimeMillis();
///* 1551 */       ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(LocalDatastoreService.this.backingStore)));
///*      */ 
///* 1554 */       objectOut.writeLong(LocalDatastoreService.this.entityId.get());
///* 1555 */       objectOut.writeObject(LocalDatastoreService.this.profiles);
///*      */ 
///* 1557 */       objectOut.close();
///* 1558 */       LocalDatastoreService.this.dirty = false;
///* 1559 */       long end = System.currentTimeMillis();
///*      */ 
///* 1561 */       LocalDatastoreService.logger.log(Level.INFO, "Time to persist datastore: " + (end - start) + " ms");
//  }
}
/*      */ 
private class RemoveStaleTransactions
  implements Runnable
{
  public void run()
  {
/* 1488 */       synchronized (LocalDatastoreService.this.liveTxns) {
					if (debug)
						System.out.println("removing stale transactions");
/* 1489 */         LocalDatastoreService.pruneHasCreationTimeMap(System.currentTimeMillis(), LocalDatastoreService.this.maxTransactionLifetimeMs, LocalDatastoreService.this.liveTxns);
    }
  }
}
/*      */ 
private class RemoveStaleQueries
  implements Runnable
{
  public void run()
  {
/* 1474 */       synchronized (LocalDatastoreService.this.liveQueries) {
					if (debug)
						System.out.println("removing stale queries");
/* 1475 */         LocalDatastoreService.pruneHasCreationTimeMap(System.currentTimeMillis(), LocalDatastoreService.this.maxQueryLifetimeMs, LocalDatastoreService.this.liveQueries);
    }
  }
}
/*      */ 
static class LiveTxn extends LocalDatastoreService.HasCreationTime
{
  private LocalDatastoreService.Profile.EntityGroup entityGroup;
  private Long entityGroupVersion;
/* 1367 */     private final Map<OnestoreEntity.Reference, OnestoreEntity.EntityProto> written = new HashMap();
/* 1368 */     private final Set<OnestoreEntity.Reference> deleted = Collections.synchronizedSet(new HashSet());
  private String app;
/*      */ 
  public LiveTxn(long creationTime)
  {
/* 1373 */       super(creationTime);
  }
/*      */ 
  public synchronized void setEntityGroup(LocalDatastoreService.Profile.EntityGroup newEntityGroup)
  {
/* 1380 */       if (newEntityGroup == null) {
/* 1381 */         throw new NullPointerException("entityGroup cannot be null");
    }
/*      */ 
/* 1384 */       if (this.entityGroupVersion == null) {
/* 1385 */         this.entityGroupVersion = Long.valueOf(newEntityGroup.getVersion());
/* 1386 */         this.entityGroup = newEntityGroup;
    }
/*      */ 
/* 1389 */       if (!(newEntityGroup.equals(this.entityGroup)))
/* 1390 */         throw new ApiProxy.ApplicationException(DatastorePb.Error.ErrorCode.BAD_REQUEST.getValue(), "can't operate on multiple entity groups in a single transaction. found both " + this.entityGroup + " and " + newEntityGroup);
  }
/*      */ 
  public synchronized LocalDatastoreService.Profile.EntityGroup getEntityGroup()
  {
/* 1397 */       return this.entityGroup;
  }
/*      */ 
  public synchronized void checkEntityGroupVersion() {
/* 1401 */       if (!(this.entityGroupVersion.equals(Long.valueOf(this.entityGroup.getVersion()))))
/* 1402 */         throw new ApiProxy.ApplicationException(DatastorePb.Error.ErrorCode.CONCURRENT_TRANSACTION.getValue(), "too much contention on these datastore entities. please try again.");
  }
/*      */ 
  public synchronized Long getEntityGroupVersion()
  {
/* 1409 */       return this.entityGroupVersion;
  }
/*      */ 
  public synchronized void addWrittenEntity(OnestoreEntity.EntityProto entity)
  {
/* 1416 */       OnestoreEntity.Reference key = entity.getKey();
/* 1417 */       this.app = key.getApp();
/* 1418 */       this.written.put(key, entity);
/*      */ 
/* 1422 */       this.deleted.remove(key);
  }
/*      */ 
  public synchronized void addDeletedEntity(OnestoreEntity.Reference key)
  {
/* 1429 */       this.app = key.getApp();
/* 1430 */       this.deleted.add(key);
/*      */ 
/* 1434 */       this.written.remove(key);
  }
/*      */ 
  public synchronized Collection<OnestoreEntity.EntityProto> getWrittenEntities() {
/* 1438 */       return new ArrayList(this.written.values());
  }
/*      */ 
  public synchronized Collection<OnestoreEntity.Reference> getDeletedKeys() {
/* 1442 */       return new ArrayList(this.deleted);
  }
/*      */ 
  public synchronized String getApp() {
/* 1446 */       return this.app;
  }
/*      */ 
  public synchronized boolean isDirty() {
/* 1450 */       return (this.written.size() + this.deleted.size() > 0);
  }
/*      */ 
  public void close()
  {
/* 1460 */       if (this.entityGroup != null)
/* 1461 */         this.entityGroup.removeTransaction(this);
  }
}
/*      */ 
static class LiveQuery extends LocalDatastoreService.HasCreationTime
{
  private final List<OnestoreEntity.EntityProto> entities;
  private final boolean keysOnly;
/*      */ 
  public LiveQuery(List<OnestoreEntity.EntityProto> entities, long creationTime, boolean keysOnly)
  {
/* 1325 */       super(creationTime);
/* 1326 */       if (entities == null) {
/* 1327 */         throw new NullPointerException("entities cannot be null");
    }
/* 1329 */       this.keysOnly = keysOnly;
/*      */ 
/* 1331 */       if (keysOnly) {
/* 1332 */         this.entities = new ArrayList();
/* 1333 */         for (OnestoreEntity.EntityProto entity : entities)
      {
/* 1335 */           this.entities.add(((OnestoreEntity.EntityProto)entity.clone()).clearOwner().clearProperty().clearRawProperty());
      }
    } else {
/* 1338 */         this.entities = entities;
    }
  }
/*      */ 
  public List<OnestoreEntity.EntityProto> getEntities() {
/* 1343 */       return this.entities;
  }
/*      */ 
  public boolean isKeysOnly() {
/* 1347 */       return this.keysOnly;
  }
}
/*      */ 
static abstract class HasCreationTime
{
  private final long creationTime;
/*      */ 
  HasCreationTime(long creationTime)
  {
/* 1308 */       this.creationTime = creationTime;
  }
/*      */ 
  long getCreationTime() {
/* 1312 */       return this.creationTime;
  }
}
/*      */ 
private static class Extent
  implements Serializable
{
  private Map<OnestoreEntity.Reference, OnestoreEntity.EntityProto> entities;
/*      */ 
  private Extent()
  {
/* 1297 */       this.entities = new LinkedHashMap(); }
/*      */ 
  public Map<OnestoreEntity.Reference, OnestoreEntity.EntityProto> getEntities() {
/* 1300 */       return this.entities;
  }
}
/*      */ 
private static class Profile
  implements Serializable
{
  private final Map<String, LocalDatastoreService.Extent> extents;
  private transient Map<OnestoreEntity.Path, EntityGroup> groups;
/*      */ 
  private Profile()
  {
/* 1250 */       this.extents = Collections.synchronizedMap(new HashMap());
					if (debug)
					System.out.println("profile is created");
				
  }
/*      */ 
  public Map<String, LocalDatastoreService.Extent> getExtents()
  {
/* 1267 */       return this.extents;
  }
/*      */ 
  public synchronized EntityGroup getGroup(OnestoreEntity.Path path) {
/* 1271 */       if (this.groups == null) {
/* 1272 */         this.groups = new HashMap();
    }
/* 1274 */       EntityGroup group = (EntityGroup)this.groups.get(path);
/* 1275 */       if (group == null) {
/* 1276 */         group = new EntityGroup(path);
/* 1277 */         this.groups.put(path, group);
    }
/* 1279 */       return group;
  }
/*      */ 
  private class EntityGroup
  {
    private final OnestoreEntity.Path path;
/* 1155 */       private final AtomicLong version = new AtomicLong();
/* 1156 */       private final WeakHashMap<LocalDatastoreService.LiveTxn, LocalDatastoreService.Profile> snapshots = new WeakHashMap();
/*      */ 
    private EntityGroup(OnestoreEntity.Path paramPath) {
/* 1159 */         this.path = paramPath;
    }
/*      */ 
    public long getVersion() {
/* 1163 */         return this.version.get();
    }
/*      */ 
    public void incrementVersion()
    {
/* 1173 */         long oldVersion = this.version.getAndIncrement();
/* 1174 */         LocalDatastoreService.Profile snapshot = null;
/* 1175 */         for (LocalDatastoreService.LiveTxn txn : this.snapshots.keySet())
/* 1176 */           if (txn.getEntityGroupVersion().longValue() == oldVersion) {
/* 1177 */             if (snapshot == null) {
/* 1178 */               snapshot = takeSnapshot();
          }
/* 1180 */             this.snapshots.put(txn, snapshot);
        }
    }
/*      */ 
    public OnestoreEntity.EntityProto get(LocalDatastoreService.LiveTxn liveTxn, OnestoreEntity.Reference key)
    {
/* 1192 */         LocalDatastoreService.Profile profile = getSnapshot(liveTxn);
/* 1193 */         Map extents = profile.getExtents();
/* 1194 */         OnestoreEntity.Path.Element lastPath = (OnestoreEntity.Path.Element)LocalDatastoreService.getLast(key.getPath().elements());
/* 1195 */         LocalDatastoreService.Extent extent = (LocalDatastoreService.Extent)extents.get(lastPath.getType());
/* 1196 */         if (extent != null) {
/* 1197 */           Map entities = extent.getEntities();
/* 1198 */           return ((OnestoreEntity.EntityProto)entities.get(key));
      }
/* 1200 */         return null;
    }
/*      */ 
    public void addTransaction(LocalDatastoreService.LiveTxn txn) {
/* 1204 */         txn.setEntityGroup(this);
/* 1205 */         if (!(this.snapshots.containsKey(txn)))
/* 1206 */           this.snapshots.put(txn, null);
    }
/*      */ 
    public void removeTransaction(LocalDatastoreService.LiveTxn txn)
    {
/* 1211 */         synchronized (LocalDatastoreService.Profile.this) {
/* 1212 */           this.snapshots.remove(txn);
      }
    }
/*      */ 			
				/**
				 * need to implement this in database end
				 */
    private LocalDatastoreService.Profile getSnapshot(LocalDatastoreService.LiveTxn txn) {
/* 1217 */         if (txn == null) {
/* 1218 */           return LocalDatastoreService.Profile.this;
      }
/* 1220 */         LocalDatastoreService.Profile snapshot =
