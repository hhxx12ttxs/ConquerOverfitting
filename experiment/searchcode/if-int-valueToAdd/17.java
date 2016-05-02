/*
 * kretst@rpi.edu
 * Timothy "XBigTK13X" Kretschmer
 * Release Date: 12/01/2010
 *  
 * Server application that holds a stack of numbers and communicates through sockets
 */

package cacheserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class CacheServer {

	private static LinkedList<Integer> _stack = new LinkedList<Integer>();
	private static final int STACK_SIZE_LIMIT = 500;
	
	//Validates the command line arguments and handles client requests by creating a thread for each request
	public static void main(String[] args) {
		if(args.length != 1)
		{
			System.err.println("You must supply the server with a port number on which to listen.");
			System.err.println("Usage: CacheServer [int,8000:9000]");
			System.exit(-1);
		}
		else
		{
	      short listenPort = Short.parseShort(args[0]);                                                                                                                      
	      if(listenPort >= 8000 && listenPort <= 9000)                                                                                                                    
	        {                                                                                                                                                             
	    	  try
				{
	    		  System.out.println("Started cache-server");
	    		  System.out.println("Listening on port "+listenPort);
	    		  while(true)
	    		  {
	    			  ServerSocket server = new ServerSocket(listenPort);
	    			  Socket listenSocket = server.accept();
	    			  new ClientJob(listenSocket);
	    			  server.close();
	    		  }
				}
	    	 catch(Exception e)
				{
					e.printStackTrace();
				}
	        }
		      else
		      {
		    	  System.err.println("The supplied port was out of range. Valid integers are in the range [8000:9000].");
		      }
		}
	}
	
	//A thread safe 'removeAll()' operation on the server's integer stack
	public synchronized static Integer removeElement(int valueToRemove)
	{
		int elementsRemovedCount = 0;
		while(_stack.contains(valueToRemove))
		{
			_stack.remove((Object)valueToRemove);
			elementsRemovedCount++;
		}
		return elementsRemovedCount;
	}
	
	//A thread safe 'add()' operation for the server's integer stack
	public synchronized static int pushElement(int valueToAdd)
	{
		if(STACK_SIZE_LIMIT > _stack.size())
		{
			_stack.add(valueToAdd);
			return 0;
		}
		return -1;
	}
	
	//A thread safe 'pop()' operation on the server's integer stack
	public synchronized static int popFromStack()
	{
		if(0<_stack.size())
		{
			return _stack.removeLast();
		}
		return (Integer)null;
	}
	
	//A thread safe 'size()' operation on the server's integer stack
	public synchronized static int sizeOfStack()
	{
		return _stack.size();
	}
}

