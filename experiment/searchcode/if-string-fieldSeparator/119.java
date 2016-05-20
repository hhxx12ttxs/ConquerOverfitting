/**                 ***COPYRIGHT STARTS HERE***
 *  BoutTime - the wrestling tournament administrator.
 *
 *  Copyright (C) 2010  Jeffrey K. Rutt
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *                  ***COPYRIGHT ENDS HERE***                                */

package bouttime.fileinput;

import bouttime.dao.Dao;
import bouttime.dao.xml.XmlDao;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for bouttime.fileinput.TextFileInput class.
 */
public class TextFileInputTest {

    private static File inputFile = null;
    private static final String INPUT_FILENAME = "testInput.txt";
    private static final String DAO_FILENAME = "testFileInput.xml";
    private Dao dao = null;

    public TextFileInputTest() { }

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Create the input file
        inputFile = new File(INPUT_FILENAME);
        assertNotNull(inputFile);
        assertTrue(inputFile.createNewFile());
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(inputFile));
        bufferedWriter.write("David,Robinson,Open,5,88,A,Spurs"); bufferedWriter.newLine();
        bufferedWriter.write("Tim,Duncan,Open,4,99,A,Spurs"); bufferedWriter.newLine();
        bufferedWriter.write("Tony,Parker,Rookie,1,77,Spurs"); bufferedWriter.newLine();
        bufferedWriter.write("Manu,Ginobili,Rookie,3,82,Spurs,More,Input,OK"); bufferedWriter.newLine();
        bufferedWriter.write("David,Robinson,Open,5,88,A,Spurs"); bufferedWriter.newLine();
        bufferedWriter.write("Bad,Input");
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Delete the input file
        if (inputFile != null) {
            inputFile.delete();
        }
    }

    @Before
    public void setUp()throws Exception {
        dao = new XmlDao();
        assertTrue(dao.openNew(DAO_FILENAME));
    }

    @After
    public void tearDown() throws Exception {
        if (dao != null) {
            dao.close();
            new File(DAO_FILENAME).delete();
            dao =  null;
        }
    }

    /**
     * Test of getInputFromFile method, of class TextFileInput.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetInputFromFile_Map_Dao() {
        Map config = new HashMap();
        config.put("firstName", "1");
        config.put("lastName", "2");
        config.put("teamName", "7");
        config.put("classification", "3");
        config.put("division", "4");
        config.put("weightClass", "5");
        config.put("actualWeight", "0");
        config.put("level", "6");
        config.put("serialNumber", "0");
        config.put("fieldSeparator", ",");
        config.put("fileName", INPUT_FILENAME);

        TextFileInput instance = new TextFileInput();
        FileInputResult result = instance.getInputFromFile(config, dao);
        assertNotNull(result);
        assertEquals(6, result.getRecordsProcessed().intValue());
        assertEquals(3, result.getRecordsAccepted().intValue());
        assertEquals(3, result.getRecordsRejected().intValue());
        if (result.getRejects() != null) {
            assertEquals(3, result.getRejects().size());
        }
        assertEquals(3, dao.getAllWrestlers().size());

        config.put("fieldSeparator", ":");
        result = instance.getInputFromFile(config, dao);
        assertNotNull(result);
        assertEquals(6, result.getRecordsProcessed().intValue());
        assertEquals(0, result.getRecordsAccepted().intValue());
        assertEquals(6, result.getRecordsRejected().intValue());
        if (result.getRejects() != null) {
            assertEquals(6, result.getRejects().size());
        }
        assertEquals(3, dao.getAllWrestlers().size());

        config.put("fileName", "BadFilename");
        result = instance.getInputFromFile(config, dao);
        assertNull(result);

        TextFileInputConfig fiConfig = dao.getTextFileInputConfig();
        assertEquals("1", fiConfig.getFirstName());
        assertEquals("2", fiConfig.getLastName());
        assertEquals("3", fiConfig.getClassification());
        assertEquals("4", fiConfig.getDivision());
        assertEquals("5", fiConfig.getWeightClass());
        assertEquals("6", fiConfig.getLevel());
        assertEquals("7", fiConfig.getTeamName());
        assertEquals("0", fiConfig.getSerialNumber());
        assertEquals("0", fiConfig.getActualWeight());
        assertEquals(":", fiConfig.getFieldSeparator());
    }

}
