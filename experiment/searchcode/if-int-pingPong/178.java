import java.net.*;
import java.io.*;
import java.util.*;


public class Simpella implements Runnable {
	int i,backlog;
	public static int tcpPort, udpPort= 0;
	public ServerSocket tcpSocket= null;
	public static String ipAddr= null;
	PingPong pp;
	public Simpella(int tcpPort, int backlog) {
			try {
				if((tcpPort >= 1) && (tcpPort <= 60000)) {
					
					ServerSocket tcpSocket = new ServerSocket(tcpPort, backlog);
					Socket testSocket= new Socket("8.8.8.8",53);
					ipAddr= testSocket.getLocalAddress().getHostAddress();
					Simpella.tcpPort= tcpPort;
					System.out.println("Listening on "+ ipAddr + " TCP port- " + tcpPort);
					this.backlog= backlog;
					testSocket.close();
					listening(tcpSocket);
				}
				else {
					System.out.println("Port Numbers should be between 1 and 60000");
				}
			}
			catch (SocketTimeoutException s) {
				System.out.println("timeout");
			}
			catch (IOException ioe) {
				System.out.println("could not listen on port "+ tcpPort);
				System.exit(-1);
			}
	}
	public Simpella (ServerSocket s) {
		this.tcpSocket= s;
	}
	public Simpella () {
	
	}
	void listening(ServerSocket s){
			try {
					System.out.println();
					Thread t1 = new Thread((Runnable) new Simpella(s));
					t1.start();
					Thread t2= new Thread((Runnable) new AcceptInput());
					t2.start();
					/*Thread t3= new Thread((Runnable)pp);
					t3.start();*/
					}
				catch (Exception e) {
					System.out.println("Cannot accept connection");
				}
			}
	
	public void Client(String addr, int port) 
	{
		try {
			Socket tcpClient= new Socket();
			//System.out.println(tcpClient.isConnected());
			tcpClient = new Socket(addr,port);
			if(tcpClient.isConnected()){
				ConnectionList.addTo(tcpClient); //maintain a list
				this.pp= new PingPong(tcpClient);
				String msg= pp.createPing();
				//for (int i=0; i<ConnectionList.list.size();i++) {
					pp.sendPing(msg);
				//}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (true) {
			try {
				Socket sock1=new Socket();
				sock1= tcpSocket.accept();
				System.out.println("got Conn. request from "+sock1.getInetAddress().toString()+" "+sock1.getPort());
				if(sock1.isConnected()) {
					ConnectionList.addTo(sock1);
					pp= new PingPong(sock1);
				}
			}
			catch (IOException o) {
				System.out.println("Cannot connect: Read Failed");
				}
			}
		}

	public static void main(String[] args) {
		new Simpella(Integer.parseInt(args[0]),5);
		
	}
}
