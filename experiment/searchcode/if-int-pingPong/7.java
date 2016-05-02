
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;

public class PingPong implements Runnable {
	static ArrayList<String> strBuffer = new ArrayList<String>(); 
	Socket CurrentSocket;
	int SocketIndex;
	static int count;
	
	PingPong(Socket CurrentSocket) {
		//now here start this thread number of times
		this.CurrentSocket=CurrentSocket;
		this.SocketIndex= getSocketIndex(CurrentSocket);
		Thread t3= new Thread(this);
		t3.start();
		for (int i=0; i<ConnectionList.list.size();i++) {
		// useless loop to be removed
		}
	}
	int getSocketIndex(Socket s) {
		int p= 0;
		for (int j=0;j<ConnectionList.list.size();j++) {
			if (s.equals(ConnectionList.list.get(j))) {
				p = j;
			}
		}
		//System.out.println("SocketIndex= "+p);
		return p;
	}
	
	String createPing() {
		Socket s= this.CurrentSocket;
		String local_addr= s.getLocalAddress().toString();
		int local_port= s.getLocalPort();
		int remote_port= s.getPort();
		String remote_addr= s.getInetAddress().toString();
		String message= "["+local_addr+","+local_port+"] is connected to ["+remote_addr+","+remote_port+"]";
		PingPong.strBuffer.add(message);
		PingPong.count = 1;
		return message;
	}
	
	void sendPing(String msg) {
		
		for (int i=0; i<ConnectionList.list.size();i++) {
			if (ConnectionList.list.get(SocketIndex) != ConnectionList.list.get(i) || count == 1) {
				try {
					count++;
					Socket socket= ConnectionList.list.get(i);
					DataOutputStream dout= new DataOutputStream(socket.getOutputStream());
					dout.writeUTF(msg);
					dout.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				sendPong();
			}
		}
	}
	
	void sendPong() {
		DataOutputStream dout;
		try {
			dout = new DataOutputStream(ConnectionList.list.get(SocketIndex).getOutputStream());
			String str= "PONG: "+ConnectionList.list.get(SocketIndex).getLocalPort();
			dout.writeUTF(str);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	void recvPong() {
		
	}
	
	void recvPing() throws IOException {
				boolean b=true;
				while (b) {
					DataInputStream din;

					try {
						//System.out.println("Server local,remote: "+ConnectionList.list.get(SocketIndex).getLocalPort() + ConnectionList.list.get(SocketIndex).getPort());
						din = new DataInputStream(ConnectionList.list.get(SocketIndex).getInputStream()); //which socket goes here 
						String str= din.readUTF();
						boolean isNew= addtoBuffer(str);
						if (isNew) {
							System.out.println(str);
							sendPing(str);
						}
						else {
						//din.close();
						}
					}
					catch (IOException e) {
						e.printStackTrace();
						b=false;
					}
				}	
			}
	
	boolean addtoBuffer(String msg) {
		if (PingPong.strBuffer.contains(msg)) {
			return false;
		}
		else {
			PingPong.strBuffer.add(msg);
			return true;
		}
		
	}
	
	public void run() {
			try {
				recvPing();
				//recvPong();
			} catch (IOException e) {
					e.printStackTrace();
			}
	}
}

