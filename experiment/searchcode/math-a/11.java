/**
 * 
 */
package uk.ac.lkl.migen.system.expresser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import uk.ac.lkl.common.util.config.MiGenConfiguration;
import uk.ac.lkl.common.util.restlet.RestletException;
import uk.ac.lkl.migen.system.ExpresserLauncher;
import uk.ac.lkl.migen.system.MiGenContext;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.ModelColor;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.server.DummyUser;
import uk.ac.lkl.migen.system.server.MiGenServerCommunicator;
import uk.ac.lkl.migen.system.server.UserSet;
import uk.ac.lkl.migen.system.task.TaskIdentifier;
import uk.ac.lkl.migen.system.util.MiGenUtilities;
import uk.ac.lkl.migen.system.util.TimeStampMatch;

/**
 * For testing the ExternalInterface save and fetch model methods
 * 
 * 
 * @author Ken Kahn
 *
 */
public class TransferModelToServerTest extends StandaloneExpresserTest {
//    @Test
    // commented out the test since this causes an exception in the server
    // ServerRequestQueueProcessor.handleAddRequest
    public void saveAndFetch1() throws RestletException, InterruptedException, IOException {
	long time0 = System.currentTimeMillis();
	String serverName = MiGenConfiguration.getServerName();
	int serverPort = MiGenConfiguration.getServerPort();
	MiGenServerCommunicator serverCommunicator = new MiGenServerCommunicator(serverName, serverPort);
	MiGenContext.setServerCommunicator(serverCommunicator);
	DummyUser dummyUser = new DummyUser();
	UserSet userSet = new UserSet(dummyUser);
	serverCommunicator.addUser(dummyUser);
	// not clear if a MiGenSession makes sense here
	ExpresserLauncher.setUserSet(userSet);
	serverCommunicator.addUserSet(userSet);
	ExpresserModel model1 = MiGenUtilities.getExpresserModelFromFile("footpath");
	long time1 = System.currentTimeMillis();
	String name1 = TaskIdentifier.TRAINTRACK.getId() + "_" + Long.toString(time1, 16) + "_U";
	MiGenUtilities.saveModelOnServer(model1, name1, userSet, serverCommunicator);
	Thread.sleep(1000);
	long time2 = System.currentTimeMillis();
	ExpresserModel model2 = MiGenUtilities.getExpresserModelFromFile("footpath-wrong-color");
	long time3 = System.currentTimeMillis();
	String name2 = TaskIdentifier.GARDEN.getId() + "_" + Long.toString(time3, 16) + "_U";
	MiGenUtilities.saveModelOnServer(model2, name2, userSet, serverCommunicator);
	ExpresserModel fetchedModel1 = MiGenUtilities.fetchModelFromServer(time0, TimeStampMatch.CLOSEST_LATER_TIMESTAMP, null, userSet);
	assertTrue(fetchedModel1 != null);
	BlockShape fetchedShape1 = fetchedModel1.getShapes().get(0);
	assertEquals(fetchedShape1.getColor(), ModelColor.RED);
	ExpresserModel fetchedModel2 = MiGenUtilities.fetchModelFromServer(time2, TimeStampMatch.CLOSEST_LATER_TIMESTAMP, null, userSet);
	assertTrue(fetchedModel2 != null);
	BlockShape fetchedShape2 = fetchedModel2.getShapes().get(0);
	assertEquals(fetchedShape2.getColor(), ModelColor.GREEN);
	// test activity name filtering
	fetchedModel2 = MiGenUtilities.fetchModelFromServer(time0, TimeStampMatch.CLOSEST_LATER_TIMESTAMP, TaskIdentifier.GARDEN, userSet);
	assertTrue(fetchedModel2 != null);
	fetchedShape2 = fetchedModel2.getShapes().get(0);
	assertEquals(fetchedShape2.getColor(), ModelColor.GREEN);
    }

}

