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

package ch.psi.fda.serializer;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.psi.fda.core.messages.ComponentMetadata;
import ch.psi.fda.core.messages.DataMessage;
import ch.psi.fda.core.messages.DataMessageMetadata;
import ch.psi.fda.core.messages.DataQueue;
import ch.psi.fda.core.messages.StreamDelimiterMessage;
import ch.psi.fda.core.messages.EndOfStreamMessage;
import ch.psi.fda.core.messages.Message;
import ch.psi.fda.serializer.DataSerializer;
import ch.psi.fda.serializer.DataSerializerMAT;
import ch.psi.fda.serializer.DataSerializerMAT2D;
import ch.psi.fda.serializer.DataSerializerTXT;
import ch.psi.fda.serializer.DataSerializerTXT2D;
import ch.psi.fda.serializer.DataSerializerTXTSplit;

/**
 * @author ebner
 *
 */
public class DataSerializerTest {

	private static final String tmpDirectory = "target/tmp";
	
	
	private DataQueue queue;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		new File(tmpDirectory).mkdirs();
		BlockingQueue<Message> q3 = new LinkedBlockingQueue<Message>();
		DataMessageMetadata m3 = new DataMessageMetadata();
		
		this.queue = new DataQueue(q3, m3);

	}
	
	/**
	 * Generate 1D data
	 * @throws InterruptedException
	 */
	private void generate1DData() throws InterruptedException{
		
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id0", 0));
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id1", 0));
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id2", 0));
		
		// Dimension
		DataMessage m = new DataMessage();
		m.getData().add(0.000000000000000001);
		m.getData().add(0.1);
		m.getData().add(1d); // have this value as double
		queue.getQueue().put(m);
		
		m = new DataMessage();
		m.getData().add(0.02);
		m.getData().add(0.2);
		m.getData().add(2d); // have this value as double
		queue.getQueue().put(m);
		queue.getQueue().put(new EndOfStreamMessage());
	}
	
	/**
	 * Generate 2D test data
	 * @throws InterruptedException
	 */
	private void generate2DData() throws InterruptedException{
		
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id0", 1));
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id1", 0));
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id2", 0));
		
		for(double i=0;i<5;i++){
			for(double t=0.1; t<10; t=t+0.1){
				// Dimension
				DataMessage m = new DataMessage();
				m.getData().add(i);
				m.getData().add(t);
				m.getData().add(Math.log(t)); // have this value as double
				queue.getQueue().put(m);
			}
			queue.getQueue().put(new StreamDelimiterMessage(0));
		}
		queue.getQueue().put(new StreamDelimiterMessage(1));
		
		
		queue.getQueue().put(new EndOfStreamMessage());
	}
	
	/**
	 * Generate 3d test data
	 * @throws InterruptedException
	 */
	private void generate3DData() throws InterruptedException{
		
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id0", 2));
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id1", 1));
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id2", 0));
		queue.getDataMessageMetadata().getComponents().add(new ComponentMetadata("id3", 0));
		
		for(double z=30;z<36;z++){
			for(double i=0;i<6;i++){
				for(double t=0.1; t<1.1; t=t+0.1){
					// Dimension
					DataMessage m = new DataMessage();
					m.getData().add(z);
					m.getData().add(i);
					m.getData().add(t);
					m.getData().add(Math.log(t)); // have this value as double
					queue.getQueue().put(m);
				}
				queue.getQueue().put(new StreamDelimiterMessage(0));
			}
			queue.getQueue().put(new StreamDelimiterMessage(1));
		}
		queue.getQueue().put(new StreamDelimiterMessage(2));
		
		
		queue.getQueue().put(new EndOfStreamMessage());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link ch.psi.fda.serializer.DataSerializerTXT#run()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRunTXT() throws InterruptedException {
		generate1DData();
		DataSerializer serializer = new DataSerializerTXT(queue, new File(tmpDirectory+"/test.txt"), true);
		serializer.run();
	}

	/**
	 * Test method for {@link ch.psi.fda.serializer.DataSerializerTXT#run()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRunMAT() throws InterruptedException {
		generate1DData();
		DataSerializer serializer = new DataSerializerMAT(queue, new File(tmpDirectory+"/test.mat"));
		serializer.run();
	}
	
	/**
	 * Test method for {@link ch.psi.fda.serializer.DataSerializerTXT#run()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRunMAT2D() throws InterruptedException {
		generate2DData();
		DataSerializer serializer = new DataSerializerMAT2D(queue, new File(tmpDirectory+"/test-2d.mat"));
		serializer.run();
	}
	
	/**
	 * Test method for {@link ch.psi.fda.serializer.DataSerializerTXT#run()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRunTXT2D() throws InterruptedException {
		generate2DData();
		DataSerializer serializer = new DataSerializerTXT2D(queue, new File(tmpDirectory+"/test-2d.txt"));
		serializer.run();
	}
	
	/**
	 * Test method for {@link ch.psi.fda.serializer.DataSerializerTXT#run()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRunSplitTXT() throws InterruptedException {
		generate2DData();
		DataSerializer serializer = new DataSerializerTXTSplit(queue, new File(tmpDirectory+"/test-2d-split.txt"));
		serializer.run();
	}
	
	/**
	 * Test method for {@link ch.psi.fda.serializer.DataSerializerMDA#run()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRun2D() throws InterruptedException {
		generate2DData();
		DataSerializer serializer = new DataSerializerMDA(queue, new File(tmpDirectory+"/test-2d.mda"));
		serializer.run();
	}

	/**
	 * Test method for {@link ch.psi.fda.serializer.DataSerializerMDA#run()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testRun3D() throws InterruptedException {
		generate3DData();
		DataSerializer serializer = new DataSerializerMDA(queue, new File(tmpDirectory+"/test-3d.mda"));
		serializer.run();
	}
}

