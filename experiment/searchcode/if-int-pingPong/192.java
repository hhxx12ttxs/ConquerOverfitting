/**
 * A library to interact with Virtual Worlds such as OpenSim
 * Copyright (C) 2012  Jitendra Chauhan, Email: jitendra.chauhan@gmail.com
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.ngt.jopenmetaverse.shared.sim;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import junit.framework.Assert;

import com.ngt.jopenmetaverse.shared.sim.events.ThreadPool;
import com.ngt.jopenmetaverse.shared.sim.events.ThreadPoolFactory;

public class UDPServer {

	private  ThreadPool threadPool = ThreadPoolFactory.getThreadPool();

	public void start(final int port) throws Exception
	{
		threadPool.execute(new Runnable()
		{
			public void run()
			{
				pingpong(port);
			}
		});
	}
	
	private void pingpong(int listenport)
	{
		try
		{
//			System.out.println("Starting UDP server on port:" + listenport);
		DatagramSocket serverSocket = new DatagramSocket(listenport);
		byte[] receiveData = new byte[65355];
		byte[] sendData = new byte[65355];
		while(true)
		{
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//			System.out.println("Going to receive Data");
			serverSocket.receive(receivePacket);
			String sentence = new String( receivePacket.getData());
//			System.out.println("RECEIVED of length" + receivePacket.getLength() + " " + sentence);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();
//			String capitalizedSentence = sentence.toUpperCase();
			
			sendData = sentence.getBytes();
			DatagramPacket sendPacket =
					new DatagramPacket(sendData, receivePacket.getLength(), IPAddress, port);
			serverSocket.send(sendPacket);
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}
}
