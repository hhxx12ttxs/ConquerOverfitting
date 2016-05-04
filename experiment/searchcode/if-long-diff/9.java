/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
package ru.adv.test.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.util.StringUtils;

import ru.adv.db.adapter.DBAdapter;
import ru.adv.db.base.DBValue;
import ru.adv.db.base.FileValue;
import ru.adv.db.base.Id;
import ru.adv.db.base.MCast;
import ru.adv.db.config.ConfigIndex;
import ru.adv.db.config.ConfigObject;
import ru.adv.db.config.ConfigParser;
import ru.adv.event.Interactive;
import ru.adv.event.QuestionEvent;
import ru.adv.event.QuestionListener;
import ru.adv.event.Response;
import ru.adv.repository.ConflictLogger;
import ru.adv.repository.FileComparator;
import ru.adv.repository.ReplicaConfig;
import ru.adv.repository.ReplicaSynchronizer;
import ru.adv.repository.ReplicationConfig;
import ru.adv.repository.SyncHandler;
import ru.adv.repository.UniqueAttrChecker;
import ru.adv.repository.channel.client.ChannelClient;
import ru.adv.repository.channel.io.ChannelException;
import ru.adv.repository.channel.io.UserInfo;
import ru.adv.repository.dump.DumpFile;
import ru.adv.repository.dump.DumpHeader;
import ru.adv.repository.dump.DumpInfo;
import ru.adv.repository.dump.DumpInfoImpl;
import ru.adv.repository.dump.DumpObjectHeader;
import ru.adv.repository.dump.DumpReader;
import ru.adv.repository.dump.DumpRow;
import ru.adv.repository.dump.ObjectInfo;
import ru.adv.repository.dump.ObjectInfoImpl;
import ru.adv.repository.dump.TypeAttr;
import ru.adv.test.repository.dump.test.DumpInfoTestImpl;
import ru.adv.test.repository.dump.test.ObjectInfoTestImpl;
import ru.adv.test.repository.test.DBInfo;
import ru.adv.test.repository.test.TestOldRepositoryContext;
import ru.adv.test.repository.test.TestRepository;
import ru.adv.util.ProgressCallback;

/**
 * -*- java -*-
 * User: vic
 * Date: 26.09.2003
 * Time: 18:46:58
 * $Id: SynchronizerTester.java 1258 2009-08-07 12:02:50Z vic $
 */
public abstract class SynchronizerTester extends AbstractOldRepositoryTester {
	
    protected static final String ROOT = "tmp/repository-synch";
	private static final String CONFLICT_FILE_LOG = "tmp"+File.separator+"conflict.log";
    public static final String DB_NAME_PREFIX = "testsynch_";
    public static final int CHANNEL_PORT = 12345;

    public String getRoot() {
        return ROOT;
    }

    @Test
    public void testNearistStartSequenceValue() {
        log("/============ testNearistStartSequenceValue() ==================/");
        assertEquals(40, ReplicaSynchronizer.getNearestStartSequenceValue(35, 5, 0));
        assertEquals(41, ReplicaSynchronizer.getNearestStartSequenceValue(35, 5, 1));
        assertEquals(37, ReplicaSynchronizer.getNearestStartSequenceValue(32, 5, 2));
        assertEquals(10, ReplicaSynchronizer.getNearestStartSequenceValue(0, 10, 0));
        assertEquals(10, ReplicaSynchronizer.getNearestStartSequenceValue(9, 10, 0));
        assertEquals(20, ReplicaSynchronizer.getNearestStartSequenceValue(10, 10, 0));
        assertEquals(23, ReplicaSynchronizer.getNearestStartSequenceValue(10, 10, 3));
        assertEquals(32, ReplicaSynchronizer.getNearestStartSequenceValue(24, 10, 2));
    }

    @Test
    public void testChannelUploadDownloadFile() throws Exception {
        log("/============ testChannelUploadDownloadFile() ==================/");

        ChannelClient client = new ChannelClient();
        client.open("localhost", CHANNEL_PORT, new UserInfo(getEnvironment().getDb01().getDbUser().getName(),
                getEnvironment().getDb01().getDbUser().getPassword()));

        try {
            String tmpFileName = repository.getTempDir().getAbsolutePath() + File.separatorChar + "upload-test-filo.txt";

            // ???????? ???????? ????
            FileOutputStream fi = new FileOutputStream(tmpFileName);
            fi.write("super puper tempo file!".getBytes());
            fi.flush();
            fi.close();

            Thermometer thermometer = new Thermometer();

            String fileId = client.uploadFile(new File(tmpFileName), repository.getTempDir(), thermometer);

            log("fileId = " + fileId);

            try {
                client.downloadFile("this-fileId-not-exists", repository.getTempDir(), thermometer); // -> exception now such fileId
                fail("wait for exception: no such fileId!");
            } catch (ChannelException e) {
                log("downloadFile 1 - good exception: " + e.getMessage());
            }

            try {
                client.downloadFile(fileId, new File("this dir not exist"), thermometer); // -> bad tmp dir
                fail("wait exception on bad temporary directory");
            } catch (FileNotFoundException e) {
                log("downloadFile 2 - good exception: " + e.getMessage());
            }

            DumpFile dFile = client.downloadFile(fileId, repository.getTempDir(), thermometer); // -> must be Ok

            log("downloadFile 3 - Ok");

            // compare file
            assertEquals(dFile, new DumpFile(tmpFileName, true));

        } finally {
            client.close();
        }

    }

    /**
     * @throws Exception
     */
    @Test
    public void testCreateLocalReplica() throws Exception {
        log("/============ testCreateLocalReplica() ==================/");

        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        prepareAndFillDatabase(master);

        DBAdapter masterAdapter = repository.getDBConfig(master.getName()).getDBAdapter();

        // ???????? ??????? ?? ????????? ???????????
        ReplicationConfig replications = repository.getReplicationConfig();
        ReplicaConfig repConfig = replications.getReplicaConfig(master.getName(), "localhost", CHANNEL_PORT, replica.getName());
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());
        assertEquals("Count of indexes in databases",
                repository.countOfIndexes(master),
                repository.countOfIndexes(replica));
        assertEquals("Count of triggers in databases",
                repository.countOfTriggers(master),
                repository.countOfTriggers(replica));

        // ?????????? ???????????
        repository.shutdown();
        TestOldRepositoryContext.IS_DELETE_OLD_ROOT = false;
        repository = new TestRepository(ROOT, getEnvironment());

        // ???????? ??????? ???????? ?? ? ????????? ???????
        Set expectedNames = new HashSet(Arrays.asList(new Object[]{master.getName(), replica.getName()}));
        assertEquals(expectedNames, new HashSet(repository.getDBNames()));

        // ???????? ??? ??????? ?? ???????? base.xml & base_prepared.xml
        File conf;
        conf = new File(repository.getContext().getConfigSrcFilePath(replica.getName()));
        assertTrue("Config is exists " + conf.getPath(), !conf.exists()); // base.xml
        conf = new File(repository.getContext().getConfigPreparedFilePath(replica.getName()));
        assertTrue("Config is exists " + conf.getPath(), !conf.exists()); // base_prepared.xml

        // ??????? ????? ? ???????? ??????? ??
        final String masterDumpName = "m";
        final String replicaDumpName = "r";
        repository.dump(master.getName(), masterDumpName, true, null);
        repository.dump(replica.getName(), replicaDumpName, true, null);

        // ???????? ? ?????????? sequence ? master ? replica
        // ??????? ??? ?????, ????????? ?????? ????????
        DumpReader mDump = new DumpReader();
        DumpReader rDump = new DumpReader();
        mDump.setTmpDir(repository.getTempDir());
        rDump.setTmpDir(repository.getTempDir());
        mDump.open(new FileInputStream(repository.getDumpFilePath(master.getName(), masterDumpName)));
        rDump.open(new FileInputStream(repository.getDumpFilePath(replica.getName(), replicaDumpName)));
        mDump.getDumpHeader();
        rDump.getDumpHeader();
        while (mDump.nextIsObjectHeader()) {
            DumpObjectHeader objectHeader1 = mDump.readObjectHeader();
            if (!rDump.nextIsObjectHeader()) {
                fail("Not equals, expected DumpObjectHeader for " + objectHeader1.getObjectName());
            }
            DumpObjectHeader objectHeader2 = rDump.readObjectHeader();
            if (!objectHeader1.equals(objectHeader2)) {
                fail("Not equals, DumpObjectHeader is different " + objectHeader1 + " <> " + objectHeader2);
            }
            while (mDump.nextIsRow()) {
                DumpRow row1 = mDump.readRow();
                if (!rDump.nextIsRow()) {
                    fail("Not equals, expected DumpRow for " + objectHeader1.getObjectName());
                }
                DumpRow row2 = rDump.readRow();
                if (objectHeader1.isSequence() && objectHeader2.isSequence() && objectHeader1.getObjectName().equals(masterAdapter.getCommonSequenceName())) {
                    // ????????? mz_sequence
                    long masterStart = Long.parseLong(((DBValue) row1.getValues().get(0)).get().toString());
                    long masterInc = Integer.parseInt(((DBValue) row1.getValues().get(1)).get().toString());
                    long replStart = Long.parseLong(((DBValue) row2.getValues().get(0)).get().toString());
                    long replInc = Integer.parseInt(((DBValue) row2.getValues().get(1)).get().toString());
                    if (masterInc != ReplicationConfig.INCREMENT_BY) {
                        fail("master has wrong mz_sequence increment = " + masterInc);
                    }
                    if (replInc != ReplicationConfig.INCREMENT_BY) {
                        fail("new replica has wrong mz_sequence increment = " + replInc);
                    }
                    logger.debug("masterStart=" + masterStart);
                    logger.debug("replStart=" + replStart);
                    logger.debug("masterStart % ReplicationConfig.INCREMENT_BY=" + (masterStart % ReplicationConfig.INCREMENT_BY));
                    logger.debug("ReplicationConfig.MASTER_SHIFT=" + ReplicationConfig.MASTER_SHIFT);
                    logger.debug("replStart % ReplicationConfig.INCREMENT_BY=" + (replStart % ReplicationConfig.INCREMENT_BY));
                    logger.debug("repConfig.getSequenceShift()=" + repConfig.getSequenceShift());
                    if (!(
                            masterStart < replStart &&
                            masterStart % ReplicationConfig.INCREMENT_BY == ReplicationConfig.MASTER_SHIFT &&
                            replStart % ReplicationConfig.INCREMENT_BY == repConfig.getSequenceShift()
                            )
                    ) {
                        fail("master and slave has wrong mz_sequence's");
                    }

                } else if ((!objectHeader1.isSequence()) && (!row1.equals(row2))) {
                    logger.error("objectHeader: " + objectHeader1);
                    fail("Not equals, DumpRow is different  " + row1 + " <> " + row2);
                }
            }
        }
        mDump.close();
        rDump.close();
    }

    /**
     * ???????? ???????????? ???????? ???-????? ??? ??
     * @throws Exception
     */
    @Test
    public void testCreateHashDump() throws Exception {
        log("/============ testCreateHashDump() ==================/");
        DBInfo db = getEnvironment().getDb01();
        // ??????? ???? ???????? ?? ? ?????????, ???? ???? ????????
        prepareAndFillDatabase(db);
        // ???????? hash-dump ?? ? ???????? ???? ?? ????????
        DumpFile hashFile = repository.createHashFile(db.getName(), db.getUserInfo(), null);
        checkHashFileStatistic(hashFile);
        checkHashFileStructure(hashFile,false);
        
        // test created md5 files sums
// FIXME    DumpInfoTestImpl infoNoModifyHash = new DumpInfoTestImpl(noModifyHash.getFullName(), repository.getTempDir());
    }
    
    /**
     * ???????? ???????????? ???????? ???-????? ??? ??
     * with unique attributes
     * @throws Exception
     */
    @Test
    public void testCreateHashDumpWithUniqueAttrs() throws Exception {
        log("/============ testCreateHashDumpWithUniqueAttrs() ==================/");
        DBInfo db = getEnvironment().getDb01();
        // ??????? ???? ???????? ?? ? ?????????, ???? ???? ????????
        prepareAndFillDatabase(db);
        // ???????? hash-dump ?? ? ???????? ???? ?? ????????
        DumpFile hashFile = repository.createHashWithOwnUniqueAtributes(db.getName(), db.getUserInfo(),null);
        checkHashFileStatistic(hashFile);
        checkHashFileStructure(hashFile,true);
    }
    
    /**
     * ???????? ???????????? ???????? ???-????? ??? ??
     * with unique attributes
     * @throws Exception
     */
    @Test
    public void testCreateHashDumpNoMatch() throws Exception {
        log("/============ testCreateHashDumpNoMatch ==================/");
        DBInfo db = getEnvironment().getDb01();
        // ??????? ???? ???????? ?? ? ?????????, ???? ???? ????????
        prepareAndFillDatabase(db);
        // ???????? hash-dump ?? ? ???????? ???? ?? ????????
        String matchSuffix = "bla:bla";
        DumpFile hashFile = repository.createHashFile(db.getName(), db.getUserInfo(),true, (String)null, matchSuffix, null);
        checkHashFileMatch(hashFile,"bla:bla",false);
    }
    
    @Test
    public void testCreateHashDumpMatch() throws Exception {
        log("/============ testCreateHashDumpNoMatch ==================/");
        DBInfo db = getEnvironment().getDb01();
        // ??????? ???? ???????? ?? ? ?????????, ???? ???? ????????
        prepareAndFillDatabase(db);
        // ???????? hash-dump ?? ? ???????? ???? ?? ????????
        String matchSuffix = ":"+repository.getId()+":"+db.getName();
        DumpFile hashFile = repository.createHashFile(db.getName(), db.getUserInfo(),true, matchSuffix, (String)null, null);
        checkHashFileStatistic(hashFile); // must contain all records
        checkHashFileStructure(hashFile,true);
        checkHashFileMatch(hashFile,matchSuffix,true);
    }

    private void checkHashFileStatistic(DumpFile hashFile) throws Exception {
        DumpInfo info = new DumpInfoTestImpl(hashFile.getFullName(), repository.getTempDir());
        log("hashFile contains: " + info);
        // ?????????? ?? ?????????? ????????
        String msg = "hash contains bad information about object ";
        // Note: HashFile contains only id,objversion attributes
        //assertTrue(msg + "'a'", info.getObjects().contains(new ObjectInfoTestImpl("a", 5, 2, 0)));
        assertTrue(msg + "'a'", info.getObjects().contains(new ObjectInfoTestImpl("a", 5, 0, 0)));
        assertTrue(msg + "'c'", info.getObjects().contains(new ObjectInfoTestImpl("c", 5, 0, 0)));
        assertTrue(msg + "'b'", info.getObjects().contains(new ObjectInfoTestImpl("b", 7, 0, 0)));
        assertTrue(msg + "'j'", info.getObjects().contains(new ObjectInfoTestImpl("j", 14, 0, 0)));
    }

    private void checkHashFileStructure(DumpFile hashFile, boolean hasUniqueAttributes) throws Exception {
        log("-------- checkHashFileStructure()");

        DumpReader hashDump = new DumpReader();
        hashDump.setTmpDir(repository.getTempDir());
        hashDump.open(new FileInputStream(hashFile.getFullName()));
        DumpHeader header = hashDump.getDumpHeader();

        assertTrue("this is hash-dump header", header.isHashDump());

        while (hashDump.nextIsObjectHeader()) {
            DumpObjectHeader objHeader = hashDump.readObjectHeader();
          
            // ???????? ?????????? id ?? ???????????,
            Integer prevId = new Integer(-100);
            // ?????? tables
            assertTrue("there is sequence " + objHeader.getObjectName() + " object in hash dump", !objHeader.isSequence());
            
        	ConfigObject cObject = header.getDBConfig().getConfigObject(objHeader.getObjectName());

        	if (hasUniqueAttributes) {
        		log(objHeader.getObjectName()+": dump headers contains: "+getColumnNames(objHeader));
        		log(objHeader.getObjectName()+": config contains: "+getColumnNamesInUniqueGroups(cObject));
        		assertTrue("Dump must contains all attributes that exists in unique groups",
        				getColumnNames(objHeader).containsAll(getColumnNamesInUniqueGroups(cObject))
        		);
        	}
            
            
            while (hashDump.nextIsRow()) {
                DumpRow row = hashDump.readRow();
                List attrs = objHeader.getTypeAttrs();
                for (int i = 0; i < attrs.size(); i++) {
                    TypeAttr tA = (TypeAttr) attrs.get(i);
                    if (tA.isFile()) {
                        if (row.getDbValue(i).get() instanceof Boolean) {
                            continue;
                        } else if (row.getDbValue(i).get() instanceof DumpFile) {
                            assertTrue("must be checksumm for attr-file", !((DumpFile) row.getDbValue(i).get()).isRealFileValue());
                        } else {
                            fail("bad value of file attribute=" + row.getDbValue(i).get());
                        }
                    } else if (tA.getAttrName().equals("id") || tA.getAttrName().equals(ConfigParser.VERSION_ATTR_ID)) {
                        // Ok
                        if (tA.getAttrName().equals("id")) {
                            // ???????? ??????????? id
                            Integer currId = MCast.toInteger(row.getDbValue(i).get());
                            if (currId.compareTo(prevId) <= 0) {
                                throw new Exception("rows not sorted by id in object " + objHeader.getObjectName());
                            }
                            prevId = currId;
                        }
                    } else if (hasUniqueAttributes) {
                    	if (!getColumnNamesInUniqueGroups(cObject).contains(tA.getAttrName())){
                            fail("depricated attribute in hash-dump: not in unique group: " + tA.getAttrName());
                    	}
                    } else {
                        fail("depricated attribute in hash-dump:" + tA.getAttrName());
                    }

                }
            }
        }
        hashDump.close();
    }

    private void checkHashFileMatch(DumpFile hashFile, String match, boolean isMatch) throws Exception {
        log("-------- checkHashFileStructure()");

        DumpReader hashDump = new DumpReader();
        hashDump.setTmpDir(repository.getTempDir());
        hashDump.open(new FileInputStream(hashFile.getFullName()));
        DumpHeader header = hashDump.getDumpHeader();

        assertTrue("this is hash-dump header", header.isHashDump());

        while (hashDump.nextIsObjectHeader()) {
        	DumpObjectHeader objHeader = hashDump.readObjectHeader();
        	while (hashDump.nextIsRow()) {
        		DumpRow row = hashDump.readRow();
        		int idx = objHeader.indexOfTypeAttr(ConfigParser.VERSION_ATTR_ID);
        		if (idx<0) {
        			fail("not found objversion attribute");
        		}
        		//TypeAttr tA = (TypeAttr) attrs.get(idx);
        		String objVersion = row.getDbValue(idx).get().toString();
        		log("OBJVERSION="+objVersion);
        		if ( isMatch && !objVersion.endsWith(match) ){
        			fail("Hash dump contains objversion='"+objVersion+"'" +
        					" for match="+match+" and isMatch="+isMatch);
        		}
        		if ( (!isMatch) && objVersion.endsWith(match) ){
        			fail("Hash dump contains objversion='"+objVersion+"'" +
        					" for match="+match+" and isMatch="+isMatch);
        		}
        	}
        }
        hashDump.close();
    }
    
    
    private Set getColumnNames(DumpObjectHeader objectHeader) {
    	Set names = new HashSet();
    	for ( Iterator i = objectHeader.getTypeAttrs().iterator(); i.hasNext();){
    		names.add( ((TypeAttr)i.next()).getAttrName());
    	}
    	return names;
    }
    
    /**
     * calculate attribute names that exists in unique groups of ConfigObject
     * @param cObject
     * @return
     */
    private Set getColumnNamesInUniqueGroups(ConfigObject cObject) {
    	Set columnNames = new HashSet();
    	for (Iterator ii = cObject.getIndexes().iterator(); ii.hasNext(); ){
    		ConfigIndex cI = (ConfigIndex)ii.next();
    		if (cI.isUnique()) {
    			columnNames.addAll(cI.getColumns());
    		}
    	}
    	return columnNames;
    }

    private void prepareAndFillDatabase(DBInfo db) throws Exception {
        final int maxCountAnswers = 1000;
        logger.debug("start prepare & fill " + db.getName());
        repository.prepare(db.getName(), db.getUserInfo(), true);
        SyncHandler sh = repository.getSyncHandler(db.getName(), db.getUserInfo(), true);
        sh.createTempDump(false);
        int answers = 0;
        while (sh.hasEvent() == true) {
            QuestionEvent qe = sh.getEvent();
            logger.debug("SYNC Q:" + qe.getMessage());
            sh.setResponse(new Response(true));
            logger.debug("SYNC A: true");
            answers++;
            if (answers >= maxCountAnswers) {
                throw new Exception("Error in prepare databse " + db.getName() + ": max count loops");
            }
        }
        repository.fillDatabase(db);
    }


    /**
     * ???? ?????????? ????? ?? ???-?????
     */
    @Test @NotTransactional
    public void testLocalSynchronizeByDump() throws Exception {
        try {

            // ?????????? ????? ?????????????????? ?????? (excludes)
            logger.error("?????????? ????? ?????????????????? ?????? (excludes)");
            Set<String> excludes = new HashSet<String>(Arrays.asList(new String[]{"c", "j"}));

            // DB_NAME_01 - ????? ???????? ; DB_NAME_02 - ????????? ???????
            logger.error("DB_NAME_01 - ????? ???????? ; DB_NAME_02 - ????????? ???????");
            DBInfo master = getEnvironment().getDb01();
            DBInfo replica = getEnvironment().getDb02();

            // ??????? ??? ?????????? ??
            logger.error("??????? ??? ?????????? ??");
            prepareAndFillDatabase(master);
            prepareAndFillDatabase(replica);

            SyncChecker checker = new SyncChecker(master, replica);
            checker.modifyMasterDB();
            checker.collectInfoBeforeSyncReplica();

            // ??????? ???-???? ?????????????????? ???????
            logger.error("??????? ???-???? ?????????????????? ??");
            DumpFile replicaNoModifiedHash = repository.createHashFile(replica.getName(), replica.getUserInfo(), null);
            // note: hash dump don't contains files (only id & objversion)
            DumpInfoTestImpl replicaNoModifiedHashInfo = new DumpInfoTestImpl(replicaNoModifiedHash.getFullName(), repository.getTempDir());

            // ??????? ???? ?? ???-????? ?? ???????????????? ??
            logger.error("??????? ???? ?? ???-????? ?? ???????????????? ??");
            FileComparator fc = new SimpleFileComparator(replica.getName());
            DumpFile generalDumpByHash = repository.createGeneralDumpByHash(master.getName(), replicaNoModifiedHash, fc, excludes, new HashSet<String>(), master.getUserInfo(), true, null);
            // ????????? ??????? ??????????? ????? ? ??????? DumpInfoTestImpl
            logger.error("????????? ??????? ??????????? ????? ? ??????? DumpInfoTestImpl");
            DumpInfoTestImpl generalDumpByHashInfo = new DumpInfoTestImpl(generalDumpByHash.getFullName(), repository.getTempDir());
            
            
            // ??? ?????? logupdate, log
            logger.error("??? ?????? logupdate, log");
            hashDumpInfoNotContainsSystemTables(replicaNoModifiedHashInfo, master.getAdapter());
            hashDumpInfoNotContainsSystemTables(generalDumpByHashInfo, master.getAdapter());
            //?????? "c,j" - ?? ?????????? ? infoDumpByHash - it's in exclude
            logger.error("?????? \"c,j\" - ?? ?????????? ? infoDumpByHash - it's in exclude");
            hashDumpInfoNotContainsTables(generalDumpByHashInfo, excludes);

            // ?????? "?" ?????? ?????????? ??? ????? ? ???? MD5
            logger.error("?????? \"?\" ?????? ?????????? ??? ????? ? ???? MD5");
            //Note: Hash file contains only id, objversion attributes !
            assertEquals(new ObjectInfoTestImpl("a", 5, 0, 0), replicaNoModifiedHashInfo.getObjectInfo("a"));
            assertEquals(new ObjectInfoTestImpl("a", 6, 1, 2), generalDumpByHashInfo.getObjectInfo("a"));
            // ?????? "e" - ???? ??????
            logger.error("?????? \"e\" - ???? ??????");
            assertEquals(replicaNoModifiedHashInfo.getObjectInfo("e").getRowCount(),
                    generalDumpByHashInfo.getObjectInfo("e").getRowCount() + 1);

            // ?????? ????????? ????????? ? ?? ??????? ? DB_NAME_02
            logger.error("?????? ????????? ????????? ? ?? ??????? ? DB_NAME_02");
            logger.error("infoDumpByHash = " + generalDumpByHashInfo);
            final String dumpName = "dumpbyhash";
            repository.setDump(replica.getName(), dumpName, generalDumpByHash);
            repository.restoreDump(replica.getName(), dumpName, true, true, null);

            checker.checkReplicaSyncResult(excludes, master.getAdapter());
        } catch (Exception e) {
            logger.debug(e.getMessage());
            throw e;
        }
    }

    private void hashDumpInfoNotContainsSystemTables(DumpInfo infoDumpByHash, DBAdapter adapter) {
        hashDumpInfoNotContainsTables(infoDumpByHash, adapter.getSystemObjects());
    }

    private void hashDumpInfoNotContainsTables(DumpInfo infoDump, Set exludes) {
        for (Iterator iterator = infoDump.getObjects().iterator(); iterator.hasNext();) {
            ru.adv.repository.dump.ObjectInfo o = (ru.adv.repository.dump.ObjectInfo) iterator.next();
            assertTrue("DumpInfoTestImpl contains system object: " + o.getObjectName(), !exludes.contains(o.getObjectName()));
        }
    }


    /**
     * @throws Exception
     */
    @Test
    public void testSynchronizeByDump() throws Exception {
        log("/============ testSynchronizeByDump() ==================/");
        startSynchronize(true);
        log("/============ testSynchronizeByDump(): finished ==================/");
    }

    /**
     * @throws Exception
     */
    public void startSynchronize(boolean isDiff) throws Exception {

        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        // =======================================
        // ??????? master
        // =======================================
        prepareAndFillDatabase(master);

        // =======================================
        // ???????? ??????? ?? ????????? ???????????
        // =======================================
        ReplicationConfig replications = repository.getReplicationConfig();
        ReplicaConfig repConfig = replications.getReplicaConfig(master.getName(), "localhost", CHANNEL_PORT, replica.getName());
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());

        // =======================================
        // ???????? ???????
        // =======================================
        SyncChecker checker = new SyncChecker(master, replica);
        checker.modifyMasterDB();
        checker.collectInfoBeforeSyncReplica();

        // =======================================
        // ????????????????
        // =======================================
        synchronizer = repository.createReplicaSynchronizer(repConfig);
        Interactive inter = new Interactive();
        inter.setQuestionListener(new QuestionLogger());
        if (!isDiff) {
            synchronizer.synchronizeFullReplica(master.getUserInfo(), replica.getUserInfo(), inter, new TreeSet(), new TreeSet());
        } else {
            synchronizer.synchronizeDiffReplica(master.getUserInfo(), replica.getUserInfo(), inter, new TreeSet(), new TreeSet());
        }

        // =======================================
        // ????????
        // =======================================
        checker.checkReplicaSyncResult(repConfig.getExcludeObjectNames(repository.getDBConfig(master.getName()),null), master.getAdapter());

    }

    @Test
    public void testFullSyncWithCriticalObjects() throws Exception {
        log("/============ testFullSyncWithCriticalObjects() ==================/");
        startSynchronizeWithCriticalObjects(false);
        log("/============ testFullSyncWithCriticalObjects(): finished ==================/");
    }
    
    @Test
    public void testDiffSyncWithCriticalObjects() throws Exception {
        log("/============ testDiffSyncWithCriticalObjects() ==================/");
        startSynchronizeWithCriticalObjects(true);
        log("/============ testDiffSyncWithCriticalObjects(): finished ==================/");
    }

    /**
     * @throws Exception
     */
    public void startSynchronizeWithCriticalObjects(boolean isDiff) throws Exception {

        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        // =======================================
        // ??????? master
        // =======================================
        prepareAndFillDatabase(master);

        // =======================================
        // ???????? ??????? ?? ????????? ???????????
        // =======================================
        ReplicationConfig replications = repository.getReplicationConfig();
        ReplicaConfig repConfig = replications.getReplicaConfig(master.getName(), "localhost", CHANNEL_PORT, replica.getName());
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());

        // =======================================
        // ??????? ??? ?????? ?? ??????? a
        // ??? ???????? ? ???????? ?????? ?? ???????? b,f,j 
        // =======================================
        repository.deleteAllFromTable(replica.getName(), "a");
        
        assertEquals(0,repository.countOfRows(replica.getName(), "a", replica.getDbUser()));
        assertEquals(0,repository.countOfRows(replica.getName(), "b", replica.getDbUser()));
        assertEquals(0,repository.countOfRows(replica.getName(), "f", replica.getDbUser()));
        assertEquals(0,repository.countOfRows(replica.getName(), "j", replica.getDbUser()));
        
        long eCount = repository.countOfRows(replica.getName(), "e", replica.getDbUser());
        long dCount = repository.countOfRows(replica.getName(), "d", replica.getDbUser());
        long iCount = repository.countOfRows(replica.getName(), "i", replica.getDbUser());

        // =======================================
        // a - exclude
        // ??? ???????? ? ???????? ?????? ?? ???????? b,f,j
        // ? ???????? ??????????
        // =======================================
        Set<String> excludeObjects = new HashSet<String>(Arrays.asList(new String[]{"a"}));
        
        // =======================================
        // ????????????????
        // =======================================
        synchronizer = repository.createReplicaSynchronizer(repConfig);
        Interactive inter = new Interactive();
        inter.setQuestionListener(new QuestionLogger());
        
        if (!isDiff) {
            synchronizer.synchronizeFullReplica(master.getUserInfo(), replica.getUserInfo(), inter, new TreeSet<String>(), excludeObjects);
        } else {
            synchronizer.synchronizeDiffReplica(master.getUserInfo(), replica.getUserInfo(), inter, new TreeSet<String>(), excludeObjects);
        }
     
        // =======================================
        // ????????, ??????? a,b,f,j ?????? ???? ???????
        // =======================================
        assertEquals(0,repository.countOfRows(replica.getName(), "a", replica.getDbUser()));
        if (isDiff) {
        	// DIFF replication mode makes integrity check that delete broken links
        	assertEquals(0,repository.countOfRows(replica.getName(), "b", replica.getDbUser()));
        	assertEquals(0,repository.countOfRows(replica.getName(), "f", replica.getDbUser()));
        	assertEquals(0,repository.countOfRows(replica.getName(), "j", replica.getDbUser()));
        }
        
        assertEquals(eCount , repository.countOfRows(replica.getName(), "e", replica.getDbUser()));
        assertEquals(dCount , repository.countOfRows(replica.getName(), "d", replica.getDbUser()));
        assertEquals(iCount , repository.countOfRows(replica.getName(), "i", replica.getDbUser()));

    }
    
    class QuestionLogger implements QuestionListener {
    	List messages = new LinkedList();
        public void conversationClosed() {
        }

        public void conversationClosed(Exception e) {
        }

        public Response questionPerformed(QuestionEvent e) {
        	messages.add(e.toString());
            log(e.toString());
            return new Response("true");
        }
        
        public boolean containMessage(String mess) {
        	for (Iterator i=messages.iterator(); i.hasNext();) {
        		String s = i.next().toString();
        		if(s.indexOf(mess)!=-1){
        			return true;
        		}
        	}
        	return false;
        }
        
        public String toString() {
            String s="";
            for (Iterator i=messages.iterator(); i.hasNext();) {
                s+=i.next().toString()+"\n";
            }
            return s;
        }
        
    }

    /**
     * ????????? ???????????? ??????? diff dump ??? ?????????? ???????
     *
     * @throws Exception
     */
    @Test
    public void testCreateDiffDump() throws Exception {
        log("/============ testCreateDiffDump() ==================/");
        // ?????????? ????? ?????????????????? ?????? (excludes)
        logger.error("Define exclude tables");
        Set<String> excludes = new HashSet<String>(Arrays.asList(new String[]{"c", "j"}));

        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        // create master
        prepareAndFillDatabase(master);

        // create replica
        ReplicationConfig replications = repository.getReplicationConfig();
        ReplicaConfig repConfig = replications.getReplicaConfig(master.getName(), "localhost", CHANNEL_PORT, replica.getName());
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());

        // Here master is equals to replica

        // modify master
        repository.modifyDB2(master);
        repository.updateSqlAttrValueTo(replica.getName(), "a", "title", "'updated'", "15");

        // create hash dup on remote replica
        DumpFile hashReplicaFile = repository.createHashFile(replica.getName(), replica.getUserInfo(), null);

        // Create diff-dump
        FileComparator fc = new SimpleFileComparator(replica.getName());
        DumpFile diffDumpByHash = repository.createDiffDumpByHash(master.getName(), hashReplicaFile, fc, excludes, new HashSet(), master.getUserInfo(), true, null);

        // Check content of the diff-dump
        DumpInfoTestImpl infoDiffDumpByHash = new DumpInfoTestImpl(diffDumpByHash.getFullName(), repository.getTempDir());

        logger.error("There are no tables: logupdate, log");
        hashDumpInfoNotContainsSystemTables(infoDiffDumpByHash, master.getAdapter());
        logger.error("There are no exlcude tables");
        hashDumpInfoNotContainsTables(infoDiffDumpByHash, excludes);

        logger.error("Diff file contains only few rows");
        // ?????? "?" ?????? ????????? ??? ????? ? ???? MD5
        assertEquals(new ObjectInfoTestImpl("a", 3, 1, 2), infoDiffDumpByHash.getObjectInfo("a"));
        assertEquals(new ObjectInfoImpl("a", 1, 2, 0, 1, 1), diffDumpByHash.getDumpInfo().getObjectInfo("a"));
        // ?????? "e" - ???? ??????
        logger.info(infoDiffDumpByHash.getObjectInfo("e"));
        assertEquals(1, infoDiffDumpByHash.getObjectInfo("e").getRowCount());
        assertEquals(new ObjectInfoImpl("e", 0, 0, 1, 0, 0), diffDumpByHash.getDumpInfo().getObjectInfo("e"));
        // ?????? "c" - exclude
        try {
            infoDiffDumpByHash.getObjectInfo("c");
            fail("Object 'c' must be not present in dump");
        } catch (Exception e) {
            //OK
        }
        try {
            diffDumpByHash.getDumpInfo().getObjectInfo("c");
            fail("Object 'c' must be not present in generated dump");
        } catch (Exception e) {
            //OK
        }
        try {
            infoDiffDumpByHash.getObjectInfo("j");
            fail("Object 'j' must be not present in dump");
        } catch (Exception e) {
            //OK
        }
        try {
            diffDumpByHash.getDumpInfo().getObjectInfo("j");
            fail("Object 'j' must be not present in generated dump");
        } catch (Exception e) {
            //OK
        }
        assertEquals(0, infoDiffDumpByHash.getObjectInfo("b").getRowCount());
        assertEquals(true, diffDumpByHash.getDumpInfo().getObjectInfo("b").isEmpty());

        assertEquals(0, infoDiffDumpByHash.getObjectInfo("d").getRowCount());
        assertEquals(true, diffDumpByHash.getDumpInfo().getObjectInfo("d").isEmpty());

        // ????????? ?????????? ???????????? extended-tree
        try {
            infoDiffDumpByHash.getObjectInfo("i_tree");
            fail("Object 'i_tree' must be not present in dump");
        } catch (Exception e) {
            //OK
        }

    }

    /**
     * ????????? ???????????? ????????? diff dump ?? ???????
     *
     * @throws Exception
     */
    @Test
    public void testLocalSinchronizeByDiffDump() throws Exception {
        log("/============ testLocalSinchronizeByDiffDump() ==================/");
        try {

            // ?????????? ????? ?????????????????? ?????? (excludes)
            logger.error("?????????? ????? ?????????????????? ?????? (excludes)");
            Set excludes = new HashSet(Arrays.asList(new String[]{"c", "j"}));

            // DB_NAME_01 - ????? ???????? ; DB_NAME_02 - ????????? ???????
            logger.error("DB_NAME_01 - ????? ???????? ; DB_NAME_02 - ????????? ???????");
            DBInfo master = getEnvironment().getDb01();
            DBInfo replica = getEnvironment().getDb02();

            // ??????? ??? ?????????? ??
            logger.error("??????? ??? ?????????? ??");
            prepareAndFillDatabase(master);
            prepareAndFillDatabase(replica);

            log(" Modify master ");
            SyncChecker checker = new SyncChecker(master, replica);
            checker.modifyMasterDB();
            checker.collectInfoBeforeSyncReplica();

            log(" Create hash-dump of replica ");
            // ??????? ???-???? ?????????????????? ??
            logger.error("??????? ???-???? ?????????????????? ??");
            DumpFile noModifyHash = repository.createHashFile(replica.getName(), replica.getUserInfo(), null);

            // ??????? ???? ?? ???-????? ?? ???????????????? ??
            log(" Create diff-file by hash-dump of replica");
            FileComparator fc = new SimpleFileComparator(replica.getName());
            DumpFile dumpDiffByHash = repository.createDiffDumpByHash(master.getName(), noModifyHash, fc, excludes, new HashSet(), master.getUserInfo(), true, null);

            log(" Apply diff-file to the replica");
            // ?????? ????????? ????????? ? ?? ??????? ? DB_NAME_02
            final String dumpName = "dumpdiffbyhash";
            repository.setDump(replica.getName(), dumpName, dumpDiffByHash);
            repository.restoreDump(replica.getName(), dumpName, true, true, null);

            log(" Comapre master and replica");
            checker.checkReplicaSyncResult(excludes, master.getAdapter());

        } catch (Exception e) {
            logger.debug(e.getMessage());
            throw e;
        }
    }

    
    @Test
    public void testUniqueAttrCheckerByHashDump() throws Exception {
        log("/============ testUniqueAttrCheckerByHashDump ==================/");
        
        //
        // create and fill master & replica
        //

        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        // create master
        prepareAndFillDatabase(master);

        // create replica
        ReplicationConfig replications = repository.getReplicationConfig();
        ReplicaConfig repConfig = replications.getReplicaConfig(master.getName(), "localhost", CHANNEL_PORT, replica.getName());
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());
        // Here master is equals to replica
        //
        // update some unique attributes in replica
        //
        repository.updateSqlAttrValueTo(replica.getName(), "a", "title", "'aaa'", "15");
        //
        // update the same unique attributes in master
        //
        repository.updateSqlAttrValueTo(master.getName(), "a", "title", "'aaa'", "14");
        
        // create hashdump with unique attributes for replica
        String objVersionForDump = ":"+repository.getId()+":"+replica.getName(); 
        DumpFile dumpWithUniqueAttrs = repository.createHashFile(replica.getName(), replica.getUserInfo(),true, objVersionForDump, null, null);
        // Check content of the diff-dump
        DumpInfoTestImpl infoDiffDumpByHash = new DumpInfoTestImpl(dumpWithUniqueAttrs.getFullName(), repository.getTempDir());
        assertEquals("Must contains only 1 record for updated object", 1, infoDiffDumpByHash.getObjectInfo("a").getRowCount() );
        
        Set excludeA = new HashSet();
        excludeA.add("a");
        
        // must be Ok
        new File(CONFLICT_FILE_LOG).delete();
        assertTrue("log file exists",!new File(CONFLICT_FILE_LOG).delete());
        ConflictLogger cc = new ConflictLogger(master.getName(), replica.getName(), "I dont know");
        UniqueAttrChecker uChecker = new UniqueAttrChecker(repository, master.getName());
        log("Start unique check 1");
        uChecker.check(dumpWithUniqueAttrs,cc,excludeA);
        assertTrue("No conflicts",cc.isNoConflicts());
        log("Finish unique check 1");
        
        // must be error and report
        log("Start unique check 2");
        uChecker.check(dumpWithUniqueAttrs,cc,null);
        assertTrue("Must be conflicts",!cc.isNoConflicts());
        assertTrue("Nust be conflicts with object 'a'",cc.getConflictObjectNames().contains("a"));
        assertTrue("Must be conflicts with only one object 'a'",cc.getConflictObjectNames().size()==1);
        log("Finish unique check 2");
        log(cc.toString());
        
        // save conflicts to file
        cc.appendToFile(new File(CONFLICT_FILE_LOG));
        assertTrue("There is log file", new File(CONFLICT_FILE_LOG).exists());

    }


    
    /**
     * @throws Exception
     */
    @Test
    public void testSyncInjectBadUnique() throws Exception {

        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        // =======================================
        // ??????? master
        // =======================================
        prepareAndFillDatabase(master);

        // =======================================
        // ???????? ??????? ?? ????????? ???????????
        // =======================================
        ReplicaConfig repConfig =
        	new ReplicaConfig(ReplicaConfig.MODE_INJECT, master.getName(), replica.getName(),
        			"localhost", CHANNEL_PORT, 1,
                    new HashSet<String>(), new HashSet<String>(), null, null);
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());

        // =======================================
        // ??????? unique conflict
        // =======================================
        repository.updateSqlAttrValueTo( replica.getName(), "a", "title", "'aaa'", "15");
        repository.updateSqlAttrValueTo( master.getName(), "a", "title", "'aaa'", "14");

        // =======================================
        // ????????????????
        // =======================================
        synchronizer = repository.createReplicaSynchronizer(repConfig);
        Interactive inter = new Interactive();
        QuestionLogger qLogger = new QuestionLogger();
        inter.setQuestionListener(qLogger);
        
       	synchronizer.synchronizeInjectReplica(master.getUserInfo(), replica.getUserInfo(), inter, new TreeSet(), new TreeSet());
        
        log(qLogger.toString());
        assertTrue("Must unique conflict found",qLogger.containMessage(ReplicaSynchronizer.CONFLICT_FOUND_MESSGE));
    }

    
    /**
     * @throws Exception
     */
    @Test
    public void testSyncInject() throws Exception {

        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        // =======================================
        // ??????? master
        // =======================================
        prepareAndFillDatabase(master);

        // =======================================
        // ???????? ??????? ?? ????????? ???????????
        // =======================================
        ReplicaConfig repConfig =
        	new ReplicaConfig(ReplicaConfig.MODE_INJECT, master.getName(), replica.getName(),
        			"localhost", CHANNEL_PORT, 1, new HashSet<String>(), new HashSet<String>(), null, null);
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());

        // =======================================
        // ??????? ?? ??????? ????????? ?????????
        // =======================================
        modifyForInjectTest(master, replica);

        // =======================================
        // ????????????????
        // =======================================
        synchronizer = repository.createReplicaSynchronizer(repConfig);
        Interactive inter = new Interactive();
        QuestionLogger qLogger = new QuestionLogger();
        inter.setQuestionListener(qLogger);
        
       	synchronizer.synchronizeInjectReplica(master.getUserInfo(), replica.getUserInfo(), inter, new TreeSet<String>(), new TreeSet<String>());
        
        // =======================================
       	// ????????
        // =======================================
       	checkInjectModified(master, replica);
       	
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSyncInjectEmptyByDump() throws Exception {
        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();

        // =======================================
        // ??????? master
        // =======================================
        prepareAndFillDatabase(master);

        // =======================================
        // ???????? ??????? ?? ????????? ???????????
        // =======================================
        ReplicaConfig repConfig =
        	new ReplicaConfig(ReplicaConfig.MODE_INJECT, master.getName(), replica.getName(),
        			"localhost", CHANNEL_PORT, 1,
                    new HashSet<String>(), new HashSet<String>(), null, null);
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());

        // ==================================
        // START check for empty modifiers
        // ==================================
        // create hash dump by version master
        DumpFile hashWithMasterVersions = repository.createHashFile(replica.getName(),replica.getUserInfo(),null);
        FileComparator fc = new SimpleFileComparator(replica.getName());
        DumpFile dumpInject = repository.createDiffInjectDumpByHash(master.getName(),hashWithMasterVersions,fc,
                Collections.EMPTY_SET,Collections.EMPTY_SET,
                master.getUserInfo(),false,null);
        // empty difference checks
        log("Dump INFO:=\n"+dumpInject.getDumpInfo());
        assertTrue( "Must be non changed",dumpInject.getDumpInfo().getChangedObject().isEmpty() );
        // ==================================
        // END check for empty modifiers
        // ==================================
        
    }

    
    
    /**
     * @throws Exception
     */
    @Test
    public void testSyncInjectByDump() throws Exception {
        DBInfo master = getEnvironment().getDb01();
        DBInfo replica = getEnvironment().getDbRemote();
        
        // =======================================
        // ??????? master
        // =======================================
        prepareAndFillDatabase(master);

        // =======================================
        // ???????? ??????? ?? ????????? ???????????
        // =======================================
        ReplicaConfig repConfig =
        	new ReplicaConfig(
        			ReplicaConfig.MODE_INJECT, master.getName(), replica.getName(),
        			"localhost", CHANNEL_PORT, 1, new HashSet<String>(), new HashSet<String>(), null, null
        	);
        
        ReplicaSynchronizer synchronizer = repository.createReplicaSynchronizer(repConfig);
        synchronizer.createReplica(master.getUserInfo(), replica.getUserInfo(), new Interactive());

        // =======================================
        // ??????? ?? ??????? ????????? ?????????
        // =======================================
        modifyForInjectTest(master, replica);
        
        // =======================================
        // ????????????????
        // =======================================
        DumpFile hashAllVersions;
        FileComparator fc;
        DumpFile dumpInject;
        
        // create hash dump by version master and check it
        hashAllVersions = repository.createHashFile(replica.getName(),replica.getUserInfo(),null);

        // create dump by hash
        fc = new SimpleFileComparator(replica.getName());
        dumpInject = repository.createDiffInjectDumpByHash(master.getName(),hashAllVersions,fc,
                Collections.EMPTY_SET,Collections.EMPTY_SET,
                master.getUserInfo(),false,null);

        // check created DiffInjectDump
        log("Dump INFO:=\n"+dumpInject.getDumpInfo());
        assertEquals(
        		new DumpInfoImpl(new HashSet(Arrays.asList(new ObjectInfo[]{
        				new ObjectInfoImpl("a",0,1,0),
        				new ObjectInfoImpl("b",0,0,2),
        				new ObjectInfoImpl("d",0,0,1),
             			new ObjectInfoImpl("f",0,0,1),
                        new ObjectInfoImpl("j",0,0,3),
                        new ObjectInfoImpl("m",0,0,3)
        		})), 
        			Collections.EMPTY_SET,Collections.EMPTY_SET,Collections.EMPTY_SET),
        		dumpInject.getDumpInfo()
        		);
        
        //restore diffInjectDump
        repository.setDump(replica.getName(), "wow", dumpInject);
        repository.restoreDump(replica.getName(), "wow", true, false, null);
        
        // =======================================
       	// ????????
        // =======================================
       	checkInjectModified(master, replica);
       	
    }

    
	/**
	 * @param master
	 * @param replica
	 * @throws Exception
	 */
	private void checkInjectModified(DBInfo master, DBInfo replica) throws Exception {
		String repVer =repository.getObjectVersionSuffix(replica.getName());
       	String masterVer =repository.getObjectVersionSuffix(master.getName());
       	//1) ?????? d(title=s-updated,title2=s-updated) ?? replice ?????? ???????? ????? ????? 
       	assertTrue(
       			"?????? d(title=s-updated,title1=s-updated) ?? replice ?????? ???????? ????? ?????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"d", 
       					Arrays.asList(new String[]{"title","title2"}),
       					Arrays.asList(new String[]{"='s-updated'","='s-updated'"}))==1
       	);
       	//2)
       	assertTrue(
       			"?????? a(id=15,title=aaa) ?? replice ?????? ???????? ????? ?????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"a", 
       					Arrays.asList(new String[]{"id","title"}),
       					Arrays.asList(new String[]{"=15","='aaa'"}))==1
       	);
       	//3)
       	assertTrue(
       			"?????? d(id=4) ?? replice ?????? ???????? ????? ?????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"d", 
       					Arrays.asList(new String[]{"id"}),
       					Arrays.asList(new String[]{"=4"}))==1
       	);
       	assertTrue(
       			"?????? d(id=4) ?????? ????? objversion from replica",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"d", 
       					Arrays.asList(new String[]{"id","objversion"}),
       					Arrays.asList(new String[]{"=4"," LIKE '%"+repVer+"'"}))==1
       	);
       	
       	//4)  ??????? ?? master ????? ??????, ?? ?????? ????????? ?? ???????
       	assertTrue(
       			"?????? b(id=21) ?? ?????? ????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"b", 
       					Arrays.asList(new String[]{"id"}),
       					Arrays.asList(new String[]{"=21"}))==0
       	);
       	//5)
       	assertTrue(
       			"?????? a(id=1,title='a1-updated') ?????? ????? objversion from master",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"a", 
       					Arrays.asList(new String[]{"id","title", "objversion"}),
       					Arrays.asList(new String[]{"=11","='a1-updated'"," LIKE '%"+masterVer+"'"}))==1
       	);
       	// check records b(27) for a(15), that was deleted in master
       	assertTrue(
       			"?????? b(27) ?????? ???? ???????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"b", 
       					Arrays.asList(new String[]{"id"}),
       					Arrays.asList(new String[]{"=27"}))==0
       	);
       	// check records f(53) for a(15), that was deleted in master
       	assertTrue(
       			"?????? f(53) ?????? ???? ???????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"f", 
       					Arrays.asList(new String[]{"id"}),
       					Arrays.asList(new String[]{"=53"}))==0
       	);
       	// check records j(70,71.72) for a(15), that was deleted in master
       	assertTrue(
       			"?????? j(70,71,72) ?????? ???? ???????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"j", 
       					Arrays.asList(new String[]{"id"}),
       					Arrays.asList(new String[]{" IN (70,71,72)"}))==0
       	);
       	// check records m(100,101,102) for a(15), that was deleted in master
       	assertTrue(
       			"?????? m(100,101,102) ?????? ???? ???????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"m", 
       					Arrays.asList(new String[]{"id"}),
       					Arrays.asList(new String[]{" IN (100,101,102)"}))==0
       	);
       	//6)
       	// check records a(14), updated on replica
       	assertTrue(
       			"?????? a(14) ?? ?????? ??????????",
       			repository.selectCount(replica.getDbUser(),replica.getName(),
       					"a", 
       					Arrays.asList(new String[]{"id","title"}),
       					Arrays.asList(new String[]{"=14","='a14-updated'"}))==1
       	);
       	
	}

	/**
	 * @param master
	 * @param replica
	 * @throws Exception
	 */
	private void modifyForInjectTest(DBInfo master, DBInfo replica) throws Exception {
		// 1) ???????? ?????? ?? replica, ??? ?????? ???????? ????? ?????
        // insert d(title,title2)
        repository.insertSqlIntoTable(replica.getDbUser(),replica.getName(),
        		"d", 
        		Arrays.asList(new String[]{"title","title2"}),
        		Arrays.asList(new String[]{"'s-updated'","'s-updated'"}));
        // 2) ??????? ????????, ???????? ???????? ?? ?????? ????? ????? ??????????
        repository.updateSqlAttrValueTo(replica.getName(), "a", "title", "'aaa'", "15");
        
        // 3) ????????? ? ??????????? ???????, foreign ???????? ?????? ?? master ?????,
        //		foreign ?????? ?????? ???? ???????????? ? objversion ???????
        // ???????: ???????? ?? replica ??????????? ?????? (update a(id=15)) - 
        // ??????? ?? master ?????? (delete d(id=4))
        repository.deleteFromTable(master.getName(), "d", "4");
        
        // 4) ??????? ?? master ????? ??????, ?? ?????? ????????? ?? ???????
        // delete master: b(id=21)
        repository.deleteFromTable(master.getName(), "b", "21");
        
        // 5) ???????? ?? master ????? ??????, ? ?? ?????? ?????????? ?? ???????
        // master: update a(id=1,title='a1-updated')
        repository.updateSqlAttrValueTo(master.getName(), "a", "title", "'a1-updated'", "11");
        
        // 6) ???????? ?? replica ????? ??????, ? ?? ??????? ???? ?????????? ????????????
        // master: update a(id=1,title='a1-updated')
        repository.updateSqlAttrValueTo(replica.getName(), "a", "title", "'a14-updated'", "14");
	}

    
    /**
     * ????????? ???????????? ????????? diff dump ?? ???????
     * @throws Exception
     */
	@Test
    public void testSinchronizeByDiffDump() throws Exception {
        log("/============ testSinchronizeByDiffDump() ==================/");
        startSynchronize(true);
        log("/============ testSinchronizeByDiffDump() finish ==================/");
    }

    /**
     * ???????????? master ? replica; ?????????? ??????????? ????????
     */
    class SyncChecker {
        private DBInfo dbMaster;
        private DBInfo dbReplica;
        private DumpInfo masterInfo;
        private DumpInfo replicaStartInfo;
        private DumpInfo replicaFinishInfo;
        static final String masterDumpName = "master_dump";
        static final String replicaStartDumpName = "relica_start_dump";
        static final String replicaFinishDumpName = "relica_finish_dump";

        public SyncChecker(DBInfo dbMaster, DBInfo dbReplica) {
            this.dbMaster = dbMaster;
            this.dbReplica = dbReplica;
        }

        void modifyMasterDB() throws Exception {
            // ??????? - DB_NAME_01 ??????????? ??????????
            // - ???????? 1 ???? ??????? "a"
            // - ???????? ?????? "a" ? ??????
            // - ??????? ?? ??????? "?" ? "e"
            // ??????? ?? ??????? ?? exlude objects ? ?? exclude, ????? ????????? "??????????????" exclude ????????
            repository.modifyDB2(dbMaster);
        }

        void collectInfoBeforeSyncReplica() throws Exception {
            repository.dump(dbReplica.getName(), replicaStartDumpName, true, null);
            replicaStartInfo = new DumpInfoTestImpl(repository.getDumpFilePath(dbReplica.getName(), replicaStartDumpName), repository.getTempDir());
            repository.dump(dbMaster.getName(), masterDumpName, true, null);
            masterInfo = new DumpInfoTestImpl(repository.getDumpFilePath(dbMaster.getName(), masterDumpName), repository.getTempDir());
        }

        private void collectInfoAfterSyncReplica() throws Exception {
            repository.dump(dbReplica.getName(), replicaFinishDumpName, true, null);
            replicaFinishInfo = new DumpInfoTestImpl(repository.getDumpFilePath(dbReplica.getName(), replicaFinishDumpName), repository.getTempDir());
        }

        void checkReplicaSyncResult(Set<String> paramExcludeObjectNames, DBAdapter adapter) throws Exception {
        	
            Set<String> excludeObjectNames = new HashSet<String>(paramExcludeObjectNames);
            excludeObjectNames.addAll(adapter.getSystemObjects());
            collectInfoAfterSyncReplica();

            for (ObjectInfo masterObject : masterInfo.getObjects()) {
                String objectName = masterObject.getObjectName();
                if (!excludeObjectNames.contains(objectName)) {
                    // ????????? ? ????????
                    assertEquals(masterObject, replicaFinishInfo.getObjectInfo(objectName));
                } else {
                    // ??????? ???????? ??? ? ???? - excludes ? ????????? ???????
                    assertEquals(replicaStartInfo.getObjectInfo(objectName), replicaFinishInfo.getObjectInfo(objectName));
                }
            }

            if (!excludeObjectNames.contains("i")) {
                assertEquals(masterInfo.getObjectInfo("i_tree"), replicaFinishInfo.getObjectInfo("i_tree"));
            } else {
                assertEquals(replicaStartInfo.getObjectInfo("i_tree"), replicaFinishInfo.getObjectInfo("i_tree"));
            }

            assertEquals(replicaStartInfo.getObjectInfo("log"), replicaFinishInfo.getObjectInfo("log"));
            assertEquals(replicaStartInfo.getObjectInfo("logupdate"), replicaFinishInfo.getObjectInfo("logupdate"));

        }

    }


    class SimpleFileComparator implements FileComparator {
        private String dbName;
        private HashSet remoteObjectNames=null;

        public SimpleFileComparator(String dbName) {
            this.dbName = dbName;
        }

        public void setRemoteObjectNames(Set objectNames) {
            //To change body of implemented methods use File | Settings | File Templates.
            remoteObjectNames = new HashSet(objectNames);
        }

        public boolean compare(Id id, String objectName, String attrName, FileValue fv) throws ru.adv.util.ErrorCodeException {
            if (remoteObjectNames!=null && !remoteObjectNames.contains(objectName)) {
                return false; // Object not exists on remote Repository
            }
            boolean result = true;
            // compare file size first
            if (fv.getLength() != repository.getFileLength(dbName, id, objectName, attrName).intValue()) {
                result = false;
            }
            if (! fv.getName().equals( 
            		StringUtils.getFilename(repository.getAbsoluteFileName(dbName, id, objectName, attrName)))
            ){
                result = false;
            }

            return result;
        }

        public void destroy() {
        }
    }

    private class Thermometer implements ProgressCallback {

        private class Data {
            private long maxValue;
            private long currValue;
            public long shift;

            public String toString() {
                return "[" + maxValue + "," + currValue + "," + shift + "]";
            }
        }

        private List list = new ArrayList();
        private Data curData;

        public void init(long maxValue) {
            curData = new Data();
            curData.maxValue = maxValue;
            list.add(curData);
        }

        public void setProgressShift(long shift) {
            curData.shift = shift;
        }

        public void setProgress(long currValue) {
            curData.currValue = currValue;
        }

        public void done() {
        }

        public String toString() {
            return list.toString();
        }

    }
}

