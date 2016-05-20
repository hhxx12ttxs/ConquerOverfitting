import java.net.*;
import java.io.*;
import java.lang.management.*;
import java.util.Random;
import java.util.*;


class Record implements Comparable  //Our ordered list would contain records of type 'Record'
{
	public int ts;
	public int pid;

	public Record(int ts,int pid)
	{
		this.ts= ts;
		this.pid = pid;
	}
	
	public int compareTo(Object obj)  //The record specific compareTo() method
	{
		Record tmp = (Record)obj;
		if ( this.ts < tmp.ts)
			return -1;
		else if ( this.ts > tmp.ts)
			return 1;
		else if (this.ts == tmp.ts)
			{
			if (this.pid< tmp.pid)
				return -1;
			else if (this.pid > tmp.pid)
				return 1;		
			else
				return 0;	
			}
		return 0;
	}
	
}


public class Server
{
	static protected DatagramSocket socket; //The Unicast socket
	static protected Thread receiveThread; //Object holding the Receive thread
	static int port;
	static int ts;
	static int pid;
	static Set<InetAddress> receivedList = new HashSet();


	static class Receive implements Runnable  //The receive thread class
	{
		public void run() //The Receive thread's core
		{

			while(true)
			{
				try
				{
								
					byte buf[] = new byte[256];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					String received = new String(packet.getData(), 0, packet.getLength());
					System.out.println("Received data:" + received);
					InetAddress address = packet.getAddress();
					String addr = address.toString();
					System.out.println(addr);
					ts++;
					processReceive(received,address);			
				}
				catch (IOException e)
				{
					System.out.println(e);
				}
			}			
		}
		
		public static void processReceive(String received,InetAddress address ) //Process the incoming packet
		{
			String args[] = received.split(":");  //Format of received: [ts:pid:type] type is REQ or RPLY
			String type = args[2];
			int inTs = Integer.parseInt(args[0]);
			int inPid = Integer.parseInt(args[1]);
			if (type.equals("RPLY"))
			{		
				receivedList.add(address);
				if (receivedList.size()== 5)
					System.out.println("I won!");	
			}
			if (type.equals("REQ"))
			{
				if (inTs > ts)
					sendReply(address);
				else if ( inTs==ts && inPid> pid)
					sendReply(address);
				//else put it in some dumb list
			}	
		}
	}
	
	public static void sendRequest(int choice)  //Send a request to everyone
	{
		try
		{
			byte[] buf = new byte[256];
			InetAddress address = InetAddress.getByName("128.111.53.206");			
			String msg = Integer.toString(ts)+":"+Integer.toString(pid)+":"+"REQ" ;
			buf = msg.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length,address, port);
			socket.send(packet);
			System.out.println("Sent the message..."+ msg);
		}
		catch (Exception e)
		{ 
				System.out.println(e);
		}
	}
	
	public static void sendReply(InetAddress address) //Send a reply to someone
	{
		
	}
	
	public static void init() throws IOException //Initialize the server
	{
		

		socket = new DatagramSocket(port);
		receiveThread = new Thread(new Receive());
		receiveThread.start();		
	}
	
	public static void main(String args[]) throws IOException //Main thread
	{
	    /*Record[] list = new Record[3];
	    list[0] = new Record(4,2);
	    list[1] = new Record(2,2);
	    list[2] = new Record(1,2);
            Arrays.sort(list);
	    for (int z=0;z<3;z++)
		System.out.println(list[z].ts + " " + list[z].pid);*/

	    System.out.print("Enter 1 to send a request: ");
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    int choice=0;
	    port = 4445;
	    init();		
	    while (true)
	    {
		    try 
			{
		        choice = Integer.parseInt(br.readLine());
		        
		    } 
		    catch (IOException ioe) 
		    {
		        System.out.println("IO error trying to read your name!");
		        System.exit(1);
		    }
		    
		    sendRequest(choice);
	    }
		
		
				
	}

	
}

