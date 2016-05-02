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

package ch.psi.fda.core.actors;


import static org.junit.Assert.fail;
import gov.aps.jca.CAException;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.psi.fda.TestChannels;
import ch.psi.fda.core.actors.ChannelAccessFunctionActuator;
import ch.psi.jcae.ChannelBean;
import ch.psi.jcae.ChannelBeanFactory;

/**
 * Test class specific for the FunctionActuatorChannelAccess implementation.
 * @author ebner
 *
 */
public class ChannelAccessFunctionActuatorTest {
	
	// Get Logger
	private static Logger logger = Logger.getLogger(ChannelAccessFunctionActuatorTest.class.getName());
	
	private static final String channelName = TestChannels.ANALOG_OUT;
	private static final String channelNameDone = TestChannels.BINARY_OUT;
	private static final int doneValue = 1;
	private static final double doneDelay = 0;
	private static final Long timeout = 1800000l; // 30 minutes
	
	private ChannelBean<Double> channel;
	private ChannelBean<Integer> doneChannel;
	
	private Function function;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		channel = ChannelBeanFactory.getFactory().createChannelBean(Double.class, channelName, false);
		doneChannel = ChannelBeanFactory.getFactory().createChannelBean(Integer.class, channelNameDone, false);
		
		function = new Function() {
			
			@Override
			public double calculate(double parameter) {
				return parameter+2; // Translation by 2
			}
		};
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#FunctionActuatorChannelAccess(String, double, double, double)}.
	 * Check whether Exception is thrown if a negative step size is specified.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testChannelAccessFunctionActuatorNegativeStepSize() throws SocketException, CAException, Exception {
		// Need to throw exception because of negative step size
		new ChannelAccessFunctionActuator<Object>(channelName, function,0, 1, -0.1, timeout);
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#FunctionActuatorChannelAccess(String, double, double, double)}.
	 */
	@Test
	public void testChannelAccessFunctionActuatorTimeout() throws SocketException, CAException, Exception {
		
		// Negative timeout
		boolean flag = false;
		try{
			// Need to return IllegalArgumentException due to negative Timeout
			new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 1, 0.1, -1l);
		}
		catch(IllegalArgumentException e){
			flag=true;
		}
		if(!flag){
			fail("Negative timeout is not handeled correctly");
		}
		
		// 0 timeout
		flag=false;
		try{
			// Need to return IllegalArgumentException
			new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 1, 0.1, -0l);
		}
		catch(IllegalArgumentException e){
			flag=true;
		}
		if(!flag){
			fail("0 timeout is not handled correctly");
		}
		
		// Accept null timeout
		new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 1, 0.1, null);
		
		// Accept positive timeout
		new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 1, 0.1, 1l);
		
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#FunctionActuatorChannelAccess(String, double, double, double)}.
	 * Check whether Exception is thrown if a zero step size is specified.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testChannelAccessFunctionActuatorZeroStepSize() throws SocketException, CAException, Exception {
		// Zero step size need to cause an exception
		new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 1, 0, timeout);
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#FunctionActuatorChannelAccess(String, double, double, double)}.
	 * Check correct initialization
	 */
	public void testChannelAccessFunctionActuator() throws SocketException, CAException, Exception {
		// Zero step size need to cause an exception
		new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 10, 1, timeout);
	}
	
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#set()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testSet() throws InterruptedException {
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 0.09999, 0.1, timeout);
		actuator.set();
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#hasNext()}.
	 * Check whether the actuator throws an Exception if there is no next point but set() is called
	 * @throws InterruptedException 
	 */
	@Test(expected=IllegalStateException.class)
	public void testSetNoNext() throws InterruptedException {
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 0.09999, 0.1, timeout);
		actuator.set();
		actuator.set();
	}

	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#hasNext()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testHasNextOneStep() throws InterruptedException {
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 0.09999, 0.1, timeout);
		// Execute first set (because there is always a first move)
		actuator.set();
		
		// Check whether actuator returns that there is no next point
		boolean next = actuator.hasNext();
		if(next){
			fail("There must be no next step");
		}
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#hasNext()}.
	 * @throws InterruptedException 
	 */
	@Test
	public void testHasNext() throws InterruptedException {
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, 0, 10, 0.1, timeout);
		
		int count = 0;
		int steps = (int) ((10-0)/0.1)+1;
		while(actuator.hasNext()){
			actuator.set();
			count++;
		}
		
		logger.fine("Actual steps: "+count+"  - Needed steps: "+steps);
		if(count != steps){
			fail("Actuator set more steps than specified");
		}
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#set()}.
	 * Test actuator move  start<end and start>end ...
	 * @throws InterruptedException 
	 */
	@Test
	public void testSetLoop() throws InterruptedException {
		
		List<double[]> settings = new ArrayList<double[]>();
		// start end stepsize
		settings.add(new double[] {0, 10, 0.1});
		settings.add(new double[] {0, -1, 0.1});
		
		
		for(double[] svalue: settings){
			ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, svalue[0], svalue[1], svalue[2], timeout);
			
			int count =0;
			while(actuator.hasNext()){
				actuator.set();
				count++;
			}
			
			int cnt = (int) Math.floor(Math.abs(svalue[0]-svalue[1])/svalue[2])+1;
			
			if(count != cnt){
				fail("Actuator did not move required steps [actual count: "+count+" needed count: "+cnt+" ]");
			}
		}
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#set()}.
	 * Test actuator move  start<end and start>end ...
	 * @throws CAException 
	 */
	@Test
	public void testReverse() throws CAException, InterruptedException {
		
		List<double[]> settings = new ArrayList<double[]>();
		// start end stepsize
		settings.add(new double[] {0, 10, 0.1});
		settings.add(new double[] {0, -1, 0.1});
		
		
		for(double[] svalue: settings){
			ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, svalue[0], svalue[1], svalue[2], timeout);
			actuator.init();
			
			int count =0;
			while(actuator.hasNext()){
				actuator.set();
				
				// Check set value
				double val = channel.getValue();
				logger.info("Value: "+val);
				if(svalue[0]<svalue[1]){
					double sval = function.calculate(svalue[0]+(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				else{
					double sval = function.calculate(svalue[0]-(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				count++;
			}
			
			int cnt = (int) Math.floor(Math.abs(svalue[0]-svalue[1])/svalue[2])+1;
			
			if(count != cnt){
				fail("Actuator did not move required steps [actual count: "+count+" needed count: "+cnt+" ]");
			}
			
			
			actuator.reverse();
			actuator.init();
			count =0;
			while(actuator.hasNext()){
				actuator.set();
				
				// Check set value
				double val = channel.getValue();
				logger.info("Value: "+val);
				if(svalue[1]<svalue[0]){
					double sval = function.calculate(svalue[1]+(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				else{
					double sval = function.calculate(svalue[1]-(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				
				count++;
			}
			
			cnt = (int) Math.floor(Math.abs(svalue[0]-svalue[1])/svalue[2])+1;
			
			if(count != cnt){
				fail("Actuator did not move required steps [actual count: "+count+" needed count: "+cnt+" ]");
			}
			
			
		}
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessFunctionActuator#set()}.
	 * Test actuator move  start<end and start>end ...
	 * @throws CAException 
	 */
	@Test
	public void testReverseReset() throws CAException, InterruptedException {
		
		List<double[]> settings = new ArrayList<double[]>();
		// start end stepsize
		settings.add(new double[] {0, 10, 0.1});
		settings.add(new double[] {0, -1, 0.1});
		
		
		for(double[] svalue: settings){
			ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, svalue[0], svalue[1], svalue[2], timeout);
			actuator.init();
			
			int count =0;
			while(actuator.hasNext()){
				actuator.set();
				
				// Check set value
				double val = channel.getValue();
				logger.info("Value: "+val);
				if(svalue[0]<svalue[1]){
					double sval = function.calculate(svalue[0]+(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				else{
					double sval = function.calculate(svalue[0]-(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				count++;
			}
			
			int cnt = (int) Math.floor(Math.abs(svalue[0]-svalue[1])/svalue[2])+1;
			
			if(count != cnt){
				fail("Actuator did not move required steps [actual count: "+count+" needed count: "+cnt+" ]");
			}
			
			
			actuator.reverse();
			actuator.reset();
			actuator.init();
			count =0;
			while(actuator.hasNext()){
				actuator.set();
				
				// Check set value
				double val = channel.getValue();
				logger.info("Value: "+val);
				if(svalue[0]<svalue[1]){
					double sval = function.calculate(svalue[0]+(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				else{
					double sval = function.calculate(svalue[0]-(count*svalue[2]));
					if(Math.abs(val-sval) > 0.001){ // 0.001 is precision
						fail("Set value ["+sval+"] does not match actual value ["+val+"]");
					}
				}
				count++;
			}
			
			cnt = (int) Math.floor(Math.abs(svalue[0]-svalue[1])/svalue[2])+1;
			
			if(count != cnt){
				fail("Actuator did not move required steps [actual count: "+count+" needed count: "+cnt+" ]");
			}
			
			
		}
	}
	
	
	// TODO Check if actuator makes the required number of steps
	// TODO Check if actuator makes the steps in the right direction
	// TODO Check if actuator really blocks until motor/actuator is moved.
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#FunctionActuatorChannelAccess(String, double, double, double)}.
	 * Check whether Exception is thrown if a negative step size is specified.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testChannelAccessGPFunctionActuatorNegativeStepSize() throws SocketException, CAException, Exception {
		// Need to throw exception because of negative step size
		new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function, 0, 1, -0.1, timeout);
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#FunctionActuatorChannelAccess(String, double, double, double)}.
	 * Check whether Exception is thrown if a zero step size is specified.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testChannelAccessGPFunctionActuatorZeroStepSize() throws SocketException, CAException, Exception {
		// Zero step size need to cause an exception
		new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function, 0, 1, 0, timeout);
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#FunctionActuatorChannelAccess(String, double, double, double)}.
	 * Check correct initialization
	 */
	public void testChannelAccessGPFunctionActuator() throws SocketException, CAException, Exception {
		// Zero step size need to cause an exception
		new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function, 0, 10, 1, timeout);
	}
	
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#set()}.
	 * @throws CAException 
	 */
	@Test
	public void testDoneSet() throws CAException, InterruptedException {

		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function, 0, 0.09999, 0.1, timeout);
		
		// Simulate done channel
		doneChannel.setValue(0);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(3000);
					doneChannel.setValue(1);
				} catch (Exception e) {
				}
				
			}
		}).start();
		
		actuator.set();
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#set()}.
	 * @throws CAException 
	 */
	@Test
	public void testDoneDelay() throws CAException, InterruptedException {

		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, 1.5, function, 0, 1, 0.1, timeout);
		
		// Simulate done channel
		doneChannel.setValue(1);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(1000);
					doneChannel.setValue(0);
					Thread.sleep(4000);
					doneChannel.setValue(1);
				} catch (Exception e) {
				}
				
			}
		}).start();
		
		long start = System.currentTimeMillis();
		actuator.set();
		long end = System.currentTimeMillis();
		
		logger.fine("Elapsed time: "+(end-start));
		if((end-start)<4000){ // Check whether all the moves took less than 6 seconds (thats the delay the done is set to 1)
			fail("Done delay does not work");
		}
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#hasNext()}.
	 * Check whether the actuator throws an Exception if there is no next point but set() is called
	 * @throws CAException 
	 */
	@Test(expected=IllegalStateException.class)
	public void testDoneSetNoNext() throws CAException, InterruptedException {
		
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function,0, 0.09999, 0.1, timeout);
		
		
		// Simulate done channel
		doneChannel.setValue(0);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(3000);
					doneChannel.setValue(1);
				} catch (Exception e) {
				}
				
			}
		}).start();
		
		actuator.set();
		
		
		// Simulate done channel
		doneChannel.setValue(0);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(3000);
					doneChannel.setValue(1);
				} catch (Exception e) {
				}
				
			}
		}).start();
		
		actuator.set();
	}

	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#hasNext()}.
	 * @throws CAException 
	 */
	@Test
	public void testDoneHasNextOneStep() throws CAException, InterruptedException {
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function, 0, 0.09999, 0.1, timeout);
		
		// Simulate done channel
		doneChannel.setValue(0);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					Thread.sleep(3000);
					doneChannel.setValue(1);
				} catch (Exception e) {
				}
				
			}
		}).start();
		
		// Execute first set (because there is always a first move)
		actuator.set();
		
		// Check whether actuator returns that there is no next point
		boolean next = actuator.hasNext();
		if(next){
			fail("There must be no next step");
		}
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#hasNext()}.
	 * @throws CAException 
	 */
	@Test
	public void testDoneHasNext() throws CAException, InterruptedException {
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function,0, 10, 0.1, timeout);
		
		int count = 0;
		int steps = (int) ((10-0)/0.1)+1;
		while(actuator.hasNext()){
			
			// Simulate done channel
			doneChannel.setValue(0);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						Thread.sleep(10);
						doneChannel.setValue(1);
					} catch (Exception e) {
					}
					
				}
			}).start();
			
			actuator.set();
			count++;
		}
		
		logger.fine("Actual steps: "+count+"  - Needed steps: "+steps);
		if(count != steps){
			fail("Actuator set more steps than specified");
		}
	}
	
	
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#set()}.
	 * Test actuator move  start<end and start>end ...
	 * @throws CAException 
	 */
	@Test
	public void testDoneSetLoop() throws CAException, InterruptedException {
		
		List<double[]> settings = new ArrayList<double[]>();
		// start end stepsize
		settings.add(new double[] {0, 10, 0.1});
		settings.add(new double[] {0, -1, 0.1});
		
		
		for(double[] svalue: settings){
			ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function, svalue[0], svalue[1], svalue[2], timeout);
			
			int count =0;
			while(actuator.hasNext()){
				
				// Simulate done channel
				doneChannel.setValue(0);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						try {
							Thread.sleep(10);
							doneChannel.setValue(1);
						} catch (Exception e) {
						}
						
					}
				}).start();
				
				actuator.set();
				count++;
			}
			
			int cnt = (int) Math.floor(Math.abs(svalue[0]-svalue[1])/svalue[2])+1;
			
			if(count != cnt){
				fail("Actuator did not move required steps [actual count: "+count+" needed count: "+cnt+" ]");
			}
		}
	}
	
	/**
	 * Test method for {@link ch.psi.fda.core.actors.ChannelAccessGPFunctionActuator#set()}.
	 * Test whether the actuator returns if the actuator is already on the position it should be before the move ...
	 * (see issue XASEC-278)
	 * @throws CAException 
	 */
	@Test
	public void testMoveToActualPosition() throws CAException, InterruptedException {
		
		double start = 0;
		double end = 2;
		double stepSize = 1;
		
		channel.setValue(start);
		
		Thread.sleep(1000);
		
		logger.info("Current channel value: "+channel.getValue());
		
		ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, channelNameDone, doneValue, doneDelay, function, start, end, stepSize, timeout);
		while(actuator.hasNext()){
				
				// Simulate done channel
				doneChannel.setValue(1);
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						
//						try {
//							Thread.sleep(10);
////							doneChannel.setValue(1);
//						} catch (Exception e) {
//						}
//						
//					}
//				}).start();
				
				actuator.set();
			}
			
		}
	
	
	@Test
	public void testSetLoop2() throws InterruptedException {
		
		Function function = new Function() {
			
			@Override
			public double calculate(double parameter) {
				return parameter+2;
			}
		};
		
		List<double[]> settings = new ArrayList<double[]>();
		// start end stepsize
		settings.add(new double[] {0, 10, 0.1});
		settings.add(new double[] {0, -1, 0.1});
		
		
		for(double[] svalue: settings){
			ChannelAccessFunctionActuator<Object> actuator = new ChannelAccessFunctionActuator<Object>(channelName, function, svalue[0], svalue[1], svalue[2], timeout);
			
			int count =0;
			while(actuator.hasNext()){
				actuator.set();
				count++;
			}
			
			int cnt = (int) Math.floor(Math.abs(svalue[0]-svalue[1])/svalue[2])+1;
			
			if(count != cnt){
				fail("Actuator did not move required steps [actual count: "+count+" needed count: "+cnt+" ]");
			}
		}
	}
	
}

