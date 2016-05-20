/*
 * Copyright 2009-2010 Nanjing RedOrange ltd (http://www.red-orange.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package redora.junit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redora.exceptions.RedoraException;
import redora.test.rdo.model.JUnitChild;
import redora.test.rdo.model.JUnitMaster;
import redora.test.rdo.service.JUnitChildService;
import redora.test.rdo.service.JUnitMasterService;
import redora.test.rdo.service.ServiceFactory;

import java.util.Date;

import static java.lang.Boolean.FALSE;
import static redora.api.fetch.Page.ALL_LIST;
import static redora.api.fetch.Page.ALL_TABLE;
import static redora.api.fetch.Scope.Table;
import static redora.junit.AbstractDBTest.*;
import static redora.test.rdo.model.fields.JUnitMasterFields.notnull;
import static redora.util.JUnitUtil.assertRedoraPersist;

/**
 * AttributeTest will check the attributes in the model: <br>
 * Is the max length business rule is implemented;<br>
 * Are the createDate and updateDate maintained correctly;<br>
 * Is the NotNull business rule implemented; <br>
 * Is Lazy implemented;<br>
 * Is regexp business rule working;<br>
 * Are the default values created;<br>
 * Very big fields can persist;<br>
 * Is the finder created and working;<br>
 * 
 * @author Nanjing RedOrange ltd (www.red-orange.cn)
 */
public class AttributeTest {

    static JUnitMasterService jUnitMasterService;
    static JUnitChildService jUnitChildService;

    @BeforeClass
    public static void startService() throws RedoraException {
        makeTestTables();
        jUnitMasterService = ServiceFactory.jUnitMasterService();
        jUnitChildService = ServiceFactory.jUnitChildService();
    }

    @AfterClass
    public static void stopService() throws RedoraException {
        dropTestTables();
        jUnitMasterService.close(); //DON'T COPY THIS, use ServiceFactory.close() in your code.
        jUnitChildService.close();
    }

    /**
     * Test if the default=xyz attribute is set correctly.
     */
    @Test
    public void pojoDefault() {
        JUnitMaster def = new JUnitMaster();
        assertEquals("Default value for String is not ok.", "i am here", def.getDdefault());
        assertEquals("Default value for Enum is not ok.", "One", def.getEnumm().name());
        assertEquals("Default value for Integer is not ok.", Integer.valueOf(22), def.getIinteger());
        assertEquals("Default value for Long is not ok.", Long.valueOf(23), def.getLlong());
        assertEquals("Default value for Boolean is not ok.", FALSE, def.getBboolean());
        assertEquals("Default value for Double is not ok.", 2.2, def.getDdouble());
    }

    /**
     * Test if the default=xyz attribute is set correctly in the database.
     * Defaults are set in the table definition, and are not persisted through
     * the pojo.
     */
    @Test
    public void sqlDefault() throws RedoraException {
        JUnitMaster def = new JUnitMaster();
        def.avoidNull();
        assertRedoraPersist(jUnitMasterService.persist(def));
        // "refresh"
        def = jUnitMasterService.findById(def.getId(), Table);

        assertEquals("Default value for String is not ok.", "i am here", def.getDdefault());
        assertEquals("Default value for Enum is not ok.", "One", def.getEnumm().name());
        assertEquals("Default value for Integer is not ok.", Integer.valueOf(22), def.getIinteger());
        assertEquals("Default value for Long is not ok.", Long.valueOf(23), def.getLlong());
        assertEquals("Default value for Boolean is not ok.", FALSE, def.getBboolean());
        assertEquals("Default value for Double is not ok.", 2.2, def.getDdouble());
    }

    @Test
    public void createAndUpdateDate() throws RedoraException {

        JUnitMaster master = new JUnitMaster();
        master.avoidNull();
        assertRedoraPersist(jUnitMasterService.persist(master));

        // "refresh"
        master = jUnitMasterService.findById(master.getId(), Table);

        assertNotNull("CreationDate is not set", master.getCreationDate());
        Date firstDate = (Date) master.getCreationDate().clone();
        assertNull("UpdateDate has been set on first persist", master.getUpdateDate());

        assertRedoraPersist(jUnitMasterService.persist(master));

        assertNull("UpdateDate has changed while the object was never changed", master
                .getUpdateDate());
        master.avoidNull();

        assertTrue("Nothing has changed, really", master.dirty.isEmpty());

        master.setSstring("Changed, really");

        assertRedoraPersist(jUnitMasterService.persist(master));
        assertNotNull("UpdateDate is not set while object was updated", master.getUpdateDate());
        assertEquals("Creation date has changed", firstDate, master.getCreationDate());
    }


    static String OWASP = "null: \u0000 bs: \u0008 tab: \t lf: \n cr: \r sub: \u001a \" % ' \\ _";
    @Test
    public void difficult() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();
        master.setNotnull(OWASP);

        assertRedoraPersist(jUnitMasterService.persist(master));

        master = jUnitMasterService.findById(master.getId(), Table);

        assertEquals("Medium seems to be changed", OWASP, master.getNotnull());
    }

    @Test
    public void mediumField() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();

        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 6500; i++)
            big.append("1234567890");

        master.setMedium(big.toString());

        int collectionSize = jUnitMasterService.findAll(ALL_LIST).size();
        assertRedoraPersist(jUnitMasterService.persist(master));
        assertEquals("Hmm, not really persisted", collectionSize + 1, jUnitMasterService.findAll(ALL_LIST).size());
        // "refresh"
        master = jUnitMasterService.findById(master.getId(), Table);

        assertEquals("Medium seems to be changed", master.getMedium().length(), big.length());
    }
    @Test
    public void largeField() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();

        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 98000; i++)
            big.append("1234567890");

        master.setLarge(big.toString());

        int sizeBefore = jUnitMasterService.findAll(ALL_LIST).size();
        assertRedoraPersist(jUnitMasterService.persist(master));
        assertEquals("Hmm, not really persisted", sizeBefore + 1, jUnitMasterService.findAll(ALL_LIST).size());
        // "refresh"
        master = jUnitMasterService.findById(master.getId(), Table);

        assertEquals("Large seems to be changed", master.getLarge().length(), big.length());
    }

    @Test
    public void htmlField() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();

        master.setAntisamyNotLazy("<b>Hi<script>i am wrong</script>");
        assertEquals("HTML is not clean enough", "<b>Hi</b>", master.getAntisamyNotLazy());
    }

    /**
     * When lazy=true, an attribute should only be retrieved from the database
     * when the getter is used. This is checked for string attributes
     * (non-default set to lazy), class attributes, html and xml attributes are
     * lazy by default.
     * 
     */
    @Test
    public void lazy() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();
        master.setFinder("lazy");
        master.setLazy("The description is not empty (redorange)");
        master.setAntisamyLazySlashdot("<b>Hi");
        master.setMedium("I am not empty too");
        assertRedoraPersist(jUnitMasterService.persist(master));
        // "refresh"
        master = jUnitMasterService.findByFinder("lazy", ALL_TABLE).get(0);
        assertNull("Lazy should be null, it has lazy retrieval", master.lazy);
        assertNull("HTML should be null, it has lazy retrieval", master.antisamyLazySlashdot);
        assertNull("Medium should be null, it has lazy retrieval", master.medium);
        assertNotNull("Description should NOT be null, i am using the getter", master.getLazy());
        assertNotNull("HTML should NOT be null, i am using the getter", master
                .getAntisamyLazySlashdot());
        assertNotNull("Medium should NOT be null, i am using the getter", master
                .getMedium());

        JUnitChild child = new JUnitChild();
        child.setJUnitMaster(master);
        assertRedoraPersist(jUnitChildService.persist(child));

        // "refresh"
        child = jUnitChildService.findById(child.getId(), Table);
        assertNull("Master should be null, it has lazy retrieval", child.jUnitMaster);
        assertEquals("Retrieved the wrong Master", master.getId(), child.getJUnitMaster().getId());
    }

    @Test
    public void finder() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();
        master.setFinder("Find me");
        assertRedoraPersist(jUnitMasterService.persist(master));

        assertEquals("I expected to find one 'Find me' record", 1
                , jUnitMasterService.findByFinder("Find me", ALL_TABLE).size());
    }

    /**
     * The dirty should keep the old (persisted) value, this can be used for example
     * for business rule checking where you need the old value to evaluate the new.
     * @throws RedoraException Passing on
     */
    @Test
    public void dirtyOldValue() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();
        master.setNotnull("First old value test");

        assertNull("The old value should be null", master.dirty.get(notnull));

        assertRedoraPersist(jUnitMasterService.persist(master));

        master.setNotnull("Second old value test");
        assertEquals("The old value is correct", "First old value test", master.dirty.get(notnull));
        master.setNotnull("Third old value test");
        assertEquals("The old value should not change until it is persisted", "First old value test", master.dirty.get(notnull));
    }

    /**
     * Accessing non list scope attributes while the object is fetched with the list scope
     * should provoke an error.
     * Persisting a list scope object should provoke an error.
     *
     * @throws RedoraException Passing on
     */
    @Test
    public void listScope() throws RedoraException {
        JUnitMaster master = new JUnitMaster();
        master.avoidNull();
        master.setFinder("List exception check");
        assertRedoraPersist(jUnitMasterService.persist(master));

        // "refresh"
        master = jUnitMasterService.findByFinder("List exception check", ALL_LIST).get(0);

        try {
            master.setNotnull("Hi");
            master.getBboolean();
            fail("An AssertionError was expected from all setters");
        } catch (AssertionError e) {}
    }
}
