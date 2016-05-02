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

import java.io.FileWriter;
import java.io.InputStreamReader;
import java.security.Principal;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import ru.adv.db.DBConnection;
import ru.adv.db.adapter.DBAdapter;
import ru.adv.db.base.MObject;
import ru.adv.db.base.MValue;
import ru.adv.db.config.DBConfig;
import ru.adv.db.create.DBCreateException;
import ru.adv.db.handler.Handler;
import ru.adv.db.handler.SaveOptions;
import ru.adv.event.Response;
import ru.adv.repository.RepositoryException;
import ru.adv.repository.SyncHandler;
import ru.adv.security.Editor;
import ru.adv.security.SecurityOptions;
import ru.adv.test.repository.test.DBInfo;
import ru.adv.util.InputOutput;

/**
 * Test class for {@link DBAdapter} that supports native triggers
 * 
 * @author vic
 */
abstract public class AbstractAdapterNativeTriggersTester extends AbstractAdapterTester {
	
    static final String BASE_CFG_FUNC_VIEW_ORDER="resource://ru/adv/test/repository/test/base-func-view-order.xml";
    static final String BASE_CFG_INT_COLUMN_TYPES="resource://ru/adv/test/repository/test/base-func-view-order-int.xml";

    public static final String TEST_TABLE_NAME = "t";

	
	
    @Test
    public void testOrderCreatetionViewAndFunction() throws Exception {
        logger.info("============= testOrderCreatetionViewAndFunction() ================");
        DBInfo db = getEnvironment().getDb("viewfunc");

        // Create database
        repository.createDB(db.getName(),db.getUserInfo(),db.getAdapter().getName());

        // modify database config
        modifyConfigTo(db,BASE_CFG_FUNC_VIEW_ORDER);

        syncDB(db);

        Assert.assertTrue("Database have been created", repository.getDBNames().contains(db.getName()));
        Assert.assertTrue("Database is available for access", !repository.getNotInUseDBNames().contains(db.getName()));

        logger.info(repository.getDBNames());

    }

    @Test
    public void testAlterIntegerColumnType() throws Exception {
    	
        logger.info("============= testAlterIntegerColumnType() ================");
        DBInfo db = getEnvironment().getDb("alterinttype");
        
        if (!db.getAdapter().isSupportNativeSystemTriggers()) {
        	logger.info("skip this test for adapter "+db.getAdapter());
        	return;
        }

        // Create database
        repository.createDB(db.getName(),db.getUserInfo(),db.getAdapter().getName());

        // modify database config
        modifyConfigTo(db,BASE_CFG_FUNC_VIEW_ORDER);

        syncDB(db);

        Assert.assertTrue("Database have been created", repository.getDBNames().contains(db.getName()));
        Assert.assertTrue("Database is available for access", !repository.getNotInUseDBNames().contains(db.getName()));

        // fill test table with numbers
        insertSingleObjectIntoTableT(db);

        // modify database config. alter all numeric columns to int
        modifyConfigTo(db,BASE_CFG_INT_COLUMN_TYPES);
        // synchronize database: all to int
        syncDB(db);

        // modify database config. into previous config (differnt numeric types)
        modifyConfigTo(db,BASE_CFG_FUNC_VIEW_ORDER);
        // back synchronize database: int to original types
        syncDB(db);

    }

    
    private void modifyConfigTo(DBInfo db, String baseXml) throws Exception {
    	InputOutput io = InputOutput.create(baseXml);
    	// replace adapter to current
    	String xmlStr = FileCopyUtils.copyToString(
    			new InputStreamReader(io.getInputStream(),"UTF-8")
    	).replaceFirst("postgres", db.getAdapter().getName());
    	FileCopyUtils.copy(
    			xmlStr, 
    			new FileWriter(repository.getContext().getConfigSrcFilePath(db.getName())) 
    	);
    }

    private void syncDB(DBInfo db) throws RepositoryException, DBCreateException {
        // test prepare
        repository.prepare(db.getName(), db.getUserInfo(), true);

        // synchronize database structure
        SyncHandler sh = repository.getSyncHandler(db.getName(), db.getUserInfo(), true);
        sh.createTempDump(false);
        while (sh.hasEvent() == true) {
            sh.getEvent();
            sh.setResponse(new Response(true));
        }
        sh.destroy();
    }


    private void insertSingleObjectIntoTableT(DBInfo db) throws Exception {
        DBConfig dbConfig = repository.getDBConfig(db.getName());
        DBConnection conn = repository.createDBConnection(dbConfig,db);

        Handler h = new Handler(dbConfig, conn, ":"+this.getClass().getName());
        try {
            MObject mObject = h.getDBConfig().createMObject(TEST_TABLE_NAME,null);
            mObject.getAttribute("int").setValue(new MValue("555"));
            mObject.getAttribute("shortint").setValue(new MValue("7"));
            mObject.getAttribute("long").setValue(new MValue("99999"));
            mObject.getAttribute("float").setValue(new MValue("9.0234"));
            mObject.getAttribute("double").setValue(new MValue("199.239487"));

            SaveOptions sopt = new SaveOptions(mObject, getSecurityOptions());
            sopt.setMode(Handler.NEWONLY_SAVE_MODE);
            h.save(sopt);
        } finally{
            h.destroy();
        }
    }
    
    public SecurityOptions getSecurityOptions() {
        return new SecurityOptions() {
            public Principal getUserPrincipal() {
                return new Editor();
            }
            public boolean isUserInRole(String role) {
                return "editor".equals(role);
            }
        };
    }
    

}

