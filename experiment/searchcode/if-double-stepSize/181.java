/**
 * 
 * Copyright 2010 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful,
 * but without any warranty; without even the implied warranty of
 * merchantability or fitness for a particular purpose. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ch.psi.fda.core.loops.cr;


import gov.aps.jca.CAException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.psi.fda.TestConfiguration;
import ch.psi.fda.core.Sensor;
import ch.psi.fda.core.actors.OTFActuator;
import ch.psi.fda.core.messages.EndOfStreamMessage;
import ch.psi.fda.core.messages.Message;
import ch.psi.fda.core.sensors.ChannelAccessDoubleSensor;
import ch.psi.fda.core.sensors.MillisecondTimestampSensor;
import ch.psi.fda.core.sensors.OTFNamedChannelSensor;
import ch.psi.fda.core.sensors.OTFScalerChannelSensor;
import ch.psi.jcae.ChannelBeanFactory;

/**
 * @author ebner
 *
 */
public class ParallelCrlogicTest {
	
	// Get Logger
	private static final Logger logger = Logger.getLogger(ParallelCrlogicTest.class.getName());
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Ignore
	@Test(timeout=60000)
	public void testExecute() throws InterruptedException, CAException{
		
		TestConfiguration c = TestConfiguration.getInstance();
		
		boolean zigZag = false;
		
		String readback = null;
		double start = 0;
		double end = 2;
		double stepSize = 0.01;
		double integrationTime = 0.01;
		double additionalBacklash = 0;
		
		List<Sensor> sensors = new ArrayList<Sensor>();
		ChannelAccessDoubleSensor s2 = new ChannelAccessDoubleSensor("mot1", c.getMotor1()+".RVAL");
		ChannelAccessDoubleSensor s1 = new ChannelAccessDoubleSensor("mot1", c.getMotor1()+".RBV");
		
		sensors.add(s1);
		sensors.add(s2);
		
		ScrlogicLoop scrlogic = new ScrlogicLoop(sensors);
		
		
		CrlogicLoop crlogic = new CrlogicLoop(c.getCrlogicPrefix(), c.getServer(), c.getShare(), c.getSmbShare(), zigZag);
		crlogic.setActor(new OTFActuator("cmot", c.getMotor1(), readback, start, end, stepSize, integrationTime, additionalBacklash));
		crlogic.getSensors().add(new OTFNamedChannelSensor("trigger", "TRIGGER0"));
		crlogic.getSensors().add(new OTFScalerChannelSensor("scaler0", 0));
		crlogic.getSensors().add(new OTFScalerChannelSensor("scaler1", 1));
		crlogic.getSensors().add(new MillisecondTimestampSensor("timestamp"));
		
		
		// Initialize scaler template
		VSC16ScalerChannelsTemplate scalertemplate = new VSC16ScalerChannelsTemplate();
		ChannelBeanFactory.getFactory().createChannelBeans(scalertemplate, c.getPrefixScaler());
		
		
		ParallelCrlogic pcrlogic = new ParallelCrlogic(crlogic, scrlogic);
		
		logger.info("Start scaler");
		scalertemplate.getCommand().setValueNoWait(VSC16ScalerChannelsTemplate.Command.Count.ordinal());
		
		pcrlogic.prepare();
		pcrlogic.execute();
		pcrlogic.cleanup();
		
		logger.info("Stop scaler");
		scalertemplate.getCommand().setValue(VSC16ScalerChannelsTemplate.Command.Done.ordinal());
		
		System.out.println("PARALLEL CRLOGIC data:");
		BlockingQueue<Message> queue = pcrlogic.getDataQueue().getQueue();
		Message m = queue.take();
		while(! (m instanceof EndOfStreamMessage)){
			System.out.println(m.toString());
			m = queue.take();
		}
		
		// Destroy scaler template
		ChannelBeanFactory.getFactory().destroyChannelBeans(scalertemplate);
		
		pcrlogic.destroy();
		
		
//		System.out.println("CRLOGIC data:");
//		BlockingQueue<Message> queue = crlogic.getDataQueue().getQueue();
//		Message m = queue.take();
//		while(! (m instanceof EndOfStreamMessage)){
//			System.out.println(m.toString());
//			m = queue.take();
//		}
//		
//		
//		System.out.println("SCRLOGIC data:");
//		queue = scrlogic.getDataQueue().getQueue();
//		m = queue.take();
//		while(! (m instanceof EndOfStreamMessage)){
//			System.out.println(m.toString());
//			m = queue.take();
//		}
	}

}

